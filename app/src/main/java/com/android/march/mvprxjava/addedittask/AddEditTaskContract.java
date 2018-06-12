package com.android.march.mvprxjava.addedittask;

import com.android.march.mvprxjava.base.BasePresenter;
import com.android.march.mvprxjava.base.BaseView;

public interface AddEditTaskContract {

    interface Presenter extends BasePresenter {

        void addTask(String title, String description);

        boolean isDataMissing();
    }

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void setTitle(String title);

        void setDescription(String description);

        // 显示提示信息
        void showMessage(String message);

        void showTasks();
    }
}