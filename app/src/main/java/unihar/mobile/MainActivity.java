package unihar.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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

import unihar.mobile.model.AutoencoderModelHelper;
import unihar.mobile.model.ModelHelper;
import unihar.mobile.model.RecognizerModelHelper;
import unihar.mobile.sensor.SensorCollector;

public class MainActivity extends AppCompatActivity{



    private EditText recordIDText;
    private TextView infoView;
    private Button recordBtn;
    private Button cancelBtn;
    private RadioGroup labelRadioGroup;
    private RadioGroup labelRadioGroupOther;

    private int recordNum;
    private SensorCollector sensorCollector;
    private boolean recorded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recoverRecordNum(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        Utils.checkPermission(this);
        sensorCollector = new SensorCollector(this);

        Button testAEBtn = findViewById(R.id.test_ae);
        testAEBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModelHelper autoencoderModelHelper = new AutoencoderModelHelper(MainActivity.this);
                autoencoderModelHelper.initFromAsset("ae.tflite");
                autoencoderModelHelper.train(Utils.randomFloat3Array(new int[]{64, 20, 6}), 1);
            }
        });

        Button testREBtn = findViewById(R.id.test_re);
        testREBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModelHelper recognizerModelHelper = new RecognizerModelHelper(MainActivity.this);
                recognizerModelHelper.initFromAsset("re.tflite");
                int[] inferLabels = recognizerModelHelper.infer(Utils.randomFloat3Array(new int[]{64, 20, 6}));
            }
        });


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
                if(sensorCollector.save(recordText)){
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
                recordBtn.setText(getString(R.string.btn_start_off));
                recordBtn.setBackgroundColor(getColor(R.color.btn_start_off));
                infoView.setText(recordText + " start.");

            }
        });
        cancelBtn.setOnClickListener(v -> {
            if(recorded){
                recorded = false;
                sensorCollector.stop();
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
            int label = Config.LABEL_ACTIVITY_NONE;
            switch (id) {
                case R.id.label_none:
                    label = Config.LABEL_ACTIVITY_NONE;
                    clearLabelGroup(labelRadioGroupOther);
                    break;
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
            if(recorded) sensorCollector.updateLabel(label);
        }
    };
    private void clearLabelGroup(RadioGroup group){
        group.setOnCheckedChangeListener(null);
        group.clearCheck();
        group.setOnCheckedChangeListener(labelChangeListener);
    }
}