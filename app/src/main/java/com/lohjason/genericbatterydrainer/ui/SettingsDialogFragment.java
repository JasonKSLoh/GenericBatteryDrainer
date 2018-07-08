package com.lohjason.genericbatterydrainer.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lohjason.genericbatterydrainer.R;
import com.lohjason.genericbatterydrainer.utils.SharedPrefsUtils;

/**
 * SettingsDialogFragment
 * Created by jason on 9/7/18.
 */
public class SettingsDialogFragment extends BottomSheetDialogFragment {

    public static final int MAX_AVAILABLE_TEMP = 55;
    public static final int MAX_SAFE_TEMP = 50;
    private static final int MIN_TEMP = 40;

    TextView tvTempValue;
    TextView tvLevelValue;
    SeekBar seekBarTemp;
    SeekBar seekBarLevel;
    TextView tvSaveSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings_dialog, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
    }

    private void setupViews(View view){
        tvTempValue = view.findViewById(R.id.tv_settings_battery_temp_seekbar_value);
        tvLevelValue = view.findViewById(R.id.tv_settings_battery_level_seekbar_value);
        seekBarTemp = view.findViewById(R.id.seekbar_settings_battery_temp);
        seekBarLevel = view.findViewById(R.id.seekbar_settings_battery_level);
        tvSaveSettings = view.findViewById(R.id.tv_save_settings);

        seekBarTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = convertProgressToTemp(progress);

                String labelValue = value + "°C";
                tvTempValue.setText(labelValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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

            }
        });

        tvSaveSettings.setOnClickListener(v -> saveSettings());

        int tempLimit = SharedPrefsUtils.getTempLimit(getContext());
        int levelLimit = SharedPrefsUtils.getLevelLimit(getContext());
        seekBarTemp.setProgress(convertTempToProgress(tempLimit));
        seekBarLevel.setProgress(levelLimit);
        tvTempValue.setText(tempLimit + "°C");
        tvLevelValue.setText(levelLimit + "%");
    }

    private void saveSettings(){
        SharedPrefsUtils.setLevelLimit(requireContext(), seekBarLevel.getProgress());
        SharedPrefsUtils.setTempLimit(requireContext(), convertProgressToTemp(seekBarTemp.getProgress()));

        Toast.makeText(requireContext(), "Settings Saved", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private int convertProgressToTemp(int progress){
        float scale = 100f / ((float)MAX_AVAILABLE_TEMP - (float)MIN_TEMP);
        float floatValue = (progress / scale) + MIN_TEMP;
        return Math.round(floatValue);
    }

    private int convertTempToProgress(int temp){
        float scale = 100f / ((float)MAX_AVAILABLE_TEMP - (float)MIN_TEMP);
        return Math.round((temp - MIN_TEMP) * scale);
    }

}
