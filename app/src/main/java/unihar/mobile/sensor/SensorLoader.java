package unihar.mobile.sensor;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import unihar.mobile.Utils;

public class SensorLoader {

    public SensorLoader() {

    }

    public ArrayList<String> loadSensorFiles(){
        String folderPath = Utils.dataFolderPath();
        File folder = new File(folderPath);
        if (folder.exists()){
            File[] sensorFiles = folder.listFiles();
            ArrayList<String> filePaths = new ArrayList<>();
            for (File f : sensorFiles){
                filePaths.add(f.getPath());
            }
            return filePaths;
        }
        return null;
    }

    public Hashtable<Integer, float[][]> loadSensorReadings(String path){
        ArrayList<String> data = Utils.loadSensorFile(path);
        if (data != null){
            ArrayList<SensorEventContainer> sensorEvents = new ArrayList<>();
            for (int i = 1; i < data.size(); i++){
                SensorEventContainer sensorEventContainer = SensorEventContainer.fromCSVString(data.get(i));
                if (sensorEventContainer != null) sensorEvents.add(sensorEventContainer);
            }
            return SensorEventContainer.organizeSensorReadings(sensorEvents);
        }
        return null;
    }

    private int columnNum(String header){
        return header.split(",").length;
    }
}
