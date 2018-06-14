package com.android.march.mvprxjava.addedittask;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.android.march.mvprxjava.Injection;
import com.android.march.mvprxjava.R;

public class AddEditTaskActivity extends AppCompatActivity {

    public static final int REQUEST_ADD_TASK = 1;

    public static final String SHOULD_LOAD_DATA_FROM_REPO_KEY = "SHOULD_LOAD_DATA_FROM_REPO_KEY";

    private AddEditTaskPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        String taskId = getIntent().getStringExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (taskId == null) {
            toolbar.setTitle("添加任务");
        } else {
            toolbar.setTitle("编辑任务");
        }
        toolbar.setContentInsetStartWithNavigation(0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        AddEditTaskFragment addEditTaskFragment = (AddEditTaskFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (addEditTaskFragment == null) {
            addEditTaskFragment = AddEditTaskFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, addEditTaskFragment).commit();
        }

        boolean shouldLoadDataFromRepo = true;
        if (savedInstanceState != null) {
            shouldLoadDataFromRepo = savedInstanceState.getBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY);
        }

        presenter = new AddEditTaskPresenter(taskId, Injection.provideTasksRepository(getApplicationContext()),
                addEditTaskFragment, shouldLoadDataFromRepo, Injection.provideSchedulerProvider());
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY, presenter.isDataMissing());
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}