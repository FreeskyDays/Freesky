package com.dgensolutions.freesky.freesky.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dgensolutions.freesky.freesky.R;

import java.util.Set;

import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * Created by Ganesh Kaple on 17-09-2016.
 */
public class DeviceFragment extends Fragment {
    TextView mStatustextView;
    View view;
    DeviceConnectedFragment fragmentConnected;

    // EXTRA string to send on to mainactivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static boolean isConnected = false;
    public static String Macaddress ="";


    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;


    public DeviceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.device_list, container, false);
            return view;
        }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            mStatustextView.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            if (info.equals(R.string.none_paired)) {
                Toast.makeText(getContext(),getResources().getText(R.string.none_paired).toString(), Toast.LENGTH_SHORT);
            }

            else {
                if (info.length() > 18) Macaddress = info.substring(info.length() - 17);
                else Macaddress = info;

                // Make an intent to start next activity while taking an extra which is the MAC address.
                //Intent i = new Intent(DeviceListActivity.this, sendReceiveActivity.class);
                //i.putExtra(EXTRA_DEVICE_ADDRESS, address);
                //startActivity(i);
                isConnected = true;

                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_DEVICE_ADDRESS, Macaddress);
                fragmentConnected = new DeviceConnectedFragment();
                fragmentConnected.setArguments(bundle);
                FragmentTransaction transaction = getFragmentManager().beginTransaction().addToBackStack(null);
                transaction.replace(R.id.device_list, fragmentConnected);
                transaction.commit();
            }
        }
    };
        @Override
        public void onResume () {
            super.onResume();
            checkBTState();
            mStatustextView = (TextView) view.findViewById(R.id.connecting);
            mStatustextView.setTextSize(40);
            mStatustextView.setText(" ");
            // Initialize array adapter for paired devices
            mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.device_name);
            // Find and set up the ListView for paired devices
            ListView pairedListView = (ListView) view.findViewById(R.id.paired_devices);
            pairedListView.setAdapter(mPairedDevicesArrayAdapter);
            pairedListView.setOnItemClickListener(mDeviceClickListener);
            // Get the local Bluetooth adapter
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            // Get a set of currently paired devices and append to 'pairedDevices'
            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
            // Add previosuly paired devices to the array
            if (pairedDevices.size() > 0) {
                getActivity().findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);//make title viewable
                for (BluetoothDevice device : pairedDevices) {
                    mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else {
                String noDevices = getResources().getText(R.string.none_paired).toString();
                mPairedDevicesArrayAdapter.add(noDevices); // No Device No Connection
                // Set up on-click listener for the list (nicked this - unsure)
            }
        }

    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        mBtAdapter=BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if(mBtAdapter==null) {
            Toast.makeText(getContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
}



