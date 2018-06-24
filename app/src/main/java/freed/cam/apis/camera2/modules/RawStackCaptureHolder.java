package freed.cam.apis.camera2.modules;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;

import java.nio.ByteBuffer;

import freed.ActivityInterface;
import freed.cam.apis.basecamera.modules.ModuleInterface;
import freed.cam.apis.basecamera.modules.WorkFinishEvents;
import freed.dng.DngProfile;
import freed.jni.RawStack;
import freed.utils.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RawStackCaptureHolder extends ImageCaptureHolder {

    private final static String TAG = RawStackCaptureHolder.class.getSimpleName();

    private RawStack rawStack;
    private int stackCoutn = 0;
    private DngProfile dngProfile;

    public RawStackCaptureHolder(CameraCharacteristics characteristicss, boolean isRawCapture, boolean isJpgCapture, ActivityInterface activitiy, ModuleInterface imageSaver, WorkFinishEvents finish, RdyToSaveImg rdyToSaveImg) {
        super(characteristicss, isRawCapture, isJpgCapture, activitiy, imageSaver, finish, rdyToSaveImg);
        rawStack =new RawStack();
        stackCoutn = 0;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image img = null;
        Log.d(TAG, "OnRawAvailible");
        Image image = reader.acquireLatestImage();
        if (image.getFormat() != ImageFormat.RAW_SENSOR)
            image.close();
        else {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            if (stackCoutn == 0) {
                dngProfile = getDngProfile(DngProfile.Plain, image);
                rawStack.setFirstFrame(bytes, image.getWidth(), image.getHeight());
            } else
                rawStack.stackNextFrame(bytes);
            stackCoutn++;
            image.close();
            buffer.clear();
            bytes = null;
            rdyToSaveImg.onRdyToSaveImg(RawStackCaptureHolder.this);
        }
    }

    public void writeDng(String fileout)
    {
        rawStack.saveDng(dngProfile, dngProfile.matrixes,fileout);
    }
}
