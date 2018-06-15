package com.android.march.mvprxjava.data.source;

import com.android.march.mvprxjava.data.TaskBean;

import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

public class TasksRepository implements TasksDataSource {

    private static TasksRepository INSTANCE = null;

    private TasksDataSource tasksRemoteDataSource;
    private TasksDataSource tasksLocalDataSource;

    Map<String, TaskBean> cachedTasksMap;
    private boolean cacheIsDirty = false;

    // 私有构造方法,防止直接实例化
    private TasksRepository(TasksDataSource tasksRemoteDataSource, TasksDataSource tasksLocalDataSource) {
        this.tasksRemoteDataSource = tasksRemoteDataSource;
        this.tasksLocalDataSource = tasksLocalDataSource;
    }

    /**
     * 返回该类的单个实例
     *
     * @param tasksRemoteDataSource 远程数据
     * @param tasksLocalDataSource  本地数据
     * @return {@link TasksRepository}实例
     */
    public static TasksRepository getInstance(TasksDataSource tasksRemoteDataSource, TasksDataSource tasksLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new TasksRepository(tasksRemoteDataSource, tasksLocalDataSource);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Flowable<List<TaskBean>> loadTasks() {
        if (cachedTasksMap != null && !cacheIsDirty) {
            return Flowable.fromIterable(cachedTasksMap.values()).toList().toFlowable();
        } else if (cachedTasksMap == null) {
            cachedTasksMap = new LinkedHashMap<>();
        }

        if (cacheIsDirty) {
            // 从网络中获取新的数据
            return getTasksFromRemoteDataSource();
        } else {
            Flowable<List<TaskBean>> localTasks = tasksLocalDataSource.loadTasks()
                    .flatMap(new Function<List<TaskBean>, Publisher<List<TaskBean>>>() {
                        @Override
                        public Publisher<List<TaskBean>> apply(List<TaskBean> taskBeans) {
                            return Flowable.fromIterable(taskBeans)
                                    .doOnNext(new Consumer<TaskBean>() {
                                        @Override
                                        public void accept(TaskBean taskBean) {
                                            cachedTasksMap.put(taskBean.getId(), taskBean);
                                        }
                                    })
                                    .toList().toFlowable();
                        }
                    });
            Flowable<List<TaskBean>> remoteTasks = getTasksFromRemoteDataSource();
            return Flowable.concat(localTasks, remoteTasks)
                    .filter(new Predicate<List<TaskBean>>() {
                        @Override
                        public boolean test(List<TaskBean> taskBeans) {
                            return !taskBeans.isEmpty();
                        }
                    }).firstOrError().toFlowable();
        }
    }

    @Override
    public Flowable<TaskBean> getTask(final String taskId) {
        TaskBean cachedTaskBean = getTaskWithId(taskId);

        if (cachedTaskBean != null) {
            return Flowable.just(cachedTaskBean);
        }

        if (cachedTasksMap == null) {
            cachedTasksMap = new LinkedHashMap<>();
        }

        Flowable<TaskBean> localTask = tasksLocalDataSource.getTask(taskId).doOnNext(new Consumer<TaskBean>() {
            @Override
            public void accept(TaskBean taskBean) throws Exception {
                cachedTasksMap.put(taskBean.getId(), taskBean);
            }
        }).firstElement().toFlowable();
        Flowable<TaskBean> remoteTask = tasksRemoteDataSource.getTask(taskId).doOnNext(new Consumer<TaskBean>() {
            @Override
            public void accept(TaskBean taskBean) throws Exception {
                cachedTasksMap.put(taskBean.getId(), taskBean);
            }
        });
        return localTask;/*Flowable.concat(localTask, remoteTask)
                .filter(new Predicate<TaskBean>() {
                    @Override
                    public boolean test(TaskBean taskBean) throws Exception {
                        return taskBean != null;
                    }
                })
                .firstElement().toFlowable();*/
    }

    @Override
    public void clearCompletedTasks() {
        tasksRemoteDataSource.clearCompletedTasks();
        tasksLocalDataSource.clearCompletedTasks();

        if (cachedTasksMap == null || cachedTasksMap.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<String, TaskBean>> iterator = cachedTasksMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TaskBean> entry = iterator.next();
            if (entry.getValue().isCompleted()) {
                iterator.remove();
            }
        }
    }

    @Override
    public void refreshTasks() {
        cacheIsDirty = true;
    }

    @Override
    public void addTask(TaskBean taskBean) {
        tasksRemoteDataSource.addTask(taskBean);
        tasksLocalDataSource.addTask(taskBean);

        if (cachedTasksMap == null) {
            cachedTasksMap = new LinkedHashMap<>();
        }
        cachedTasksMap.put(taskBean.getId(), taskBean);
    }

    @Override
    public void updateTask(TaskBean taskBean) {
        tasksRemoteDataSource.updateTask(taskBean);
        tasksLocalDataSource.updateTask(taskBean);

        if (cachedTasksMap == null) {
            cachedTasksMap = new LinkedHashMap<>();
        }
        cachedTasksMap.put(taskBean.getId(), taskBean);
    }

    @Override
    public void completeTask(TaskBean completedTaskBean) {
        if (completedTaskBean == null) {
            return;
        }

        tasksRemoteDataSource.completeTask(completedTaskBean);
        tasksLocalDataSource.completeTask(completedTaskBean);

        TaskBean taskBean = new TaskBean(completedTaskBean.getId(),
                completedTaskBean.getTitle(), completedTaskBean.getDescription(), true);

        if (cachedTasksMap == null) {
            cachedTasksMap = new LinkedHashMap<>();
        }
        cachedTasksMap.put(completedTaskBean.getId(), taskBean);
    }

    @Override
    public void completeTask(String taskId) {
        completeTask(getTaskWithId(taskId));
    }

    @Override
    public void activateTask(TaskBean activeTaskBean) {
        if (activeTaskBean == null) {
            return;
        }

        tasksRemoteDataSource.activateTask(activeTaskBean);
        tasksLocalDataSource.activateTask(activeTaskBean);

        TaskBean taskBean = new TaskBean(activeTaskBean.getId(),
                activeTaskBean.getTitle(), activeTaskBean.getDescription(), false);

        if (cachedTasksMap == null) {
            cachedTasksMap = new LinkedHashMap<>();
        }
        cachedTasksMap.put(activeTaskBean.getId(), taskBean);
    }

    @Override
    public void activateTask(String taskId) {
        activateTask(getTaskWithId(taskId));
    }

    @Override
    public void deleteAllTasks() {
        tasksRemoteDataSource.deleteAllTasks();
        tasksLocalDataSource.deleteAllTasks();

        if (cachedTasksMap == null) {
            cachedTasksMap = new LinkedHashMap<>();
        }
        cachedTasksMap.clear();
    }

    @Override
    public void deleteTask(String taskId) {
        tasksRemoteDataSource.deleteTask(taskId);
        tasksLocalDataSource.deleteTask(taskId);

        cachedTasksMap.remove(taskId);
    }

    // 从网络获取数据
    private Flowable<List<TaskBean>> getTasksFromRemoteDataSource() {
        return tasksRemoteDataSource.loadTasks()
                .flatMap(new Function<List<TaskBean>, Publisher<List<TaskBean>>>() {
                    @Override
                    public Publisher<List<TaskBean>> apply(List<TaskBean> taskBeans) {
                        return Flowable.fromIterable(taskBeans).doOnNext(new Consumer<TaskBean>() {
                            @Override
                            public void accept(TaskBean taskBean) {
                                cachedTasksMap.put(taskBean.getId(), taskBean);
                                tasksLocalDataSource.addTask(taskBean);
                            }
                        }).toList().toFlowable();
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() {
                        cacheIsDirty = false;
                    }
                });
    }

    // 根据ID得到任务
    private TaskBean getTaskWithId(String taskId) {
        if (cachedTasksMap == null || cachedTasksMap.isEmpty()) {
            return null;
        } else {
            return cachedTasksMap.get(taskId);
        }
    }
}