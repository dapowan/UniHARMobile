package unihar.mobile.sensor;

import android.hardware.SensorEvent;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;

import unihar.mobile.Config;
import unihar.mobile.Utils;

public class SensorRecorder {

    private ArrayList<SensorEventContainer> sensorSet;
    private Hashtable<Integer, String> sensorNameSet;
    private int label = Config.LABEL_ACTIVITY_NONE;
    private int samplingDelay;


    public SensorRecorder(int samplingDelay) {
        this.samplingDelay = samplingDelay;
        sensorSet = new ArrayList<>();
        sensorNameSet = new Hashtable<>();
    }

    public void initSensorSet(int sensorType, String sensorName){
        if(!sensorNameSet.containsKey(sensorType)){
            sensorNameSet.put(sensorType, sensorName);
        }
    }

    public void clearSensorSet(){
        if(sensorSet != null) sensorSet.clear();
        if(sensorNameSet != null) sensorNameSet.clear();
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public void addSensorItem(int sensorType, SensorEvent sensorEvent){
        if (sensorNameSet.containsKey(sensorType)) {
            long timestamp = System.currentTimeMillis()
                    + (sensorEvent.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000;
            long timeTag = timestamp / this.samplingDelay * this.samplingDelay;
            int s = sensorSet.size();
            if (s == 0 || sensorSet.get(s - 1).timeTag != timeTag) {
                SensorEventContainer sensorEventContainer = new SensorEventContainer(timeTag, label);
                sensorEventContainer.insertSensorEvent(sensorType, sensorEvent.values);
                sensorSet.add(sensorEventContainer);
                if (sensorSet.size() - Config.BUFFER_MAX_NUM > 0) {
                    sensorSet.subList(0, sensorSet.size() - Config.BUFFER_MAX_NUM).clear();
                }
            } else {
                SensorEventContainer sensorEventContainer = sensorSet.get(s - 1);
                sensorEventContainer.insertSensorEvent(sensorType, sensorEvent.values);
            }
        }else {
            Log.w("Adding Wrong Sensor Data", sensorNameSet.get(sensorType));
        }
    }

    public boolean saveData(String filePath){
        ArrayList<String> data = new ArrayList<>();
        data.add(SensorEventContainer.headerString());
        for(int i = 0; i < sensorSet.size(); i++){
            SensorEventContainer sensorEventContainer = sensorSet.get(i);
            String item = SensorEventContainer.toCSVString(sensorEventContainer);
            if (item != null) data.add(item);
        }
        clearSensorData();
        if (data.size() > 0) {

            return Utils.saveSensorFile(filePath + "-" + samplingDelay + ".csv", data);
        }
        return false;
    }

    public void clearSensorData(){
        sensorSet.clear();
    }

    public ArrayList<SensorEventContainer> getLatestReadings(int num){
        int is = Math.max(sensorSet.size() - num - 1, 1); // skip the first the latest event
        return Utils.subArrayList(sensorSet, is, sensorSet.size() - 1);
    }

}
