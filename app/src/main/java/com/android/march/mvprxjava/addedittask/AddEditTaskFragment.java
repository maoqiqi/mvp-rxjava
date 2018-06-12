package com.android.march.mvprxjava.addedittask;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.android.march.mvprxjava.R;

public class AddEditTaskFragment extends Fragment implements AddEditTaskContract.View {

    public static final String ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID";

    private AddEditTaskContract.Presenter presenter;

    private EditText editTextTitle;
    private EditText editTextDescription;

    public static AddEditTaskFragment newInstance() {
        return new AddEditTaskFragment();
    }

    @Override
    public void setPresenter(AddEditTaskContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_edit_task, container, false);

        editTextTitle = root.findViewById(R.id.editTextTitle);
        editTextDescription = root.findViewById(R.id.editTextDescription);

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fabAddEditTask = getActivity().findViewById(R.id.fabAddEditTask);
        fabAddEditTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.addTask(editTextTitle.getText().toString(), editTextDescription.getText().toString());
            }
        });
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void setTitle(String title) {
        editTextTitle.setText(title);
    }

    @Override
    public void setDescription(String description) {
        editTextDescription.setText(description);
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showTasks() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }
}