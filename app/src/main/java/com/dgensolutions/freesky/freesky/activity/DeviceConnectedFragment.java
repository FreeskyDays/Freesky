package com.dgensolutions.freesky.freesky.activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dgensolutions.freesky.freesky.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by Ganesh Kaple on 19-10-2016.
 */

public class DeviceConnectedFragment extends Fragment {

        public static final String TAG = "com.dgensolutions.freesky.freesky.deviceconnectedfragment";
        Button btnOn, btnOff;
        View view;
        TextView txtArduino, txtString, txtStringLength, sensorView;
        Handler bluetoothIn;
        int flag;//little hack
        int dataLength;//hack
        final int handlerState = 0;                        //used to identify handler message
        private BluetoothAdapter btAdapter = null;
        private BluetoothSocket btSocket = null;
        private StringBuilder recDataString = new StringBuilder();

        private ConnectedThread mConnectedThread;

        // SPP UUID service - this should work for most devices
        private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        // String for MAC address
        private static String address;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
                address = savedInstanceState.getString(DeviceFragment.EXTRA_DEVICE_ADDRESS);
            }
            else address = DeviceFragment.Macaddress;
            initializeDevice();

            btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
            checkBTState();
            //mConnectedThread.write(time);


            // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
            /*btnOff.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mConnectedThread.write("0");    // Send "0" via Bluetooth
                    Toast.makeText(getContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
                }
            });

            btnOn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mConnectedThread.write("1");    // Send "1" via Bluetooth
                    Toast.makeText(getContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
                }
            }); */
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.device_list, container, false);
        return view;
    }
    public DeviceConnectedFragment() {
        // Required empty public constructor
    }


    private void initializeDevice() {
        String response;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat sdf2 = new SimpleDateFormat("hh: mm a");
        String time = sdf2.format(calendar.getTime());
        String date = sdf.format(calendar.getTime());
           /* if (calendar.get(PM) == calendar.get(Calendar.AM_PM))
                time = ":" + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + ": PM :";
            else
                time = ":" + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + ": AM :";

            String date = "," + calendar.get(Calendar.DAY_OF_MONTH) + "," + calendar.get(Calendar.MONTH) + "," + calendar.get(Calendar.YEAR) + ":";
*/

        setupStream();
        sendDate(time, date);

    }

    private void sendDate(String time, String date) {
        String response;
        if (mConnectedThread != null) {
            mConnectedThread.write(time);//Calendar calendar = new GregorianCalendar();
            response = readData();
            if (response.equals("ok")) mConnectedThread.write(date);
            else mConnectedThread.write("not ok");
        }
        else {
            Toast.makeText(getContext(),"thread not ready", Toast.LENGTH_SHORT);
        }
    }

    private String readData() {
        flag = -1;
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                     // determine the end-of-line
                    int endOfLineIndex = recDataString.indexOf("\n");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        txtString.setText("Data Received = " + dataInPrint);
                        recDataString.delete(0,endOfLineIndex);
                        if (recDataString.equals("ok")) flag = 1;
                        else  {
                                flag = 0;
                                dataLength = dataInPrint.length();                          //get length of data received

                            }
                        return;

                    }
                }
            }
        };
        if (flag == 1) return "ok";
        else if (flag == 0) return "length received: " + dataLength;
        else return "nothing received";

    }

   private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
           return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
            //creates secure outgoing connecetion with BT device using UUID
        }

        @Override
        public void onResume() {
            super.onResume();

            //Get MAC address from DeviceListActivity via intent
//            Intent intent = getIntent();

            //Get the MAC address from the DeviceListActivty via EXTRA
  //          address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            setupStream();


        }

    protected void setupStream() {
        //create device and set the MAC address
        if (btAdapter == null) {
            Toast.makeText(getContext(),"Adapter not set" ,Toast.LENGTH_SHORT);
        }
        else {
            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            try {
                btSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
            }
            // Establish the Bluetooth socket connection.
            try {
                btSocket.connect();
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    //insert code to deal with this
                }
            }
            mConnectedThread = new ConnectedThread(btSocket);
            mConnectedThread.start();

            //I send a character when resuming.beginning transmission to check device is connected
            //If it is not an exception will be thrown in the write method and finish() will be called
            mConnectedThread.write("x");
        }
    }

    @Override
        public void onPause() {
            super.onPause();
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }

        //Checks that the Android device Bluetooth is available and prompts to be turned on if off
        private void checkBTState() {

            if (btAdapter == null) {
                Toast.makeText(getContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
            } else {
                if (btAdapter.isEnabled()) {
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }
            }
        }

        //create new class for connect thread
        private class ConnectedThread extends Thread {
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;
            int bytes;
            String readMessage;


            //creation of the connect thread
            public ConnectedThread(BluetoothSocket socket) {
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                try {
                    //Create I/O streams for connection
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }

            @SuppressLint("LongLogTag")
            public void run() {
                byte[] buffer = new byte[256];

                // Keep looping to listen for received messages
                while (true) {
                    try {
                        bytes = mmInStream.read(buffer);            //read bytes from input buffer
                        readMessage = new String(buffer, 0, bytes);
                        Log.d(TAG, readMessage);

                        final String dataBytes = readMessage;
                        // Send the obtained bytes to the UI Activity via handler
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //sensorView.setText(readMessage);
                                if (bluetoothIn != null) {
                                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();

                                }
                                else {
                                    Log.d(TAG,"blutooth in is null");
                                }
                                     }
                        });

                    } catch (IOException e) {
                        Log.d(TAG, "Exception :" + e);

                        break;
                    }
                }
            }

            //write method
            public void write(String input) {
                byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
                try {
                    mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
                } catch (IOException e) {
                    //if you cannot write, close the application
                    Toast.makeText(getContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                    //finish();

                }
            }
        }
    }


