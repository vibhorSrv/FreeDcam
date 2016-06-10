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

package com.freedcam.apis.camera1.parameters.manual.mtk;

import android.content.Context;
import android.hardware.Camera.Parameters;

import com.freedcam.apis.KEYS;
import com.freedcam.apis.camera1.CameraHolder;
import com.freedcam.apis.camera1.parameters.ParametersHandler;
import com.freedcam.apis.camera1.parameters.manual.mtk.AE_Handler_MTK.AeManual;
import com.freedcam.apis.camera1.parameters.manual.mtk.AE_Handler_MTK.AeManualEvent;
import com.freedcam.apis.camera1.parameters.manual.BaseManualParameter;

import java.util.ArrayList;

/**
 * Created by GeorgeKiarie on 20/04/2016.
 */
public class ISOManualParameterMTK extends BaseManualParameter
{
    private CameraHolder cameraHolder;
    private AeManualEvent manualEvent;

    public ISOManualParameterMTK(Context context,Parameters parameters, CameraHolder cameraHolder, ParametersHandler parametersHandler, AeManualEvent manualevent, int maxiso) {
        super(context, parameters, "", "", "", parametersHandler,1);

        this.cameraHolder = cameraHolder;

        isSupported = true;
        isVisible = isSupported;
        ArrayList<String> s = new ArrayList<>();
        s.add(KEYS.AUTO);
        for (int i =100; i <= maxiso; i +=100)
        {
            s.add(i + "");
        }
        stringvalues = new String[s.size()];
        s.toArray(stringvalues);
        manualEvent = manualevent;
    }

    @Override
    public boolean IsSupported() {
        return super.IsSupported();
    }

    @Override
    public boolean IsVisible() {
        return super.IsSupported();
    }

    @Override
    public int GetValue() {
        return  currentInt;
    }

    @Override
    public void SetValue(int valueToSet)
    {
        currentInt = valueToSet;
        if (valueToSet == 0)
        {
            manualEvent.onManualChanged(AeManual.iso, true, valueToSet);
        }
        else
        {
            manualEvent.onManualChanged(AeManual.iso, false,valueToSet);
        }
    }

    public void setValue(int value)
    {

        if (value == 0)
        {
            parameters.set("m-sr-g", "0");
        }
        else
        {
            currentInt = value;
            //cap-isp-g= 1024 == iso100? cause cap-sr-g=7808 / 1024 *100 = 762,5 same with 256 = 3050
            parameters.set("m-sr-g", String.valueOf(Integer.valueOf( stringvalues[value])/100 *1024));
        }
        ThrowCurrentValueStringCHanged(stringvalues[value]);
    }

    @Override
    public String GetStringValue() {
        try {
            return stringvalues[currentInt];
        } catch (NullPointerException ex) {
            return KEYS.AUTO;
        }
    }

    @Override
    public String[] getStringValues() {
        return stringvalues;
    }
}