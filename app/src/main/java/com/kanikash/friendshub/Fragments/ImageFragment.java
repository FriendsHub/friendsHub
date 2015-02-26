package com.kanikash.friendshub.Fragments;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kanikash.friendshub.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ImageFragment extends DialogFragment {
    private ImageView ivImage;
    private ProgressBar loadingPanel;
    private TextView tvCaption;

    public static ImageFragment newInstance (String assetPath, String caption) {
        ImageFragment imFrag = new ImageFragment();
        Bundle args = new Bundle();
        args.putString("assetPath", assetPath);
        args.putString("caption", caption);
        //args.putParcelable("image", bmp);
        imFrag.setArguments(args);
        return imFrag;
    }

    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.image_display, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Get the subviews
        ivImage = (ImageView) v.findViewById(R.id.ivImage);
        loadingPanel = (ProgressBar) v.findViewById(R.id.loadingPanel);
        tvCaption = (TextView) v.findViewById(R.id.tvCaption);
        String assetPath = getArguments().getString("assetPath");
        String caption = getArguments().getString("caption");

        tvCaption.setText(caption);
        Target loadTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                loadingPanel.setVisibility(View.GONE);
                ivImage.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable drawable) {
                loadingPanel.setVisibility(View.GONE);
                Log.i("INFO", "failed to load image");
            }

            @Override
            public void onPrepareLoad(Drawable drawable) {
                Log.i("INFO", "starting to load image");
            }
        };
        // Get the screen resolution
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Picasso.with(getActivity().getBaseContext())
                        .load(assetPath)
                        .resize(display.getWidth(), display.getHeight())
                        .centerCrop()
                        .into(loadTarget);
        ivImage.setTag(loadTarget);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
