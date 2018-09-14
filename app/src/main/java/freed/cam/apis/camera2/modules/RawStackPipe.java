package freed.cam.apis.camera2.modules;

import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.troop.freedcam.R;

import java.io.File;

import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.modules.ModuleHandlerAbstract;
import freed.cam.apis.basecamera.parameters.AbstractParameter;
import freed.cam.apis.basecamera.parameters.modes.ToneMapChooser;
import freed.cam.apis.camera2.modules.helper.ImageCaptureHolder;
import freed.cam.apis.camera2.modules.helper.RawStackCaptureHolder;
import freed.settings.SettingKeys;
import freed.settings.SettingsManager;
import freed.utils.Log;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class RawStackPipe extends PictureModuleApi2 {

    private final static String TAG = RawStackPipe.class.getSimpleName();

    private RawStackCaptureHolder rawStackCaptureHolder;
    public RawStackPipe(CameraWrapperInterface cameraUiWrapper, Handler mBackgroundHandler, Handler mainHandler) {
        super(cameraUiWrapper, mBackgroundHandler, mainHandler);
        name = cameraUiWrapper.getResString(R.string.module_stacking);
    }

    @Override
    public String LongName() {
        return "HDR+";
    }

    @Override
    public String ShortName() {
        return "HDR+";
    }

    @Override
    public void DoWork() {
        if(!isWorking)
            mBackgroundHandler.post(()->TakePicture());
    }


    @Override
    protected void TakePicture() {
        rawStackCaptureHolder = new RawStackCaptureHolder(cameraHolder.characteristics, true, false, cameraUiWrapper.getActivityInterface(),this,this, this);
        rawStackCaptureHolder.setFilePath(getFileString(), SettingsManager.getInstance().GetWriteExternal());
        rawStackCaptureHolder.setForceRawToDng(SettingsManager.get(SettingKeys.forceRawToDng).get());
        rawStackCaptureHolder.setToneMapProfile(((ToneMapChooser)cameraUiWrapper.getParameterHandler().get(SettingKeys.TONEMAP_SET)).getToneMap());
        rawStackCaptureHolder.setSupport12bitRaw(SettingsManager.get(SettingKeys.support12bitRaw).get());
        rawStackCaptureHolder.setWidth(output.raw_width);
        rawStackCaptureHolder.setHeight(output.raw_height);
        String cmat = SettingsManager.get(SettingKeys.MATRIX_SET).get();
        if (cmat != null && !TextUtils.isEmpty(cmat) &&!cmat.equals("off")) {
            rawStackCaptureHolder.setCustomMatrix(SettingsManager.getInstance().getMatrixesMap().get(cmat));
        }
        if (cameraUiWrapper.getParameterHandler().get(SettingKeys.LOCATION_MODE).GetStringValue().equals(SettingsManager.getInstance().getResString(R.string.on_)))
        {
            cameraUiWrapper.captureSessionHandler.SetParameter(CaptureRequest.JPEG_GPS_LOCATION,cameraUiWrapper.getActivityInterface().getLocationManager().getCurrentLocation());
        }

        if (jpegReader != null)
            jpegReader.setOnImageAvailableListener(rawStackCaptureHolder,mBackgroundHandler);
        if (rawReader != null)
        {
            rawReader.setOnImageAvailableListener(rawStackCaptureHolder,mBackgroundHandler);
        }
        super.TakePicture();
    }

    @Override
    public void InitModule() {

        SettingsManager.get(SettingKeys.lastPictureFormat).set(SettingsManager.get(SettingKeys.PictureFormat).get());
        SettingsManager.get(SettingKeys.PictureFormat).set(SettingsManager.getInstance().getResString(R.string.pictureformat_dng16));

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

    /**
     * Capture a still picture. This method should be called when we get a response in
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void captureStillPicture() {

        Log.d(TAG,"########### captureStillPicture ###########");


        
        prepareCaptureBuilder(BurstCounter.getImageCaptured());
        changeCaptureState(ModuleHandlerAbstract.CaptureStates.image_capture_start);
        Log.d(TAG, "StartStillCapture");
        cameraUiWrapper.captureSessionHandler.StartImageCapture(rawStackCaptureHolder, mBackgroundHandler);
    }

    @Override
    public void onRdyToSaveImg(ImageCaptureHolder holder) {
        //holder.getRunner().run();

        Log.d(TAG,"onRdyToSaveImg " + BurstCounter.getBurstCount() +"/" +BurstCounter.getImageCaptured() + "/stack " +rawStackCaptureHolder.getStackCoutn());
        if (BurstCounter.getBurstCount()-1 == BurstCounter.getImageCaptured()) {
            String file = getFileString() + ".dng";
            synchronized (RawStackCaptureHolder.class) {
                while (BurstCounter.getBurstCount()-1 != rawStackCaptureHolder.getStackCoutn()) {
                    try {
                        RawStackCaptureHolder.class.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                rawStackCaptureHolder.writeDng(file);
            }
            fireOnWorkFinish(new File(file));
            //fireOnWorkFinish(new File(file.replace("dng","ppm")));
        }
        finishCapture();
    }
}
