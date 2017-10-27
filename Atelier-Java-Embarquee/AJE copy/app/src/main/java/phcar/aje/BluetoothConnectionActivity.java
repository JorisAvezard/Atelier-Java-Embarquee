package phcar.aje;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by Joris on 24/10/2017.
 */

public class BluetoothConnectionActivity extends Activity{

    private BluetoothAdapter bluetoothAdapter;
    private final static int REQUEST_CODE_ENABLE_BLUETOOTH = 0;
    private Set<BluetoothDevice> devices;
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(BluetoothConnectionActivity.this, "New Device = " + device.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Detecte si le bluetooth est utilisable sur l'appareil
/*        if (bluetoothAdapter == null)
            Toast.makeText(BluetoothConnectionActivity.this, "Pas de Bluetooth",
                    Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(BluetoothConnectionActivity.this, "Avec Bluetooth",
                    Toast.LENGTH_SHORT).show();
*/
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
        }

        devices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice blueDevice : devices) {
            Toast.makeText(BluetoothConnectionActivity.this, "Device = " + blueDevice.getName(), Toast.LENGTH_SHORT).show();
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, filter);
        bluetoothAdapter.startDiscovery();

/*        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
*/

        Button serveur_button = (Button) findViewById(R.id.serveur);
        serveur_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Créér un intent et lancer l'activité serveur
                Intent intent = new Intent(BluetoothConnectionActivity.this, ServeurActivity.class);
                startActivity(intent);

            }
        });

        Button client_button = (Button) findViewById(R.id.client);
        client_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Créér un intent et lancer l'activité client
                Intent intent = new Intent(BluetoothConnectionActivity.this, ClientActivity.class);
                startActivity(intent);

            }
        });
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
            bluetoothAdapter.enable();
        } else {
            // L'utilisation n'a pas activé le bluetooth
        }
    }
}
