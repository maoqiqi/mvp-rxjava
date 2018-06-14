package com.android.march.mvprxjava.data.source.remote;

import com.android.march.mvprxjava.data.TaskBean;
import com.android.march.mvprxjava.data.source.TasksDataSource;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;

public class TasksRemoteDataSource implements TasksDataSource {

    private static TasksRemoteDataSource INSTANCE;

    private static final int SERVICE_LATENCY_IN_MILLIS = 5000;

    private final static Map<String, TaskBean> TASKS_SERVICE_DATA;

    static {
        TASKS_SERVICE_DATA = new LinkedHashMap<>(2);
        addTask("Build tower in Pisa", "Ground looks good, no foundation work required.");
        addTask("Finish bridge in Tacoma", "Found awesome girders at half the cost!");
    }

    private TasksRemoteDataSource() {

    }

    public static TasksRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TasksRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public Observable<List<TaskBean>> loadTasks() {
        return Observable
                .from(TASKS_SERVICE_DATA.values())
                .delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS)
                .toList();
    }

    @Override
    public Observable<TaskBean> getTask(String taskId) {
        final TaskBean taskBean = TASKS_SERVICE_DATA.get(taskId);
        if (taskBean != null) {
            return Observable.just(taskBean).delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
        } else {
            return Observable.empty();
        }
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

    private static void addTask(String title, String description) {
        TaskBean taskBean = new TaskBean(title, description, false);
        TASKS_SERVICE_DATA.put(taskBean.getId(), taskBean);
    }
}