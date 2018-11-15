package com.romssilva.smartupnp.smartupnp;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
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
 * Created by romssilva on 2018-05-15.
 */

public class DeviceInSightAdapter extends RecyclerView.Adapter<DeviceInSightAdapter.DeviceInSightViewHolder> {

    private List<DeviceDisplay> devices;
    private Context context;

    public DeviceInSightAdapter(Context context) {
        devices = new ArrayList<>();
        this.context = context;
    }

    @Override
    public DeviceInSightViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.device_in_sight_display, parent, false);

        DeviceInSightAdapter.DeviceInSightViewHolder deviceInSightViewHolder = new DeviceInSightAdapter.DeviceInSightViewHolder(view);

        return deviceInSightViewHolder;
    }

    @Override
    public void onBindViewHolder(DeviceInSightViewHolder holder, int position) {
        if (devices != null && devices.size() > 0) {
            final DeviceDisplay deviceDisplay = devices.get(position);
            holder.deviceName.setText(deviceDisplay.device.getDetails().getFriendlyName());
            holder.deviceDesc.setText(deviceDisplay.device.getDisplayString());
            if (deviceDisplay.device.getDetails().getFriendlyName().toLowerCase().contains("light"))
                holder.deviceImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_light));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (deviceDisplay.getDevice().getDetails().getPresentationURI() != null) {
                        String appPackageName = deviceDisplay.getDevice().getDetails().getPresentationURI().toString();
                        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
                        if (launchIntent != null) {
                            context.startActivity(launchIntent);//null pointer check in case package name was not found
                        } else {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
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

    public class DeviceInSightViewHolder extends RecyclerView.ViewHolder {

        public TextView deviceName;
        public TextView deviceDesc;
        public ImageView deviceImage;

        public DeviceInSightViewHolder(View itemView) {
            super(itemView);

            deviceName = (TextView) itemView.findViewById(R.id.device_name);
            deviceDesc = (TextView) itemView.findViewById(R.id.device_desc);
            deviceImage = (ImageView) itemView.findViewById(R.id.device_in_sight_image);
        }
    }

    public void addDevice(Device device) {
        DeviceDisplay deviceDisplay = new DeviceDisplay(device);
        if (!devices.contains(deviceDisplay) && deviceDisplay.device.isFullyHydrated()) {
            devices.add(deviceDisplay);
        }
    }

    public void removeDevice(Device device) {
        DeviceDisplay deviceDisplay = new DeviceDisplay(device);
        if (devices.contains(deviceDisplay)) {
            devices.remove(deviceDisplay);
        }
    }

    public int getDevicePosition(Device device) {
        DeviceDisplay deviceDisplay = new DeviceDisplay(device);
        return devices.indexOf(deviceDisplay);
    }
}
