package com.android.march.mvprxjava.statistics;

import android.support.v4.util.Pair;

import com.android.march.mvprxjava.data.TaskBean;
import com.android.march.mvprxjava.data.source.TasksDataSource;
import com.android.march.mvprxjava.data.source.TasksRepository;
import com.android.march.mvprxjava.utils.schedulers.BaseSchedulerProvider;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;

public class StatisticsPresenter implements StatisticsContract.Presenter {

    private TasksRepository tasksRepository;
    private StatisticsContract.View statisticsView;
    private BaseSchedulerProvider schedulerProvider;

    public StatisticsPresenter(TasksRepository tasksRepository, StatisticsContract.View statisticsView, BaseSchedulerProvider schedulerProvider) {
        this.tasksRepository = tasksRepository;
        this.statisticsView = statisticsView;
        this.schedulerProvider = schedulerProvider;
        this.statisticsView.setPresenter(this);
    }

    @Override
    public void start() {
        loadStatistics();
    }

    @Override
    public void loadStatistics() {
        statisticsView.setLoadingIndicator(true);

        Observable<TaskBean> tasks = tasksRepository.loadTasks().flatMap(new Func1<List<TaskBean>, Observable<TaskBean>>() {
            @Override
            public Observable<TaskBean> call(List<TaskBean> taskBeans) {
                return Observable.from(taskBeans);
            }
        });

        Observable<Integer> activeTasks = tasks.filter(new Func1<TaskBean, Boolean>() {
            @Override
            public Boolean call(TaskBean taskBean) {
                return taskBean.isActive();
            }
        }).count();

        Observable<Integer> completedTasks = tasks.filter(new Func1<TaskBean, Boolean>() {
            @Override
            public Boolean call(TaskBean taskBean) {
                return taskBean.isCompleted();
            }
        }).count();

        Observable
                .zip(completedTasks, activeTasks, new Func2<Integer, Integer, Pair>() {
                    @Override
                    public Pair call(Integer completed, Integer active) {
                        return Pair.create(active, completed);
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(new Subscriber<Pair>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!statisticsView.isActive()) {
                            return;
                        }
                        statisticsView.showMessage("Loading statistics error");
                    }

                    @Override
                    public void onNext(Pair pair) {
                        if (!statisticsView.isActive()) {
                            return;
                        }
                        statisticsView.setLoadingIndicator(false);
                        statisticsView.showStatistics(Integer.valueOf(pair.first.toString()),
                                Integer.valueOf(pair.first.toString()));
                    }
                });
    }
}