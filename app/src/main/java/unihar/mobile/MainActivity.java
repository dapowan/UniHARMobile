package unihar.mobile;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

import static unihar.mobile.Utils.getNum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import unihar.mobile.model.ModelManger;
import unihar.mobile.network.Cacher;
import unihar.mobile.network.GetFileTask;
import unihar.mobile.network.GetJSONTask;
import unihar.mobile.network.PostFileTask;
import unihar.mobile.sensor.SensorCollector;
import unihar.mobile.sensor.SensorLoader;

public class MainActivity extends AppCompatActivity{

    private TextView accView_x, accView_y, accView_z;
    private TextView gyrView_x, gyrView_y, gyrView_z;

    private EditText recordIDText;
    private Button recordBtn;
    private Button cancelBtn;
    private Button autoencoderBtn;
    private Button recognizerBtn;
    private Button networkUpdateBtn;
    private Button networkUploadBtn;
    private Button activateBtn;
    private TextView manualInfoView;
    private TextView autoInfoView;

    private int recordNum;
    private SensorCollector sensorCollector;
    private SensorLoader sensorLoader;
    private ModelManger modelManger;
    private int mode = Config.MODE_NONE;

    private Handler autoSensorHandler;
    private AutoSensorTask autoSensorTask;
    private Handler autoModelHandler;
    private AutoModelTask autoModelTask;
    private long lastIdleTimeTag;

    private int trainingSize;

    private Cacher cacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recoverRecordNum(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        Utils.checkPermission(this);
        cacher = new Cacher(this);
        sensorCollector = new SensorCollector(this);
        sensorLoader = new SensorLoader();
        modelManger = new ModelManger(this);
        autoSensorHandler = new Handler();
        autoModelHandler = new Handler();
        resetIdleTime();
        initData();
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
        networkUpdateBtn = this.findViewById(R.id.btn_net_update);
        networkUploadBtn = this.findViewById(R.id.btn_net_upload);
        manualInfoView = this.findViewById(R.id.info_manual);
        manualInfoView.setMovementMethod(new ScrollingMovementMethod());
        autoencoderBtn = findViewById(R.id.autoencoder_train);
        recognizerBtn = findViewById(R.id.recognizer_infer);
        activateBtn = findViewById(R.id.activate);
        autoInfoView = findViewById(R.id.info_auto);
        autoInfoView.setMovementMethod(new ScrollingMovementMethod());
        bindViewListener();
    }

