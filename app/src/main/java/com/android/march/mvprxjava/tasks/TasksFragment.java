package com.android.march.mvprxjava.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.android.march.mvprxjava.R;
import com.android.march.mvprxjava.addedittask.AddEditTaskActivity;
import com.android.march.mvprxjava.data.TaskBean;
import com.android.march.mvprxjava.statistics.StatisticsActivity;
import com.android.march.mvprxjava.taskdetail.TaskDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment implements TasksContract.View {

    private TasksContract.Presenter presenter;

    private List<TaskBean> list;
    private TasksAdapter adapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private LinearLayout llTasks;
    private TextView tvFilteringLabel;
    private RecyclerView recyclerView;

    private LinearLayout llNoTasks;
    private ImageView ivNoTasks;
    private TextView tvNoTasks;

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public void setPresenter(TasksContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list = new ArrayList<>(0);
        adapter = new TasksAdapter(getContext(), list, taskItemListener);
    }

    private TaskItemListener taskItemListener = new TaskItemListener() {

        @Override
        public void onActivateTask(TaskBean activateTaskBean) {
            presenter.activateTask(activateTaskBean);
        }

        @Override
        public void onCompleteTask(TaskBean completedTaskBean) {
            presenter.completeTask(completedTaskBean);
        }

        @Override
        public void onOpenTaskDetails(TaskBean requestedTaskBean) {
            presenter.openTaskDetails(requestedTaskBean);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tasks, container, false);

        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.loadTasks(false);
            }
        });

        llTasks = root.findViewById(R.id.llTasks);
        tvFilteringLabel = root.findViewById(R.id.tvFilteringLabel);
        recyclerView = root.findViewById(R.id.recyclerView);

        llNoTasks = root.findViewById(R.id.llNoTasks);
        ivNoTasks = root.findViewById(R.id.ivNoTasks);
        tvNoTasks = root.findViewById(R.id.tvNoTasks);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAddTask = getActivity().findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.addTask();
            }
        });

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tasks, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                break;
            case R.id.menu_clear:
                presenter.clearCompletedTasks();
                break;
            case R.id.menu_refresh:
                presenter.loadTasks(true);
                break;
            case R.id.menu_statistics:
                Intent intent = new Intent(getContext(), StatisticsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.result(requestCode, resultCode);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.menu_filter_tasks, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_active:
                        presenter.setFiltering(TasksFilterType.ACTIVE_TASKS);
                        break;
                    case R.id.menu_completed:
                        presenter.setFiltering(TasksFilterType.COMPLETED_TASKS);
                        break;
                    default:
                        presenter.setFiltering(TasksFilterType.ALL_TASKS);
                        break;
                }
                presenter.loadTasks(false);
                return true;
            }
        });

        popup.show();
    }

    @Override
    public void setLoadingIndicator(final boolean showLoading) {
        if (getView() == null) {
            return;
        }

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(showLoading);
            }
        });
    }

    @Override
    public void showFilterLabel(TasksFilterType tasksFilterType) {
        switch (tasksFilterType) {
            case ALL_TASKS:
                tvFilteringLabel.setText("所有任务");
                break;
            case ACTIVE_TASKS:
                tvFilteringLabel.setText("未完成任务");
                break;
            case COMPLETED_TASKS:
                tvFilteringLabel.setText("已完成任务");
                break;
        }
    }

    @Override
    public void showTasks(List<TaskBean> taskBeanList) {
        llTasks.setVisibility(View.VISIBLE);
        llNoTasks.setVisibility(View.GONE);

        list.clear();
        list.addAll(taskBeanList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void showNoTasks(TasksFilterType tasksFilterType) {
        switch (tasksFilterType) {
            case ALL_TASKS:
                showNoTasksViews("你还没有任务", R.drawable.ic_all_tasks);
                break;
            case ACTIVE_TASKS:
                showNoTasksViews("你还没有未完成的任务", R.drawable.ic_active_tasks);
                break;
            case COMPLETED_TASKS:
                showNoTasksViews("你还没有已完成的任务", R.drawable.ic_completed_tasks);
                break;
        }
    }

    private void showNoTasksViews(String text, int iconRes) {
        llTasks.setVisibility(View.GONE);
        llNoTasks.setVisibility(View.VISIBLE);

        tvNoTasks.setText(text);
        ivNoTasks.setImageResource(iconRes);
    }

    @Override
    public void addTask() {
        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
        startActivityForResult(intent, AddEditTaskActivity.REQUEST_ADD_TASK);
    }

    @Override
    public void openTaskDetails(String taskId) {
        Intent intent = new Intent(getContext(), TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId);
        startActivity(intent);
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    private static final class TasksAdapter extends RecyclerView.Adapter<ViewHolder> {

        private Context context;
        private List<TaskBean> taskBeanList;
        private TaskItemListener listener;

        public TasksAdapter(Context context, List<TaskBean> taskBeanList, TaskItemListener listener) {
            this.context = context;
            this.taskBeanList = taskBeanList;
            this.listener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_task, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final TaskBean taskBean = taskBeanList.get(position);

            holder.tvTitle.setText(taskBean.getTitle());
            holder.cbComplete.setChecked(taskBean.isCompleted());

            if (taskBean.isCompleted()) {
                holder.llItem.setBackgroundResource(R.drawable.bg_item_completed);
            } else {
                holder.llItem.setBackgroundResource(R.drawable.bg_item);
            }

            holder.cbComplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (taskBean.isCompleted()) {
                        listener.onActivateTask(taskBean);
                    } else {
                        listener.onCompleteTask(taskBean);
                    }
                }
            });
            holder.llItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onOpenTaskDetails(taskBean);
                }
            });
        }

        @Override
        public int getItemCount() {
            return taskBeanList.size();
        }
    }

    private static final class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llItem;
        CheckBox cbComplete;
        TextView tvTitle;

        ViewHolder(View itemView) {
            super(itemView);
            llItem = itemView.findViewById(R.id.llItem);
            cbComplete = itemView.findViewById(R.id.cbComplete);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }

    private interface TaskItemListener {

        void onActivateTask(TaskBean activateTaskBean);

        void onCompleteTask(TaskBean completedTaskBean);

        void onOpenTaskDetails(TaskBean requestedTaskBean);
    }
}