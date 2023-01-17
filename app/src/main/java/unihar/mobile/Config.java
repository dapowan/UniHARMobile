package unihar.mobile;

import android.hardware.Sensor;
import android.os.Environment;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {

    public static final int LABEL_ACTIVITY_NONE = 0;
    public static final int LABEL_ACTIVITY_STILL = 1;
    public static final int LABEL_ACTIVITY_WALKING = 2;
    public static final int LABEL_ACTIVITY_UPSTAIRS = 3;
    public static final int LABEL_ACTIVITY_DOWNSTAIRS = 4;
    public static final int LABEL_ACTIVITY_JUMP = 5;

    public static final String MSG_DATA_ERROR = "Unknown Data Collection Error";

    public static final int SENSOR_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    public static final int SENSOR_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
    public static final List<Integer> SENSOR_LISTS = Arrays.asList(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE);
    public static final List<String> SENSOR_NAME_LISTS = Arrays.asList("Accelerometer", "Gyroscope");
    public static final List<Integer> SENSOR_DIMENSIONS = Arrays.asList(3, 3);

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yy_MM_dd");
    public static final SimpleDateFormat DATE_FORMAT_AUTO = new SimpleDateFormat("HH_mm_ss");
    public static final int BUFFER_MAX_NUM = 12000;

    public static final String SAVE_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "UniHAR_Sensor";

    public static final String SAVE_PATH_MODEL_AUTOENCODER = Config.SAVE_PATH + File.separator + "autoencoder.ckpt";
    public static final String SAVE_PATH_MODEL_RECOGNIZER = Config.SAVE_PATH + File.separator + "recognizer.ckpt";

    public static final int MODE_NONE = -1;
    public static final int MODE_AUTO = 0;
    public static final int MODE_MANUAL = 1;

    private static final String URL_SERVER = "http://192.168.137.1:8080/";
    public static final String URL_CONFIG = URL_SERVER + "config.json";

    public int SAMPLE_INTERVAL = 50; // 50ms = sampling rate 20Hz
    public float SENSOR_DIRTY_RATE = 0.04f;

    public int ACTIVITY_NUM = 4;
    public String[] ACTIVITY_NAME_LIST = {"Still", "Walking", "Walking upstairs", "Walking downstairs"};
    public int BATCH_SIZE = 64;
    public float MASK_RATIO = 0.15f;
    public int SEQUENCE_LENGTH = 20;
    public int EPOCH_NUM = 1;

    public int AUTO_SENSOR_SAVE_INTERVAL = 5; // 30s to save
    public int AUTO_MODEL_TRAIN_INTERVAL = 10; // 30s to save
    public int AUTO_MODEL_TRAIN_IDLE_TIME = 5;

    private static Config config = null;

    private Config(){}

    public static Config getInstance(){
        if(config == null) config = new Config();
        return config;
    }

    public boolean load(JSONObject data){
        if (data == null) return false;
        try {
            SAMPLE_INTERVAL = data.getInt("sample_interval");
            SENSOR_DIRTY_RATE = (float) data.getDouble("sensor_dirty_rate");
            ACTIVITY_NUM = data.getInt("activity_num");
            JSONArray aNames = data.getJSONArray("activity_name_list");
            String[] names = new String[data.length()];
            for (int i = 0;i < aNames.length(); i++) names[i] = (String) aNames.get(i);
            ACTIVITY_NAME_LIST = names;
            BATCH_SIZE = data.getInt("batch_size");
            MASK_RATIO = (float) data.getDouble("mask_ratio");
            SEQUENCE_LENGTH = data.getInt("sample_interval");
            EPOCH_NUM = data.getInt("epoch_num");
            AUTO_SENSOR_SAVE_INTERVAL = data.getInt("auto_sensor_save_interval");
            AUTO_MODEL_TRAIN_INTERVAL = data.getInt("auto_model_train_interval");
            AUTO_MODEL_TRAIN_IDLE_TIME = data.getInt("auto_model_train_idle_time");

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
