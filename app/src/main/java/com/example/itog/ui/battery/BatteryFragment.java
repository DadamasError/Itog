package com.example.itog.ui.battery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.itog.R;
import com.example.itog.databinding.FragmentBatteryBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BatteryFragment extends Fragment {
    private static final String TAG = "BatteryFragment";
    private static final int MAX_DISCOVERY_ATTEMPTS = 3;

    private FragmentBatteryBinding binding;
    private BluetoothAdapter btAdapter;
    private ImageButton bluetoothButton;
    private ImageButton devicesButton;
    private ArrayList<String> activeDevicesList = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private AlertDialog devicesDialog;
    private ProgressBar progressBar;
    private TextView statusText;
    private final HashSet<String> discoveredDevices = new HashSet<>();
    private final Set<BluetoothDevice> bondedDevices = new HashSet<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Счетчик попыток сканирования
    private int discoveryAttempts = 0;

    // Ресивер для состояния Bluetooth
    private final BroadcastReceiver btStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBluetoothIcon();
        }
    };

    // Ресивер для обнаружения устройств
    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getAddress() != null) {
                    handleDiscoveredDevice(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                completeDiscovery();
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBatteryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Инициализация Bluetooth
        BluetoothManager bluetoothManager = (BluetoothManager) requireContext().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            btAdapter = bluetoothManager.getAdapter();
        }

        if (btAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth не поддерживается", Toast.LENGTH_SHORT).show();
            return root;
        }

        // Кнопка Bluetooth
        bluetoothButton = root.findViewById(R.id.bluetoothBT);
        setupBluetoothButton();

        // Кнопка сканирования устройств
        devicesButton = root.findViewById(R.id.imageButton);
        devicesButton.setOnClickListener(v -> {
            if (checkBluetoothPermissions()) {
                startDeviceDiscovery();
            } else {
                requestBluetoothPermissions();
            }
        });

        // Адаптер для списка устройств
        listAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                activeDevicesList
        );

        return root;
    }

    private boolean checkBluetoothPermissions() {
        if (btAdapter == null || !btAdapter.isEnabled()) {
            return false;
        }

        // Для Android 12+ требуются новые разрешения
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        }

        // Для старых версий достаточно ACCESS_FINE_LOCATION
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestBluetoothPermissions() {
        if (btAdapter == null || !btAdapter.isEnabled()) {
            Toast.makeText(getContext(), "Сначала включите Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        // Для Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    2
            );
        }
        // Для старых версий
        else {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    2
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                startDeviceDiscovery();
            } else {
                Toast.makeText(getContext(), "Разрешения необходимы для поиска устройств", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startDeviceDiscovery() {
        if (btAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth не поддерживается", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!btAdapter.isEnabled()) {
            Toast.makeText(getContext(), "Включите Bluetooth", Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
            return;
        }

        if (!checkBluetoothPermissions()) {
            requestBluetoothPermissions();
            return;
        }

        // Сброс счетчика попыток
        discoveryAttempts = 0;

        // Очистка предыдущих результатов
        activeDevicesList.clear();
        discoveredDevices.clear();
        bondedDevices.clear();
        bondedDevices.addAll(btAdapter.getBondedDevices());

        // Создание диалога
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Поиск активных устройств");

        // Используем отдельный layout для диалога
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_device_discovery, null);
        progressBar = dialogView.findViewById(R.id.progress_bar);
        statusText = dialogView.findViewById(R.id.status_text);
        ListView listView = dialogView.findViewById(R.id.devices_list);
        listView.setAdapter(listAdapter);

        builder.setView(dialogView);
        builder.setPositiveButton("ОК", (dialog, id) -> {
            stopDiscovery();
            dialog.dismiss();
        });
        builder.setNegativeButton("Отмена", (dialog, id) -> {
            stopDiscovery();
            dialog.dismiss();
        });
        builder.setOnDismissListener(dialog -> stopDiscovery());

        devicesDialog = builder.create();
        devicesDialog.show();

        // Обновление статуса
        statusText.setText("Сканирование...");
        progressBar.setVisibility(View.VISIBLE);

        // Регистрация ресиверов
        registerReceivers();

        // Отмена текущего сканирования
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
            // Увеличенная задержка перед повторной попыткой
            handler.postDelayed(this::attemptDiscoveryStart, 1000);
        } else {
            attemptDiscoveryStart();
        }
    }

    @SuppressLint("MissingPermission")
    private void attemptDiscoveryStart() {
        if (discoveryAttempts >= MAX_DISCOVERY_ATTEMPTS) {
            statusText.setText("Превышено количество попыток");
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Не удалось начать сканирование после " + MAX_DISCOVERY_ATTEMPTS + " попыток");
            return;
        }

        try {
            if (!btAdapter.isEnabled()) {
                statusText.setText("Bluetooth отключен");
                progressBar.setVisibility(View.GONE);
                return;
            }

            boolean started = btAdapter.startDiscovery();
            if (started) {
                Log.d(TAG, "Сканирование Bluetooth успешно запущено (попытка " + (discoveryAttempts + 1) + ")");
                handler.postDelayed(this::completeDiscovery, 10000); // 10 секунд таймаут
            } else {
                discoveryAttempts++;
                Log.e(TAG, "Ошибка запуска сканирования (попытка " + (discoveryAttempts + 1) + ")");
                statusText.setText("Ошибка: не удалось начать сканирование");

                // Повторная попытка через увеличенное время
                handler.postDelayed(this::attemptDiscoveryStart, 2000 * discoveryAttempts);
            }
        } catch (SecurityException e) {
            statusText.setText("Ошибка: недостаточно прав");
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "SecurityException при запуске сканирования: " + e.getMessage());
        } catch (Exception e) {
            statusText.setText("Ошибка: " + e.getMessage());
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Общая ошибка при запуске сканирования: " + e.getMessage());
        }
    }

    private void registerReceivers() {
        try {
            // Ресивер для состояния Bluetooth
            IntentFilter stateFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            requireActivity().registerReceiver(btStateReceiver, stateFilter);

            // Ресивер для обнаружения устройств
            IntentFilter discoveryFilter = new IntentFilter();
            discoveryFilter.addAction(BluetoothDevice.ACTION_FOUND);
            discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            requireActivity().registerReceiver(discoveryReceiver, discoveryFilter);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка регистрации ресиверов: " + e.getMessage());
        }
    }

    private void unregisterReceivers() {
        try {
            requireActivity().unregisterReceiver(btStateReceiver);
            requireActivity().unregisterReceiver(discoveryReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Ресивер не был зарегистрирован", e);
        }
    }

    @SuppressLint("MissingPermission")
    private void handleDiscoveredDevice(BluetoothDevice device) {
        if (device == null || device.getAddress() == null) {
            Log.w(TAG, "Получено устройство с null адресом");
            return;
        }

        String deviceAddress = device.getAddress();

        // Игнорируем дубликаты
        if (discoveredDevices.contains(deviceAddress)) {
            return;
        }

        discoveredDevices.add(deviceAddress);

        // Показываем только сопряженные и активные устройства
        if (isBondedDevice(device)) {
            String deviceName = device.getName() != null ? device.getName() : "Неизвестное устройство";
            String deviceInfo = deviceName + "\n" + deviceAddress;
            activeDevicesList.add(deviceInfo);
            listAdapter.notifyDataSetChanged();
        }
    }

    private boolean isBondedDevice(BluetoothDevice device) {
        for (BluetoothDevice bondedDevice : bondedDevices) {
            if (bondedDevice.getAddress().equals(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    private void completeDiscovery() {
        if (btAdapter != null && btAdapter.isDiscovering()) {
            try {
                btAdapter.cancelDiscovery();
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException при отмене сканирования: " + e.getMessage());
            }
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (statusText != null) {
            if (activeDevicesList.isEmpty()) {
                statusText.setText("Активные устройства не найдены");
            } else {
                statusText.setText("Найдено устройств: " + activeDevicesList.size());
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void stopDiscovery() {
        // Отменяем все отложенные задачи
        handler.removeCallbacksAndMessages(null);

        if (btAdapter != null) {
            try {
                if (btAdapter.isDiscovering()) {
                    btAdapter.cancelDiscovery();
                }
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException при отмене сканирования", e);
            }
        }

        unregisterReceivers();
    }

    @SuppressLint("MissingPermission")
    private void setupBluetoothButton() {
        updateBluetoothIcon();
        bluetoothButton.setOnClickListener(v -> toggleBluetooth());
    }

    @SuppressLint("MissingPermission")
    private void toggleBluetooth() {
        if (btAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth не поддерживается", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btAdapter.isEnabled()) {
            try {
                btAdapter.disable();
                Toast.makeText(getContext(), "Bluetooth выключен", Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                Toast.makeText(getContext(), "Ошибка: недостаточно прав для выключения Bluetooth", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "SecurityException при выключении Bluetooth: " + e.getMessage());
            }
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    private void updateBluetoothIcon() {
        if (btAdapter != null && btAdapter.isEnabled()) {
            bluetoothButton.setImageResource(R.drawable.bluetooth_alt);
        } else {
            bluetoothButton.setImageResource(R.drawable.bluetooth_disabled);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (btAdapter != null && btAdapter.isEnabled()) {
                    if (checkBluetoothPermissions()) {
                        startDeviceDiscovery();
                    } else {
                        requestBluetoothPermissions();
                    }
                } else {
                    Toast.makeText(getContext(), "Bluetooth должен быть включен для сканирования", Toast.LENGTH_SHORT).show();
                }
            }, 1500); // Задержка 1.5 секунды
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            requireActivity().registerReceiver(
                    btStateReceiver,
                    new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            );
        } catch (Exception e) {
            Log.e(TAG, "Ошибка регистрации ресивера состояния Bluetooth", e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopDiscovery();
        if (devicesDialog != null && devicesDialog.isShowing()) {
            devicesDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        stopDiscovery();
    }
}