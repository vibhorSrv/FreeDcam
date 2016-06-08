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

package com.troop.androiddng;

import com.freedcam.utils.Logger;
import com.freedcam.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by troop on 02.05.2016.
 */
public class CustomMatrix
{
    public float[] ColorMatrix1;
    public float[] ColorMatrix2;
    public float[] NeutralMatrix;
    public float[] ForwardMatrix1;
    public float[] ForwardMatrix2;
    public float[] ReductionMatrix1;
    public float[] ReductionMatrix2;
    public float[] NoiseReductionMatrix;


    public static final String MEDIAPROFILESPATH = StringUtils.GetFreeDcamConfigFolder+"matrix/";
    private static final String TAG = CustomMatrix.class.getSimpleName();

    private CustomMatrix(){}

    public CustomMatrix(float[]matrix1, float[] matrix2, float[]neutral,float[]fmatrix1, float[] fmatrix2,float[]rmatrix1, float[] rmatrix2,float[]noise)
    {
        ColorMatrix1 = matrix1;
        ColorMatrix2 = matrix2;
        NeutralMatrix = neutral;
        ForwardMatrix1 = fmatrix1;
        ForwardMatrix2 = fmatrix2;
        ReductionMatrix1 = rmatrix1;
        ReductionMatrix2 = rmatrix2;
        NoiseReductionMatrix = noise;
    }


    private static float[] getMatrixFromString(String m)
    {
        String[] split = m.split(",");
        float[] ar = new float[split.length];
        for (int i = 0; i< split.length; i++)
        {
            ar[i] = Float.parseFloat(split[i]);
        }
        return ar;
    }

    public static CustomMatrix loadCustomMatrixFromFile(File customMAtrix)
    {
        CustomMatrix matrix = new CustomMatrix();
        try
        {
            Logger.d(TAG, "CustomMediaProfile exists loading....");
            BufferedReader br = null;

            br = new BufferedReader(new FileReader(customMAtrix));

            String line;
            int count = 0;
            while ((line = br.readLine()) != null)
            {
                if (!line.startsWith("#")) {
                    switch (count)
                    {
                        case 0:
                            matrix.ColorMatrix1 = getMatrixFromString(line);
                            break;
                        case 1:
                            matrix.ColorMatrix2 = getMatrixFromString(line);
                            break;
                        case 2:
                            matrix.NeutralMatrix = getMatrixFromString(line);
                            break;
                        case 3:
                            matrix.ForwardMatrix1 = getMatrixFromString(line);
                            break;
                        case 4:
                            matrix.ForwardMatrix2 = getMatrixFromString(line);
                            break;
                        case 5:
                            matrix.ReductionMatrix1 = getMatrixFromString(line);
                            break;
                        case 6:
                            matrix.ReductionMatrix2 = getMatrixFromString(line);
                            break;
                        case 7:
                            matrix.NoiseReductionMatrix = getMatrixFromString(line);
                            break;
                    }
                    count++;
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matrix;
    }
}
