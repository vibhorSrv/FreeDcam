package freed.jni;

import java.nio.ByteBuffer;

import freed.dng.CustomMatrix;
import freed.dng.DngProfile;

public class RawStack {
    static
    {
        System.loadLibrary("freedcam");
    }

    private ByteBuffer byteBuffer;

    private native ByteBuffer init();
    private native void setBaseFrame(ByteBuffer buffer, byte[] fileBytes,int width, int heigt);
    private native void stackFrame(ByteBuffer buffer, byte[] nextframe);
    private native void writeDng(ByteBuffer buffer, ByteBuffer dngprofile, ByteBuffer customMatrix, String outfile);

    public RawStack()
    {
        byteBuffer = init();
    }

    public void setFirstFrame(byte[] bytes, int width, int height)
    {
        setBaseFrame(byteBuffer,bytes,width,height);
    }

    public void stackNextFrame(byte[] bytes)
    {
        stackFrame(byteBuffer,bytes);
    }

    public void saveDng(DngProfile profile, CustomMatrix customMatrix, String fileout)
    {
        writeDng(byteBuffer,profile.getByteBuffer(),customMatrix.getByteBuffer(),fileout);
        byteBuffer = null;
    }
}
