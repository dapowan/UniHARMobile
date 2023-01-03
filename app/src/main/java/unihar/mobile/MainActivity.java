package unihar.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Hashtable;

import unihar.mobile.model.ModelManger;
import unihar.mobile.sensor.SensorCollector;

public class MainActivity extends AppCompatActivity{

    private TextView accView_x, accView_y, accView_z;
    private TextView gyrView_x, gyrView_y, gyrView_z;

    private EditText recordIDText;
    private TextView infoView;
    private Button recordBtn;
    private Button cancelBtn;
//    private RadioGroup labelRadioGroup;
//    private RadioGroup labelRadioGroupOther;

    private int recordNum;
    private SensorCollector sensorCollector;
    private ModelManger modelManger;
    private boolean recorded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recoverRecordNum(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        Utils.checkPermission(this);
        sensorCollector = new SensorCollector(this);
        modelManger = new ModelManger(this);
        Button testAEBtn = findViewById(R.id.autoencoder_train);
        testAEBtn.setOnClickListener(v -> {
            Hashtable<Integer, float[][]> readings = sensorCollector.latestSensorReadings(Config.SEQUENCE_LENGTH * Config.BATCH_SIZE);
            String lossInfo = modelManger.trainAutoencoder(readings, Config.EPOCH_NUM_TEST);
            infoView.setText(String.format("Aotuoencoder Training: %s.", lossInfo));
        });

        Button testREBtn = findViewById(R.id.recognizer_infer);
        testREBtn.setOnClickListener(v -> {
            Hashtable<Integer, float[][]> readings = sensorCollector.latestSensorReadings(Config.SEQUENCE_LENGTH);
            String activityInfo = modelManger.inferRealTimeActivity(readings);
            infoView.setText(String.format("Recognizer Inferece: %s.", activityInfo));
        });


    }

    private void initView()
    {
        setContentView(R.layout.activity_main);
        accView_x = this.findViewById(R.id.acc_x);
        accView_y = this.findViewById(R.id.acc_y);
        accView_z = this.findViewById(R.id.acc_z);
        gyrView_x = this.findViewById(R.id.gyr_x);
        gyrView_y = this.findViewById(R.id.gyr_y);
        gyrView_z = this.findViewById(R.id.gyr_z);
        recordIDText = this.findViewById(R.id.record_id);
        recordIDText.setText(Utils.unifyIDText(recordNum));
        recordBtn = this.findViewById(R.id.record);
        cancelBtn = this.findViewById(R.id.cancel);
        infoView = this.findViewById(R.id.info);
//        labelRadioGroup = this.findViewById(R.id.label);
//        labelRadioGroupOther = this.findViewById(R.id.label_other);
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
                if(sensorCollector.stopRecord(recordText)){
                    infoView.setText(recordText + " saved successfully!");
                }else {
                    infoView.setText(recordText + " saved failed! Some file may not be saved successfully.");
                }
                recordBtn.setText(getString(R.string.btn_start_on));
                recordBtn.setBackgroundColor(getColor(R.color.btn_start_on));
                recordNum++;
                recordIDText.setText(recordText);
            }else {
                recorded = true;
                sensorCollector.startRecord();
                recordBtn.setText(getString(R.string.btn_start_off));
                recordBtn.setBackgroundColor(getColor(R.color.btn_start_off));
                infoView.setText(recordText + " start.");

            }
        });
        cancelBtn.setOnClickListener(v -> {
            if(recorded){
                recorded = false;
                sensorCollector.cancelRecord();
                infoView.setText(Utils.unifyIDText(recordNum) + " cancelled.");
                recordBtn.setText(getString(R.string.btn_start_on));
                recordBtn.setBackgroundColor(getColor(R.color.btn_start_on));
            }else {
                Toast toast= Toast.makeText(MainActivity.this,"Recorder is not working", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
//        labelRadioGroup.setOnCheckedChangeListener(labelChangeListener);
//        labelRadioGroupOther.setOnCheckedChangeListener(labelChangeListener);
    }

    public void setSensorText(int sensorType, float[] data){
        switch (sensorType) {
            case Config.SENSOR_ACCELEROMETER:
                accView_x.setText(Utils.unifyNumberText(data[0]));
                accView_y.setText(Utils.unifyNumberText(data[1]));
                accView_z.setText(Utils.unifyNumberText(data[2]));
                break;
            case Config.SENSOR_GYROSCOPE:
                gyrView_x.setText(Utils.unifyNumberText(data[0]));
                gyrView_y.setText(Utils.unifyNumberText(data[1]));
                gyrView_z.setText(Utils.unifyNumberText(data[2]));
                break;
            default:
                break;
        }
    }

    private void recoverRecordNum(Bundle savedInstanceState){
        String date = Config.DATE_FORMAT.format(new Date());
        if(savedInstanceState != null && savedInstanceState.getInt(date) != 0) {
            recordNum = savedInstanceState.getInt(date);
        }else{
            recordNum = 1;
        }
    }

//    private final RadioGroup.OnCheckedChangeListener labelChangeListener = new RadioGroup.OnCheckedChangeListener() {
//        @Override
//        public void onCheckedChanged(RadioGroup group, int checkedId) {
//            int id = group.getCheckedRadioButtonId();
//            int label = Config.LABEL_ACTIVITY_NONE;
//            switch (id) {
//                case R.id.label_none:
//                    label = Config.LABEL_ACTIVITY_NONE;
//                    clearLabelGroup(labelRadioGroupOther);
//                    break;
//                case R.id.label_still:
//                    label = Config.LABEL_ACTIVITY_STILL;
//                    clearLabelGroup(labelRadioGroupOther);
//                    break;
//                case R.id.label_walk:
//                    label = Config.LABEL_ACTIVITY_WALKING;
//                    clearLabelGroup(labelRadioGroupOther);
//                    break;
//                case R.id.label_upstairs:
//                    label = Config.LABEL_ACTIVITY_UPSTAIRS;
//                    clearLabelGroup(labelRadioGroupOther);
//                    break;
//                case R.id.label_downstairs:
//                    label = Config.LABEL_ACTIVITY_DOWNSTAIRS;
//                    clearLabelGroup(labelRadioGroupOther);
//                    break;
//                case R.id.label_jump:
//                    label = Config.LABEL_ACTIVITY_JUMP;
//                    clearLabelGroup(labelRadioGroup);
//                    break;
//                default:
//                    Log.w("Unknown Label", id + "");
//                    break;
//            }
//            if(recorded) sensorCollector.updateLabel(label);
//        }
//    };
//
//    private void clearLabelGroup(RadioGroup group){
//        group.setOnCheckedChangeListener(null);
//        group.clearCheck();
//        group.setOnCheckedChangeListener(labelChangeListener);
//    }
}