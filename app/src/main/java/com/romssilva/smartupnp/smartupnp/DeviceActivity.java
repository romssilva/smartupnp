package com.romssilva.smartupnp.smartupnp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.UDNHeader;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.util.ArrayList;

public class DeviceActivity extends AppCompatActivity {

    private DeviceDisplay deviceDisplay;
    private TextView deviceName;
    private UDN udn;

    private BrowseRegistryListener registryListener = new BrowseRegistryListener();

    private AndroidUpnpService upnpService;

    private DeviceActionAdapter deviceActionAdapter;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i("deviceAct", "onServiceConnected");
            upnpService = (AndroidUpnpService) service;

            // Get ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);

            upnpService.getRegistry().removeAllLocalDevices();
            upnpService.getRegistry().removeAllRemoteDevices();

            // Search asynchronously for all devices, they will respond soon
            upnpService.getControlPoint().search(new UDNHeader(udn));

            initDeviceActionAdapter();
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("deviceAct", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        deviceName = findViewById(R.id.device_name);
        deviceName.setText("Gathering device information...");

        getIncomingIntent();
    }

    private void initDeviceActionAdapter() {
        RecyclerView deviceActionsList = (RecyclerView) findViewById(R.id.device_actions_list);
        deviceActionAdapter = new DeviceActionAdapter(new ArrayList<Action>(), getApplicationContext(), upnpService, this);

        deviceActionsList.setAdapter(deviceActionAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        deviceActionsList.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        // This will stop the UPnP service if nobody else is bound to it
        getApplicationContext().unbindService(serviceConnection);
    }

    public void getIncomingIntent() {
        Log.i("deviceAct", "getIcomingIntent");
        if (getIntent().hasExtra("device_udn")) {
            udn = new UDN(getIntent().getStringExtra("device_udn"));

            // This will start the UPnP service if it wasn't already started
            getApplicationContext().bindService(
                    new Intent(this, AndroidUpnpServiceImpl.class),
                    serviceConnection,
                    Context.BIND_AUTO_CREATE
            );
        }
    }

    protected class BrowseRegistryListener extends DefaultRegistryListener {

        /* Discovery performance optimization for very slow Android devices! */
        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            deviceRemoved(device);
        }
        /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            if (device.isFullyHydrated()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deviceName.setText(device.getDetails().getFriendlyName());
                    }
                });
                for (Service service : device.getServices()) {
                    for (Action action : service.getActions()) {
                        deviceActionAdapter.addAction(action);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                deviceActionAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }

            }
        }

        public void deviceRemoved(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                }
            });
        }

        public void executeAction(AndroidUpnpService upnpService, Action action) {

            ActionInvocation toggleActionInvocation = new GenericActionInvocation(action);

            upnpService.getControlPoint().execute(new ActionCallback(toggleActionInvocation) {
                @Override
                public void success(ActionInvocation actionInvocation) {
                    Log.i("Action Callback", "Success!");
                }

                @Override
                public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                    Log.i("Action Callback", "Failed!");
                }
            });
        }

        public void executeActions (AndroidUpnpService upnpService, DeviceDisplay deviceDisplay) {
            Device device = deviceDisplay.getDevice();

            for (Service service : device.getServices()) {
                for (Action action : service.getActions()) {

                    ActionInvocation toggleActionInvocation = new GenericActionInvocation(action);

                    upnpService.getControlPoint().execute(new ActionCallback(toggleActionInvocation) {
                        @Override
                        public void success(ActionInvocation actionInvocation) {
                            Log.i("Action Callback", "Success!");
                        }

                        @Override
                        public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                            Log.i("Action Callback", "Failed!");
                        }
                    });
                }
            }
        }
    }
}
