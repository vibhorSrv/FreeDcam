package freed.gallery.model;

import android.graphics.Bitmap;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import java.lang.ref.WeakReference;

import freed.file.holder.BaseHolder;
import freed.gallery.util.BitmapLoadRunnable;
import freed.image.ImageManager;
import freed.image.ImageTask;
import freed.utils.Log;
import freed.viewer.helper.BitmapHelper;

public class GalleryItemModel {
    private final static String TAG = GalleryItemModel.class.getSimpleName();
    private BaseHolder baseHolder;

    public void setBaseHolder(BaseHolder baseHolder)
    {
        this.baseHolder = baseHolder;
    }

    public BaseHolder getBaseHolder()
    {
        return baseHolder;
    }
}
