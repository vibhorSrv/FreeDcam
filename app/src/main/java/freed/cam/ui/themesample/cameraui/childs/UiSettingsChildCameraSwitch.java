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

package freed.cam.ui.themesample.cameraui.childs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import freed.ActivityInterface;
import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.sonyremote.SonyCameraRemoteFragment;
import freed.settings.SettingsManager;

/**
 * Created by troop on 13.06.2015.
 */
public class UiSettingsChildCameraSwitch extends UiSettingsChild
{
    private CameraWrapperInterface cameraUiWrapper;
    private int currentCamera;
    public UiSettingsChildCameraSwitch(Context context) {
        super(context);
    }

    public UiSettingsChildCameraSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*@Override
    protected void init(Context context) {
        super.init(context);

        //setOnClickListener(v -> switchCamera());
    }*/

    @Override
    public void SetStuff(ActivityInterface fragment_activityInterface, String settingvalue) {
        super.SetStuff(fragment_activityInterface, settingvalue);

        currentCamera = SettingsManager.getInstance().GetCurrentCamera();
        valueText.setText(getCamera(currentCamera));
    }

    @Override
    public void onClick(View v) {
        if (onItemClick != null)
            onItemClick.onSettingsChildClick(this, fromleft);
    }

    public void SetCameraUiWrapper(CameraWrapperInterface cameraUiWrapper)
    {
        this.cameraUiWrapper = cameraUiWrapper;
        if (cameraUiWrapper instanceof SonyCameraRemoteFragment)
        {
            setVisibility(View.GONE);
        }
        else {
            setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void SetValue(String value)
    {
        String[] split = value.split(" ");
        currentCamera = Integer.parseInt(split[1]);
        SettingsManager.getInstance().SetCurrentCamera(currentCamera);
        cameraUiWrapper.restartCameraAsync();
        valueText.setText(getCamera(currentCamera));
    }

    public void switchCameraLens()
    {
        int TempID = 0;

        if(currentCamera == 0)
            TempID = 2;
        else if(currentCamera == 1)
            TempID = 3;
        else if(currentCamera == 2)
            TempID = 0;
        else if(currentCamera == 3)
            TempID =1;

        SettingsManager.getInstance().SetCurrentCamera(TempID);
        sendLog("Stop Preview and Camera");
        cameraUiWrapper.restartCameraAsync();
        //valueText.setText(getCamera(TempID));
    }

    private void switchCamera()
    {
        int maxcams = SettingsManager.getInstance().getCameraIds().length;
        if (currentCamera++ >= maxcams - 1)
            currentCamera = 0;

        SettingsManager.getInstance().SetCurrentCamera(currentCamera);
        sendLog("Stop Preview and Camera");
        cameraUiWrapper.restartCameraAsync();
        valueText.setText(getCamera(currentCamera));
    }

    private String getCamera(int i)
    {
        if (SettingsManager.getInstance().getIsFrontCamera())
            return "Front " + i;
        else if (i == 2)
            return "BackW";
        else if (i == 3)
            return "FrontW";
        else
            return "Back " + i;
    }

    @Override
    public String[] GetValues() {
        int[] camids = SettingsManager.getInstance().getCameraIds();
        String[] retarr = new String[camids.length];
        for (int i = 0; i < camids.length; i++)
        {
            if (SettingsManager.getInstance().getCamIsFrontCamera(i))
                retarr[i] = "Front "+i;
            else
                retarr[i] = "Back "+i;
        }
        return retarr;
    }


}
