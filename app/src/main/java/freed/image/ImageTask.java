package freed.image;

import freed.utils.Log;

/**
 * Created by KillerInk on 13.11.2017.
 */

public abstract class ImageTask implements Runnable {
    private final String TAG = ImageSaveTask.class.getSimpleName();
    public abstract boolean process();
    private Thread currentThread;
    public Thread getThread()
    {
        return currentThread;
    }

    @Override
    public void run() {
        currentThread = Thread.currentThread();
        if (currentThread.interrupted()) {
            Log.e(TAG, "thread is interrupted");
            return;
        }
        process();
    }
}
