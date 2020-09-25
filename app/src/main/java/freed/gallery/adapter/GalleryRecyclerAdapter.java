package freed.gallery.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.troop.freedcam.R;
import com.troop.freedcam.databinding.GalleryItemBinding;

import java.util.ArrayList;
import java.util.List;

import freed.file.holder.BaseHolder;
import freed.gallery.model.GalleryItemModel;
import freed.gallery.util.BitmapLoadRunnable;
import freed.image.ImageManager;
import freed.utils.Log;

public class GalleryRecyclerAdapter extends RecyclerView.Adapter<GalleryRecyclerAdapter.MyViewHolder> {

    private final String TAG = GalleryRecyclerAdapter.class.getSimpleName();
    private List<GalleryItemModel> galleryItemModelList;


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GalleryItemBinding galleryListItemBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.gallery_item, parent, false);
        return new MyViewHolder(galleryListItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.binding.imageView.setImageBitmap(null);
        Log.d(TAG, "update pos: "+ position + " adapterpos:" + holder.getAdapterPosition());
        BitmapLoadRunnable bitmapLoadRunnable = new BitmapLoadRunnable(galleryItemModelList.get(position).getBaseHolder());
        holder.setBitmapRunnable(bitmapLoadRunnable);
        ImageManager.putImageLoadTask(bitmapLoadRunnable);
    }

    @Override
    public int getItemCount() {
        if (galleryItemModelList != null)
            return galleryItemModelList.size();
        return 0;
    }

    public void setFileHolders(List<BaseHolder> fileHolders)
    {
        galleryItemModelList = new ArrayList<>();
        for (BaseHolder b : fileHolders) {
            GalleryItemModel itemModel = new GalleryItemModel();
            itemModel.setBaseHolder(b);
            galleryItemModelList.add(itemModel);
        }
        notifyDataSetChanged();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements BitmapLoadRunnable.BitmapLoadingEvents {
        GalleryItemBinding binding;
        BitmapLoadRunnable runnable;
        public MyViewHolder(GalleryItemBinding v) {
            super(v.getRoot());
            binding = v;
        }

        public ImageView getImageView()
        {
            return binding.imageView;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap) {
            this.runnable.setBitmapLoadingListner(null);
            this.runnable = null;
            binding.imageView.post(()->binding.imageView.setImageBitmap(bitmap));
        }

        public void setBitmapRunnable(BitmapLoadRunnable runnable)
        {
            if (this.runnable != null)
                this.runnable.setBitmapLoadingListner(null);
            this.runnable = runnable;
            this.runnable.setBitmapLoadingListner(this);
        }

        @Override
        public void onBitmapLoadingFailed() {

        }
    }

}
