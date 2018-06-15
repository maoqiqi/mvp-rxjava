package com.android.march.mvprxjava.statistics;

import android.support.v4.util.Pair;

import com.android.march.mvprxjava.data.TaskBean;
import com.android.march.mvprxjava.data.source.TasksRepository;
import com.android.march.mvprxjava.utils.schedulers.BaseSchedulerProvider;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

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

        Flowable<TaskBean> tasks = tasksRepository.loadTasks()
                .flatMap(new Function<List<TaskBean>, Publisher<TaskBean>>() {
                    @Override
                    public Publisher<TaskBean> apply(List<TaskBean> taskBeans) throws Exception {
                        return Flowable.fromIterable(taskBeans);
                    }
                });

        Flowable<Long> activeTasks = tasks.filter(new Predicate<TaskBean>() {
            @Override
            public boolean test(TaskBean taskBean) throws Exception {
                return taskBean.isActive();
            }
        }).count().toFlowable();

        Flowable<Long> completedTasks = tasks.filter(new Predicate<TaskBean>() {
            @Override
            public boolean test(TaskBean taskBean) throws Exception {
                return taskBean.isCompleted();
            }
        }).count().toFlowable();

        Flowable
                .zip(completedTasks, activeTasks, new BiFunction<Long, Long, Pair<Long, Long>>() {
                    @Override
                    public Pair<Long, Long> apply(Long completed, Long active) throws Exception {
                        return Pair.create(active, completed);
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(new FlowableSubscriber<Pair<Long, Long>>() {
                    @Override
                    public void onSubscribe(Subscription s) {

                    }

                    @Override
                    public void onNext(Pair<Long, Long> pair) {
                        if (!statisticsView.isActive()) {
                            return;
                        }
                        statisticsView.setLoadingIndicator(false);
                        statisticsView.showStatistics(Integer.parseInt(pair.first.toString()), Integer.parseInt(pair.second.toString()));
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (!statisticsView.isActive()) {
                            return;
                        }
                        statisticsView.showMessage("Loading statistics error");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}