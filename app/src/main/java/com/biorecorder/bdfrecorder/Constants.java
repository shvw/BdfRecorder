package com.biorecorder.bdfrecorder;

public class Constants {
    public interface ACTION {
        static final int NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001;
        static final String INTENT_CLASS_MAIN_ACTIVITY = BuildConfig.APPLICATION_ID + ".MainActivity";
        static final String NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID + ".Channel";
        public static String STARTFOREGROUND_ACTION = "com.biorecorder.bdfrecorder.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.biorecorder.bdfrecorder.action.stopforeground";
    }
}
