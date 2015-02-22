package com.kanikash.friendshub.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kanikash.friendshub.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by kanikash on 2/23/15.
 */
public class ImageFragment extends DialogFragment {
    private ImageView ivImage;
    private ImageLoaderConfiguration config;


    public static ImageFragment newInstance (String assetPath) {
        ImageFragment imFrag = new ImageFragment();
        Bundle args = new Bundle();
        args.putString("assetPath", assetPath);
        imFrag.setArguments(args);
        return imFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.image_display, container, false);
        // Get the subviews
        ivImage = (ImageView) v.findViewById(R.id.ivImage);
        // Get image source
        String assetPath = getArguments().getString("assetPath");
        // Set the ImageLoaderConfigurations
        config = new ImageLoaderConfiguration.Builder(getActivity()).build();
        ImageLoader.getInstance().init(config);
        // Load the image in ImageView
        ImageLoader.getInstance().displayImage(assetPath, ivImage);
        return v;
    }
}
