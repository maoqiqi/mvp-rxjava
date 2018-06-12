package com.android.march.mvprxjava.taskdetail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.android.march.mvprxjava.R;
import com.android.march.mvprxjava.addedittask.AddEditTaskActivity;
import com.android.march.mvprxjava.addedittask.AddEditTaskFragment;
import com.android.march.mvprxjava.data.TaskBean;

public class TaskDetailFragment extends Fragment implements TaskDetailContract.View {

    private static final int REQUEST_EDIT_TASK = 1;

    private TaskDetailContract.Presenter presenter;

    private CheckBox cbComplete;
    private TextView tvTitle;
    private TextView tvDescription;

    public static TaskDetailFragment newInstance() {
        return new TaskDetailFragment();
    }

    @Override
    public void setPresenter(TaskDetailContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_task_detail, container, false);

        cbComplete = root.findViewById(R.id.cbComplete);
        tvTitle = root.findViewById(R.id.tvTitle);
        tvDescription = root.findViewById(R.id.tvDescription);

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fabEditTask = getActivity().findViewById(R.id.fabEditTask);
        fabEditTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.editTask();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_TASK) {
            if (resultCode == Activity.RESULT_OK) {
                getActivity().finish();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                presenter.deleteTask();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void setLoadingIndicator(boolean showLoading) {

    }

    @Override
    public void showTask(TaskBean taskBean) {
        tvTitle.setText(taskBean.getTitle());
        tvDescription.setText(taskBean.getDescription());

        cbComplete.setChecked(taskBean.isCompleted());
        cbComplete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    presenter.completeTask();
                } else {
                    presenter.activateTask();
                }
            }
        });
    }

    @Override
    public void showNoTask() {
        tvTitle.setText("");
        tvDescription.setText("没有数据");
    }

    @Override
    public void editTask(String taskId) {
        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

    @Override
    public void deleteTask() {
        getActivity().finish();
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }
}