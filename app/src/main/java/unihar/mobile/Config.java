package unihar.mobile;

import android.hardware.Sensor;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public class Config {
    public static final int LABEL_ACTIVITY_NONE = 0;
    public static final int LABEL_ACTIVITY_STILL = 1;
    public static final int LABEL_ACTIVITY_WALKING = 2;
    public static final int LABEL_ACTIVITY_UPSTAIRS = 3;
    public static final int LABEL_ACTIVITY_DOWNSTAIRS = 4;
    public static final int LABEL_ACTIVITY_JUMP = 5;

    public static final String ACTIVITY_NAME_NONE = "Unknown";
    public static final List<String> ACTIVITY_NAME_LISTS = Arrays.asList("Still", "Walking", "Walking upstairs", "Walking downstairs");

    public static final int SENSOR_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    public static final int SENSOR_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
    public static final List<Integer> SENSOR_LISTS = Arrays.asList(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE);
    public static final List<String> SENSOR_NAME_LISTS = Arrays.asList("Accelerometer", "Gyroscope");
    public static final int SAMPLE_DEPLAY = 50; // sampling rate 20Hz
    public static final int BUFFER_MAX_NUM = 12000;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yy_MM_dd");

    public static final String SAVE_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "UniHAR_Sensor";

    public static final String SAVE_PATH_MODEL_AUTOENCODER = Config.SAVE_PATH + File.separator + "autoencoder.ckpt";
    public static final String SAVE_PATH_MODEL_RECOGNIZER =Config.SAVE_PATH + File.separator + "recognizer.ckpt";

    public static final int ACTIVITY_NUM = 4;
    public static final int BATCH_SIZE = 64;
    public static final float MASK_RATIO = 0.15f;
    public static final int SEQUENCE_LENGTH = 20;
}
