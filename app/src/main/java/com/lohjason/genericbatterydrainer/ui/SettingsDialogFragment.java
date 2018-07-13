package com.lohjason.genericbatterydrainer.ui;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lohjason.genericbatterydrainer.R;
import com.lohjason.genericbatterydrainer.utils.SharedPrefsUtils;

/**
 * SettingsDialogFragment
 * Created by jason on 9/7/18.
 */
public class SettingsDialogFragment extends BottomSheetDialogFragment {

    public static final  float MAX_AVAILABLE_TEMP  = 55;
    public static final  float MAX_SAFE_TEMP       = 50;
    public static final  float CHARGING_TEMP_LIMIT = 45;
    private static final float MIN_TEMP            = 40;

    TextView     tvTempValue;
    TextView     tvLevelValue;
    SeekBar      seekBarTemp;
    SeekBar      seekBarLevel;
    SwitchCompat switchUseFahrenheit;
    SwitchCompat switchResetLevel;
    TextView     tvCloseSettings;

    private MainViewModel mainViewModel;
    private SettingsViewModel settingsViewModel;

    public static SettingsDialogFragment getNewInstance() {
        return new SettingsDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainViewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel.class);
        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        setupObservers();
    }

    public void setupObservers(){
        Observer<Boolean> usesFahrenheitObserver = usesFahrenheit -> {
            if(usesFahrenheit != null){
                SharedPrefsUtils.setUsesFahrenheit(requireContext(), usesFahrenheit);
                updateTempDisplay(seekBarTemp.getProgress());
                mainViewModel.settingsUpdated(usesFahrenheit);
            }
        };
        settingsViewModel.getUsesFahrenheitLiveData().observe(this, usesFahrenheitObserver);

        Observer<Integer> batteryLevelProgressObserver = progress -> {
            if(progress != null){
                seekBarLevel.setProgress(progress);
                SharedPrefsUtils.setLevelLimit(requireContext(), progress);
            }
        };
        settingsViewModel.getBatteryLevelProgressLiveData().observe(this, batteryLevelProgressObserver);

        Observer<Integer> batteryTempProgressObserver = progress -> {
            if(progress != null){
                SharedPrefsUtils.setTempLimit(requireContext(),
                                              convertProgressToTemp(seekBarTemp.getProgress()));
            }
        };
        settingsViewModel.getBatteryTempProgressLiveData().observe(this, batteryTempProgressObserver);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final BottomSheetDialog bottomSheetDialog =
                (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        bottomSheetDialog.setOnShowListener(dialog -> {
            FrameLayout bottomSheet =
                    bottomSheetDialog.findViewById(android.support.design.R.id.design_bottom_sheet);

            if (null != bottomSheet) {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setHideable(false);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return bottomSheetDialog;
    }

    private void setupViews(View view) {
        tvTempValue = view.findViewById(R.id.tv_settings_battery_temp_seekbar_value);
        tvLevelValue = view.findViewById(R.id.tv_settings_battery_level_seekbar_value);
        seekBarTemp = view.findViewById(R.id.seekbar_settings_battery_temp);
        seekBarLevel = view.findViewById(R.id.seekbar_settings_battery_level);
        switchUseFahrenheit = view.findViewById(R.id.switch_use_fahrenheit);
        switchResetLevel = view.findViewById(R.id.switch_reset_level);
        tvCloseSettings = view.findViewById(R.id.tv_close_settings);

        if (SharedPrefsUtils.getUsesFahrenheit(requireContext())) {
            switchUseFahrenheit.setChecked(true);
        }
        if (SharedPrefsUtils.getResetLevelOnRestart(requireContext())){
            switchResetLevel.setChecked(true);
        }

        seekBarTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTempDisplay(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                settingsViewModel.setBatteryTempProgress(seekBarTemp.getProgress());
            }
        });

        seekBarLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String labelLevel = progress + "%";
                tvLevelValue.setText(labelLevel);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                settingsViewModel.setBatteryLevelProgress(seekBar.getProgress(),
                                                          mainViewModel.getLastBatteryLevel());
            }
        });

        switchUseFahrenheit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsViewModel.setUsesFahrenheit(isChecked);
        });

        switchResetLevel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPrefsUtils.setResetLevelOnRestart(requireContext(), isChecked);
        });

        tvCloseSettings.setOnClickListener(v -> dismiss());

        initLimits();
    }

    private void updateTempDisplay(int progress){
        int value = convertProgressToTemp(progress);
        if (value > MAX_SAFE_TEMP) {
            tvTempValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.material_red_600));
        } else if(value > CHARGING_TEMP_LIMIT){
            tvTempValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.material_orange_600));
        } else {
            tvTempValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.material_green_600));
        }
        String unit = "°C";

        if (SharedPrefsUtils.getUsesFahrenheit(requireContext())) {
            value = (int) Math.round(value * 1.8 + 32);
            unit = "°F";
        }
        String labelValue = value + unit;
        tvTempValue.setText(labelValue);
    }

    private void initLimits() {
        int tempLimit  = SharedPrefsUtils.getTempLimit(requireContext());
        int levelLimit = SharedPrefsUtils.getLevelLimit(requireContext());
        seekBarTemp.setProgress(convertTempToProgress(tempLimit));
        seekBarLevel.setProgress(levelLimit);
        String levelString = levelLimit + "%";
        tvLevelValue.setText(levelString);
        String tempString;

        if (SharedPrefsUtils.getUsesFahrenheit(requireContext())) {
            tempString = Math.round(tempLimit * 1.8 + 32) + "°F";
        } else {
            tempString = Math.round(tempLimit) + "°C";
        }
        tvTempValue.setText(tempString);
    }

    private int convertProgressToTemp(int progress) {
        float scale      = 100f / (MAX_AVAILABLE_TEMP - MIN_TEMP);
        float floatValue = (progress / scale) + MIN_TEMP;
        return Math.round(floatValue);
    }

    private int convertTempToProgress(int temp) {
        float scale = 100f / (MAX_AVAILABLE_TEMP - MIN_TEMP);
        return Math.round((temp - MIN_TEMP) * scale);
    }

}
