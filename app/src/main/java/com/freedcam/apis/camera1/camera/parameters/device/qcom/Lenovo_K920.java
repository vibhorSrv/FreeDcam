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

package com.freedcam.apis.camera1.camera.parameters.device.qcom;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;

import com.freedcam.apis.KEYS;
import com.freedcam.apis.basecamera.camera.parameters.manual.AbstractManualParameter;
import com.freedcam.apis.basecamera.camera.parameters.modes.MatrixChooserParameter;
import com.freedcam.apis.camera1.camera.CameraUiWrapper;
import com.freedcam.apis.camera1.camera.parameters.device.BaseQcomDevice;
import com.freedcam.apis.camera1.camera.parameters.manual.BaseFocusManual;
import com.troop.androiddng.DngProfile;

/**
 * Created by troop on 01.06.2016.
 */
public class Lenovo_K920 extends BaseQcomDevice {
    public Lenovo_K920(Parameters parameters, CameraUiWrapper cameraUiWrapper) {
        super(parameters, cameraUiWrapper);
    }

    @Override
    public AbstractManualParameter getManualFocusParameter() {
        return new BaseFocusManual(parameters, KEYS.KEY_MANUAL_FOCUS_POSITION,KEYS.MAX_FOCUS_POS_INDEX, KEYS.MIN_FOCUS_POS_INDEX,KEYS.KEY_FOCUS_MODE_MANUAL, parametersHandler,1,1);
    }
    @Override
    public boolean IsDngSupported() {
        return true;
    }
    @Override
    public DngProfile getDngProfile(int filesize)
    {
        switch (filesize)
        {
            case 19992576:  //lenovo k920
                return new DngProfile(64, 5328,3000, DngProfile.Mipi, DngProfile.GBRG, 0,matrixChooserParameter.GetCustomMatrix(MatrixChooserParameter.NEXUS6));
        }
        return null;
    }
}
