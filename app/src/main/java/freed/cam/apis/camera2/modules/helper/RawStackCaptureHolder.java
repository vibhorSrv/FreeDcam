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
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import freed.ActivityInterface;
import freed.cam.apis.basecamera.modules.ModuleInterface;
import freed.cam.apis.basecamera.modules.WorkFinishEvents;
import freed.cam.ui.themesample.handler.UserMessageHandler;
import freed.dng.DngProfile;
import freed.jni.ExifInfo;
import freed.jni.RawStack;
import freed.settings.SettingKeys;
import freed.settings.SettingsManager;
import freed.utils.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RawStackCaptureHolder extends ImageCaptureHolder {

    private final static String TAG = RawStackCaptureHolder.class.getSimpleName();
    private final BlockingQueue<Image> imageBlockingQueue;
    private StackRunner stackRunner;
    private RawStack rawStack;
    public int getStackCoutn() {
        return stackCoutn;
    }
    private int stackCoutn = 0;
    private int width;
    private int height;
    private long rawsize;
    private int upshift = 0;


    public RawStackCaptureHolder(CameraCharacteristics characteristicss, CaptureType captureType, ActivityInterface activitiy, ModuleInterface imageSaver, WorkFinishEvents finish, RdyToSaveImg rdyToSaveImg) {
        super(characteristicss, captureType, activitiy, imageSaver, finish, rdyToSaveImg);
        imageBlockingQueue = new LinkedBlockingQueue<>(4);

        rawStack =new RawStack();
        stackCoutn = 0;
        stackRunner = new StackRunner();
        new Thread(stackRunner).start();
        //use freedcam dng converter
        if (SettingsManager.get(SettingKeys.forceRawToDng).get())
        {
            //if input data is 12bit only scale it by 2, else it would clip high/lights = 14bit
            if (SettingsManager.get(SettingKeys.support12bitRaw).get())
                upshift = 2;
            else //shift 10bit input up to 14bit
                upshift = 4;
        }
        else //use stock dngcreator, dont scale it up till we found a way to manipulate black and whitelvl from the capture result
            upshift = 0;

        rawStack.setShift(upshift);
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
        Log.d(TAG, "OnRawAvailible waiting: ");

        final Image image = reader.acquireLatestImage();
        if (image == null)
            return;
        if (image.getFormat() != ImageFormat.RAW_SENSOR)
            image.close();
        else {
            Log.d(TAG, "add image to Queue left:" + imageBlockingQueue.remainingCapacity());
            try {
                imageBlockingQueue.put(image);
            } catch (InterruptedException e) {
                Log.WriteEx(e);
            }
            rdyToSaveImg.onRdyToSaveImg(RawStackCaptureHolder.this);
        }

    }

    private class StackRunner implements Runnable
    {
        //private final  Image image;
        boolean run = false;
        public StackRunner()
        {
            run = true;
        }

        public void stop(){ run = false; }

        @Override
        public void run() {

            Image image = null;
            while (run) {
                try {
                    image = imageBlockingQueue.take();
                } catch (InterruptedException e) {
                    Log.WriteEx(e);
                }
                final ByteBuffer buffer = image.getPlanes()[0].getBuffer();
               /* final int w = image.getWidth();
                final int h = image.getHeight();*/

                Log.d(TAG, "stackframes");
                if (stackCoutn == 0) {
                    rawStack.setFirstFrame(buffer, width, height);
                    rawsize = buffer.remaining();
                } else
                    rawStack.stackNextFrame(buffer);

                image.close();
                buffer.clear();
                stackCoutn++;
                UserMessageHandler.sendMSG("Stacked:" +stackCoutn,false);
                Log.d(TAG, "stackframes done " + stackCoutn);
                synchronized (RawStackCaptureHolder.class) {
                    RawStackCaptureHolder.class.notify();
                }
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
            if (SettingsManager.get(SettingKeys.useCustomMatrixOnCamera2).get() && SettingsManager.getInstance().getDngProfilesMap().get(rawsize) != null) {
                dngProfile = SettingsManager.getInstance().getDngProfilesMap().get(rawsize);
                if (upshift > 0)
                {
                    dngProfile.setBlackLevel(dngProfile.getBlacklvl() << upshift);
                    dngProfile.setWhiteLevel(dngProfile.getWhitelvl() << upshift);
                }
            }
            else
                dngProfile = getDngProfile(DngProfile.Plain, width, height, upshift);

            ExifInfo exifInfo = getExifInfo();
            //String jpegout = fileout.replace("dng", "ppm");
            //rawStack.savePNG(dngProfile,dngProfile.matrixes,jpegout,exifInfo);
            rawStack.saveDng(dngProfile, dngProfile.matrixes, fileout, exifInfo);
            rawStack = null;
            stackRunner.stop();
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
                dngCreator.writeByteBuffer(new FileOutputStream(fileout),outSize,outbuffer,0);
                outbuffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
            dngCreator.close();
            rawStack = null;
            stackRunner.stop();
        }
    }
}
