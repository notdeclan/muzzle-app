package wtf.declan.muzzle.taskqueue;

public class TaskThread extends Thread {

    private static final String TAG = TaskThread.class.getSimpleName();

    private final TaskQueue taskQueue;

    public TaskThread(String name, TaskQueue taskQueue) {
        super(name);
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        while (true) {                              // Run forever
            Task task = taskQueue.getNext();        // Wait until it gets a new task
            runTask(task);                          // Run task
        }
    }

    private void runTask(Task task) {
        try {
            task.onRun();                           // call Tasks onRun function
        } catch (Exception e) {                     // if exception
            task.onException(e);                    // run canceled
        }
    }

}
