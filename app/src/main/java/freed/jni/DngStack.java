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

    private static native void startStack(String outputFile,String[] filesToStack, int buffersize, int minoffset,int maxoffset, int l1mindistance, int l1maxdistance);

    private final String[] dngToStack;

    public DngStack(String[] dngs_to_stack)
    {
        this.dngToStack = dngs_to_stack;
    }

    public void StartStack(Context context,int bufsize,int minoffset,int maxoffset, int l1mindistance, int l1maxdistance)
    {
        File file = new File(dngToStack[0]);
        File out = file.getParentFile();
        out = new File(out.getAbsolutePath() + "/" + file.getName() + "_Stack.dng");
        startStack(out.getAbsolutePath(),dngToStack , bufsize, minoffset,maxoffset,l1mindistance,l1maxdistance);
        MediaScannerManager.ScanMedia(context,out);
    }
}
