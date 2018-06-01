package com.romssilva.smartupnp.smartupnp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by romssilva on 2018-04-08.
 */

public class HomeTab extends Fragment {

    private RecyclerView favoriteList;
    private LinearLayout favoriteEmpty;

    private FavoriteDeviceAdapter favoriteDeviceAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_tab, container, false);

        favoriteList = rootView.findViewById(R.id.favorites_list);
        favoriteEmpty = rootView.findViewById(R.id.favorites_empty);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        favoriteList.setLayoutManager(linearLayoutManager);

        favoriteDeviceAdapter = new FavoriteDeviceAdapter(rootView.getContext());
        favoriteList.setAdapter(favoriteDeviceAdapter);

        return rootView;
    }

    public void updateFavorites() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                favoriteDeviceAdapter.notifyDataSetChanged();
                if (FavoritesManagar.getInstance().getFavorites().isEmpty()) {
                    favoriteEmpty.setVisibility(View.VISIBLE);
                    favoriteList.setVisibility(View.GONE);
                } else {
                    favoriteEmpty.setVisibility(View.GONE);
                    favoriteList.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
