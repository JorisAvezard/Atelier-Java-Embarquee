package com.jorisgmail.avezard.aje;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Joris on 25/10/2017.
 */

public class ClientActivity extends Activity {

    private BluetoothAdapter bluetoothAdapter;
    private final static int REQUEST_CODE_ENABLE_BLUETOOTH = 0;
    private Set<BluetoothDevice> devices;
    private ArrayList<BluetoothDevice> receivers = new ArrayList<>();
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                receivers.add(device);
                Toast.makeText(ClientActivity.this, "New Device = " + device.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    };
    private final int CODE_PERMISSIONS = 10;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] neededPermissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        setContentView(R.layout.activity_client);
        listView = (ListView)findViewById(R.id.listViewClient);
        ActivityCompat.requestPermissions(this, neededPermissions, CODE_PERMISSIONS);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
        }

        Button scan = (Button) findViewById(R.id.clientScan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receivers.clear();
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(bluetoothReceiver, filter);
                bluetoothAdapter.startDiscovery();
            }
        });
    }

    public void list(View v){
        //devices = bluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();
        for (BluetoothDevice blueDevice : receivers) {
            //Toast.makeText(ClientActivity.this, "Device = " + blueDevice.getName(), Toast.LENGTH_SHORT).show();
            list.add(blueDevice.getName());
        }
        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();
        final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        for (BluetoothDevice blueDevice : receivers) {
            //Toast.makeText(ClientActivity.this, "Device = " + blueDevice.getName(), Toast.LENGTH_SHORT).show();
            if(blueDevice.getName().equals("SHITTYFLUTE FOR FEVER")) {
                ConnectThread thread = new ConnectThread(blueDevice, bluetoothAdapter);
                thread.start();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(bluetoothReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE_ENABLE_BLUETOOTH)
            return;
        if (resultCode == RESULT_OK) {
            // L'utilisation a activé le bluetooth
            Toast.makeText(ClientActivity.this, "Bluetooth activé", Toast.LENGTH_SHORT).show();
        } else {
            // L'utilisation n'a pas activé le bluetooth
            Toast.makeText(ClientActivity.this, "Bluetooth non activé", Toast.LENGTH_SHORT).show();
        }
    }
}

class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothAdapter mBluetoothAdapter;
    private static final UUID uuid = UUID.fromString("a60f35f0-b93a-11de-8a39-08102009c666");

    public ConnectThread(BluetoothDevice device, BluetoothAdapter bluetoothAdapter) {
        BluetoothSocket tmp = null;
        mBluetoothAdapter = bluetoothAdapter;
        try {
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) { }
        mmSocket = tmp;
    }

    public void run() {
        mBluetoothAdapter.cancelDiscovery();
        try {
            mmSocket.connect();
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }
        //manageConnectedSocket(mmSocket);
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

}
