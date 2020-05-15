package wtf.declan.muzzle.taskqueue;

/**
 * Abstract Class is used as the base class for all threaded tasks
 * Each task should inherit and implement the functions
 */
public abstract class Task {

    /**
     * When there is a free space within the task queue, this function will be called.
     *
     * This is where all the threaded operations within a task should take place
     *
     * @throws Exception: if an error occur within the operation, any exception can be raised and
     * the onException function will consequently be called
     */
    public abstract void onRun() throws Exception;

    /**
     * When an exception is raised within onRun() this function will be called
     *
     * This can be used to re insert the task to try again, or to print logging messages
     *
     * @param e: exception raised from onRun()
     */
    public abstract void onException(Exception e);
}
