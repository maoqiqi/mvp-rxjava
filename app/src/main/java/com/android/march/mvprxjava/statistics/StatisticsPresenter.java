package com.android.march.mvprxjava.statistics;

import com.android.march.mvprxjava.data.TaskBean;
import com.android.march.mvprxjava.data.source.TasksDataSource;
import com.android.march.mvprxjava.data.source.TasksRepository;

import java.util.List;

public class StatisticsPresenter implements StatisticsContract.Presenter {

    private TasksRepository tasksRepository;
    private StatisticsContract.View statisticsView;

    public StatisticsPresenter(TasksRepository tasksRepository, StatisticsContract.View statisticsView) {
        this.tasksRepository = tasksRepository;
        this.statisticsView = statisticsView;
        this.statisticsView.setPresenter(this);
    }

    @Override
    public void start() {
        loadStatistics();
    }

    @Override
    public void loadStatistics() {
        statisticsView.setLoadingIndicator(true);
        tasksRepository.loadTasks(new TasksDataSource.LoadTasksCallBack() {
            @Override
            public void onTasksLoaded(List<TaskBean> taskBeanList) {
                int activeTasks = 0;
                int completedTasks = 0;

                for (TaskBean taskBean : taskBeanList) {
                    if (taskBean.isCompleted()) {
                        completedTasks += 1;
                    } else {
                        activeTasks += 1;
                    }
                }

                if (!statisticsView.isActive()) {
                    return;
                }

                statisticsView.setLoadingIndicator(false);
                statisticsView.showStatistics(activeTasks, completedTasks);
            }

            @Override
            public void onDataNotAvailable() {
                if (!statisticsView.isActive()) {
                    return;
                }

                statisticsView.showMessage("Loading statistics error");
            }
        });
    }
}