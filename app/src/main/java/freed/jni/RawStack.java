package freed.jni;

import java.nio.ByteBuffer;

import freed.dng.CustomMatrix;
import freed.dng.DngProfile;
import freed.settings.SettingsManager;

public class RawStack {
    static
    {
        System.loadLibrary("freedcam");
    }

    private ByteBuffer byteBuffer;

    private native ByteBuffer init();
    private native void setBaseFrame(ByteBuffer buffer, byte[] fileBytes,int width, int heigt);
    private native void stackFrame(ByteBuffer buffer, byte[] nextframe);
    private native void writeDng(ByteBuffer buffer, ByteBuffer dngprofile, ByteBuffer customMatrix, String outfile, ByteBuffer exifinfo);
    private native void SetOpCode3(byte[] opcode,ByteBuffer byteBuffer);
    private native void SetOpCode2(byte[] opcode,ByteBuffer byteBuffer);

    public RawStack()
    {
        byteBuffer = init();
    }

    public synchronized void setFirstFrame(byte[] bytes, int width, int height)
    {
        setBaseFrame(byteBuffer,bytes,width,height);
    }

    public synchronized void stackNextFrame(byte[] bytes)
    {
        stackFrame(byteBuffer,bytes);
    }

    public synchronized void saveDng(DngProfile profile, CustomMatrix customMatrix, String fileout, ExifInfo exifInfo)
    {
        if (SettingsManager.getInstance().getOpcode2() != null)
            SetOpCode2(SettingsManager.getInstance().getOpcode2(),byteBuffer);
        if (SettingsManager.getInstance().getOpcode3() != null)
            SetOpCode3(SettingsManager.getInstance().getOpcode3(),byteBuffer);
        writeDng(byteBuffer,profile.getByteBuffer(),customMatrix.getByteBuffer(),fileout,exifInfo.getByteBuffer());
        byteBuffer = null;
    }
}
