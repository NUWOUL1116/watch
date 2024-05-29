package com.tm.watch;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothActivity";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private UUID uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 블루투스 초기화 및 연결
        initializeBluetooth();
        connectToDevice();
        receiveData();
    }

    // 블루투스 어댑터 초기화
    private void initializeBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported on this device");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth가 비활성화된 경우 사용자에게 활성화를 요청할 수 있습니다.
            // Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    // 블루투스 디바이스 찾기 및 연결
    private void connectToDevice() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("AI헬스")) { // 디바이스 이름으로 검색
                    bluetoothDevice = device;
                    break;
                }
            }
        }

        if (bluetoothDevice != null) {
            try {
                uuid = bluetoothDevice.getUuids()[0].getUuid();
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                inputStream = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to device", e);
            }
        }
    }

    // 블루투스 데이터 수신
    private void receiveData() {
        if (inputStream != null) {
            new Thread(() -> {
                byte[] buffer = new byte[1024];
                int bytes;

                while (true) {
                    try {
                        bytes = inputStream.read(buffer);
                        String data = new String(buffer, 0, bytes);
                        Log.d(TAG, "Received data: " + data);

                        // UI 업데이트가 필요한 경우 Handler를 사용합니다.
                        runOnUiThread(() -> {
                            // UI 업데이트 코드 (예: TextView에 수신된 데이터 표시)
                        });
                    } catch (IOException e) {
                        Log.e(TAG, "Error receiving data", e);
                        break;
                    }
                }
            }).start();
        }
    }
}
