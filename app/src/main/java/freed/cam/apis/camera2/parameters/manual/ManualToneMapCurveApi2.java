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

package freed.cam.apis.camera2.parameters.manual;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.TonemapCurve;
import android.os.Build.VERSION_CODES;

import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.parameters.AbstractParameter;
import freed.cam.apis.basecamera.parameters.ParameterEvents;
import freed.cam.apis.camera2.CameraHolderApi2;
import freed.utils.Log;

/**
 * Created by troop on 05.05.2015.
 */
//http://www.cambridgeincolour.com/tutorials/photoshop-curves.htm
@TargetApi(VERSION_CODES.LOLLIPOP)
public class ManualToneMapCurveApi2 implements ParameterEvents
{
    final String TAG = ManualToneMapCurveApi2.class.getSimpleName();
    //  linearcurve       x/y
    private final float[] blackpoint = { 0f,0f};
    private final float[] shadows = {0.25f,0.25f};
    private final float[] midtones = {0.5f,0.5f};
    private final float[] highlights = { 0.75f,0.75f};
    private final float[] whitepoint = {1.0f,1.0f};
    public  Contrast contrast;
    public  Brightness brightness;
    public  ColorParameter black;
    public  ColorParameter shadowsp;
    public  ColorParameter midtonesp;
    public  ColorParameter highlightsp;
    public  ColorParameter whitep;



    public ManualToneMapCurveApi2(CameraWrapperInterface cameraUiWrapper)
    {
        /*contrast = new Contrast(cameraUiWrapper);
        brightness = new Brightness(cameraUiWrapper);*/
        black = new ColorParameter(cameraUiWrapper,blackpoint,0);
        shadowsp = new ColorParameter(cameraUiWrapper,shadows,25);
        midtonesp = new ColorParameter(cameraUiWrapper,midtones,50);
        highlightsp = new ColorParameter(cameraUiWrapper,highlights,75);
        whitep = new ColorParameter(cameraUiWrapper,whitepoint,100);
    }

    @Override
    public void onIsSupportedChanged(boolean value) {

    }

    @Override
    public void onIsSetSupportedChanged(boolean value) {

    }

    @Override
    public void onIntValueChanged(int current) {

    }

    @Override
    public void onValuesChanged(String[] values) {

    }

    @Override
    public void onStringValueChanged(String value) {
        boolean isSupported;
        boolean canSet;
        boolean visible;
        if (value.equals("CONTRAST_CURVE"))
        {
            canSet = true;
            isSupported = true;
            visible = true;
        }
        else {
            canSet = false;
            isSupported = false;
            visible = false;
        }
        if (contrast != null) {
            contrast.fireIsReadOnlyChanged(canSet);
            contrast.fireIsSupportedChanged(isSupported);
        }
        if (brightness != null) {
            brightness.fireIsSupportedChanged(isSupported);
            brightness.fireIsReadOnlyChanged(canSet);
        }
        black.fireIsSupportedChanged(isSupported);
        black.fireIsReadOnlyChanged(canSet);
        shadowsp.fireIsSupportedChanged(isSupported);
        shadowsp.fireIsReadOnlyChanged(canSet);
        midtonesp.fireIsSupportedChanged(isSupported);
        midtonesp.fireIsReadOnlyChanged(canSet);
        highlightsp.fireIsSupportedChanged(isSupported);
        highlightsp.fireIsReadOnlyChanged(canSet);
        whitep.fireIsSupportedChanged(isSupported);
        whitep.fireIsReadOnlyChanged(canSet);
    }

    public class Contrast extends AbstractParameter
    {
        boolean firststart = true;
        public Contrast(CameraWrapperInterface cameraUiWrapper) {
            super(cameraUiWrapper);
            stringvalues = createStringArray(0,100,1);
            currentInt = 50;
            isSupported = true;
            isVisible = true;
        }



        @Override
        public int GetValue() {
            return currentInt;
        }

        @Override
        public void SetValue(int valueToSet)
        {
            Log.d(TAG, "Contrast value to set:" + valueToSet);
            if (valueToSet == -1)
            {
                Log.d(TAG, "Current TonemapMode:" + cameraUiWrapper.getParameterHandler().ToneMapMode.GetValue());
                if (cameraUiWrapper.getParameterHandler().ToneMapMode.GetStringValue().equals("CONTRAST_CURVE"))
                {
                    cameraUiWrapper.getParameterHandler().ToneMapMode.SetValue("FAST", true);
                    Log.d(TAG, "Disabled Contrast Curve");
                }
            }
            else {
                Log.d(TAG, "Current TonemapMode:" + cameraUiWrapper.getParameterHandler().ToneMapMode.GetValue());
                if (!cameraUiWrapper.getParameterHandler().ToneMapMode.GetStringValue().equals("CONTRAST_CURVE") && !firststart)
                {
                    cameraUiWrapper.getParameterHandler().ToneMapMode.SetValue("CONTRAST_CURVE", true);
                    Log.d(TAG, "Enabled Contrast Curve");
                }
                valueToSet = valueToSet * 3;
                currentInt = valueToSet;

                float toset = 0;
                if (valueToSet > 150) {
                    toset = (valueToSet - 100) * 0.001f;
                    highlights[0] = 0.75f - toset;
                    highlights[1] = 0.75f + toset;
                    shadows[0] = 0.25f - toset;
                    shadows[1] = 0.25f + toset;
                }
                if (valueToSet == 150) {
                    highlights[0] = 0.75f;
                    highlights[1] = 0.75f;
                    shadows[0] = 0.25f;
                    shadows[1] = 0.25f;
                } else {
                    toset = (150 - valueToSet) * 0.001f;
                    highlights[0] = 0.75f + toset;
                    highlights[1] = 0.75f - toset;
                    shadows[0] = 0.25f + toset;
                    shadows[1] = 0.25f - toset;
                }

                Log.d(TAG, "toset:" + toset + " val:" + valueToSet + " hx:" + highlights[0] + " hy:" + highlights[1] + " sx:" + shadows[0] + " sy:" + shadows[1]);

                float[] tonemap = {blackpoint[0], blackpoint[1], shadows[0], shadows[1], midtones[0], midtones[1], highlights[0], highlights[1], whitepoint[0], whitepoint[1]};
                TonemapCurve tonemapCurve = new TonemapCurve(tonemap, tonemap, tonemap);
                ((CameraHolderApi2) cameraUiWrapper.getCameraHolder()).captureSessionHandler.SetParameterRepeating(CaptureRequest.TONEMAP_CURVE, tonemapCurve);
            }
            firststart = false;
        }

