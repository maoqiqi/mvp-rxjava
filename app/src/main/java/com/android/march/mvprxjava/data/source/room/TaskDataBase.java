package com.android.march.mvprxjava.data.source.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.android.march.mvprxjava.data.TaskBean;

@Database(entities = {TaskBean.class}, version = 1)
public abstract class TaskDataBase extends RoomDatabase {

    private static TaskDataBase INSTANCE;

    public abstract TasksDAO tasksDAO();

    private static final Object lock = new Object();

    public static TaskDataBase getInstance(Context context) {
        synchronized (lock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        TaskDataBase.class, "room_tasks.db").build();
            }
            return INSTANCE;
        }
    }
}