    private void initData(){
        String config = cacher.getString(Cacher.NAME_CONFIG);
        if (config == null){
            new FetchConfigTask().execute(Config.URL_CONFIG);
        }else{
            try {
                Config.getInstance().load(new JSONObject(config));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
                                Toast toast= Toast.makeText(MainActivity.this,"Invalid Input.", Toast.LENGTH_SHORT);
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
            if (mode == Config.MODE_AUTO){
                Toast toast= Toast.makeText(MainActivity.this,"Auto mode is still working.", Toast.LENGTH_SHORT);
                toast.show();
            }else if(mode == Config.MODE_MANUAL){
                mode = Config.MODE_NONE;
                if(sensorCollector.stopRecord(String.format("manual-%s", recordText))){
                    displayInfo(manualInfoView, String.format("%s saved successfully!", recordText));
//                    manualInfoView.setText(String.format("%s saved successfully!", recordText));
                }else {
                    displayInfo(manualInfoView, String.format("%s saved failed! Some file may not be saved successfully.", recordText));
//                    manualInfoView.setText(String.format("%s saved failed! Some file may not be saved successfully.", recordText));
                }
                recordBtn.setText(getString(R.string.btn_start_on));
                recordBtn.setBackgroundColor(getColor(R.color.btn_start_on));
                recordNum++;
                recordIDText.setText(recordText);
            }else {
                mode = Config.MODE_MANUAL;
                sensorCollector.startRecord();
                recordBtn.setText(getString(R.string.btn_start_off));
                recordBtn.setBackgroundColor(getColor(R.color.btn_start_off));
                displayInfo(manualInfoView, String.format("%s start.", recordText));
//                manualInfoView.setText(String.format("%s start.", recordText));

            }
        });
        cancelBtn.setOnClickListener(v -> {
            if (mode == Config.MODE_AUTO){
                Toast toast= Toast.makeText(MainActivity.this,"Auto mode is still working.", Toast.LENGTH_SHORT);
                toast.show();
            }else if(mode == Config.MODE_MANUAL){
                mode = Config.MODE_NONE;
                sensorCollector.cancelRecord();
                recordBtn.setText(getString(R.string.btn_start_on));
                recordBtn.setBackgroundColor(getColor(R.color.btn_start_on));
                displayInfo(manualInfoView, String.format("%s cancelled.", Utils.unifyIDText(recordNum)));
//                manualInfoView.setText(String.format("%s cancelled.", Utils.unifyIDText(recordNum)));
            }else {
                Toast toast= Toast.makeText(MainActivity.this,"Recorder is not working", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        recognizerBtn.setOnClickListener(v -> {
            if(mode == Config.MODE_MANUAL) {
                new Handler().post(() -> {
                    Hashtable<Integer, float[][]> readings = sensorCollector.latestSensorReadings(Config.getInstance().SEQUENCE_LENGTH);
                    String activityInfo = modelManger.inferRealTimeActivity(readings);
                    displayInfo(manualInfoView, String.format("Recognizer Inference: %s.", activityInfo));
//                    manualInfoView.setText(String.format("Recognizer Inference: %s.", activityInfo));
                });
            }else if (mode == Config.MODE_AUTO){
                Toast toast= Toast.makeText(MainActivity.this,"Auto mode is still working.", Toast.LENGTH_SHORT);
                toast.show();
            }else{
                Toast toast= Toast.makeText(MainActivity.this,"Data collection is not working.", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        autoencoderBtn.setOnClickListener(v -> {
            if(mode == Config.MODE_MANUAL) {
                new Handler().post(() -> {
                    int num = Config.getInstance().SEQUENCE_LENGTH * Config.getInstance().BATCH_SIZE;
                    Hashtable<Integer, float[][]> readings = sensorCollector.latestSensorReadings(num);
                    trainingSize += getNum(readings);
                    String lossInfo = modelManger.trainAutoencoder(readings, Config.getInstance().EPOCH_NUM);
                    displayInfo(manualInfoView, String.format("Autoencoder Training: %s.", lossInfo));
//                    manualInfoView.setText(String.format("Autoencoder Training: %s.", lossInfo));
                });
            }else if (mode == Config.MODE_AUTO){
                Toast toast= Toast.makeText(MainActivity.this,"Auto mode is still working.", Toast.LENGTH_SHORT);
                toast.show();
            }else{
                Toast toast= Toast.makeText(MainActivity.this,"Data collection is not working.", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        networkUpdateBtn.setOnClickListener(v -> {
            new FetchConfigTask().executeOnExecutor(THREAD_POOL_EXECUTOR, Config.URL_CONFIG);
            new FetchModelTask(Config.MODEL_NAME_AUTOENCODER, Config.SAVE_PATH_MODEL_AUTOENCODER).executeOnExecutor(THREAD_POOL_EXECUTOR, Config.URL_AUTOENCODER);
            new FetchModelTask(Config.MODEL_NAME_RECOGNIZER, Config.SAVE_PATH_MODEL_RECOGNIZER).executeOnExecutor(THREAD_POOL_EXECUTOR, Config.URL_RECOGNIZER);
        });
        networkUploadBtn.setOnClickListener(v -> {
            new UploadModelTask(Config.MODEL_NAME_AUTOENCODER, Config.getMetaInfo(), Config.SAVE_PATH_MODEL_AUTOENCODER, trainingSize)
                    .executeOnExecutor(THREAD_POOL_EXECUTOR, Config.URL_UPLOAD);
        });
        activateBtn.setOnClickListener(v -> {
            if (mode == Config.MODE_MANUAL){
                Toast toast= Toast.makeText(MainActivity.this,"Manual mode is still working.", Toast.LENGTH_SHORT);
                toast.show();
            }else if (mode == Config.MODE_AUTO){
                mode = Config.MODE_NONE;
                activateBtn.setText(getString(R.string.btn_activate_on));
                activateBtn.setBackgroundColor(getColor(R.color.btn_start_on));
                autoSensorTask.cancel();
                autoSensorHandler.removeCallbacks(autoSensorTask);
                autoModelTask.cancel();
                autoModelHandler.removeCallbacks(autoModelTask);
                displayInfo(autoInfoView, "Auto mode stops.");
//                autoInfoView.setText("Auto mode stops.");
            }else {
                mode = Config.MODE_AUTO;
                activateBtn.setText(getString(R.string.btn_activate_off));
                activateBtn.setBackgroundColor(getColor(R.color.btn_start_off));
                autoSensorTask = new AutoSensorTask();
                autoSensorHandler.postDelayed(autoSensorTask, Config.getInstance().AUTO_SENSOR_SAVE_INTERVAL * 1000);
                autoModelTask = new AutoModelTask();
                autoModelHandler.post(autoModelTask);
                displayInfo(autoInfoView, "Auto mode starts.");
//                autoInfoView.setText("Auto mode starts.");
            }

        });
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

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetIdleTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetIdleTime();
    }

    private void resetIdleTime(){
        this.lastIdleTimeTag = System.currentTimeMillis();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String date = Config.DATE_FORMAT.format(new Date());
        outState.putInt(date, recordNum);
    }

    private void recoverRecordNum(Bundle savedInstanceState){
        String date = Config.DATE_FORMAT.format(new Date());
        if(savedInstanceState != null && savedInstanceState.getInt(date) != 0) {
            recordNum = savedInstanceState.getInt(date);
        }else{
            recordNum = 1;
        }
    }

    private void displayInfo(TextView info, String newInfo){
        info.append("\n" + newInfo);
//        info.setText(String.format("%s\n%s", info.getText(), newInfo));
    }


    private class AutoSensorTask implements Runnable{

        private long timeTag;

        public AutoSensorTask(){
            timeTag = System.currentTimeMillis();
            sensorCollector.startRecord();
        }

        @Override
        public void run() {
            String dateInfo = Config.DATE_FORMAT_AUTO.format(new Date());
            if(sensorCollector.stopRecord(String.format("auto-%s", dateInfo))){
                displayInfo(autoInfoView, String.format("%s saved successfully!", dateInfo));
//                autoInfoView.setText(String.format("%s saved successfully!", dateInfo));
            }else {
                displayInfo(autoInfoView, String.format("%s saved failed! Some file may not be saved successfully.", dateInfo));
//                autoInfoView.setText(String.format("%s saved failed! Some file may not be saved successfully.", dateInfo));
            }
            sensorCollector.startRecord();
            autoSensorHandler.postDelayed(this, Config.getInstance().AUTO_SENSOR_SAVE_INTERVAL * 1000);
        }

        public void cancel(){
            sensorCollector.cancelRecord();
        }
    }

    private class AutoModelTask implements Runnable{

        private boolean isRunning = true;

        public AutoModelTask(){
            sensorCollector.startRecord();
        }

        @Override
        public void run() {
            boolean isIdle = (System.currentTimeMillis() - lastIdleTimeTag) > Config.getInstance().AUTO_MODEL_TRAIN_IDLE_TIME * 1000;
            if (isIdle && Utils.isCharging(MainActivity.this)){
                ArrayList<String> sensorFiles = sensorLoader.loadSensorFiles();
                for(String s: sensorFiles){
                    if (isRunning){
                        displayInfo(autoInfoView, String.format("Training data from %s.", s));
//                        autoInfoView.setText(String.format("Training data from %s.", s));
                        Hashtable<Integer, float[][]> readings = sensorLoader.loadSensorReadings(s);
                        trainingSize += getNum(readings);
                        String lossInfo = modelManger.trainAutoencoder(readings, Config.getInstance().EPOCH_NUM);
                        displayInfo(autoInfoView, String.format("Autoencoder Training: %s.", lossInfo));
//                        autoInfoView.setText(String.format("Autoencoder Training: %s.", lossInfo));
                    }
                }
            }
            autoModelHandler.postDelayed(this, Config.getInstance().AUTO_MODEL_TRAIN_INTERVAL * 1000);
        }

        public void cancel(){
            isRunning = false;
        }
    }

    private class FetchConfigTask extends GetJSONTask
    {
        @Override
        protected void onPostExecute(JSONObject response)
        {
            if(response != null){
                cacher.addString(Cacher.NAME_CONFIG, response.toString());
                try {
                    boolean result = Config.getInstance().load(new JSONObject(response.toString()));
                    if(result){
                        displayInfo(manualInfoView, "Config data is updated.");
                    }else{
                        displayInfo(manualInfoView, "Config data update fails.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class FetchModelTask extends GetFileTask{

        private String model;

        public FetchModelTask(String model, String filePath) {
            super(filePath);
            this.model = model;
        }

        protected void onPostExecute(Boolean re)
        {
            if(re){
                modelManger.update(this.model);
                displayInfo(manualInfoView, this.model + " is updated.");
            }else{
                displayInfo(manualInfoView, this.model + " update fails.");
            }
        }
    }

    private class UploadModelTask extends PostFileTask {

        public UploadModelTask(String fileName, String metaInfo, String filePath, int trainingSize) {
            super(fileName, metaInfo, filePath, trainingSize);
        }

        protected void onPostExecute(Boolean re)
        {
            if(re){
                modelManger.update(this.fileName);
                displayInfo(manualInfoView, this.fileName + " is uploaded.");
                trainingSize = 0;
            }else{
                displayInfo(manualInfoView, this.fileName + " upload fails.");
            }
        }
    }
}