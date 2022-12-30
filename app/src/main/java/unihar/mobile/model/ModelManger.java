package unihar.mobile.model;

import android.app.Activity;

import java.util.Hashtable;

import unihar.mobile.Config;
import unihar.mobile.Utils;

public class ModelManger {

    private ModelHelper autoencoder;
    private ModelHelper recognizer;

    public ModelManger(Activity activity){
        autoencoder = new AutoencoderModelHelper(activity, Config.SAVE_PATH_MODEL_AUTOENCODER);
        autoencoder.setBatchSize(Config.BATCH_SIZE);
        recognizer = new RecognizerModelHelper(activity, Config.SAVE_PATH_MODEL_RECOGNIZER);
        recognizer.setBatchSize(Config.BATCH_SIZE);
    }

    public void init(){
        autoencoder.initFromAsset("ae.tflite");
        recognizer.initFromFile("re.tflite");
    }

    public void activateAutoMode(){

    }

    public void trainAutoencoder(){
        // Utils.randomFloat3Array(new int[]{64, 20, 6}), 1
    }

    public String inferRealTimeActivity(Hashtable<Integer, float[][]> readings){
        if (!readings.containsKey(Config.SENSOR_ACCELEROMETER)
                || !readings.containsKey(Config.SENSOR_GYROSCOPE)) return Config.ACTIVITY_NAME_NONE;
        float[][] accReadings = readings.get(Config.SENSOR_ACCELEROMETER);
        float[][] gyrReadings = readings.get(Config.SENSOR_GYROSCOPE);
        float[][] input = Utils.concat(accReadings, gyrReadings);
        // Utils.randomFloat3Array(new int[]{64, 20, 6}
        int label = recognizer.infer(input);
        return Config.ACTIVITY_NAME_LISTS.get(label);
    }
}
