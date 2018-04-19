package com.romssilva.smartupnp.smartupnp;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Action;

import java.util.ArrayList;

/**
 * Created by romssilva on 2018-04-15.
 */

public class DeviceActionAdapter extends RecyclerView.Adapter<DeviceActionAdapter.ViewHolder> {

    private ArrayList<Action> actions;
    private Context context;
    private AndroidUpnpService upnpService;
    private DeviceActivity deviceActivity;

    public DeviceActionAdapter(ArrayList<Action> actions, Context context, AndroidUpnpService upnpService, DeviceActivity deviceActivity) {
        this.actions = actions;
        this.context = context;
        this.upnpService = upnpService;
        this.deviceActivity = deviceActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_action, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Action action = actions.get(position);
        final Boolean editable = action.getInputArguments().length > 0;

        holder.actionName.setText(action.getName());
        holder.actionIOField.setEnabled(editable);
        holder.invokeButton.setText(editable ? "Set" : "Get");
        holder.invokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("invoking", "clicked");
                GenericActionInvocation genericActionInvocation = new GenericActionInvocation(action);
                Log.e("invoking", "action created");
                try {
                    if (editable && !holder.actionIOField.getText().toString().isEmpty()) {
                        genericActionInvocation.setInput(action.getFirstInputArgument().getName(), holder.actionIOField.getText().toString());
                    } else {
                        genericActionInvocation.getOutput(action.getFirstOutputArgument().getName());
                    }
                } catch (Exception e) {
                    Log.e("Action Invocation Error", e.getMessage());
                }
                upnpService.getControlPoint().execute(new ActionCallback(genericActionInvocation) {
                    @Override
                    public void success(final ActionInvocation actionInvocation) {
                        deviceActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (actionInvocation.getOutput().length > 0) {
                                    holder.actionIOField.setText(actionInvocation.getOutputMap().toString());
                                } else {
                                    holder.actionIOField.setText("Success!");
                                }
                            }
                        });
                    }

                    @Override
                    public void failure(ActionInvocation actionInvocation, final UpnpResponse upnpResponse, String s) {
                        deviceActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.actionIOField.setText("Fail! " + upnpResponse.getResponseDetails());
                            }
                        });
                    }
                });
                holder.actionIOField.setText("Waiting for response...");
            }
        });
    }

    @Override
    public int getItemCount() {
        return actions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout deviceActionParent;
        TextView actionName;
        EditText actionIOField;
        Button invokeButton;

        public ViewHolder(View itemView) {
            super(itemView);
            deviceActionParent = itemView.findViewById(R.id.device_action_parent);
            actionName = itemView.findViewById(R.id.action_name);
            actionIOField = itemView.findViewById(R.id.action_io_field);
            invokeButton = itemView.findViewById(R.id.invoke_button);
        }
    }

    public void addAction(Action action) {
        actions.add(action);
    }
}
