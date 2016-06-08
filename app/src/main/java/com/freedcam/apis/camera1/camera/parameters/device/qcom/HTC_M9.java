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

import com.freedcam.apis.basecamera.camera.parameters.modes.MatrixChooserParameter;
import com.freedcam.apis.camera1.camera.CameraUiWrapper;
import com.troop.androiddng.DngProfile;

/**
 * Created by troop on 01.06.2016.
 */
public class HTC_M9 extends HTC_M8 {
    public HTC_M9(Parameters parameters, CameraUiWrapper cameraUiWrapper) {
        super(parameters, cameraUiWrapper);
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
            case 25677824://m9 mipi
                return new DngProfile(64, 5388, 3752, DngProfile.Mipi16, DngProfile.GRBG, 0,matrixChooserParameter.GetCustomMatrix(MatrixChooserParameter.NEXUS6));
            case 27127808: //HTC M9 QCom
                return new DngProfile(64, 5388, 3752, DngProfile.Qcom, DngProfile.GRBG, 0,matrixChooserParameter.GetCustomMatrix(MatrixChooserParameter.NEXUS6));
        }
        return null;
    }
}
