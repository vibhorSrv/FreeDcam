package freed.cam.apis.camera2.modules;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import freed.ActivityInterface;
import freed.cam.apis.basecamera.modules.ModuleInterface;
import freed.cam.apis.basecamera.modules.WorkFinishEvents;
import freed.dng.DngProfile;
import freed.jni.RawStack;
import freed.utils.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RawStackCaptureHolder extends ImageCaptureHolder {

    private final static String TAG = RawStackCaptureHolder.class.getSimpleName();

    private final BlockingQueue<Runnable> imagesToSaveQueue;
    private final ThreadPoolExecutor imageSaveExecutor;
    private final int KEEP_ALIVE_TIME = 500;

    private RawStack rawStack;

    public int getStackCoutn() {
        return stackCoutn;
    }

    private int stackCoutn = 0;
    private DngProfile dngProfile;
    private int width;
    private int height;


    public RawStackCaptureHolder(CameraCharacteristics characteristicss, boolean isRawCapture, boolean isJpgCapture, ActivityInterface activitiy, ModuleInterface imageSaver, WorkFinishEvents finish, RdyToSaveImg rdyToSaveImg) {
        super(characteristicss, isRawCapture, isJpgCapture, activitiy, imageSaver, finish, rdyToSaveImg);
        imagesToSaveQueue = new ArrayBlockingQueue<>(15);

        imageSaveExecutor = new ThreadPoolExecutor(
                1,       // Initial pool size
                1,       // Max pool size
                KEEP_ALIVE_TIME,
                TimeUnit.MILLISECONDS,
                imagesToSaveQueue);
        //handel case that queue is full, and wait till its free
        imageSaveExecutor.setRejectedExecutionHandler((r, executor) -> {
            Log.d(TAG, "imageSave Queue full");
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        rawStack =new RawStack();
        stackCoutn = 0;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Log.d(TAG, "OnRawAvailible waiting: " + imageSaveExecutor.getActiveCount());
        final Image image = reader.acquireLatestImage();
        if ( image == null)
            return;
        if (image.getFormat() != ImageFormat.RAW_SENSOR)
            image.close();
        else {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            final byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            final int w = image.getWidth();
            final int h = image.getHeight();
            image.close();
            buffer.clear();
            rdyToSaveImg.onRdyToSaveImg(RawStackCaptureHolder.this);
            imageSaveExecutor.execute(() -> {

                        Log.d(TAG, "stackframes");
                        if (stackCoutn == 0) {
                            rawStack.setFirstFrame(bytes, w, h);
                        } else
                            rawStack.stackNextFrame(bytes);
                        stackCoutn++;
                        Log.d(TAG, "stackframes done");
                synchronized (RawStackCaptureHolder.this) {
                        RawStackCaptureHolder.this.notify();
                    }

            });

        }



    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
        SetCaptureResult(result);
        if (dngProfile == null)
        {
            dngProfile = getDngProfile(DngProfile.Plain, width,height);
        }
    }

    public void writeDng(String fileout)
    {
        rawStack.saveDng(dngProfile, dngProfile.matrixes,fileout);
        rawStack = null;
        imageSaveExecutor.shutdown();
    }
}
