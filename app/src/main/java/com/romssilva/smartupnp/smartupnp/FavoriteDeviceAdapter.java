package com.romssilva.smartupnp.smartupnp;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.fourthline.cling.model.meta.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by romssilva on 2018-05-31.
 */

public class FavoriteDeviceAdapter extends RecyclerView.Adapter<FavoriteDeviceAdapter.FavoriteDeviceViewHolder> {

    private List<DeviceDisplay> devices;
    private Context context;

    public FavoriteDeviceAdapter(Context context) {
        devices = FavoritesManagar.getInstance(context).getFavorites();
        this.context = context;
    }

    @Override
    public FavoriteDeviceAdapter.FavoriteDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.favorite_device_display, parent, false);

        FavoriteDeviceAdapter.FavoriteDeviceViewHolder favoriteDeviceViewHolder = new FavoriteDeviceAdapter.FavoriteDeviceViewHolder(view);

        return favoriteDeviceViewHolder;
    }

    @Override
    public void onBindViewHolder(FavoriteDeviceAdapter.FavoriteDeviceViewHolder holder, final int position) {
        if (devices != null && devices.size() > 0) {
            final DeviceDisplay deviceDisplay = devices.get(position);
            holder.favDeviceTitle.setText(deviceDisplay.device.getDetails().getFriendlyName());
            holder.favDeviceDesc.setText(deviceDisplay.device.getDisplayString());
            if (deviceDisplay.device.getDetails().getFriendlyName().toLowerCase().contains("light"))
                holder.favImageIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_light));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, DeviceActivity.class);
                    intent.putExtra("device_udn", deviceDisplay.getDevice().getIdentity().getUdn().getIdentifierString());
                    context.startActivity(intent);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }


    public class FavoriteDeviceViewHolder extends RecyclerView.ViewHolder {

        public TextView favDeviceTitle;
        public TextView favDeviceDesc;
        public ImageView favImageIcon;

        public FavoriteDeviceViewHolder(View itemView) {
            super(itemView);

            favDeviceTitle = (TextView) itemView.findViewById(R.id.fav_device_title);
            favDeviceDesc = (TextView) itemView.findViewById(R.id.fav_device_desc);
            favImageIcon = (ImageView) itemView.findViewById(R.id.fav_device_image);
        }
    }
}
