package com.android.march.mvprxjava.tasks;

import com.android.march.mvprxjava.base.BasePresenter;
import com.android.march.mvprxjava.base.BaseView;
import com.android.march.mvprxjava.data.TaskBean;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface TasksContract {

    interface Presenter extends BasePresenter {

        // 加载任务
        void loadTasks(boolean forceUpdate);

        // 设置Filtering
        void setFiltering(TasksFilterType requestType);

        // 得到当前Filtering
        TasksFilterType getFiltering();

        // 清除已完成任务
        void clearCompletedTasks();

        // 添加任务
        void addTask();

        // 完成该任务
        void completeTask(TaskBean completedTaskBean);

        // 未完成该任务
        void activateTask(TaskBean activeTaskBean);

        // 打开任务详情
        void openTaskDetails(TaskBean requestedTaskBean);

        // 回调
        void result(int requestCode, int resultCode);
    }

    interface View extends BaseView<Presenter> {

        boolean isActive();

        // 显示弹窗
        void showFilteringPopUpMenu();

        // 显示LOADING
        void setLoadingIndicator(boolean showLoading);

        // 显示Filtering标题
        void showFilterLabel(TasksFilterType tasksFilterType);

        // 显示任务列表
        void showTasks(List<TaskBean> taskBeanList);

        // 显示没有任务结果
        void showNoTasks(TasksFilterType tasksFilterType);

        // 添加任务
        void addTask();

        // 显示任务详情
        void openTaskDetails(String taskId);

        // 显示提示信息
        void showMessage(String message);
    }
}