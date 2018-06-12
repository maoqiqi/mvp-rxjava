package com.android.march.mvprxjava.data.source.local;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.march.mvprxjava.data.TaskBean;

import java.util.ArrayList;
import java.util.List;

public class TasksDAO {

    private DatabaseHelper helper;

    public TasksDAO(Context context) {
        helper = new DatabaseHelper(context);
    }

    public List<TaskBean> loadTasks() {
        List<TaskBean> list = new ArrayList<>();

        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "select id,title,description,completed from task";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            TaskBean taskBean = new TaskBean();
            taskBean.setId(cursor.getString(cursor.getColumnIndex("id")));
            taskBean.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            taskBean.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            taskBean.setCompleted(cursor.getInt(cursor.getColumnIndex("completed")) == 1);
            list.add(taskBean);
        }
        cursor.close();
        db.close();
        return list;
    }

    public TaskBean getTaskById(String taskId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "select id,title,description,completed from task where id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{taskId});
        if (cursor.moveToNext()) {
            TaskBean taskBean = new TaskBean();
            taskBean.setId(cursor.getString(cursor.getColumnIndex("id")));
            taskBean.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            taskBean.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            taskBean.setCompleted(cursor.getInt(cursor.getColumnIndex("completed")) == 1);
            cursor.close();
            db.close();
            return taskBean;
        }
        cursor.close();
        db.close();
        return null;
    }

    public void addTask(TaskBean taskBean) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into task(id,title,description,completed) values(?,?,?,?)";
        db.execSQL(sql, new Object[]{taskBean.getId(), taskBean.getTitle(),
                taskBean.getDescription(), taskBean.isCompleted() ? 1 : 0});
        db.close();
    }

    public void updateTask(TaskBean taskBean) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "update task set title=?,description=?,completed=? where id=?";
        db.execSQL(sql, new Object[]{taskBean.getTitle(), taskBean.getDescription(),
                taskBean.isCompleted() ? 1 : 0, taskBean.getId()});
        db.close();
    }

    public void updateCompleted(String taskId, boolean completed) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "update task set completed=? where id=?";
        db.execSQL(sql, new Object[]{completed ? 1 : 0, taskId});
        db.close();
    }

    public void deleteTaskById(String taskId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "delete from task where id=?";
        db.execSQL(sql, new Object[]{taskId});
        db.close();
    }

    public void deleteCompletedTasks() {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "delete from task where completed=1";
        db.execSQL(sql);
        db.close();
    }

    public void deleteTasks() {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "delete from task";
        db.execSQL(sql);
        db.close();
    }
}