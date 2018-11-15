package com.romssilva.smartupnp.smartupnp;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
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
import org.fourthline.cling.model.action.ActionArgumentValue;
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
    private ArrayList<Action> editableActions;
    private Context context;
    private AndroidUpnpService upnpService;
    private DeviceActivity deviceActivity;

    public DeviceActionAdapter(ArrayList<Action> actions, Context context, AndroidUpnpService upnpService, DeviceActivity deviceActivity) {
        this.actions = actions;
        this.context = context;
        this.upnpService = upnpService;
        this.deviceActivity = deviceActivity;
        this.editableActions = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_action, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Action action = editableActions.get(position);
        final Boolean editable = action.getInputArguments().length > 0;

        if (editable) {

            holder.actionName.setText(action.getFirstInputArgument().getRelatedStateVariableName().replaceAll("\\d+", "").replaceAll("(.)([A-Z])", "$1 $2"));

            StateVariable stateVariable = action.getService().getRelatedStateVariable(action.getFirstInputArgument());
            Datatype datatype = stateVariable.getTypeDetails().getDatatype();

            if (datatype.getBuiltin() == Datatype.Builtin.BOOLEAN) {

                final Switch mSwitch = holder.aSwitch;

                mSwitch.setVisibility(View.VISIBLE);

                Action currentStatusAction = null;

                for (Action oAction : action.getService().getActions()) {
                    if (oAction.getOutputArguments().length > 0 &&
                            oAction.getFirstOutputArgument().getRelatedStateVariableName().equals(stateVariable.getName())) {
                        currentStatusAction = oAction;
                        break;
                    }
                }

                GenericActionInvocation genericActionInvocation = new GenericActionInvocation(currentStatusAction);

                genericActionInvocation.getOutput(currentStatusAction.getFirstOutputArgument().getName());

                upnpService.getControlPoint().execute(new ActionCallback(genericActionInvocation) {
                    @Override
                    public void success(final ActionInvocation actionInvocation) {
                        deviceActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (actionInvocation.getOutput().length > 0) {

                                    ActionArgumentValue actionArgumentValue = (ActionArgumentValue) actionInvocation.getOutputMap().get(actionInvocation.getAction().getFirstOutputArgument().getName());

                                    String value = actionArgumentValue.toString();

                                    mSwitch.setChecked(value.equals("1"));

                                }
                            }
                        });
                    }

                    @Override
                    public void failure(ActionInvocation actionInvocation, final UpnpResponse upnpResponse, String s) {
                        deviceActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                });

                mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                final Button invokeButton = holder.invokeButton;
                final EditText mEditText = holder.editText;

                mEditText.setVisibility(View.VISIBLE);

                Action currentStatusAction = null;

                for (Action oAction : action.getService().getActions()) {
                    if (oAction.getOutputArguments().length > 0 &&
                            oAction.getFirstOutputArgument().getRelatedStateVariableName().equals(stateVariable.getName())) {
                        currentStatusAction = oAction;
                        break;
                    }
                }

                GenericActionInvocation genericActionInvocation = new GenericActionInvocation(currentStatusAction);

                genericActionInvocation.getOutput(currentStatusAction.getFirstOutputArgument().getName());

                mEditText.setText("Getting current state...");

                upnpService.getControlPoint().execute(new ActionCallback(genericActionInvocation) {
                    @Override
                    public void success(final ActionInvocation actionInvocation) {
                        deviceActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (actionInvocation.getOutput().length > 0) {

                                    ActionArgumentValue actionArgumentValue = (ActionArgumentValue) actionInvocation.getOutputMap().get(actionInvocation.getAction().getFirstOutputArgument().getName());

                                    String value = actionArgumentValue.toString();

                                    mEditText.setText(value);
                                    mEditText.setEnabled(true);
                                    invokeButton.setVisibility(View.VISIBLE);

                                }
                            }
                        });
                    }

                    @Override
                    public void failure(ActionInvocation actionInvocation, final UpnpResponse upnpResponse, String s) {
                        deviceActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                });

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
            } else if (datatype.getBuiltin() == Datatype.Builtin.I4) {
                final SeekBar seekBar = holder.seekBar;

                seekBar.setVisibility(View.VISIBLE);

                seekBar.setEnabled(false);

                final long max = stateVariable.getTypeDetails().getAllowedValueRange().getMaximum();
                final long min = stateVariable.getTypeDetails().getAllowedValueRange().getMinimum();

                Action currentStatusAction = null;

                for (Action oAction : action.getService().getActions()) {
                    if (oAction.getOutputArguments().length > 0 &&
                            oAction.getFirstOutputArgument().getRelatedStateVariableName().equals(stateVariable.getName())) {
                        currentStatusAction = oAction;
                        break;
                    }
                }

                GenericActionInvocation genericActionInvocation = new GenericActionInvocation(currentStatusAction);

                genericActionInvocation.getOutput(currentStatusAction.getFirstOutputArgument().getName());

                upnpService.getControlPoint().execute(new ActionCallback(genericActionInvocation) {
                    @Override
                    public void success(final ActionInvocation actionInvocation) {
                        deviceActivity.runOnUiThread(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void run() {
                                if (actionInvocation.getOutput().length > 0) {

                                    ActionArgumentValue actionArgumentValue = (ActionArgumentValue) actionInvocation.getOutputMap().get(actionInvocation.getAction().getFirstOutputArgument().getName());

                                    Integer value = Integer.parseInt(actionArgumentValue.toString());

                                    seekBar.setEnabled(true);
                                    seekBar.setMax(Integer.parseInt(String.valueOf(max)));
                                    if (Integer.parseInt(String.valueOf(min)) > 0) {
                                        seekBar.setMin(Integer.parseInt(String.valueOf(min)));
                                    }
                                    seekBar.setProgress(value);

                                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                        @Override
                                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                            Log.e("invoking", "clicked");
                                            GenericActionInvocation genericActionInvocation = new GenericActionInvocation(action);
                                            Log.e("invoking", "action created");

                                            genericActionInvocation.setInput(action.getFirstInputArgument().getName(), i);

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

                                        @Override
                                        public void onStartTrackingTouch(SeekBar seekBar) {

                                        }

                                        @Override
                                        public void onStopTrackingTouch(SeekBar seekBar) {

                                        }
                                    });
                                }
                            }
                        });
                    }

                    @Override
                    public void failure(ActionInvocation actionInvocation, final UpnpResponse upnpResponse, String s) {
                        deviceActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        int size = 0;

        for (Action action : actions) {
            if (action.getInputArguments().length > 0) size++;
        }

        return size;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout deviceActionParent;
        TextView actionName;
        Button invokeButton;
        Switch aSwitch;
        SeekBar seekBar;
        EditText editText;

        public ViewHolder(View itemView) {
            super(itemView);
            deviceActionParent = itemView.findViewById(R.id.device_action_parent);
            actionName = itemView.findViewById(R.id.action_name);
            invokeButton = itemView.findViewById(R.id.invoke_button);
            aSwitch = itemView.findViewById(R.id.switch1);
            seekBar = itemView.findViewById(R.id.seekBar);
            editText = itemView.findViewById(R.id.editText);
        }
    }

    public void addAction(Action action) {
        actions.add(action);
        if (action.getInputArguments().length > 0) editableActions.add(action);
    }
}
