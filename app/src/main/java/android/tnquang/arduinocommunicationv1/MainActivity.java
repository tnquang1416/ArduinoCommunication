package android.tnquang.arduinocommunicationv1;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String BUTTON_0 = "0";
    private static final String BUTTON_1 = "1";
    private static final String BUTTON_2 = "2";
    private static final String BUTTON_3 = "3";
    private static final String BUTTON_4 = "4";
    private static final String BUTTON_5 = "5";
    private static final String BUTTON_6 = "6";
    private static final String BUTTON_7 = "7";
    private static final String BUTTON_8 = "8";
    private static final String BUTTON_9 = "9";
    private static final String BUTTON_STAR = "*";
    private static final String BUTTON_SHARP = "#";
    private static String LATEST_PRESSED = "";
    private static long TIME_LATEST_PRESSED = 0;

    private UsbSerialDevice m_serialDevice;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_USB_PERMISSION:
                    boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                    if (!granted) {
                        Toast.makeText(context, "No permission", Toast.LENGTH_LONG).show();
                        break;
                    }

                    UsbManager usbMng = (UsbManager) getSystemService(USB_SERVICE);
                    UsbDeviceConnection connection = usbMng.openDevice((UsbDevice) intent.getExtras().get("device"));
                    if (connection == null) {
                        Toast.makeText(context, "No connection", Toast.LENGTH_LONG).show();
                        break;
                    }
                    m_serialDevice = UsbSerialDevice.createUsbSerialDevice((UsbDevice) intent.getExtras().get("device"), connection);
                    if (m_serialDevice == null || !m_serialDevice.open()) {
                        Toast.makeText(context, "No serial device", Toast.LENGTH_LONG).show();
                        break;
                    }

                    m_serialDevice.read(m_readCallBack);
                    Toast.makeText(context, "Device connected", Toast.LENGTH_LONG).show();
                    updateTextView("Device connected.");
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    Toast.makeText(getApplicationContext(), "Device attached", Toast.LENGTH_SHORT).show();
                    connectToDevice(null);
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    Toast.makeText(getApplicationContext(), "Device detached", Toast.LENGTH_SHORT).show();
                    if (m_serialDevice != null)
                        m_serialDevice.close();
                    break;
            }
        }
    };

    private UsbSerialInterface.UsbReadCallback m_readCallBack = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] bytes) {
            try {
                updateTextView(new String(bytes));
                handleFunction(new String(bytes));
            } catch (Exception ex) {
                updateTextView("...");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAdd(view);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        this.registerReceiver(receiver, filter);
    }

    public void onClickAdd(View view) {
        this.connectToDevice(view);
    }

    private void connectToDevice(View view) {
        UsbManager usbMng = (UsbManager) getSystemService(USB_SERVICE);
        HashMap<String, UsbDevice> usbDevices = usbMng.getDeviceList();

        if (usbDevices == null || usbDevices.isEmpty()) {
            Toast.makeText(this, "No device", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "We got " + usbDevices.size() + " devices", Toast.LENGTH_LONG).show();
        // 1 device only
        for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
            if (view != null)
                Snackbar.make(view, "Device's vendor id: " + entry.getValue().getVendorId(), Snackbar.LENGTH_LONG).show();
            // connectToDevice
            Intent intent = new Intent(ACTION_USB_PERMISSION);
            intent.putExtra("device", entry.getValue());
            PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            usbMng.requestPermission(entry.getValue(), pIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateTextView(String content) {
        final TextView tv = findViewById(R.id.content);
        final String text = content;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.append(text);
            }
        });
    }

    private void handleFunction(String content) {
        final String input = content.trim();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "-" + input + "-", Toast.LENGTH_SHORT).show();
            }
        });

        switch (input) {
            case BUTTON_0:
                LATEST_PRESSED = BUTTON_0;
            case BUTTON_1:
                if (isRelatedToButton(BUTTON_7))
                    directToSpecifyLocation(1);
                if (isRelatedToButton(BUTTON_STAR))
                    setLocation(1);
                if (isRelatedToButton(BUTTON_9))
                    cancelDirection();
                if (isRelatedToButton(BUTTON_0 + BUTTON_1))
                    callTo(1);
                if (isRelatedToButton(BUTTON_0 + BUTTON_2))
                    sendSMSTo(1);
                if (isRelatedToButton(BUTTON_0))
                    LATEST_PRESSED += BUTTON_1;
                else
                    LATEST_PRESSED = BUTTON_1;
                break;
            case BUTTON_2:
                if (LATEST_PRESSED.equalsIgnoreCase(BUTTON_7) && isRelatedToButton(BUTTON_7))
                    directToSpecifyLocation(2);
                if (LATEST_PRESSED.equalsIgnoreCase(BUTTON_STAR) && isRelatedToButton(BUTTON_STAR))
                    setLocation(2);
                if (isRelatedToButton(BUTTON_9))
                    directToDeparture();
                if (isRelatedToButton(BUTTON_0 + BUTTON_1))
                    callTo(2);
                if (isRelatedToButton(BUTTON_0 + BUTTON_2))
                    sendSMSTo(2);
                if (isRelatedToButton(BUTTON_0))
                    LATEST_PRESSED += BUTTON_2;
                else
                    LATEST_PRESSED = BUTTON_2;
                break;
            case BUTTON_3:
                if (isRelatedToButton(BUTTON_0 + BUTTON_1))
                    callTo(3);
                if (isRelatedToButton(BUTTON_0 + BUTTON_2))
                    sendSMSTo(3);
                if (isRelatedToButton(BUTTON_7))
                    directToSpecifyLocation(3);
                if (isRelatedToButton(BUTTON_STAR))
                    setLocation(3);
                LATEST_PRESSED = BUTTON_3;
                break;
            case BUTTON_4:
                if (isRelatedToButton(BUTTON_7))
                    directToSpecifyLocation(4);
                if (isRelatedToButton(BUTTON_STAR))
                    setLocation(4);
                if (isRelatedToButton(BUTTON_0 + BUTTON_1))
                    callTo(4);
                if (isRelatedToButton(BUTTON_0 + BUTTON_2))
                    sendSMSTo(4);
                LATEST_PRESSED = BUTTON_4;
                break;
            case BUTTON_5:
                if (isRelatedToButton(BUTTON_7))
                    directToSpecifyLocation(5);
                if (isRelatedToButton(BUTTON_STAR))
                    setLocation(5);
                if (isRelatedToButton(BUTTON_0 + BUTTON_1))
                    callTo(5);
                if (isRelatedToButton(BUTTON_0 + BUTTON_2))
                    sendSMSTo(5);
                LATEST_PRESSED = BUTTON_5;
                break;
            case BUTTON_6:
                if (isRelatedToButton(BUTTON_7))
                    directToSpecifyLocation(6);
                if (isRelatedToButton(BUTTON_STAR))
                    setLocation(6);
                if (isRelatedToButton(BUTTON_0 + BUTTON_1))
                    callTo(6);
                if (isRelatedToButton(BUTTON_0 + BUTTON_2))
                    sendSMSTo(6);
                LATEST_PRESSED = BUTTON_6;
                break;
            // first step of find direction
            case BUTTON_7:
                if (isRelatedToButton(BUTTON_7))
                    directToSpecifyLocation(7);
                if (isRelatedToButton(BUTTON_STAR))
                    setLocation(7);
                if (isRelatedToButton(BUTTON_0 + BUTTON_1))
                    callTo(7);
                if (isRelatedToButton(BUTTON_0 + BUTTON_2))
                    sendSMSTo(7);
                LATEST_PRESSED = BUTTON_7;
                break;
            case BUTTON_8:
                if (isRelatedToButton(BUTTON_7))
                    directToSpecifyLocation(8);
                else if (isRelatedToButton(BUTTON_STAR))
                    setLocation(8);
                else
                    locate();
                if (isRelatedToButton(BUTTON_0 + BUTTON_1))
                    callTo(8);
                if (isRelatedToButton(BUTTON_0 + BUTTON_2))
                    sendSMSTo(8);
                LATEST_PRESSED = BUTTON_8;
                break;
            case BUTTON_9:
                if (isRelatedToButton(BUTTON_7))
                    directToSpecifyLocation(9);
                if (isRelatedToButton(BUTTON_STAR))
                    setLocation(9);
                if (isRelatedToButton(BUTTON_0 + BUTTON_1))
                    callTo(9);
                if (isRelatedToButton(BUTTON_0 + BUTTON_2))
                    sendSMSTo(9);
                LATEST_PRESSED = BUTTON_9;
                break;
            case BUTTON_STAR:
                LATEST_PRESSED = BUTTON_STAR;
                break;
            case BUTTON_SHARP:
                stopCalling();
                LATEST_PRESSED = BUTTON_SHARP;
                break;
        }
        TIME_LATEST_PRESSED = System.currentTimeMillis();
    }

    /**
     * find a way to specific place
     * 7 --> 1..9
     */
    public void directToSpecifyLocation(int idLocation) {
        this.updateTextView("Go To " + idLocation);
    }

    /**
     * set location
     * * --> 1..9
     */
    public void setLocation(int idLocation) {
        this.updateTextView("Set up place " + idLocation);
    }

    /**
     * call to specific contact
     * 0 --> 1 --> 1..9
     *
     * @param idContact
     */
    public void callTo(int idContact) {
        this.updateTextView("Call " + idContact);
    }

    /**
     * send sms to specific contact
     * 0 --> 2 --> 1..9
     *
     * @param idContact
     */
    public void sendSMSTo(int idContact) {
        this.updateTextView("Send sms to " + idContact);
    }

    /**
     * locate and tell user where he is
     * 8
     */
    public void locate() {
        this.updateTextView("Locate me");
    }

    /**
     * stop calling
     */
    public void stopCalling() {
        this.updateTextView("I dont wanna call");
    }

    /**
     * cancel direction and clear map
     */
    public void cancelDirection() {
        this.updateTextView("Cancel direction");
    }

    /**
     * show direction to departure
     */
    public void directToDeparture() {
        this.updateTextView("Go home");
    }

    private boolean isRelatedToButton(String button) {
        return LATEST_PRESSED.equalsIgnoreCase(button) && (System.currentTimeMillis() - TIME_LATEST_PRESSED) < 5001;
    }
}
