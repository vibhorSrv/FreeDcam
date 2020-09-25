package freed.gallery.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.troop.freedcam.R;
import com.troop.freedcam.databinding.GalleryFragmentBinding;

import freed.gallery.adapter.GalleryRecyclerAdapter;
import freed.gallery.viewmodel.GalleryViewModel;

public class GalleryFragment extends Fragment {

    private GalleryViewModel mViewModel;
    private GalleryFragmentBinding galleryFragmentBinding;
    private GalleryRecyclerAdapter adapter;

    public static GalleryFragment newInstance() {
        return new GalleryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        galleryFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.gallery_fragment, container, false);
        mViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);
        mViewModel.create(getActivity().getApplication());
        RecyclerView recyclerView = galleryFragmentBinding.galleryRecylerview;
        GridLayoutManager gridLayoutManager =new GridLayoutManager(recyclerView.getContext(),3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        
        adapter = new GalleryRecyclerAdapter();
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);



        return galleryFragmentBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.loadFreeDcamDcimFiles();
        adapter.setFileHolders(mViewModel.getFiles());
    }
}
