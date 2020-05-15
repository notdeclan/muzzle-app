package wtf.declan.muzzle.taskqueue;

import android.content.Context;

/**
 * Abstract Task but however requires an Android Context object in initialization
 */
public abstract class ContextTask extends Task  {

    private Context context;

    protected ContextTask(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }

}
