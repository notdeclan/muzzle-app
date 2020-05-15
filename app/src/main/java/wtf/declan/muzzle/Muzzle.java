package wtf.declan.muzzle;

import android.app.Application;
import android.content.Context;

import wtf.declan.muzzle.notifications.NotificationFactory;
import wtf.declan.muzzle.taskqueue.TaskManager;

public class Muzzle extends Application {

    private TaskManager             taskManager;
    private NotificationFactory     notificationFactory;

    public static Muzzle getInstance(Context context) {
        return (Muzzle) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initializeNotificationFactory();
        initializeTaskManager();
    }

    private void initializeNotificationFactory() {
        this.notificationFactory = new NotificationFactory(this);
    }

    private void initializeTaskManager() {
        this.taskManager = new TaskManager(5);
    }

    public NotificationFactory getNotificationFactory() {
        return notificationFactory;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

}