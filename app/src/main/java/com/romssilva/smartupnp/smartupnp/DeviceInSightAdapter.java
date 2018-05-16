package com.romssilva.smartupnp.smartupnp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public class DeviceInSightViewHolder extends RecyclerView.ViewHolder {

        public TextView deviceName;
        public TextView deviceDesc;

        public DeviceInSightViewHolder(View itemView) {
            super(itemView);

            deviceName = (TextView) itemView.findViewById(R.id.device_name);
            deviceDesc = (TextView) itemView.findViewById(R.id.device_desc);
        }
    }

    public void addDevice(Device device) {
        DeviceDisplay deviceDisplay = new DeviceDisplay(device);
        if (!devices.contains(deviceDisplay) && deviceDisplay.device.isFullyHydrated()) {
            devices.add(deviceDisplay);
        }
    }
}
