package com.romssilva.smartupnp.smartupnp;

import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Action;

/**
 * Created by romssilva on 2018-04-08.
 */

public class GenericActionInvocation extends ActionInvocation {

    private String value;

    public GenericActionInvocation(Action action) {
        super(action);
    }
}
