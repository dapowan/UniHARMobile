package unihar.mobile.sensor;

import static android.content.Context.SENSOR_SERVICE;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import unihar.mobile.Config;
import unihar.mobile.MainActivity;
import unihar.mobile.Utils;

public class SensorCollector implements SensorEventListener {


    private boolean recorded;
    private SensorRecorder sensorRecorder;
    private SensorManager sensorManager;
    private MainActivity activity;

    public SensorCollector(MainActivity activity){
        sensorRecorder = new SensorRecorder(Config.SAMPLE_DEPLAY);

        sensorManager = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
        this.activity = activity;

        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.i("Available Sensors", deviceSensors.toString());

        for (int i = 0; i < Config.SENSOR_LISTS.size(); i++){
            Sensor sensor = sensorManager.getDefaultSensor(Config.SENSOR_LISTS.get(i));
            if(sensor != null) {
                sensorManager.registerListener(this, sensor, Config.SAMPLE_DEPLAY * 1000);
            }
            else {
                Log.w("Sensor Missing", Config.SENSOR_NAME_LISTS.get(i));
            }
        }
    }


    public void startRecord(){
        sensorRecorder.clearSensorData();
        for (int i = 0; i < Config.SENSOR_LISTS.size(); i++){
            Sensor sensor = sensorManager.getDefaultSensor(Config.SENSOR_LISTS.get(i));
            if(sensor != null) {
                sensorRecorder.initSensorSet(Config.SENSOR_LISTS.get(i), Config.SENSOR_NAME_LISTS.get(i));
            }
        }
        recorded = true;
    }

    public void cancelRecord(){
        sensorRecorder.clearSensorData();
        sensorRecorder.clearSensorSet();
        recorded = false;
    }

    public void destory(){
        cancelRecord();
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

    public boolean stopRecord(String info){
        String folderPath = Utils.dataFolderPath();
        String filePath = folderPath + File.separator + info;
        Log.i("Saved Path", filePath);
        if(!Utils.createFolder(folderPath)) {
            sensorRecorder.clearSensorData();
            return false;
        }
        boolean result = sensorRecorder.saveData(filePath);
        cancelRecord();
        return result;
    }


    public void updateLabel(int label){
        sensorRecorder.setLabel(label);
    }

    public Hashtable<Integer, float[][]> latestSensorReadings(int num){
        Hashtable<Integer, ArrayList<float[]>> readings = sensorRecorder.getLatestReadings(num);
        if (readings == null) return null;
        Hashtable<Integer, float[][]> readingsNew = new Hashtable<>();
        for(Map.Entry<Integer, ArrayList<float[]>> entry : readings.entrySet()){
            readingsNew.put(entry.getKey(), Utils.floatListToArray(entry.getValue()));
        }
        return readingsNew;
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
