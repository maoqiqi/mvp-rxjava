package com.android.march.mvprxjava.data.source;

import com.android.march.mvprxjava.data.TaskBean;

import java.util.List;

import rx.Observable;

public interface TasksDataSource {

    // 加载任务
    Observable<List<TaskBean>> loadTasks();

    // 得到某个任务
    Observable<TaskBean> getTask(String taskId);

    // 清除已完成任务
    void clearCompletedTasks();

    // 刷新
    void refreshTasks();

    // 添加任务
    void addTask(TaskBean taskBean);

    // 修改任务
    void updateTask(TaskBean taskBean);

    // 完成该任务
    void completeTask(TaskBean completedTaskBean);

    void completeTask(String taskId);

    // 未完成该任务
    void activateTask(TaskBean activeTaskBean);

    void activateTask(String taskId);

    // 删除所有任务
    void deleteAllTasks();

    // 删除任务
    void deleteTask(String taskId);
}