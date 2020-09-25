package freed.gallery;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.troop.freedcam.R;

import freed.gallery.views.GalleryFragment;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, GalleryFragment.newInstance())
                    .commitNow();
        }
    }
}
