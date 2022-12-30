package unihar.mobile.sensor;

import android.hardware.SensorEvent;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import unihar.mobile.Config;
import unihar.mobile.Utils;

public class SensorRecorder {

    private ArrayList<SensorEventContainer> sensorSet;
    private Hashtable<Integer, String> sensorNameSet;
    private int label = Config.LABEL_ACTIVITY_NONE;
    private int samplingDelay;
    private int bufferMaxNum = Config.BUFFER_MAX_NUM;


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
                if (sensorSet.size() - bufferMaxNum > 0) {
                    sensorSet.subList(0, sensorSet.size() - bufferMaxNum).clear();
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
        ArrayList<Integer> sensors = new ArrayList<>();
        StringBuilder header = new StringBuilder();
        header.append("Timestamp,");
        for(Map.Entry<Integer, String> entry : sensorNameSet.entrySet()){
            sensors.add(entry.getKey());
            header.append(entry.getValue()).append("_x,");
            header.append(entry.getValue()).append("_y,");
            header.append(entry.getValue()).append("_z,");
        }
        header.append("Label");
        ArrayList<String> data = new ArrayList<>();
        data.add(header.toString());
        for(int i = 0; i < sensorSet.size(); i++){
            SensorEventContainer sensorEventContainer = sensorSet.get(i);
            Hashtable<Integer, float[]> readings = sensorEventContainer.getAverageSensorReadings();
            if (readings != null){
                StringBuilder item = new StringBuilder();
                item.append(sensorEventContainer.timeTag).append(',');
                boolean tag = true;
                for(int j = 0; j < sensors.size(); j++){
                    if (readings.containsKey(sensors.get(j)) && readings.get(sensors.get(j)) != null){
                        item.append(Utils.floatArrayToString(readings.get(sensors.get(j))));
                        item.append(',');
                    }else {
                        tag = false;
                    }
                }
                item.append(sensorEventContainer.label);
                if (tag){
                    data.add(item.toString());
                }
            }
        }
        clearSensorData();
        if (data.size() > 0) {

            return Utils.saveFile(filePath + "_" + samplingDelay + ".csv", data);
        }
        return false;
    }

    public void clearSensorData(){
        sensorSet.clear();
    }

    public Hashtable<Integer, ArrayList<float[]>> getLatestReadings(int num){
        if (num > sensorSet.size()) return null;
        Hashtable<Integer, ArrayList<float[]>> lastetReadings = new Hashtable<>();
        for(Map.Entry<Integer, String> entry : sensorNameSet.entrySet()){
            lastetReadings.put(entry.getKey(), new ArrayList<>());
        }
        for(int i = sensorSet.size() - num; i < sensorSet.size(); i++){
            SensorEventContainer sensorEventContainer = sensorSet.get(i);
            Hashtable<Integer, float[]> readings = sensorEventContainer.getAverageSensorReadings();
            if (readings != null){
                for(Map.Entry<Integer, float[]> entry : readings.entrySet()){
                    if (lastetReadings.containsKey(entry.getKey()) && entry.getValue() != null)
                    {
                        lastetReadings.get(entry.getKey()).add(entry.getValue());
                    }else {
                        return null;
                    }
                }
            }
        }
        return lastetReadings;
    }

    public void setBufferMaxNum(int bufferMaxNum) {
        this.bufferMaxNum = bufferMaxNum;
    }

    private class SensorEventContainer{

        public long timeTag;
        public int label;
        public Hashtable<Integer, ArrayList<float[]>> sensorEvents;

        public SensorEventContainer(long timeTag, int label){
            this.timeTag = timeTag;
            this.label = label;
            this.sensorEvents = new Hashtable<>();
        }

        public void insertSensorEvent(int sensorType, float[] sensorValues){
            if(sensorEvents.containsKey(sensorType)){
                sensorEvents.get(sensorType).add(sensorValues);
            }else{
                ArrayList<float[]> sensor = new ArrayList<>();
                sensor.add(sensorValues);
                sensorEvents.put(sensorType, sensor);
            }

        }

        public Hashtable<Integer, float[]> getAverageSensorReadings(){
            Hashtable<Integer, float[]> averageReadings = new Hashtable<>();
            for(Map.Entry<Integer, ArrayList<float[]>> entry : sensorEvents.entrySet()){
                int sensorEventNum = entry.getValue().size();
                if(sensorEventNum == 0){
                    return null;
                }
                int sensorReadingNum = entry.getValue().get(0).length;
                float[] sensorAverageReadings = new float[sensorReadingNum];
                for(int i = 0; i < sensorReadingNum; i++){
                    float[] values = new float[sensorEventNum];
                    for(int j = 0; j < sensorEventNum; j++){
                        values[j] = entry.getValue().get(j)[i];
                    }
                    sensorAverageReadings[i] = Utils.average(values);
                }
                averageReadings.put(entry.getKey(), sensorAverageReadings);
            }
            return averageReadings;
        }
    }
}
