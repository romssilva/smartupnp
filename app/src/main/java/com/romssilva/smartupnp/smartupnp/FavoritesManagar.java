package com.romssilva.smartupnp.smartupnp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by romssilva on 2018-05-31.
 */

class FavoritesManagar {
    private static FavoritesManagar ourInstance;

    private ArrayList<DeviceDisplay> devices;
    private Context context;

    private final String FAVORITES = "favorites";
    private final String SHARED_PREFS_FILE = "favorites_file";

    static FavoritesManagar getInstance(Context context) {
        if (null == ourInstance) {
            ourInstance = new FavoritesManagar(context);
        }
        return ourInstance;
    }

    private FavoritesManagar(Context context) {
        devices = new ArrayList<>();
        this.context = context;

        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        try {
            devices = (ArrayList<DeviceDisplay>) ObjectSerializer.deserialize(prefs.getString(FAVORITES, ObjectSerializer.serialize(new ArrayList<DeviceDisplay>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addDevice(DeviceDisplay deviceDisplay) {
        devices.add(deviceDisplay);

        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putString(FAVORITES, ObjectSerializer.serialize(devices));
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
        }
        editor.commit();
    }

    public void removeDevice(DeviceDisplay deviceDisplay) {
        devices.remove(deviceDisplay);

        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putString(FAVORITES, ObjectSerializer.serialize(devices));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
    }

    public boolean isFavorite(DeviceDisplay deviceDisplay) {
        return devices.contains(deviceDisplay);
    }

    public List<DeviceDisplay> getFavorites() {
        return devices;
    }
}
