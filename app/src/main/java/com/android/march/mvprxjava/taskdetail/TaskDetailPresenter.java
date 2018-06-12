package com.android.march.mvprxjava.taskdetail;

import com.android.march.mvprxjava.data.TaskBean;
import com.android.march.mvprxjava.data.source.TasksDataSource;
import com.android.march.mvprxjava.data.source.TasksRepository;

public class TaskDetailPresenter implements TaskDetailContract.Presenter {

    private String taskId;
    private TasksRepository tasksRepository;
    private TaskDetailContract.View taskDetailView;

    public TaskDetailPresenter(String taskId, TasksRepository tasksRepository, TaskDetailContract.View taskDetailView) {
        this.taskId = taskId;
        this.tasksRepository = tasksRepository;
        this.taskDetailView = taskDetailView;

        this.taskDetailView.setPresenter(this);
    }

    @Override
    public void start() {
        getTask();
    }

    @Override
    public void getTask() {
        if (taskId == null || taskId.equals("")) {
            taskDetailView.showMessage("taskId cant't is null");
            return;
        }

        taskDetailView.setLoadingIndicator(true);
        tasksRepository.getTask(taskId, new TasksDataSource.GetTaskCallBack() {
            @Override
            public void onTaskLoaded(TaskBean taskBean) {
                if (!taskDetailView.isActive()) {
                    return;
                }
                taskDetailView.setLoadingIndicator(false);

                if (taskBean == null) {
                    taskDetailView.showNoTask();
                } else {
                    taskDetailView.showTask(taskBean);
                }
            }

            @Override
            public void onDataNotAvailable() {
                taskDetailView.showNoTask();
            }
        });
    }

    @Override
    public void editTask() {
        if (taskId == null || taskId.equals("")) {
            taskDetailView.showMessage("taskId cant't is null");
            return;
        }

        taskDetailView.editTask(taskId);
    }

    @Override
    public void completeTask() {
        if (taskId == null || taskId.equals("")) {
            taskDetailView.showMessage("taskId cant't is null");
            return;
        }

        tasksRepository.completeTask(taskId);
        taskDetailView.showMessage("Task marked complete");
    }

    @Override
    public void activateTask() {
        if (taskId == null || taskId.equals("")) {
            taskDetailView.showMessage("taskId cant't is null");
            return;
        }

        tasksRepository.activateTask(taskId);
        taskDetailView.showMessage("Task marked active");
    }

    @Override
    public void deleteTask() {
        if (taskId == null || taskId.equals("")) {
            taskDetailView.showMessage("taskId cant't is null");
            return;
        }

        tasksRepository.deleteTask(taskId);
        taskDetailView.deleteTask();
    }
}