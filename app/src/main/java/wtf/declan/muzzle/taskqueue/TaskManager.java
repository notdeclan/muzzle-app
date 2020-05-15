package wtf.declan.muzzle.taskqueue;

import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * The TaskManager class is used to handle the multithreaded task system which is used throughout
 * the application
 *
 * When initializing this class, x threads will be created indefinitely which will be used to fire
 * new tasks added to the task queue
 */
public class TaskManager {

    private final static String TAG = TaskManager.class.getSimpleName();

    private final TaskQueue taskQueue = new TaskQueue();
    private final Executor executor = Executors.newSingleThreadExecutor();

    public TaskManager(int threadCount) {
        for(int i = 0; i < threadCount; i++) {
            new TaskThread("Task Thread " + i, taskQueue).start();
        }
        Log.i(TAG, "Started " + threadCount + " task threads");
    }

    /**
     * Function is used to add a task to the executor queue
     * @param task: Task to add
     */
    public void add(Task task) {
        Log.d(TAG, "add: " + task.getClass().getSimpleName() + " added to task queue");
        executor.execute(() -> {
            try {
                taskQueue.add(task);
            } catch (Exception e) {
                Log.w(TAG, e);
                task.onException(e);
            }
        });
    }

    /**
     * Function can be used to add multiple tasks the executor queue
     *
     * @param tasks: Tasks to add to queue
     */
    public void add(Task ... tasks) {
        for(Task task : tasks) {
            add(task);
        }
    }

}
