package freed.cam.apis.camera2.modules;

import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.troop.freedcam.R;

import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.modules.ModuleHandlerAbstract;
import freed.cam.apis.basecamera.parameters.modes.ToneMapChooser;
import freed.settings.SettingKeys;
import freed.settings.SettingsManager;
import freed.utils.Log;

public class RawStackPipe extends PictureModuleApi2 {

    private final static String TAG = RawStackPipe.class.getSimpleName();

    private RawStackCaptureHolder rawStackCaptureHolder;
    public RawStackPipe(CameraWrapperInterface cameraUiWrapper, Handler mBackgroundHandler, Handler mainHandler) {
        super(cameraUiWrapper, mBackgroundHandler, mainHandler);
    }

    @Override
    protected void TakePicture() {
        rawStackCaptureHolder = new RawStackCaptureHolder(cameraHolder.characteristics, true, false, cameraUiWrapper.getActivityInterface(),this,this, this);
        rawStackCaptureHolder.setFilePath(getFileString(), SettingsManager.getInstance().GetWriteExternal());
        rawStackCaptureHolder.setForceRawToDng(SettingsManager.get(SettingKeys.forceRawToDng).get());
        rawStackCaptureHolder.setToneMapProfile(((ToneMapChooser)cameraUiWrapper.getParameterHandler().get(SettingKeys.TONEMAP_SET)).getToneMap());
        rawStackCaptureHolder.setSupport12bitRaw(SettingsManager.get(SettingKeys.support12bitRaw).get());
        String cmat = SettingsManager.get(SettingKeys.MATRIX_SET).get();
        if (cmat != null && !TextUtils.isEmpty(cmat) &&!cmat.equals("off")) {
            rawStackCaptureHolder.setCustomMatrix(SettingsManager.getInstance().getMatrixesMap().get(cmat));
        }
        if (cameraUiWrapper.getParameterHandler().get(SettingKeys.LOCATION_MODE).GetStringValue().equals(SettingsManager.getInstance().getResString(R.string.on_)))
        {
            rawStackCaptureHolder.setLocation(cameraUiWrapper.getActivityInterface().getLocationManager().getCurrentLocation());
        }
        super.TakePicture();
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void captureStillPicture() {

        Log.d(TAG,"########### captureStillPicture ###########");

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

        cameraUiWrapper.captureSessionHandler.StopRepeatingCaptureSession();
        prepareCaptureBuilder(imagecount);
        changeCaptureState(ModuleHandlerAbstract.CaptureStates.image_capture_start);
        Log.d(TAG, "StartStillCapture");
        cameraUiWrapper.captureSessionHandler.StartImageCapture(rawStackCaptureHolder, mBackgroundHandler);
    }
}
