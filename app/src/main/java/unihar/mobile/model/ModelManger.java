package unihar.mobile.model;

import android.app.Activity;
import android.opengl.Matrix;

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
        init();
    }

    public void init(){
        autoencoder.initFromAsset("ae.tflite");
        recognizer.initFromAsset("re.tflite");
    }

    public String trainAutoencoder(Hashtable<Integer, float[][]> readings, int numEpoch){
        float[][] cReadings = organizeReadings(readings);
        if (cReadings == null || cReadings.length < Config.SEQUENCE_LENGTH) {
            return Config.MSG_DATA_ERROR;
        }else {
            long start = System.currentTimeMillis();
            float[][][] inputReadings = resampleReadings(cReadings, Config.SEQUENCE_LENGTH, Config.BATCH_SIZE);
            float loss = autoencoder.train(inputReadings, numEpoch);
            double consumedTime = (System.currentTimeMillis() - start) / 1000.0;
            return String.format("Loss-%.2f, %.3f sec", loss, consumedTime);
        }
        // Utils.randomFloat3Array(new int[]{64, 20, 6}), 1
    }

    public String inferRealTimeActivity(Hashtable<Integer, float[][]> readings){
        float[][] cReadings = organizeReadings(readings);
        if (cReadings == null || cReadings.length < Config.SEQUENCE_LENGTH) {
            return Config.MSG_DATA_ERROR;
        }else {
            long start = System.currentTimeMillis();
            int label = recognizer.infer(cReadings);
            String activity = Config.ACTIVITY_NAME_LISTS.get(label);
            double consumedTime = (System.currentTimeMillis() - start) / 1000.0;
            return String.format("Activity-%s, %.3f sec", activity, consumedTime);
        }

    }

    public float[][] organizeReadings(Hashtable<Integer, float[][]> readings){
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
