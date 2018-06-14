package com.android.march.mvprxjava.utils.schedulers;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SchedulerProvider implements BaseSchedulerProvider {

    private static SchedulerProvider INSTANCE;

    private SchedulerProvider() {

    }

    public static synchronized SchedulerProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SchedulerProvider();
        }
        return INSTANCE;
    }

    @Override
    public Scheduler io() {
        return Schedulers.io();
    }

    @Override
    public Scheduler computation() {
        return Schedulers.computation();
    }

    @Override
    public Scheduler ui() {
        return AndroidSchedulers.mainThread();
    }
}