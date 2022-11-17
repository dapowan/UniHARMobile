package unihar.mobile;

import android.hardware.SensorEvent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

public class SensorRecorder {

    private Hashtable<Integer, ArrayList<String>> sensorSet;
    private Hashtable<Integer, String> sensorNameSet;
    private Hashtable<Integer, Long> sensorTimeSet;
    private Hashtable<Integer, Integer> sensorSampleRateSet;
    private ArrayList<String> labelSet;
    private MainActivity mainActivity;
    private boolean recordSampleRate;

    public SensorRecorder(MainActivity mainActivity) {
        if(mainActivity == null)
            recordSampleRate = false;
        else {
            recordSampleRate = true;
            this.mainActivity = mainActivity;
        }

        sensorSet = new Hashtable<>();
        sensorNameSet = new Hashtable<>();
        sensorTimeSet = new Hashtable<>();
        sensorSampleRateSet = new Hashtable<>();
        labelSet = new ArrayList<>();
//        boolean a = Utils.createFolder(Config.RECORD_PATH);
//        a = true;
    }

    public void initSensorSet(int sensorType, String sensorName){
        if(!sensorSet.containsKey(sensorType)){
            sensorSet.put(sensorType, new ArrayList<String>());
            sensorNameSet.put(sensorType, sensorName);
            sensorTimeSet.put(sensorType, new Date().getTime());
            sensorSampleRateSet.put(sensorType, 0);
        }
    }

    public void clearSensorSet(){
        if(sensorSet != null) sensorSet.clear();
        if(sensorNameSet != null) sensorNameSet.clear();
        if(sensorTimeSet != null) sensorTimeSet.clear();
        if(sensorSampleRateSet != null) sensorSampleRateSet.clear();
        if(labelSet != null) labelSet.clear();
    }

    public void addLabelItem(int label){
        long timestamp = new Date().getTime();
        labelSet.add(timestamp + "," + label);
    }

    public void addSensorItem(int sensorType, SensorEvent sensorEvent){
        if(sensorSet.containsKey(sensorType)) {
            float[] data = sensorEvent.values;
            long timestamp = System.currentTimeMillis()
                    + (sensorEvent.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000;
            sensorSet.get(sensorType).add("" + timestamp + floatArrayToString(data));
            if(recordSampleRate) {
                if (timestamp - sensorTimeSet.get(sensorType) > 1000) {
                    sensorTimeSet.put(sensorType, timestamp);
//                    mainActivity.sampleRateChange(sensorType, sensorSampleRateSet.get(sensorType));
                    sensorSampleRateSet.put(sensorType, 0);
                } else {
                    sensorSampleRateSet.put(sensorType, sensorSampleRateSet.get(sensorType) + 1);
                }
            }
        }else {
            Log.w("Adding Wrong Sensor Data", sensorNameSet.get(sensorType));
        }
    }

    public boolean saveAllData(String baseDir){
        boolean result = true;
        if(!Utils.createFolder(baseDir)) return false;
        for(Hashtable.Entry<Integer, ArrayList<String>> entry : sensorSet.entrySet()){
            result = result && saveFile(baseDir + File.separator + sensorNameSet.get(entry.getKey()) + ".csv", entry.getValue());
        }
        result = result && saveFile(baseDir + File.separator + "Label.csv", labelSet);
        clearSensorData();
        return result;
    }

    public void clearSensorData(){
        for(Hashtable.Entry<Integer, ArrayList<String>> entry : sensorSet.entrySet()){
            entry.getValue().clear();
        }
        labelSet.clear();
//        turnSet.clear();
    }

    private boolean saveFile(String path, ArrayList<String> data){
        File file = new File(path);
        try {
            if(file.exists()) file.delete();
            if(file.createNewFile()) {
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                for (int i = 0; i < data.size(); i++) {
                    if (i != 0)
                        bw.newLine();
                    bw.write(data.get(i));
                }
                bw.close();
                fw.close();
                return true;
            }
        }catch (Exception e){
            Log.e("file create error", path);
        }
        return false;
    }

    private String floatArrayToString(float[] data) {
        String s = "";
        for (int i = 0; i < data.length; i++) {
            s += "," + data[i];
        }
        return s;
    }

    private double average(double[] arr) {
        double sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum / arr.length;
    }

}
