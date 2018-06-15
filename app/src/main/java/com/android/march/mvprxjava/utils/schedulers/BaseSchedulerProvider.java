package com.android.march.mvprxjava.utils.schedulers;

import io.reactivex.Scheduler;

public interface BaseSchedulerProvider {

    Scheduler io();

    Scheduler computation();

    Scheduler ui();
}