package unihar.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {



    private EditText recordIDText;
    private TextView infoView;
    private Button recordBtn;
    private Button cancelBtn;
    private RadioGroup labelRadioGroup;
    private RadioGroup labelRadioGroupOther;

    private SensorManager sensorManager;
    private SensorRecorder sensorRecorder;


    private int recordNum;
    private boolean recorded = false;
    private int label = Config.LABEL_ACTIVITY_STILL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recoverRecordNum(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.i("Available Sensors", deviceSensors.toString());
        sensorRecorder = new SensorRecorder(null);
        registerSensorListener();
        Utils.checkPermission(this);
    }

    private void initView()
    {
        setContentView(R.layout.activity_main);
        recordIDText = this.findViewById(R.id.record_id);
        recordIDText.setText(Utils.unifyIDText(recordNum));
        recordBtn = this.findViewById(R.id.record);
        cancelBtn = this.findViewById(R.id.cancel);
        infoView = this.findViewById(R.id.info);
        labelRadioGroup = this.findViewById(R.id.label);
        labelRadioGroupOther = this.findViewById(R.id.label_other);
        bindViewListener();
    }

    private void bindViewListener()
    {
        recordIDText.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                            actionId == EditorInfo.IME_ACTION_DONE ||
                            event != null &&
                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed()) {
                            String input = recordIDText.getText().toString();
                            if (input.matches("-?\\d+")){
                                recordNum = Integer.parseInt(input);
                                recordIDText.setText(Utils.unifyIDText(recordNum));
                                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                recordIDText.clearFocus();
                            }else {
                                Toast toast= Toast.makeText(MainActivity.this,"Input invalid.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                            return true; // consume.
                        }
                    }
                    return false; // pass on to other listeners.
                }
        );
        recordBtn.setOnClickListener(v -> {
            String recordText = Utils.unifyIDText(recordNum);
            if(recorded){
                recorded = false;
                Log.i("Path", Utils.dataFolderPath(recordText));
                if(sensorRecorder.saveAllData(Utils.dataFolderPath(recordText))){
                    infoView.setText(recordText + " saved successfully!");
                }else {
                    infoView.setText(recordText + " saved failed! Some file may not be saved successfully.");
                }
                recordBtn.setText(getString(R.string.btn_start_on));
                recordBtn.setBackgroundColor(getColor(R.color.btn_start_on));
                recordNum++;
                recordIDText.setText(recordText);
            }else {
                sensorRecorder.addLabelItem(label);
                recorded = true;
                recordBtn.setText(getString(R.string.btn_start_off));
                recordBtn.setBackgroundColor(getColor(R.color.btn_start_off));
                infoView.setText(recordText + " start.");

            }
        });
        cancelBtn.setOnClickListener(v -> {
            if(recorded){
                recorded = false;
                sensorRecorder.clearSensorData();
                infoView.setText(Utils.unifyIDText(recordNum) + " cancelled.");
                recordBtn.setText(getString(R.string.btn_start_on));
                recordBtn.setBackgroundColor(getColor(R.color.btn_start_on));
            }else {
                Toast toast= Toast.makeText(MainActivity.this,"Recorder is not working", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        labelRadioGroup.setOnCheckedChangeListener(labelChangeListener);
        labelRadioGroupOther.setOnCheckedChangeListener(labelChangeListener);
    }

    private void registerSensorListener()
    {
        for (int i = 0; i < Config.SENSOR_LISTS.size(); i++){
            Sensor sensor = sensorManager.getDefaultSensor(Config.SENSOR_LISTS.get(i));
            if(sensor != null) {
                sensorManager.registerListener(this, sensor, Config.SAMPLE_RATE);
                sensorRecorder.initSensorSet(Config.SENSOR_LISTS.get(i), Config.SENSOR_NAME_LISTS.get(i));
            }
            else {
                Log.w("Sensor Missing", Config.SENSOR_NAME_LISTS.get(i));
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(recorded) {
            sensorRecorder.addSensorItem(sensorEvent.sensor.getType(), sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void recoverRecordNum(Bundle savedInstanceState){
        String date = Config.DATE_FORMAT.format(new Date());
        if(savedInstanceState != null && savedInstanceState.getInt(date) != 0) {
            recordNum = savedInstanceState.getInt(date);
        }else{
            recordNum = 1;
        }
    }

    private final RadioGroup.OnCheckedChangeListener labelChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int id = group.getCheckedRadioButtonId();
            switch (id) {
                case R.id.label_still:
                    label = Config.LABEL_ACTIVITY_STILL;
                    clearLabelGroup(labelRadioGroupOther);
                    break;
                case R.id.label_walk:
                    label = Config.LABEL_ACTIVITY_WALKING;
                    clearLabelGroup(labelRadioGroupOther);
                    break;
                case R.id.label_upstairs:
                    label = Config.LABEL_ACTIVITY_UPSTAIRS;
                    clearLabelGroup(labelRadioGroupOther);
                    break;
                case R.id.label_downstairs:
                    label = Config.LABEL_ACTIVITY_DOWNSTAIRS;
                    clearLabelGroup(labelRadioGroupOther);
                    break;
                case R.id.label_jump:
                    label = Config.LABEL_ACTIVITY_JUMP;
                    clearLabelGroup(labelRadioGroup);
                    break;
                default:
                    Log.w("Unknown Label", id + "");
                    break;
            }
            if(recorded) sensorRecorder.addLabelItem(label);
        }
    };
    private void clearLabelGroup(RadioGroup group){
        group.setOnCheckedChangeListener(null);
        group.clearCheck();
        group.setOnCheckedChangeListener(labelChangeListener);
    }
}