package freed.cam.apis.camera2.modules.helper;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.util.Size;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import freed.ActivityInterface;
import freed.cam.apis.basecamera.modules.ModuleInterface;
import freed.cam.apis.basecamera.modules.WorkFinishEvents;
import freed.dng.DngProfile;
import freed.jni.ExifInfo;
import freed.jni.RawStack;
import freed.settings.SettingKeys;
import freed.settings.SettingsManager;
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
    private int width;
    private int height;
    private long rawsize;


    public RawStackCaptureHolder(CameraCharacteristics characteristicss, boolean isRawCapture, boolean isJpgCapture, ActivityInterface activitiy, ModuleInterface imageSaver, WorkFinishEvents finish, RdyToSaveImg rdyToSaveImg) {
        super(characteristicss, isRawCapture, isJpgCapture, activitiy, imageSaver, finish, rdyToSaveImg);
        imagesToSaveQueue = new ArrayBlockingQueue<>(4);

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
        synchronized (RawStackCaptureHolder.class)
        {
            while (imagesToSaveQueue.remainingCapacity() == 1) {
                try {
                    RawStackCaptureHolder.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        final Image image = reader.acquireLatestImage();
        if (image == null)
            return;
        if (image.getFormat() != ImageFormat.RAW_SENSOR)
            image.close();
        else {
            /*ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            final byte[] bytes = new byte[buffer.remaining()];
            rawsize = bytes.length;
            buffer.get(bytes);*/
            Log.d(TAG, "add image to Queue left:" + imagesToSaveQueue.remainingCapacity());
            rdyToSaveImg.onRdyToSaveImg(RawStackCaptureHolder.this);
            imageSaveExecutor.execute(new StackRunner(image));
        }

    }

    private class StackRunner implements Runnable
    {
        private final  Image image;
        public StackRunner(Image image)
        {
            this.image  = image;
        }

        @Override
        public void run() {

            final ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            final int w = image.getWidth();
            final int h = image.getHeight();
            rawsize = buffer.remaining();
            Log.d(TAG, "stackframes");
            if (stackCoutn == 0) {
                rawStack.setFirstFrame(buffer, w, h);
            } else
                rawStack.stackNextFrame(buffer);
            stackCoutn++;
            image.close();
            buffer.clear();
            Log.d(TAG, "stackframes done");
            synchronized (RawStackCaptureHolder.class) {
                RawStackCaptureHolder.class.notify();
            }
        }
    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
        SetCaptureResult(result);

    }

    public void writeDng(String fileout)
    {
        if (SettingsManager.get(SettingKeys.forceRawToDng).get()) {
            DngProfile dngProfile;
            if (SettingsManager.get(SettingKeys.useCustomMatrixOnCamera2).get() && SettingsManager.getInstance().getDngProfilesMap().get(rawsize) != null)
                dngProfile = SettingsManager.getInstance().getDngProfilesMap().get(rawsize);
            else
                dngProfile = getDngProfile(DngProfile.Plain, width, height, true);
            rawStack.saveDng(dngProfile, dngProfile.matrixes, fileout, getExifInfo());
            rawStack = null;
            imageSaveExecutor.shutdown();
        }
        else
        {
            DngCreator dngCreator = new DngCreator(characteristics, captureResult);
            try {
                dngCreator.setOrientation(orientation);
            }
            catch (IllegalArgumentException ex)
            {
                Log.WriteEx(ex);
            }

            //if (location != null)
            //    dngCreator.setLocation(location);
            try {
                final Size outSize = new Size(width,height);

                final ByteBuffer outbuffer = ByteBuffer.wrap(rawStack.getOutputBuffer());
                int sss = outbuffer.remaining();
                dngCreator.writeByteBuffer(new FileOutputStream(fileout),outSize,outbuffer,0);
                outbuffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
            dngCreator.close();
            rawStack = null;
            imageSaveExecutor.shutdown();
        }
    }

    private ExifInfo getExifInfo()
    {
        float fnum, focal = 0;
        int iso;
        float exposureTime;
        float expoindex;
        try {
            focal = (captureResult.get(CaptureResult.LENS_FOCAL_LENGTH));
        } catch (NullPointerException e) {
            Log.WriteEx(e);
        }
        try {
            fnum =(captureResult.get(CaptureResult.LENS_APERTURE));
        } catch (NullPointerException e) {
            Log.WriteEx(e);
            fnum = 1.2f;
        }
        try {
            iso = (captureResult.get(CaptureResult.SENSOR_SENSITIVITY));
        } catch (NullPointerException e) {
            Log.WriteEx(e);
            iso =(100);
        }
        try {
            double mExposuretime = captureResult.get(CaptureResult.SENSOR_EXPOSURE_TIME).doubleValue() / 1000000000;
            exposureTime = ((float) mExposuretime);
        } catch (NullPointerException e) {
            Log.WriteEx(e);
            exposureTime = 0;
        }
        try {
             expoindex = (captureResult.get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION) * characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP).floatValue());
        } catch (NullPointerException e) {
            Log.WriteEx(e);
            expoindex = 0;
        }
        return new ExifInfo(iso,0,exposureTime,focal,fnum,expoindex,"",orientation+"");
    }
}
