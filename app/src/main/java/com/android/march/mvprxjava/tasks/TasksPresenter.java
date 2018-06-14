package com.android.march.mvprxjava.tasks;

import android.app.Activity;

import com.android.march.mvprxjava.addedittask.AddEditTaskActivity;
import com.android.march.mvprxjava.data.TaskBean;
import com.android.march.mvprxjava.data.source.TasksRepository;
import com.android.march.mvprxjava.utils.schedulers.BaseSchedulerProvider;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class TasksPresenter implements TasksContract.Presenter {

    private final TasksRepository tasksRepository;
    private final TasksContract.View tasksView;
    private BaseSchedulerProvider schedulerProvider;

    private TasksFilterType currentFiltering = TasksFilterType.ALL_TASKS;
    private boolean firstLoad = true;

    public TasksPresenter(TasksRepository tasksRepository, TasksContract.View tasksView, BaseSchedulerProvider schedulerProvider) {
        this.tasksRepository = tasksRepository;
        this.tasksView = tasksView;
        this.schedulerProvider = schedulerProvider;
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

        tasksRepository.loadTasks()
                .flatMap(new Func1<List<TaskBean>, Observable<TaskBean>>() {
                    @Override
                    public Observable<TaskBean> call(List<TaskBean> taskBeans) {
                        return Observable.from(taskBeans);
                    }
                })
                .filter(new Func1<TaskBean, Boolean>() {
                    @Override
                    public Boolean call(TaskBean taskBean) {
                        switch (currentFiltering) {
                            case ALL_TASKS:
                                return true;
                            case ACTIVE_TASKS:
                                return taskBean.isActive();
                            case COMPLETED_TASKS:
                                return taskBean.isCompleted();
                        }
                        return null;
                    }
                })
                .toList()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(new Subscriber<List<TaskBean>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!tasksView.isActive()) {
                            return;
                        }
                        // 显示一条消息,提示没有该类型的任务
                        tasksView.showNoTasks(currentFiltering);
                    }

                    @Override
                    public void onNext(List<TaskBean> taskBeans) {
                        if (!tasksView.isActive()) {
                            return;
                        }
                        if (showLoading) {
                            tasksView.setLoadingIndicator(false);
                        }
                        processTasks(taskBeans);
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