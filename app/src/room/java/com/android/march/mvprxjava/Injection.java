package com.android.march.mvprxjava;

import android.content.Context;

import com.android.march.mvprxjava.data.source.TasksRepository;
import com.android.march.mvprxjava.data.source.remote.TasksRemoteDataSource;
import com.android.march.mvprxjava.data.source.room.TasksRoomDataSource;
import com.android.march.mvprxjava.utils.AppExecutors;

public class Injection {

    public static TasksRepository provideTasksRepository(Context context) {
        return TasksRepository.getInstance(TasksRemoteDataSource.getInstance(),
                TasksRoomDataSource.getInstance(context, new AppExecutors()));
    }
}