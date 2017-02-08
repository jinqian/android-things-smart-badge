package com.potatobon.smartbadge.display;

interface BadgeDisplayContract {

    interface Presenter {

        void unregisterView();

        void registerView(View view);
    }

    interface View {

        void displayText(String text);

        void displayStatus(String status);
    }

}
