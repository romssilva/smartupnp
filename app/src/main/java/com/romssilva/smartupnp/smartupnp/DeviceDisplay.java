package com.romssilva.smartupnp.smartupnp;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;

/**
 * Created by romssilva on 2018-04-08.
 */

public class DeviceDisplay {

    Device device;
    String controlAppPackage;

    public DeviceDisplay(Device device) {
        this.device = device;
    }

    public Device getDevice() {
        return device;
    };

    // DOC:DETAILS
    public String getDetailsMessage() {
        StringBuilder sb = new StringBuilder();
        if (getDevice().isFullyHydrated()) {
            sb.append(getDevice().getDisplayString());
            sb.append("\n\n");
            for (Service service : getDevice().getServices()) {
                sb.append(service.getServiceType()).append("\n");
            }
        } else {
            sb.append(Resources.getSystem().getString(R.string.deviceDetailsNotYetAvailable));
        }
        return sb.toString();
    }
    // DOC:DETAILS

    public String getServicesList() {
        StringBuilder sb = new StringBuilder();
        if (getDevice().isFullyHydrated()) {
            sb.append(getDevice().getDisplayString());
            sb.append("\n\n");
            for(Service s : getDevice().getServices()) {
                for (Action a : s.getActions()) {
                    sb.append(a.getName()).append("\n");
                }
            }
        } else {
            sb.append(Resources.getSystem().getString(R.string.deviceDetailsNotYetAvailable));
        }
        return sb.toString();
    }

    public String getControlAppPackage() {
        return controlAppPackage;
    }

    public void setControlAppPackage(String controlAppPackage) {
        this.controlAppPackage = controlAppPackage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDisplay that = (DeviceDisplay) o;
        return device.equals(that.device);
    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }

    @Override
    public String toString() {
        String name =
                getDevice().getDetails() != null && getDevice().getDetails().getFriendlyName() != null
                        ? getDevice().getDetails().getFriendlyName()
                        : getDevice().getDisplayString();
        // Display a little star while the device is being loaded (see performance optimization earlier)
        return device.isFullyHydrated() ? name : name + " *";
    }
}
