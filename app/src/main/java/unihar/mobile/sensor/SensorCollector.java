package unihar.mobile.sensor;

import static android.content.Context.SENSOR_SERVICE;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import unihar.mobile.Config;
import unihar.mobile.MainActivity;
import unihar.mobile.Utils;

public class SensorCollector implements SensorEventListener {


    private boolean recorded;
    private SensorRecorder sensorRecorder;
    private SensorManager sensorManager;
    private MainActivity activity;

    public SensorCollector(MainActivity activity){
        sensorRecorder = new SensorRecorder(Config.getInstance().SAMPLE_INTERVAL);

        sensorManager = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
        this.activity = activity;

        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.i("Available Sensors", deviceSensors.toString());


    }

    public void startRecord(){
        if (!recorded) {
            sensorRecorder.clearSensorData();
            for (int i = 0; i < Config.SENSOR_LISTS.size(); i++) {
                Sensor sensor = sensorManager.getDefaultSensor(Config.SENSOR_LISTS.get(i));
                if (sensor != null) {
                    sensorRecorder.initSensorSet(Config.SENSOR_LISTS.get(i), Config.SENSOR_NAME_LISTS.get(i));
                }
            }
            registerSensorListeners();
        }
        recorded = true;
    }

    public void cancelRecord(){
        if (recorded) {
            unregisterSensorListeners();
            sensorRecorder.clearSensorData();
            sensorRecorder.clearSensorSet();
        }
        recorded = false;
    }


    public boolean stopRecord(String info){
        if (recorded) {
            String folderPath = Utils.dataFolderPath();
            String filePath = folderPath + File.separator + info;
            Log.i("Saved Path", filePath);
            if (!Utils.createFolder(folderPath)) {
                sensorRecorder.clearSensorData();
                return false;
            }
            boolean result = sensorRecorder.saveData(filePath);
            cancelRecord();
            return result;
        }
        return false;
    }

    private void registerSensorListeners(){
        for (int i = 0; i < Config.SENSOR_LISTS.size(); i++){
            Sensor sensor = sensorManager.getDefaultSensor(Config.SENSOR_LISTS.get(i));
            if(sensor != null) {
                sensorManager.registerListener(this, sensor, Config.getInstance().SAMPLE_INTERVAL * 1000);
            }
            else {
                Log.w("Sensor Missing", Config.SENSOR_NAME_LISTS.get(i));
            }
        }
    }
    private void unregisterSensorListeners(){
        for (int i = 0; i < Config.SENSOR_LISTS.size(); i++){
            Sensor sensor = sensorManager.getDefaultSensor(Config.SENSOR_LISTS.get(i));
            if(sensor != null) {
                sensorManager.unregisterListener(this, sensor);
            }
            else {
                Log.w("Sensor Missing", Config.SENSOR_NAME_LISTS.get(i));
            }
        }
    }

    public void updateLabel(int label){
        sensorRecorder.setLabel(label);
    }

    public Hashtable<Integer, float[][]> latestSensorReadings(int num){
        ArrayList<SensorEventContainer> sensorEvents = sensorRecorder.getLatestReadings(num);
        return SensorEventContainer.organizeSensorReadings(sensorEvents);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(recorded) {
            sensorRecorder.addSensorItem(event.sensor.getType(), event);
            activity.setSensorText(event.sensor.getType(), event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
