package com.romssilva.smartupnp.smartupnp;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.types.Datatype;

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



        if (editable) {

            holder.actionName.setText("Set " + action.getFirstInputArgument().getRelatedStateVariableName());

            StateVariable stateVariable = action.getService().getRelatedStateVariable(action.getFirstInputArgument());
            Datatype datatype = stateVariable.getTypeDetails().getDatatype();

            if (datatype.getBuiltin() == Datatype.Builtin.BOOLEAN) {
                holder.aSwitch.setVisibility(View.VISIBLE);

                holder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        Log.e("invoking", "clicked");
                        GenericActionInvocation genericActionInvocation = new GenericActionInvocation(action);
                        Log.e("invoking", "action created");

                        genericActionInvocation.setInput(action.getFirstInputArgument().getName(), b);

                        upnpService.getControlPoint().execute(new ActionCallback(genericActionInvocation) {
                            @Override
                            public void success(final ActionInvocation actionInvocation) {
                                deviceActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (actionInvocation.getOutput().length > 0) {
                                            Log.e("UPnP", actionInvocation.getOutputMap().toString());
                                        }
                                    }
                                });
                            }

                            @Override
                            public void failure(ActionInvocation actionInvocation, final UpnpResponse upnpResponse, String s) {
                                deviceActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e("UPnP", "Fail! " + upnpResponse.getResponseDetails());
                                    }
                                });
                            }
                        });

                    }
                });
            } else if (datatype.getBuiltin() == Datatype.Builtin.STRING) {
                holder.editText.setVisibility(View.VISIBLE);
                holder.invokeButton.setVisibility(View.VISIBLE);
                holder.invokeButton.setText("Set");

                holder.invokeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("invoking", "clicked");
                        GenericActionInvocation genericActionInvocation = new GenericActionInvocation(action);
                        Log.e("invoking", "action created");

                        genericActionInvocation.setInput(action.getFirstInputArgument().getName(), holder.editText.getText());

                        upnpService.getControlPoint().execute(new ActionCallback(genericActionInvocation) {
                            @Override
                            public void success(final ActionInvocation actionInvocation) {
                                deviceActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (actionInvocation.getOutput().length > 0) {
                                            Log.e("UPnP", actionInvocation.getOutputMap().toString());
                                        }
                                    }
                                });
                            }

                            @Override
                            public void failure(ActionInvocation actionInvocation, final UpnpResponse upnpResponse, String s) {
                                deviceActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e("UPnP", "Fail! " + upnpResponse.getResponseDetails());
                                    }
                                });
                            }
                        });
                    }
                });
            }




        } else {
            holder.actionName.setText(action.getFirstOutputArgument().getRelatedStateVariableName());

            holder.actionIOField.setVisibility(View.VISIBLE);
            holder.invokeButton.setVisibility(View.VISIBLE);

            holder.invokeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("invoking", "clicked");
                    GenericActionInvocation genericActionInvocation = new GenericActionInvocation(action);
                    Log.e("invoking", "action created");

                    genericActionInvocation.getOutput(action.getFirstOutputArgument().getName());

                    upnpService.getControlPoint().execute(new ActionCallback(genericActionInvocation) {
                        @Override
                        public void success(final ActionInvocation actionInvocation) {
                            deviceActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (actionInvocation.getOutput().length > 0) {
                                        holder.actionIOField.setText(actionInvocation.getOutputMap().toString());
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
    }

    @Override
    public int getItemCount() {
        return actions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout deviceActionParent;
        TextView actionName;
        TextView actionIOField;
        Button invokeButton;
        Switch aSwitch;
        SeekBar seekBar;
        EditText editText;

        public ViewHolder(View itemView) {
            super(itemView);
            deviceActionParent = itemView.findViewById(R.id.device_action_parent);
            actionName = itemView.findViewById(R.id.action_name);
            actionIOField = itemView.findViewById(R.id.action_io_field);
            invokeButton = itemView.findViewById(R.id.invoke_button);
            aSwitch = itemView.findViewById(R.id.switch1);
            seekBar = itemView.findViewById(R.id.seekBar);
            editText = itemView.findViewById(R.id.editText);
        }
    }

    public void addAction(Action action) {
        actions.add(action);
    }
}
