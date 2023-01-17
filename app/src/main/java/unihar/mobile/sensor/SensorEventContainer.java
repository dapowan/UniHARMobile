package unihar.mobile.sensor;

import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import unihar.mobile.Config;
import unihar.mobile.Utils;

public class SensorEventContainer {


    public long timeTag;
    public int label;
    public Hashtable<Integer, ArrayList<float[]>> sensorEvents;

    public SensorEventContainer(){
        this.sensorEvents = new Hashtable<>();
    }

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

    public static String headerString(){
        StringBuilder header = new StringBuilder();
        header.append("Timestamp,");
        for (int i = 0; i < Config.SENSOR_LISTS.size(); i++){
            header.append(Config.SENSOR_NAME_LISTS.get(i)).append("_x,");
            header.append(Config.SENSOR_NAME_LISTS.get(i)).append("_y,");
            header.append(Config.SENSOR_NAME_LISTS.get(i)).append("_z,");
        }
        header.append("Label");
        return header.toString();
    }

    public static String toCSVString(SensorEventContainer sensorEventContainer) {
        Hashtable<Integer, float[]> readings = sensorEventContainer.getAverageSensorReadings();
        if (readings != null){
            StringBuilder item = new StringBuilder();
            item.append(sensorEventContainer.timeTag).append(',');
            boolean tag = true;
            for(int i = 0; i < Config.SENSOR_LISTS.size(); i++){
                if (readings.containsKey(Config.SENSOR_LISTS.get(i))
                        && readings.get(Config.SENSOR_LISTS.get(i)) != null){
                    item.append(Utils.floatArrayToString(readings.get(Config.SENSOR_LISTS.get(i))));
                    item.append(',');
                }else {
                    tag = false;
                }
            }
            item.append(sensorEventContainer.label);
            if (tag){
                return item.toString();
            }
        }
        return null;
    }

    public static SensorEventContainer fromCSVString(String data){
        if (data == null || ! data.contains(",")) return null;
        String[] items = data.split(",");
        long timeTag = Long.parseLong(items[0]);
        int label = Integer.parseInt(items[items.length - 1]);
        SensorEventContainer sensorEventContainer = new SensorEventContainer(timeTag, label);
        for(int i = 0; i < Config.SENSOR_LISTS.size(); i++){
            int d = Config.SENSOR_DIMENSIONS.get(i);
            float[] sensor = new float[d];
            int offset;
            if (i == 0){
                offset = 0;
            }else{
                offset = Config.SENSOR_DIMENSIONS.subList(0, i).stream().mapToInt(Integer::intValue).sum();
            }
            for (int j = 0; j < d; j++){
                sensor[j] = Float.parseFloat(items[offset + j + 1]);
            }
            sensorEventContainer.insertSensorEvent(Config.SENSOR_LISTS.get(i), sensor);
        }
        return sensorEventContainer;
    }

    public static Hashtable<Integer, float[][]> organizeSensorReadings(ArrayList<SensorEventContainer> sensorEvents){
        if (sensorEvents == null || sensorEvents.size() == 0) return null;
        Hashtable<Integer, ArrayList<float[]>> readingsTemp = new Hashtable<>();
        for(Integer sensor : Config.SENSOR_LISTS) {
            readingsTemp.put(sensor, new ArrayList<>());
        }
        ArrayList<Integer> dirtySensors = new ArrayList<>();
        for (int i = 0;i < sensorEvents.size(); i++){
            Hashtable<Integer, float[]> readings = sensorEvents.get(i).getAverageSensorReadings();
            if (readings == null ) return null;
            for(Integer sensor : Config.SENSOR_LISTS) {
                if (readings.containsKey(sensor) && readings.get(sensor) != null){
                    readingsTemp.get(sensor).add(readings.get(sensor));
                }else {
                    ArrayList<float[]> sensorReadings = readingsTemp.get(sensor);
                    if (sensorReadings.size() > 1) {
                        sensorReadings.add(sensorReadings.get(sensorReadings.size() - 1));
                        dirtySensors.add(sensor);
                        if(1.0 * dirtySensors.size() / sensorEvents.size() > Config.getInstance().SENSOR_DIRTY_RATE) {
                            Log.w("Too many dirty readings", String.format("Too many dirty readings %d / %d, detailed %s."
                                    , dirtySensors.size(), sensorEvents.size(), dirtySensors));
                            return null;
                        }
                    }
                    else {
                        Log.w("Dirty sensor", readings.keys().toString());
                        return null;
                    }

                }
            }
        }
        Hashtable<Integer, float[][]> readings = new Hashtable<>();
        for(Integer sensor : Config.SENSOR_LISTS){
            readings.put(sensor, Utils.floatListToArray(readingsTemp.get(sensor)));
        }
        return readings;
    }
}
