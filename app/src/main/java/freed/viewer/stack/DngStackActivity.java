package freed.viewer.stack;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ortiz.touch.TouchImageView;
import com.troop.freedcam.R;

import freed.ActivityAbstract;
import freed.jni.DngStack;
import freed.utils.LocationManager;
import freed.viewer.dngconvert.DngConvertingFragment;
import freed.viewer.holder.FileHolder;

/**
 * Created by troop on 25.10.2016.
 */

public class DngStackActivity extends ActivityAbstract
{
    private String[] filesToStack = null;
    private Button stackButton;
    private EditText buffersize;
    private EditText minoffset;
    private EditText maxoffset;
    private EditText l1mindist;
    private EditText l1maxdist;
    private ProgressBar progressBar;
    private Thread thread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dngstackactivity);
        stackButton = findViewById(R.id.button_dngstack);
        stackButton.setOnClickListener(v -> {
            if (filesToStack != null) {
                progressBar.setVisibility(View.VISIBLE);
                stackButton.setClickable(false);
                thread =  new Thread(stackrunner);
                thread.start();
            }
        });

        buffersize = findViewById(R.id.editText_buffersize);
        buffersize.setText("5");
        minoffset = findViewById(R.id.editText_minoffset);
        minoffset.setText("-32");
        maxoffset = findViewById(R.id.editText_maxoffset);
        maxoffset.setText("32");
        l1mindist = findViewById(R.id.editText_minl1distance);
        l1mindist.setText("4");
        l1maxdist = findViewById(R.id.editText_maxl1distance);
        l1maxdist.setText("128");
        progressBar = findViewById(R.id.progressBar_dngstack);
        progressBar.setVisibility(View.GONE);
        TouchImageView imageView = findViewById(R.id.imageview_dngstack);
        filesToStack = getIntent().getStringArrayExtra(DngConvertingFragment.EXTRA_FILESTOCONVERT);
        if (filesToStack != null)
            ((TextView)findViewById(R.id.rawList)).setText(filesToStack.length+"");


    }

    private Runnable stackrunner = new Runnable() {
        @Override
        public void run() {
            DngStack stack = new DngStack(filesToStack);
            stack.StartStack(getContext(),
                    Integer.parseInt(buffersize.getText().toString()),
                    Integer.parseInt(minoffset.getText().toString()),
                    Integer.parseInt(maxoffset.getText().toString()),
                    Integer.parseInt(l1mindist.getText().toString()),
                    Integer.parseInt(l1maxdist.getText().toString())
            );
            stackDone();
        }
    };

    private void stackDone()
    {
        stackButton.post(new Runnable() {
            @Override
            public void run() {
                thread = null;
                progressBar.setVisibility(View.GONE);
                stackButton.setBackgroundResource(R.drawable.stack_done);
                stackButton.setClickable(true);
            }
        });
    }

    @Override
    public LocationManager getLocationManager() {
        return null;
    }

    @Override
    public void WorkHasFinished(FileHolder fileHolder) {

    }

    @Override
    public void WorkHasFinished(FileHolder[] fileHolder) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (thread != null && thread.isAlive())
            thread.interrupt();
    }
}
