package com.romssilva.smartupnp.smartupnp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by romssilva on 2018-05-31.
 */

class FavoritesManagar {
    private static final FavoritesManagar ourInstance = new FavoritesManagar();

    private ArrayList<DeviceDisplay> devices;

    static FavoritesManagar getInstance() {
        return ourInstance;
    }

    private FavoritesManagar() {
        devices = new ArrayList<>();
    }

    public void addDevice(DeviceDisplay deviceDisplay) {
        devices.add(deviceDisplay);
    }

    public void removeDevice(DeviceDisplay deviceDisplay) {
        devices.remove(deviceDisplay);
    }

    public boolean isFavorite(DeviceDisplay deviceDisplay) {
        return devices.contains(deviceDisplay);
    }

    public List<DeviceDisplay> getFavorites() {
        return devices;
    }
}
