package unihar.mobile.model;

import static unihar.mobile.Config.ASSET_NAME_AUTOENCODER;
import static unihar.mobile.Config.ASSET_NAME_RECOGNIZER;

import android.app.Activity;
import android.util.Log;

import java.util.Hashtable;

import unihar.mobile.Config;
import unihar.mobile.Utils;

public class ModelManger {

    private ModelHelper autoencoder;
    private ModelHelper recognizer;

    public ModelManger(Activity activity){
        autoencoder = new AutoencoderModelHelper(activity, Config.SAVE_PATH_MODEL_AUTOENCODER);
        recognizer = new RecognizerModelHelper(activity, Config.SAVE_PATH_MODEL_RECOGNIZER);
        autoencoder.initFromAsset(ASSET_NAME_AUTOENCODER);
        recognizer.initFromAsset(ASSET_NAME_RECOGNIZER);
    }

    public void update(String path){
        if (path.equals(Config.MODEL_NAME_AUTOENCODER)){
            autoencoder.restore();
        }else if (path.equals(Config.MODEL_NAME_RECOGNIZER)){
            recognizer.restore();
        }else {
            Log.w("Model Update", "Unknown model");
        }
    }

    public String trainAutoencoder(Hashtable<Integer, float[][]> readings, int numEpoch){
        float[][] cReadings = organizeReadingsToArray(readings);
        if (cReadings == null || cReadings.length < Config.getInstance().SEQUENCE_LENGTH) {
            return Config.MSG_DATA_ERROR;
        }else {
            long start = System.currentTimeMillis();
            float[][][] inputReadings = resampleReadings(cReadings,
                    Config.getInstance().SEQUENCE_LENGTH, Config.getInstance().BATCH_SIZE); // to have enough length
            float loss = autoencoder.train(inputReadings, numEpoch);
            double consumedTime = (System.currentTimeMillis() - start) / 1000.0;
            return String.format("Loss-%.2f, %.3f sec", loss, consumedTime);
        }
    }

    public String inferRealTimeActivity(Hashtable<Integer, float[][]> readings){
        float[][] cReadings = organizeReadingsToArray(readings);
        if (cReadings == null || cReadings.length < Config.getInstance().SEQUENCE_LENGTH) {
            return Config.MSG_DATA_ERROR;
        }else {
            long start = System.currentTimeMillis();
            int label = recognizer.infer(cReadings);
            String activity = Config.getInstance().ACTIVITY_NAME_LIST[label];
            double consumedTime = (System.currentTimeMillis() - start) / 1000.0;
            return String.format("Activity-%s, %.3f sec", activity, consumedTime);
        }

    }

    public float[][] organizeReadingsToArray(Hashtable<Integer, float[][]> readings){
        if (readings == null || !readings.containsKey(Config.SENSOR_ACCELEROMETER)
                || !readings.containsKey(Config.SENSOR_GYROSCOPE)) return null;
        float[][] accReadings = readings.get(Config.SENSOR_ACCELEROMETER);
        float[][] gyrReadings = readings.get(Config.SENSOR_GYROSCOPE);
        return Utils.concatLast(accReadings, gyrReadings);
    }

    private float[][][] resampleReadings(float[][] readings, int sequenceLength, int batchSize){
        float[][][] newReadings = new float[batchSize][sequenceLength][6];
        for(int i = 0;i < batchSize; i++){
            newReadings[i] = Utils.randomSample(readings, sequenceLength);
        }
        return newReadings;
    }
}
