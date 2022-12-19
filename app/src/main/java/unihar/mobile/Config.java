package unihar.mobile;

import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;

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

    public static final List<Integer> SENSOR_LISTS = Arrays.asList(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE);
    public static final List<String> SENSOR_NAME_LISTS = Arrays.asList("Accelerometer", "Gyroscope");
    public static final int SAMPLE_RATE = SENSOR_DELAY_FASTEST;


    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yy_MM_dd");

    public static final String RECORD_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "UniHAR_Sensor";
}
