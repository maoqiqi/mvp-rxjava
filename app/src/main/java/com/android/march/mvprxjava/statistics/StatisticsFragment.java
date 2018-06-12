package com.android.march.mvprxjava.statistics;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.march.mvprxjava.R;

public class StatisticsFragment extends Fragment implements StatisticsContract.View {

    private StatisticsContract.Presenter presenter;
    private TextView tvStatistics;

    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    @Override
    public void setPresenter(StatisticsContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_statistics, container, false);
        tvStatistics = root.findViewById(R.id.tvStatistics);
        return root;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void setLoadingIndicator(boolean showLoading) {
        if (showLoading) {
            tvStatistics.setText("LOADING");
        } else {
            tvStatistics.setText("");
        }
    }

    @Override
    public void showStatistics(int numberOfActiveTasks, int numberOfCompletedTasks) {
        if (numberOfActiveTasks == 0 && numberOfCompletedTasks == 0) {
            tvStatistics.setText("你还没有任务");
        } else {
            tvStatistics.setText("未完成任务总数：" + numberOfActiveTasks + "\n" + "已完成任务总数：" + numberOfCompletedTasks);
        }
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }
}