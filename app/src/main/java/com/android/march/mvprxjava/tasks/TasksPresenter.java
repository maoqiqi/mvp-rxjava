package com.android.march.mvprxjava.tasks;

import android.app.Activity;

import com.android.march.mvprxjava.addedittask.AddEditTaskActivity;
import com.android.march.mvprxjava.data.TaskBean;
import com.android.march.mvprxjava.data.source.TasksDataSource;
import com.android.march.mvprxjava.data.source.TasksRepository;

import java.util.ArrayList;
import java.util.List;

public class TasksPresenter implements TasksContract.Presenter {

    private final TasksRepository tasksRepository;
    private final TasksContract.View tasksView;

    private TasksFilterType currentFiltering = TasksFilterType.ALL_TASKS;
    private boolean firstLoad = true;

    public TasksPresenter(TasksRepository tasksRepository, TasksContract.View tasksView) {
        this.tasksRepository = tasksRepository;
        this.tasksView = tasksView;
        this.tasksView.setPresenter(this);
    }

    @Override
    public void start() {
        loadTasks(false);
    }

    @Override
    public void loadTasks(boolean forceUpdate) {
        // 第一次加载强制加载
        loadTasks(forceUpdate || firstLoad, true);
        firstLoad = false;
    }

    private void loadTasks(boolean forceUpdate, final boolean showLoading) {
        if (showLoading) {
            tasksView.setLoadingIndicator(true);
        }

        if (forceUpdate) {
            // @todo
            // tasksRepository.refreshTasks();
        }

        tasksRepository.loadTasks(new TasksDataSource.LoadTasksCallBack() {
            @Override
            public void onTasksLoaded(List<TaskBean> taskBeanList) {
                List<TaskBean> tasksToShow = new ArrayList<>();

                switch (currentFiltering) {
                    case ALL_TASKS:
                        tasksToShow.addAll(taskBeanList);
                        break;
                    case ACTIVE_TASKS:
                        for (TaskBean taskBean : taskBeanList) {
                            if (taskBean.isActive()) {
                                tasksToShow.add(taskBean);
                            }
                        }
                        break;
                    case COMPLETED_TASKS:
                        for (TaskBean taskBean : taskBeanList) {
                            if (taskBean.isCompleted()) {
                                tasksToShow.add(taskBean);
                            }
                        }
                        break;
                }

                if (!tasksView.isActive()) {
                    return;
                }

                if (showLoading) {
                    tasksView.setLoadingIndicator(false);
                }

                processTasks(tasksToShow);
            }

            @Override
            public void onDataNotAvailable() {
                if (!tasksView.isActive()) {
                    return;
                }

                // 显示一条消息,提示没有该类型的任务
                tasksView.showNoTasks(currentFiltering);
            }
        });
    }

    @Override
    public void setFiltering(TasksFilterType requestType) {
        currentFiltering = requestType;
    }

    @Override
    public TasksFilterType getFiltering() {
        return currentFiltering;
    }

    @Override
    public void clearCompletedTasks() {
        tasksRepository.clearCompletedTasks();
        tasksView.showMessage("Completed tasks cleared");
        loadTasks(false, false);
    }

    @Override
    public void addTask() {
        tasksView.addTask();
    }

    @Override
    public void completeTask(TaskBean completedTaskBean) {
        tasksRepository.completeTask(completedTaskBean);
        tasksView.showMessage("Task marked complete");
        loadTasks(false, false);
    }

    @Override
    public void activateTask(TaskBean activeTaskBean) {
        tasksRepository.activateTask(activeTaskBean);
        tasksView.showMessage("Task marked active");
        loadTasks(false, false);
    }

    @Override
    public void openTaskDetails(TaskBean requestedTaskBean) {
        tasksView.openTaskDetails(requestedTaskBean.getId());
    }

    @Override
    public void result(int requestCode, int resultCode) {
        if (AddEditTaskActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            tasksView.showMessage("Add Task successfully");
        }
    }

    private void processTasks(List<TaskBean> taskBeanList) {
        if (taskBeanList.isEmpty()) {
            // 显示一条消息,提示没有该类型的任务
            tasksView.showNoTasks(currentFiltering);
        } else {
            // 设置显示过滤标签文本
            tasksView.showFilterLabel(currentFiltering);
            // 显示任务列表
            tasksView.showTasks(taskBeanList);
        }
    }
}