package com.kanikash.friendshub;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class PinItem implements ClusterItem {
    private LatLng pinPosition;

    public PinItem(double lat, double lng) {
        pinPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return pinPosition;
    }
}
