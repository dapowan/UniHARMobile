package unihar.mobile.sensor;

import static android.content.Context.SENSOR_SERVICE;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;

import unihar.mobile.Config;
import unihar.mobile.Utils;

public class SensorCollector implements SensorEventListener {

    private Activity activity;
    private SensorRecorder sensorRecorder;
    private boolean recorded;
    private SensorManager sensorManager;
    private int label = Config.LABEL_ACTIVITY_JUMP;

    public SensorCollector(Activity activity){
        sensorRecorder = new SensorRecorder();

        sensorManager = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.i("Available Sensors", deviceSensors.toString());
        registerSensorListener();
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

    public void start(){
        clear();
        recorded = true;
    }

    public void stop(){
        recorded = false;
    }

    public boolean save(String info){
        String dir = Utils.dataFolderPath(info);
        Log.i("Path", dir);
        return sensorRecorder.saveAllData(dir);
    }

    public void clear(){
        sensorRecorder.clearSensorSet();
    }

    public void updateLabel(int label){
        this.label = label;
        sensorRecorder.setLabel(label);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(recorded) {
            sensorRecorder.addSensorItem(event.sensor.getType(), event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
