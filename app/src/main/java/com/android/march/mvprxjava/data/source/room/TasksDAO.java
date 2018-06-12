package com.android.march.mvprxjava.data.source.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.android.march.mvprxjava.data.TaskBean;

import java.util.List;

@Dao
public interface TasksDAO {

    @Query("select id,title,description,completed from task")
    List<TaskBean> loadTasks();

    @Query("select id,title,description,completed from task where id = :taskId")
    TaskBean getTaskById(String taskId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addTask(TaskBean taskBean);

    @Update
    void updateTask(TaskBean taskBean);

    @Query("update task set completed=:completed where id=:taskId")
    void updateCompleted(String taskId, boolean completed);

    @Query("delete from task where id=:taskId")
    void deleteTaskById(String taskId);

    @Query("delete from task where completed=1")
    void deleteCompletedTasks();

    @Query("delete from task")
    void deleteTasks();
}