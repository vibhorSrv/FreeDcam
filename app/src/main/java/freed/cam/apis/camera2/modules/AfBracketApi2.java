/*
 *
 *     Copyright (C) 2015 Ingo Fuchs
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * /
 */

package freed.cam.apis.camera2.modules;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import freed.cam.apis.KEYS;
import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.modules.ModuleHandlerAbstract;
import freed.cam.apis.camera2.parameters.modes.FocusModeApi2;

/**
 * Created by troop on 18.08.2016.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AfBracketApi2 extends PictureModuleApi2
{
    public AfBracketApi2(CameraWrapperInterface cameraUiWrapper) {
        super(cameraUiWrapper);
        name = KEYS.MODULE_AFBRACKET;
    }

    private int focusStep;
    private int currentFocusPos;
    private int focuslength;

    @Override
    public String ShortName() {
        return "AfBracket";
    }

    @Override
    public String LongName() {
        return "Af-Bracket";
    }

    @Override
    public void InitModule() {
        cameraUiWrapper.GetParameterHandler().Burst.SetValue(6);
        focuslength = parameterHandler.ManualFocus.getStringValues().length;
        focusStep =  focuslength/7;
        currentFocusPos = 0;
        super.InitModule();
    }


    @Override
    protected void initBurstCapture(Builder captureBuilder, CaptureCallback captureCallback)
    {
        currentFocusPos = 0;
        List<CaptureRequest> captureList = new ArrayList<>();
        captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, FocusModeApi2.FocusModes.off.ordinal());
        for (int i = 0; i < parameterHandler.Burst.GetValue()+1; i++)
        {
            currentFocusPos +=focusStep;
            if (currentFocusPos > focuslength)
                currentFocusPos = focuslength;
            captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, (float) currentFocusPos / 10);
            captureList.add(captureBuilder.build());
        }
        cameraHolder.CaptureSessionH.StopRepeatingCaptureSession();
        changeCaptureState(ModuleHandlerAbstract.CaptureStates.image_capture_start);
        cameraHolder.CaptureSessionH.StartCaptureBurst(captureList, captureCallback);
    }
}
