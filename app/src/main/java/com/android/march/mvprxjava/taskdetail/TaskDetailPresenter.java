package com.android.march.mvprxjava.taskdetail;

import com.android.march.mvprxjava.data.TaskBean;
import com.android.march.mvprxjava.data.source.TasksRepository;
import com.android.march.mvprxjava.utils.schedulers.BaseSchedulerProvider;

import org.reactivestreams.Subscription;

import io.reactivex.FlowableSubscriber;
import io.reactivex.functions.Predicate;

public class TaskDetailPresenter implements TaskDetailContract.Presenter {

    private String taskId;
    private TasksRepository tasksRepository;
    private TaskDetailContract.View taskDetailView;
    private BaseSchedulerProvider schedulerProvider;

    public TaskDetailPresenter(String taskId, TasksRepository tasksRepository, TaskDetailContract.View taskDetailView, BaseSchedulerProvider schedulerProvider) {
        this.taskId = taskId;
        this.tasksRepository = tasksRepository;
        this.taskDetailView = taskDetailView;
        this.schedulerProvider = schedulerProvider;

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
        tasksRepository.getTask(taskId)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(new FlowableSubscriber<TaskBean>() {
                    @Override
                    public void onSubscribe(Subscription s) {

                    }

                    @Override
                    public void onNext(TaskBean taskBean) {
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
                    public void onError(Throwable t) {
                        if (!taskDetailView.isActive()) {
                            return;
                        }
                        taskDetailView.showNoTask();
                    }

                    @Override
                    public void onComplete() {

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