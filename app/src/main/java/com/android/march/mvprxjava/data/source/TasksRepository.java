package com.android.march.mvprxjava.data.source;

import com.android.march.mvprxjava.data.TaskBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public void loadTasks(final LoadTasksCallBack callBack) {
        if (cachedTasksMap != null && !cacheIsDirty) {
            callBack.onTasksLoaded(new ArrayList<>(cachedTasksMap.values()));
            return;
        }

        if (cacheIsDirty) {
            // 从网络中获取新的数据
            getTasksFromRemoteDataSource(callBack);
        } else {
            // 查询本地数据
            tasksLocalDataSource.loadTasks(new LoadTasksCallBack() {
                @Override
                public void onTasksLoaded(List<TaskBean> taskBeanList) {
                    refreshCache(taskBeanList);
                    callBack.onTasksLoaded(new ArrayList<>(cachedTasksMap.values()));
                }

                @Override
                public void onDataNotAvailable() {
                    // 没有数据,从网络中获取新的数据
                    getTasksFromRemoteDataSource(callBack);
                }
            });
        }
    }

    @Override
    public void getTask(final String taskId, final GetTaskCallBack callBack) {
        TaskBean cachedTaskBean = getTaskWithId(taskId);

        if (cachedTaskBean != null) {
            callBack.onTaskLoaded(cachedTaskBean);
            return;
        }

        tasksLocalDataSource.getTask(taskId, new GetTaskCallBack() {
            @Override
            public void onTaskLoaded(TaskBean taskBean) {
                if (cachedTasksMap == null) {
                    cachedTasksMap = new LinkedHashMap<>();
                }
                cachedTasksMap.put(taskBean.getId(), taskBean);
                callBack.onTaskLoaded(taskBean);
            }

            @Override
            public void onDataNotAvailable() {
                tasksRemoteDataSource.getTask(taskId, new GetTaskCallBack() {
                    @Override
                    public void onTaskLoaded(TaskBean taskBean) {
                        if (cachedTasksMap == null) {
                            cachedTasksMap = new LinkedHashMap<>();
                        }
                        cachedTasksMap.put(taskBean.getId(), taskBean);
                        callBack.onTaskLoaded(taskBean);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        callBack.onDataNotAvailable();
                    }
                });
            }
        });
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
    private void getTasksFromRemoteDataSource(final LoadTasksCallBack callBack) {
        tasksRemoteDataSource.loadTasks(new LoadTasksCallBack() {
            @Override
            public void onTasksLoaded(List<TaskBean> taskBeanList) {
                refreshCache(taskBeanList);
                refreshLocalDataSource(taskBeanList);
                callBack.onTasksLoaded(new ArrayList<>(cachedTasksMap.values()));
            }

            @Override
            public void onDataNotAvailable() {
                callBack.onDataNotAvailable();
            }
        });
    }

    // 刷新缓存
    private void refreshCache(List<TaskBean> taskBeanList) {
        if (cachedTasksMap == null) {
            cachedTasksMap = new LinkedHashMap<>();
        }
        cachedTasksMap.clear();
        for (TaskBean taskBean : taskBeanList) {
            cachedTasksMap.put(taskBean.getId(), taskBean);
        }
        cacheIsDirty = false;
    }

    // 刷新本地数据
    private void refreshLocalDataSource(List<TaskBean> taskBeanList) {
        tasksLocalDataSource.deleteAllTasks();
        for (TaskBean taskBean : taskBeanList) {
            tasksLocalDataSource.addTask(taskBean);
        }
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