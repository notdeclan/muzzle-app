package wtf.declan.muzzle.taskqueue;

import java.util.LinkedList;
import java.util.ListIterator;

class TaskQueue {

    private final LinkedList<Task> taskQueue = new LinkedList<>();

    synchronized void add(Task task){
        taskQueue.add(task);
        notifyAll();
    }

    synchronized Task getNext() {
        try {
            Task nextTask;

            while ((nextTask = getNextTask()) == null)
                wait();

            return nextTask;
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private Task getNextTask() {
        if(!taskQueue.isEmpty()) {
            ListIterator<Task> iterator = taskQueue.listIterator();
            if (iterator.hasNext()) {
                Task task = iterator.next();
                iterator.remove();
                return task;
            }
        }

        return null;
    }

}
