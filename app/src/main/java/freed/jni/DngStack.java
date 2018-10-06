package freed.jni;

import android.content.Context;

import java.io.File;

import freed.utils.MediaScannerManager;

/**
 * Created by troop on 25.10.2016.
 */

public class DngStack
{
    static
    {
        System.loadLibrary("freedcam");
    }

    private static native void startStack(String[] filesToStack,String outputFile, String tmpfolder[]);

    private final String[] dngToStack;

    public DngStack(String[] dngs_to_stack)
    {
        this.dngToStack = dngs_to_stack;
    }

    public void StartStack(Context context)
    {
        File file = new File(dngToStack[0]);
        File out = file.getParentFile();
        out = new File(out.getAbsolutePath() + "/" + file.getName() + "_Stack.dng");
        File tmpfolder = new File(out.getParentFile().getAbsoluteFile()+"/tmp");
        if (!tmpfolder.exists())
            tmpfolder.mkdirs();
        int stacktmpsize = dngToStack.length/5;
        String tmpfiles[] = new String[stacktmpsize];
        for (int i = 0; i < stacktmpsize; i++)
        {
            tmpfiles[i] = tmpfolder.getAbsolutePath() +"/stack" + i;
        }
        startStack(dngToStack , out.getAbsolutePath(), tmpfiles);
        MediaScannerManager.ScanMedia(context,out);
    }
}
