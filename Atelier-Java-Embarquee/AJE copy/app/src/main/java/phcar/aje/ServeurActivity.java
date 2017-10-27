package phcar.aje;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Joris on 25/10/2017.
 */

public class ServeurActivity extends Activity implements View.OnClickListener{

    //public static FileInputStream videoStream;
    public static String content = "";
    private String SD_CARD_PATH = null;
    private String videoFile;
    private BluetoothAdapter bluetoothAdapter;
    private final static int REQUEST_CODE_ENABLE_BLUETOOTH = 0;
    private Set<BluetoothDevice> devices;
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(ServeurActivity.this, "New Device = " + device.getName(), Toast.LENGTH_SHORT).show();
                System.out.println("New Device = " + device.getName());
            }
        }
    };
    private final int CODE_PERMISSIONS = 10;

    private static Button downloadVideo, openDownloadedFolder, makeVideoStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] neededPermissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        setContentView(R.layout.activity_serveur);
        initViews();
        setListeners();
        ActivityCompat.requestPermissions(this, neededPermissions, CODE_PERMISSIONS);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
        }

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        try {
            AcceptThread thread = new AcceptThread(bluetoothAdapter);
            thread.start();
        } catch (NullPointerException e) {

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
        } else {
            // L'utilisation n'a pas activé le bluetooth
        }
    }

    //Initialize al Views
    private void initViews() {
        downloadVideo = (Button) findViewById(R.id.downloadVideo);
        openDownloadedFolder = (Button) findViewById(R.id.openDownloadedFolder);
        makeVideoStream = (Button) findViewById(R.id.chooseVideoStream);

    }

    //Set Listeners to Buttons
    private void setListeners() {
        downloadVideo.setOnClickListener(this);
        openDownloadedFolder.setOnClickListener(this);
        makeVideoStream.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        //Before starting any download check internet connection availability
        switch (view.getId()) {
            case R.id.downloadVideo:
                if (isConnectingToInternet())
                    new DownloadTask(ServeurActivity.this, downloadVideo, Utils.downloadVideoUrl);
                else
                    Toast.makeText(ServeurActivity.this, "Oops!! There is no internet connection. Please enable internet connection and try again.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.openDownloadedFolder:
                openDownloadedFolder();
                break;
            case R.id.chooseVideoStream:
                //getFileStream();
                OpenFileDialog();
                break;
        }

    }

    //Open downloaded folder
    private void openDownloadedFolder() {
        //First check if SD Card is present or not
        if (new CheckForSDCard().isSDCardPresent()) {

            //Get Download Directory File
            File apkStorage = new File(
                    Environment.getExternalStorageDirectory() + "/"
                            + Utils.downloadDirectory);

            //If file is not present then display Toast
            if (!apkStorage.exists())
                Toast.makeText(ServeurActivity.this, "Right now there is no directory. Please download some file first.", Toast.LENGTH_SHORT).show();

            else {

                //If directory is present Open Folder

                /** Note: Directory will open only if there is a app to open directory like File Manager, etc.  **/

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
                        + "/" + Utils.downloadDirectory);
                intent.setDataAndType(uri, "file/*");
                startActivity(Intent.createChooser(intent, "Open Download Folder"));
            }

        } else
            Toast.makeText(ServeurActivity.this, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();

    }

    //Check if internet is present or not
    private boolean isConnectingToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    /*private void getFileStream() throws FileNotFoundException {
        //First check if SD Card is present or not
        if (new CheckForSDCard().isSDCardPresent()) {

            SD_CARD_PATH = Environment.getExternalStorageDirectory().toString();
            videoFile = new File(SD_CARD_PATH + "/" + DownloadTask.downloadFileName);
            videoStream = new FileInputStream(videoFile);
            InputStream ivs = new InputStream(videoStream) {
                @Override
                public int read() throws IOException {
                    return 0;
                }
            };

        } else
            Toast.makeText(ServeurActivity.this, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();
    }*/

    private void OpenFileDialog() {
        //System.out.println("Debut fonction OpenFileDialog");
        SD_CARD_PATH = Environment.getExternalStorageDirectory().toString()+ "/" +Utils.downloadDirectory;
        //System.out.println(SD_CARD_PATH);
        videoFile = SD_CARD_PATH + "/" + DownloadTask.downloadFileName;
        FileInputStream videoStream;
        //Read file in Internal Storage
        //FileInputStream fis;
        try {
            //videoStream = openFileInput(videoFile);
            videoStream = new FileInputStream(new File(videoFile));
            byte[] input = new byte[videoStream.available()];
            while (videoStream.read(input) != -1) {
            }
            content += new String(input);
            System.out.println("Variable content modifié");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;
    private final BluetoothAdapter mBluetoothAdapter;
    private static final UUID uuid = UUID.fromString("a60f35f0-b93a-11de-8a39-08102009c666");

    public AcceptThread(BluetoothAdapter bluetoothAdapter) {
        BluetoothServerSocket tmp = null;
        mBluetoothAdapter = bluetoothAdapter;
        try {
            //tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("SHITTYFLUTE FOR FEVER", uuid);
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("MP GETAWAY", uuid);
        } catch (IOException e) { }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            } catch(NullPointerException e) {
                break;
            }

            if (socket != null) {
                //manageConnectedSocket(socket);
                System.out.println("New Device Connected");

                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }

}

