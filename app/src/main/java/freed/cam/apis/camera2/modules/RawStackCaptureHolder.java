package freed.cam.apis.camera2.modules;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.media.Image;
import android.media.ImageReader;

import freed.ActivityInterface;
import freed.cam.apis.basecamera.modules.ModuleInterface;
import freed.cam.apis.basecamera.modules.WorkFinishEvents;
import freed.utils.Log;

public class RawStackCaptureHolder extends ImageCaptureHolder {

    private final static String TAG = RawStackCaptureHolder.class.getSimpleName();
    public RawStackCaptureHolder(CameraCharacteristics characteristicss, boolean isRawCapture, boolean isJpgCapture, ActivityInterface activitiy, ModuleInterface imageSaver, WorkFinishEvents finish, RdyToSaveImg rdyToSaveImg) {
        super(characteristicss, isRawCapture, isJpgCapture, activitiy, imageSaver, finish, rdyToSaveImg);
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image img = null;
        Log.d(TAG, "OnRawAvailible");
        /*try {
            img = reader.acquireLatestImage();

            if (isJpgCapture && img.getFormat() == ImageFormat.JPEG)
                AddImage(img);
            else if (isRawCapture && (img.getFormat() == ImageFormat.RAW_SENSOR || img.getFormat() == ImageFormat.RAW10))
                AddImage(img);
            else if (!isJpgCapture && !isRawCapture)
                AddImage(img);
            else {
                if (images.contains(img))
                    images.remove(img);
                img.close();
            }
        }
        catch (IllegalStateException ex)
        {
            if (images.contains(img))
                images.remove(img);
            if (img != null)
                img.close();
        }
        if (rdyToGetSaved()) {
            save();
            rdyToSaveImg.onRdyToSaveImg(ImageCaptureHolder.this);
        }*/
    }
}
