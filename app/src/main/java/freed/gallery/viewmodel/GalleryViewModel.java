package freed.gallery.viewmodel;

import android.app.Application;

import androidx.lifecycle.ViewModel;

import com.troop.freedcam.R;

import java.util.List;

import freed.file.FileListController;
import freed.file.holder.BaseHolder;
import freed.viewer.helper.BitmapHelper;

public class GalleryViewModel extends ViewModel
{
    private BitmapHelper bitmapHelper;
    private FileListController fileListController;

    public void create(Application application)
    {
        bitmapHelper = new BitmapHelper(application.getApplicationContext(),application.getResources().getDimensionPixelSize(R.dimen.image_thumbnails_size));
        fileListController = new FileListController(application.getApplicationContext());
    }

    public void loadFreeDcamDcimFiles()
    {
        fileListController.LoadFreeDcamDCIMDirsFiles();
    }

    public List<BaseHolder> getFiles()
    {
        return fileListController.getFiles();
    }
}
