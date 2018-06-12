package com.android.march.mvprxjava.data.source.room;

import android.content.Context;

import com.android.march.mvprxjava.data.TaskBean;
import com.android.march.mvprxjava.data.source.TasksDataSource;
import com.android.march.mvprxjava.utils.AppExecutors;

import java.util.List;

public class TasksRoomDataSource implements TasksDataSource {

    private static volatile TasksRoomDataSource INSTANCE;

    private TasksDAO dao;
    private AppExecutors appExecutors;

    private TasksRoomDataSource(Context context, AppExecutors appExecutors) {
        this.dao = TaskDataBase.getInstance(context).tasksDAO();
        this.appExecutors = appExecutors;
    }

    public static TasksRoomDataSource getInstance(Context context, AppExecutors appExecutors) {
        if (INSTANCE == null) {
            synchronized (TasksRoomDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TasksRoomDataSource(context, appExecutors);
                }
            }
        }
        return INSTANCE;
    }

    public static void clearInstance() {
        INSTANCE = null;
    }

    @Override
    public void loadTasks(final LoadTasksCallBack callBack) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final List<TaskBean> list = dao.loadTasks();
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (list.isEmpty()) {
                            callBack.onDataNotAvailable();
                        } else {
                            callBack.onTasksLoaded(list);
                        }
                    }
                });
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void getTask(final String taskId, final GetTaskCallBack callBack) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final TaskBean taskBean = dao.getTaskById(taskId);
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (taskBean == null) {
                            callBack.onDataNotAvailable();
                        } else {
                            callBack.onTaskLoaded(taskBean);
                        }
                    }
                });
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void clearCompletedTasks() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dao.deleteCompletedTasks();
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void refreshTasks() {

    }

    @Override
    public void addTask(final TaskBean taskBean) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dao.addTask(taskBean);
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void updateTask(final TaskBean taskBean) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dao.updateTask(taskBean);
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void completeTask(final TaskBean completedTaskBean) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dao.updateCompleted(completedTaskBean.getId(), true);
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void completeTask(final String taskId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dao.updateCompleted(taskId, true);
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void activateTask(final TaskBean activeTaskBean) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dao.updateCompleted(activeTaskBean.getId(), false);
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void activateTask(final String taskId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dao.updateCompleted(taskId, false);
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void deleteAllTasks() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dao.deleteTasks();
            }
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void deleteTask(final String taskId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dao.deleteTaskById(taskId);
            }
        };
        appExecutors.diskIO().execute(runnable);
    }
}
