package com.android.march.mvprxjava.data.source.local;

import android.content.Context;

import com.android.march.mvprxjava.data.TaskBean;
import com.android.march.mvprxjava.data.source.TasksDataSource;

import java.util.List;

public class TasksLocalDataSource implements TasksDataSource {

    private static volatile TasksLocalDataSource INSTANCE;

    private TasksDAO dao;

    private TasksLocalDataSource(Context context) {
        dao = new TasksDAO(context);
    }

    public static TasksLocalDataSource getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TasksLocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TasksLocalDataSource(context);
                }
            }
        }
        return INSTANCE;
    }

    public static void clearInstance() {
        INSTANCE = null;
    }

    @Override
    public void loadTasks(LoadTasksCallBack callBack) {
        List<TaskBean> list = dao.loadTasks();
        if (list.isEmpty()) {
            callBack.onDataNotAvailable();
        } else {
            callBack.onTasksLoaded(list);
        }
    }

    @Override
    public void getTask(String taskId, GetTaskCallBack callBack) {
        TaskBean taskBean = dao.getTaskById(taskId);
        if (taskBean == null) {
            callBack.onDataNotAvailable();
        } else {
            callBack.onTaskLoaded(taskBean);
        }
    }

    @Override
    public void clearCompletedTasks() {
        dao.deleteCompletedTasks();
    }

    @Override
    public void refreshTasks() {

    }

    @Override
    public void addTask(TaskBean taskBean) {
        dao.addTask(taskBean);
    }

    @Override
    public void updateTask(TaskBean taskBean) {
        dao.updateTask(taskBean);
    }

    @Override
    public void completeTask(TaskBean completedTaskBean) {
        dao.updateCompleted(completedTaskBean.getId(), true);
    }

    @Override
    public void completeTask(String taskId) {
        dao.updateCompleted(taskId, true);
    }

    @Override
    public void activateTask(TaskBean activeTaskBean) {
        dao.updateCompleted(activeTaskBean.getId(), false);
    }

    @Override
    public void activateTask(String taskId) {
        dao.updateCompleted(taskId, false);
    }

    @Override
    public void deleteAllTasks() {
        dao.deleteTasks();
    }

    @Override
    public void deleteTask(String taskId) {
        dao.deleteTaskById(taskId);
    }
}