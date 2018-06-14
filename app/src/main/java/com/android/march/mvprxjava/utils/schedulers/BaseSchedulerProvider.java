package com.android.march.mvprxjava.utils.schedulers;

import rx.Scheduler;

public interface BaseSchedulerProvider {

    Scheduler io();

    Scheduler computation();

    Scheduler ui();
}