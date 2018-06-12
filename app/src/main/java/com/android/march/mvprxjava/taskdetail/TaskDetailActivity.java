package com.android.march.mvprxjava.taskdetail;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.android.march.mvprxjava.Injection;
import com.android.march.mvprxjava.R;

public class TaskDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "TASK_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("任务详情");
        toolbar.setContentInsetStartWithNavigation(0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        String taskId = getIntent().getStringExtra(EXTRA_TASK_ID);
        TaskDetailFragment taskDetailFragment = (TaskDetailFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (taskDetailFragment == null) {
            taskDetailFragment = TaskDetailFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, taskDetailFragment).commit();
        }

        new TaskDetailPresenter(taskId, Injection.provideTasksRepository(getApplicationContext()), taskDetailFragment);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}