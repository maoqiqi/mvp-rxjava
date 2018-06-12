package com.android.march.mvprxjava.data.source.remote;

import com.android.march.mvprxjava.data.TaskBean;
import com.android.march.mvprxjava.data.source.TasksDataSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class FakeTasksRemoteDataSource implements TasksDataSource {

    private static FakeTasksRemoteDataSource INSTANCE;

    private final static Map<String, TaskBean> TASKS_SERVICE_DATA = new LinkedHashMap<>();

    private FakeTasksRemoteDataSource() {

    }

    public static FakeTasksRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeTasksRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public void loadTasks(final LoadTasksCallBack callBack) {
        callBack.onTasksLoaded(new ArrayList<>(TASKS_SERVICE_DATA.values()));
    }

    @Override
    public void getTask(String taskId, final GetTaskCallBack callBack) {
        final TaskBean taskBean = TASKS_SERVICE_DATA.get(taskId);
        callBack.onTaskLoaded(taskBean);
    }

    @Override
    public void clearCompletedTasks() {
        Iterator<Map.Entry<String, TaskBean>> iterator = TASKS_SERVICE_DATA.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TaskBean> entry = iterator.next();
            if (entry.getValue().isCompleted()) {
                iterator.remove();
            }
        }
    }

    @Override
    public void refreshTasks() {

    }

    @Override
    public void addTask(TaskBean taskBean) {
        TASKS_SERVICE_DATA.put(taskBean.getId(), taskBean);
    }

    @Override
    public void updateTask(TaskBean taskBean) {
        TASKS_SERVICE_DATA.put(taskBean.getId(), taskBean);
    }

    @Override
    public void completeTask(TaskBean completedTaskBean) {
        TaskBean taskBean = new TaskBean(completedTaskBean.getId(), completedTaskBean.getTitle(), completedTaskBean.getDescription(), true);
        TASKS_SERVICE_DATA.put(completedTaskBean.getId(), taskBean);
    }

    @Override
    public void completeTask(String taskId) {

    }

    @Override
    public void activateTask(TaskBean activeTaskBean) {
        TaskBean taskBean = new TaskBean(activeTaskBean.getId(), activeTaskBean.getTitle(), activeTaskBean.getDescription(), false);
        TASKS_SERVICE_DATA.put(activeTaskBean.getId(), taskBean);
    }

    @Override
    public void activateTask(String taskId) {

    }

    @Override
    public void deleteAllTasks() {
        TASKS_SERVICE_DATA.clear();
    }

    @Override
    public void deleteTask(String taskId) {
        TASKS_SERVICE_DATA.remove(taskId);
    }
}