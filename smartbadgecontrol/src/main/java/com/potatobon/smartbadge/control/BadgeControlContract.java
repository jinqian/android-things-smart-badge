package com.potatobon.smartbadge.control;

public interface BadgeControlContract {

    interface Presenter {

        void registerView(View view);

        void unregisterView();

        void setTextToDisplay(String message);
    }

    interface View {

        void showConnectedToMessage(String endpointName);

        void showApiNotConnected();

    }
}
