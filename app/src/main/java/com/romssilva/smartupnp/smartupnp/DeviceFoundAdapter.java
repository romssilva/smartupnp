package com.romssilva.smartupnp.smartupnp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by romssilva on 2018-04-08.
 */

public class DeviceFoundAdapter extends RecyclerView.Adapter<DeviceFoundAdapter.DeviceFoundViewHolder> {

    private List<DeviceDisplay> devices;
    private Context context;

    public DeviceFoundAdapter(Context context) {
        devices = new ArrayList<>();
        this.context = context;
    }

    @Override
    public DeviceFoundAdapter.DeviceFoundViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.device_found_display, parent, false);

        DeviceFoundViewHolder deviceFoundViewHolder = new DeviceFoundViewHolder(view);

        return deviceFoundViewHolder;
    }

    @Override
    public void onBindViewHolder(DeviceFoundAdapter.DeviceFoundViewHolder holder, final int position) {
        if (devices != null && devices.size() > 0) {
            final DeviceDisplay deviceDisplay = devices.get(position);
            holder.deviceFoundTitle.setText(deviceDisplay.device.getDetails().getFriendlyName());
            holder.deviceFoundSubtitle.setText(deviceDisplay.device.getDisplayString());
            if (deviceDisplay.device.getDetails().getFriendlyName().toLowerCase().contains("light")) {
                holder.deviceFoundImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_light));
            } else {
                holder.deviceFoundImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_device));
            }


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String controlAppPackage = deviceDisplay.getDevice().getDetails().getPresentationURI().toString();
                    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(controlAppPackage);
                    if (launchIntent != null) {
                        context.startActivity(launchIntent);//null pointer check in case package name was not found
                    } else {
                        Intent intent = new Intent(context, DeviceActivity.class);
                        intent.putExtra("device_udn", deviceDisplay.getDevice().getIdentity().getUdn().getIdentifierString());
                        context.startActivity(intent);
                    }
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void addDevice(Device device) {
        DeviceDisplay deviceDisplay = new DeviceDisplay(device);
        if (!devices.contains(deviceDisplay) && deviceDisplay.device.isFullyHydrated()) {
            devices.add(deviceDisplay);
        }
    }

    public class DeviceFoundViewHolder extends RecyclerView.ViewHolder {

        public TextView deviceFoundTitle;
        public TextView deviceFoundSubtitle;
        public ImageView deviceFoundImage;

        public DeviceFoundViewHolder(View itemView) {
            super(itemView);

            deviceFoundTitle = (TextView) itemView.findViewById(R.id.device_found_title);
            deviceFoundSubtitle = (TextView) itemView.findViewById(R.id.device_found_subtitle);
            deviceFoundImage = (ImageView) itemView.findViewById(R.id.device_found_image);
        }
    }

    public void removeAllDevices() {
        devices.clear();
    }
}
