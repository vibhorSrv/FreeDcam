package freed.cam.apis.camera2.modules;

import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;

import com.troop.freedcam.R;

import java.io.File;
import java.io.IOException;

import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.modules.ModuleHandlerAbstract;
import freed.cam.apis.basecamera.parameters.AbstractParameter;
import freed.cam.apis.camera2.parameters.ae.AeManagerCamera2;
import freed.cam.ui.themesample.handler.UserMessageHandler;
import freed.dng.DngProfile;
import freed.jni.ExifInfo;
import freed.jni.RawStack;
import freed.jni.RawToDng;
import freed.settings.SettingKeys;
import freed.settings.SettingsManager;
import freed.utils.Log;
import freed.utils.StorageFileManager;
import freed.utils.StringUtils;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class RawStackPipe2 extends PictureModuleApi2 {

    private final String tmpFolder;
    private final String TAG = RawStackPipe2.class.getSimpleName();

    public RawStackPipe2(CameraWrapperInterface cameraUiWrapper, Handler mBackgroundHandler, Handler mainHandler) {
        super(cameraUiWrapper, mBackgroundHandler, mainHandler);
        name = cameraUiWrapper.getResString(R.string.module_stacking2);
        tmpFolder = cameraUiWrapper.getActivityInterface().getStorageHandler().getFreedcamFolder()+"/tmp/";
        File tmp = new File(tmpFolder);
        if (!tmp.exists())
            tmp.mkdirs();
    }

    @Override
    public String LongName() {
        return "HDR+2";
    }

    @Override
    public String ShortName() {
        return "HDR+2";
    }

    @Override
    public void InitModule() {

        SettingsManager.get(SettingKeys.lastPictureFormat).set(SettingsManager.get(SettingKeys.PictureFormat).get());
        SettingsManager.get(SettingKeys.PictureFormat).set(SettingsManager.getInstance().getResString(R.string.pictureformat_bayer));

        super.InitModule();
        cameraUiWrapper.parametersHandler.get(SettingKeys.PictureFormat).setViewState(AbstractParameter.ViewState.Hidden);
        cameraUiWrapper.parametersHandler.get(SettingKeys.M_Burst).SetValue(14,true);
    }

    @Override
    public void DestroyModule() {
        cameraUiWrapper.parametersHandler.get(SettingKeys.M_Burst).SetValue(0,true);
        cameraUiWrapper.parametersHandler.get(SettingKeys.PictureFormat).setViewState(AbstractParameter.ViewState.Visible);
        SettingsManager.get(SettingKeys.PictureFormat).set(SettingsManager.get(SettingKeys.lastPictureFormat).get());
        super.DestroyModule();
    }

    protected String getFileString()
    {
        return tmpFolder +"burst"+BurstCounter.getImageCaptured();
    }

    /**
     * Reset the capturesession to preview
     *
     */


    @Override
    public void internalFireOnWorkDone(File file)
    {
        Log.d(TAG, "internalFireOnWorkDone"  + " burstCount/imagecount:" + BurstCounter.getBurstCount() + "/" +BurstCounter.getImageCaptured());
        if (workFinishEventsListner != null)
            workFinishEventsListner.internalFireOnWorkDone(file);
        else {
            Log.d(TAG, "internalFireOnWorkDone BurstCount:" + BurstCounter.getBurstCount() + " imageCount:" + BurstCounter.getImageCaptured());
            if (BurstCounter.getBurstCount() >= BurstCounter.getImageCaptured()) {
                filesSaved.add(file);
                Log.d(TAG, "internalFireOnWorkDone Burst addFile");
            }
            if (BurstCounter.getBurstCount() == BurstCounter.getImageCaptured()) {
                Log.d(TAG, "internalFireOnWorkDone Burst done");
                try {
                    byte[] input = RawToDng.readFile(filesSaved.get(0));
                    if (input != null)
                        Log.d(TAG, "input size: " + input.length);
                    DngProfile dngProfile = currentCaptureHolder.getDngProfile(0);
                    RawStack rawStack = new RawStack();
                    rawStack.setFirstFrame(input, dngProfile.getWidth(),dngProfile.getHeight());

                    for (int i = 1; i< filesSaved.size(); i++)
                    {
                        byte[] nextinput = RawToDng.readFile(filesSaved.get(i));
                        rawStack.stackNextFrame(nextinput);
                        UserMessageHandler.sendMSG("Stacked:" +i,false);
                    }


                    ExifInfo exifInfo = currentCaptureHolder.getExifInfo();
                    String fileout = cameraUiWrapper.getActivityInterface().getStorageHandler().getNewFilePath(SettingsManager.getInstance().GetWriteExternal(),"")+".dng";
                    rawStack.saveDng(dngProfile, dngProfile.matrixes, fileout, exifInfo);
                    fireOnWorkFinish(new File(fileout));
                    for (File filetodel : filesSaved)
                    {
                        filetodel.delete();
                    }
                    filesSaved.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
