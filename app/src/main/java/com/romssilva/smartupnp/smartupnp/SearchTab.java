package com.romssilva.smartupnp.smartupnp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.w3c.dom.Text;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by romssilva on 2018-04-08.
 */

public class SearchTab extends Fragment {

    private RecyclerView devicesList;

    private DeviceFoundAdapter deviceFoundAdapter;

    private FloatingActionButton refreshButton;

    private TextView searchingLabel;

    private AndroidUpnpService upnpService;

    Timer timer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.search_tab, container, false);

        devicesList = (RecyclerView) rootView.findViewById(R.id.devices_found_list);
        searchingLabel = (TextView) rootView.findViewById(R.id.searching_label);

        refreshButton = (FloatingActionButton) rootView.findViewById(R.id.refreshBtn);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (upnpService != null) {
                    upnpService.getRegistry().removeAllRemoteDevices();
                    deviceFoundAdapter.removeAllDevices();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deviceFoundAdapter.notifyDataSetChanged();
                            searchingLabel.setVisibility(View.VISIBLE);
                        }
                    });
                    upnpService.getControlPoint().search();
                }
                startSearchTimer();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        devicesList.setLayoutManager(linearLayoutManager);

        deviceFoundAdapter = new DeviceFoundAdapter(rootView.getContext());
        devicesList.setAdapter(deviceFoundAdapter);

        startSearchTimer();

        return rootView;
    }

    public void addDevice(Device device) {
        deviceFoundAdapter.addDevice(device);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceFoundAdapter.notifyDataSetChanged();
                searchingLabel.setVisibility(View.GONE);
                timer.cancel();
            }
        });
    }

    private void startSearchTimer() {
        searchingLabel.setText("Searching for smart devices...");
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchingLabel.setText("No smart devices found. Please try again.");
                    }
                });
            }
        }, 5000);
    }

    public void setUpnpService(AndroidUpnpService upnpService) {
        this.upnpService = upnpService;
    }
}
