package com.android.march.mvprxjava;

import android.content.Context;

import com.android.march.mvprxjava.data.source.TasksRepository;
import com.android.march.mvprxjava.data.source.local.TasksLocalDataSource;
import com.android.march.mvprxjava.data.source.remote.FakeTasksRemoteDataSource;
import com.android.march.mvprxjava.utils.schedulers.BaseSchedulerProvider;
import com.android.march.mvprxjava.utils.schedulers.SchedulerProvider;

public class Injection {

    public static TasksRepository provideTasksRepository(Context context) {
        return TasksRepository.getInstance(FakeTasksRemoteDataSource.getInstance(),
                TasksLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static BaseSchedulerProvider provideSchedulerProvider() {
        return SchedulerProvider.getInstance();
    }
}