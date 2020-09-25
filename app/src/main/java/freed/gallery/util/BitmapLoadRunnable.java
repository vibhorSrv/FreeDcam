package freed.gallery.util;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import freed.file.holder.BaseHolder;
import freed.image.ImageTask;
import freed.utils.Log;
import freed.viewer.helper.BitmapHelper;

public class BitmapLoadRunnable extends ImageTask
{
    private final String TAG = BitmapLoadRunnable.class.getSimpleName();
    BaseHolder fileHolder;
    private BitmapLoadingEvents bitmapLoadingListner;

    public interface BitmapLoadingEvents
    {
        void onBitmapLoaded(Bitmap bitmap);
        void onBitmapLoadingFailed();
    }

    public BitmapLoadRunnable(BaseHolder fileHolder)
    {
        this.fileHolder = fileHolder;
    }

    public void setBitmapLoadingListner(BitmapLoadingEvents events)
    {
        this.bitmapLoadingListner = events;
    }

    @Override
    public boolean process() {
        try {
            Log.d(TAG, "load file:" + fileHolder.getName());
            final Bitmap bitmap = BitmapHelper.GET().getBitmap(fileHolder, true);
            if (bitmap != null) {
                if (bitmapLoadingListner != null)
                    bitmapLoadingListner.onBitmapLoaded(bitmap);
            }
            else
                if (bitmapLoadingListner != null)
                    bitmapLoadingListner.onBitmapLoadingFailed();
        }
        catch (NullPointerException ex)
        {
            Log.WriteEx(ex);
            if (bitmapLoadingListner != null)
                bitmapLoadingListner.onBitmapLoadingFailed();
        }
        return false;
    }
}
