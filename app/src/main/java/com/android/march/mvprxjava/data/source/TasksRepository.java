package com.android.march.mvprxjava.data.source;

import com.android.march.mvprxjava.data.TaskBean;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

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
    public Observable<List<TaskBean>> loadTasks() {
        if (cachedTasksMap != null && !cacheIsDirty) {
            return Observable.from(cachedTasksMap.values()).toList();
        } else if (cachedTasksMap == null) {
            cachedTasksMap = new LinkedHashMap<>();
        }

        if (cacheIsDirty) {
            // 从网络中获取新的数据
            return getTasksFromRemoteDataSource();
        } else {
            tasksLocalDataSource.deleteAllTasks();
            Observable<List<TaskBean>> localTasks = tasksLocalDataSource.loadTasks().flatMap(new Func1<List<TaskBean>, Observable<List<TaskBean>>>() {
                @Override
                public Observable<List<TaskBean>> call(List<TaskBean> taskBeans) {
                    return Observable.from(taskBeans).doOnNext(new Action1<TaskBean>() {
                        @Override
                        public void call(TaskBean taskBean) {
                            cachedTasksMap.put(taskBean.getId(), taskBean);
                        }
                    }).toList();
                }
            });
            Observable<List<TaskBean>> remoteTasks = getTasksFromRemoteDataSource();
            return localTasks;/*Observable.concat(localTasks, remoteTasks)
                    .filter(new Func1<List<TaskBean>, Boolean>() {
                        @Override
                        public Boolean call(List<TaskBean> taskBeans) {
                            return !taskBeans.isEmpty();
                        }
                    }).first();*/
        }
    }

    @Override
    public Observable<TaskBean> getTask(final String taskId) {
        TaskBean cachedTaskBean = getTaskWithId(taskId);

        if (cachedTaskBean != null) {
            return Observable.just(cachedTaskBean);
        }

        if (cachedTasksMap == null) {
            cachedTasksMap = new LinkedHashMap<>();
        }

        Observable<TaskBean> localTask = tasksLocalDataSource.getTask(taskId).doOnNext(new Action1<TaskBean>() {
            @Override
            public void call(TaskBean taskBean) {
                cachedTasksMap.put(taskBean.getId(), taskBean);
            }
        }).first();
        Observable<TaskBean> remoteTask = tasksRemoteDataSource.getTask(taskId).doOnNext(new Action1<TaskBean>() {
            @Override
            public void call(TaskBean taskBean) {
                cachedTasksMap.put(taskBean.getId(), taskBean);
            }
        });
        return Observable.concat(localTask, remoteTask)
                .map(new Func1<TaskBean, TaskBean>() {
                    @Override
                    public TaskBean call(TaskBean taskBean) {
                        if (taskBean == null) {
                            throw new NoSuchElementException("No task found with taskId " + taskId);
                        }
                        return taskBean;
                    }
                }).first();
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
    private Observable<List<TaskBean>> getTasksFromRemoteDataSource() {
        return tasksRemoteDataSource.loadTasks().flatMap(new Func1<List<TaskBean>, Observable<List<TaskBean>>>() {

            @Override
            public Observable<List<TaskBean>> call(List<TaskBean> taskBeans) {
                return Observable.from(taskBeans)
                        .doOnNext(new Action1<TaskBean>() {
                            @Override
                            public void call(TaskBean taskBean) {
                                cachedTasksMap.put(taskBean.getId(), taskBean);
                                tasksLocalDataSource.addTask(taskBean);
                            }
                        }).toList();
            }
        }).doOnCompleted(new Action0() {
            @Override
            public void call() {
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