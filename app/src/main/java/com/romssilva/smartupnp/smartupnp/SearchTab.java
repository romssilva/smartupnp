package com.romssilva.smartupnp.smartupnp;

import android.os.Bundle;
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

/**
 * Created by romssilva on 2018-04-08.
 */

public class SearchTab extends Fragment {

    private RecyclerView devicesList;

    private DeviceFoundAdapter deviceFoundAdapter;

    private TextView searchingLabel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.search_tab, container, false);

        devicesList = (RecyclerView) rootView.findViewById(R.id.devices_found_list);
        searchingLabel = (TextView) rootView.findViewById(R.id.searching_label);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        devicesList.setLayoutManager(linearLayoutManager);

        deviceFoundAdapter = new DeviceFoundAdapter(rootView.getContext());
        devicesList.setAdapter(deviceFoundAdapter);

        return rootView;
    }

    public void addDevice(Device device) {
        deviceFoundAdapter.addDevice(device);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceFoundAdapter.notifyDataSetChanged();
                searchingLabel.setVisibility(View.GONE);
            }
        });
    }
}
