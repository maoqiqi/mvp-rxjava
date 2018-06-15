package com.android.march.mvprxjava.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.march.mvprxjava.data.TaskBean;
import com.android.march.mvprxjava.data.source.TasksDataSource;
import com.android.march.mvprxjava.utils.schedulers.BaseSchedulerProvider;
import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;

public class TasksLocalDataSource implements TasksDataSource {

    private static volatile TasksLocalDataSource INSTANCE;

    private BriteDatabase briteDatabase;

    private Function<Cursor, TaskBean> cursorTaskBeanFunc;

    private TasksLocalDataSource(Context context, BaseSchedulerProvider schedulerProvider) {
        DatabaseHelper helper = new DatabaseHelper(context);
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        briteDatabase = sqlBrite.wrapDatabaseHelper(helper, schedulerProvider.io());

        cursorTaskBeanFunc = new Function<Cursor, TaskBean>() {
            @Override
            public TaskBean apply(Cursor cursor) {
                return getTask(cursor);
            }
        };
    }

    private TaskBean getTask(Cursor cursor) {
        TaskBean taskBean = new TaskBean();
        taskBean.setId(cursor.getString(cursor.getColumnIndex("id")));
        taskBean.setTitle(cursor.getString(cursor.getColumnIndex("title")));
        taskBean.setDescription(cursor.getString(cursor.getColumnIndex("description")));
        taskBean.setCompleted(cursor.getInt(cursor.getColumnIndex("completed")) == 1);
        return taskBean;
    }

    public static TasksLocalDataSource getInstance(Context context, BaseSchedulerProvider schedulerProvider) {
        if (INSTANCE == null) {
            synchronized (TasksLocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TasksLocalDataSource(context, schedulerProvider);
                }
            }
        }
        return INSTANCE;
    }

    public static void clearInstance() {
        INSTANCE = null;
    }

    @Override
    public Flowable<List<TaskBean>> loadTasks() {
        String sql = "select id,title,description,completed from task";
        return briteDatabase.createQuery("task", sql)
                .mapToList(cursorTaskBeanFunc)
                .toFlowable(BackpressureStrategy.BUFFER);
    }

    @Override
    public Flowable<TaskBean> getTask(String taskId) {
        String sql = "select id,title,description,completed from task where id = ?";
        return briteDatabase.createQuery("task", sql, taskId)
                .mapToOne(new Function<Cursor, TaskBean>() {
                    @Override
                    public TaskBean apply(Cursor cursor) throws Exception {
                        return cursorTaskBeanFunc.apply(cursor);
                    }
                })
                .toFlowable(BackpressureStrategy.BUFFER);
    }

    @Override
    public void clearCompletedTasks() {
        briteDatabase.delete("task", "completed = ?", "1");
    }

    @Override
    public void refreshTasks() {

    }

    @Override
    public void addTask(TaskBean taskBean) {
        ContentValues values = new ContentValues();
        values.put("id", taskBean.getId());
        values.put("title", taskBean.getTitle());
        values.put("description", taskBean.getDescription());
        values.put("completed", taskBean.isCompleted() ? 1 : 0);

        briteDatabase.insert("task", values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public void updateTask(TaskBean taskBean) {
        ContentValues values = new ContentValues();
        values.put("title", taskBean.getTitle());
        values.put("description", taskBean.getDescription());
        values.put("completed", taskBean.isCompleted() ? 1 : 0);
        briteDatabase.update("task", values, "id = ?", taskBean.getId());
    }

    @Override
    public void completeTask(TaskBean completedTaskBean) {
        completeTask(completedTaskBean.getId());
    }

    @Override
    public void completeTask(String taskId) {
        ContentValues values = new ContentValues();
        values.put("completed", true);
        briteDatabase.update("task", values, "id = ?", taskId);
    }

    @Override
    public void activateTask(TaskBean activeTaskBean) {
        activateTask(activeTaskBean.getId());
    }

    @Override
    public void activateTask(String taskId) {
        ContentValues values = new ContentValues();
        values.put("completed", false);
        briteDatabase.update("task", values, "id = ?", taskId);
    }

    @Override
    public void deleteAllTasks() {
        briteDatabase.delete("task", null);
    }

    @Override
    public void deleteTask(String taskId) {
        briteDatabase.delete("task", "id = ?", taskId);
    }
}