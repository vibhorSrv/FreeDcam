package com.freedcam.apis.sonyremote.sonyapi.parameters.modes;

import android.os.Handler;

import com.freedcam.apis.sonyremote.sonyapi.sonystuff.JsonUtils;
import com.freedcam.apis.sonyremote.sonyapi.sonystuff.SimpleRemoteApi;
import com.freedcam.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by troop on 26.01.2015.
 */
public class ContShootModeParameterSony extends BaseModeParameterSony
{
    final String TAG = ContShootModeParameterSony.class.getSimpleName();
    public ContShootModeParameterSony(Handler handler, String VALUE_TO_SET, String VALUES_TO_GET, SimpleRemoteApi mRemoteApi) {
        super(handler, "getContShootingMode", "setContShootingMode", "getAvailableContShootingMode", mRemoteApi);
    }

    protected String[] processValuesToReturn() {
        String[] ret = null;
        try {
            JSONArray array = jsonObject.getJSONArray("result");
            JSONObject ob = array.optJSONObject(0);
            JSONArray subarray = ob.getJSONArray("candidate");
            ret = JsonUtils.ConvertJSONArrayToStringArray(subarray);
        } catch (JSONException e) {
            Logger.exception(e);
        }
        return ret;
    }

    protected void processValuesToSet(String valueToSet)
    {
        try
        {
            try {
                JSONObject contshot = new JSONObject().put("contShootingMode", valueToSet);
                JSONArray array = new JSONArray().put(0, contshot);
                JSONObject jsonObject = mRemoteApi.setParameterToCamera(VALUE_TO_SET, array);
            } catch (JSONException e) {
                Logger.exception(e);
            }


        } catch (IOException e) {
            Logger.exception(e);
        }
    }

    protected String processGetString() {
        JSONArray array = null;
        String ret ="";
        try {
            array = jsonObject.getJSONArray("result");
            ret = array.getJSONObject(0).getString("contShootingMode");
        } catch (JSONException e) {
            Logger.exception(e);
        }
        return ret;
    }
}