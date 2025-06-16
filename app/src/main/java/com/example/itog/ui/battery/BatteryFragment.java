package com.example.itog.ui.battery;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;

import com.example.itog.R;
import com.example.itog.databinding.FragmentBatteryBinding;

public class BatteryFragment extends Fragment {

    private FragmentBatteryBinding binding;
    private BluetoothAdapter btAdapter;
    private ImageButton bluetoothButton;
    private ImageButton replacementButton; //иниц

    private static final int REQUEST_ENABLE_BT = 15;

    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBluetoothIcon();
        }
    };

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBatteryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        BluetoothManager bluetoothManager = (BluetoothManager) requireContext().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();

        bluetoothButton = root.findViewById(R.id.bluetoothBT);
        replacementButton = root.findViewById(R.id.replacementBT);
        setupBluetoothButton();

        return root;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void setupBluetoothButton() {
        updateBluetoothIcon();
        bluetoothButton.setOnClickListener(v -> toggleBluetooth());
        replacementButton.setOnClickListener(v -> toggleReplacement());
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void toggleBluetooth() {
        if (btAdapter.isEnabled()) {
            btAdapter.disable();
            Toast.makeText(getContext(), "Bluetooth выключен", Toast.LENGTH_SHORT).show();
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void updateBluetoothIcon() {
        if (btAdapter.isEnabled()) {
            bluetoothButton.setImageResource(R.drawable.bluetooth_alt);
        } else {
            bluetoothButton.setImageResource(R.drawable.bluetooth_disabled);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            updateBluetoothIcon();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().registerReceiver(
                btReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(btReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void toggleReplacement() {
        if (btAdapter.isEnabled()){

        } else {
            Toast.makeText(getContext(), "Включите Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }
}