        @Override
        public void SetValue(String valueToSet, boolean setToCamera) {

        }

        @Override
        public boolean IsSupported() {
            return isSupported;
        }

        @Override
        public boolean IsVisible() {
            return IsSupported();
        }

        @Override
        public boolean IsSetSupported() {
            return true;
        }

        @Override
        public String GetStringValue() {
            return super.GetStringValue();
        }
    }

    public class Brightness extends AbstractParameter
    {

        public Brightness(CameraWrapperInterface cameraUiWrapper) {
            super(cameraUiWrapper);
            stringvalues = createStringArray(0,100,1);
            currentInt = 50;
            isSupported = true;
            isVisible = true;
        }

        @Override
        public int GetValue() {
            return currentInt /4;
        }

        @Override
        public void SetValue(int valueToSet)
        {
            valueToSet = valueToSet *4;
            currentInt = valueToSet;

            float toset = 0;
            if (currentInt > 200)
            {
                toset = (currentInt - 200) * 0.001f;
                midtones[0] = 0.5f - toset;
                midtones[1] = 0.5f + toset;

            }
            if (currentInt == 100)
            {
                midtones[0] = 0.5f;
                midtones[1] = 0.5f;
            }
            else
            {
                toset = (200 - currentInt) * 0.001f;
                midtones[0] = 0.5f + toset;
                midtones[1] = 0.5f - toset;
            }

            Log.d(TAG, "toset:" + toset + " val:" + valueToSet+ " x:" + midtones[0] + " y:"+ midtones[1]);

            float[]tonemap = {blackpoint[0], blackpoint[1], shadows[0], shadows[1], midtones[0], midtones[1], highlights[0], highlights[1], whitepoint[0], whitepoint[1]};
            TonemapCurve tonemapCurve = new TonemapCurve(tonemap,tonemap,tonemap);
            ((CameraHolderApi2) cameraUiWrapper.getCameraHolder()).captureSessionHandler.SetParameterRepeating(CaptureRequest.TONEMAP_CURVE, tonemapCurve);

        }

        @Override
        public void SetValue(String valueToSet, boolean setToCamera) {

        }

        @Override
        public boolean IsSupported()
        {
            return isSupported;
        }

        @Override
        public boolean IsVisible() {
            return IsSupported();
        }

        @Override
        public boolean IsSetSupported() {
            return true;
        }

        @Override
        public String GetStringValue() {
            return super.GetStringValue();
        }
    }

    public class ColorParameter extends AbstractParameter
    {
        float[] color;
        float currentfloat;
        float defaultvalue;
        public ColorParameter(CameraWrapperInterface cameraUiWrapper, float color[], float defaultvalue) {
            super(cameraUiWrapper);
            this.color = color;
            stringvalues = createStringArray(0,100,1);
            this.defaultvalue = defaultvalue;
            currentfloat = defaultvalue;
            currentInt = (int)defaultvalue;
            color[0] = defaultvalue/100;
            color[1] = defaultvalue/100;
            isSupported = true;
            isVisible = true;
        }

        @Override
        public int GetValue() {
            return currentInt;
        }

        @Override
        public void SetValue(int valueToSet)
        {
            currentInt = valueToSet;
            float toset = Float.parseFloat(stringvalues[valueToSet]) / 100;
            color[0] = defaultvalue;
            color[1] = toset;
            if (color[0] < 0)
                color[0] = 0;
            if (color[1] < 0)
                color[1] = 0;
            if (color[0] > 1)
                color[0] = 1;
            if (color[1] > 1)
                color[1] = 1;

            Log.d(TAG, "toset:" + toset + " val:" + valueToSet+ " x:" + color[0] + " y:"+ color[1]);

            float[]tonemap = {blackpoint[0], blackpoint[1], shadows[0], shadows[1], midtones[0], midtones[1], highlights[0], highlights[1], whitepoint[0], whitepoint[1]};
            TonemapCurve tonemapCurve = new TonemapCurve(tonemap,tonemap,tonemap);
            ((CameraHolderApi2) cameraUiWrapper.getCameraHolder()).captureSessionHandler.SetParameterRepeating(CaptureRequest.TONEMAP_CURVE, tonemapCurve);
            fireStringValueChanged(stringvalues[valueToSet]);
            fireIntValueChanged(valueToSet);

        }

        @Override
        public void SetValue(String valueToSet, boolean setToCamera) {

        }

        @Override
        public boolean IsSupported()
        {
            return isSupported;
        }

        @Override
        public boolean IsVisible() {
            return IsSupported();
        }

        @Override
        public boolean IsSetSupported() {
            return true;
        }

        @Override
        public String GetStringValue() {
            return stringvalues[currentInt]+"";
        }

        @Override
        public String[] getStringValues() {
            return stringvalues;
        }
    }
}
