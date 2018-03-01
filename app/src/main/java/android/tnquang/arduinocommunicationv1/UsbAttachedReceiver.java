package android.tnquang.arduinocommunicationv1;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class UsbAttachedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent.getAction() != UsbManager.ACTION_USB_DEVICE_ATTACHED)
            return;

        UsbManager usbMng = (UsbManager) context.getSystemService(MainActivity.USB_SERVICE);
        HashMap<String, UsbDevice> usbDevices = usbMng.getDeviceList();

        if (usbDevices == null || usbDevices.isEmpty()) {
            Toast.makeText(context.getApplicationContext(), "No device", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(context.getApplicationContext(), "We got " + usbDevices.size() + " devices", Toast.LENGTH_LONG).show();
        // 1 device only
        for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
            // connectToDevice
            Intent permissionIntent = new Intent(MainActivity.ACTION_USB_PERMISSION);
            intent.putExtra("device", entry.getValue());
            PendingIntent pIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, permissionIntent, 0);
            usbMng.requestPermission(entry.getValue(), pIntent);
        }
    }
}
