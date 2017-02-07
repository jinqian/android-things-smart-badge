package com.potatobon.smartbadge;

interface DisplayContract {

    interface Presenter {

        void unregisterView();

        void registerView(View view);
    }

    interface View {

        void displayText(String text);

        void displayStatus(String status);
    }

}
