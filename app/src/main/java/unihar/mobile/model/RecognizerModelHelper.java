package unihar.mobile.model;

import android.app.Activity;
import android.util.Log;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import unihar.mobile.Config;
import unihar.mobile.Utils;

public class RecognizerModelHelper extends ModelHelper{

    private int classNum = Config.ACTIVITY_NUM;

    public RecognizerModelHelper(Activity activity, String saveModelPath){
        super(activity, saveModelPath);
        saveModelPath = Config.SAVE_PATH + File.separator + "recognizer.ckpt";
    }

    public float train(float[][][] trainingData, float[][] trainingLabels, int numEpochs){
        return (float)0.0;
    }

    public int[] infer(float[][][] inferData){
        int inferSize = inferData.length;
        int sampleSize = inferData[0].length * inferData[0][0].length;
        int[] inferLabels = new int[inferSize];

        for (int i = 0; i < inferSize; i += batchSize) {
            long start = System.currentTimeMillis();

            int index_end = Math.min(i + batchSize, inferSize);
            float[][][] inferInputsBatch = Utils.copyFloat3Array(inferData, i, index_end);

            FloatBuffer inferInputs = FloatBuffer.allocate(batchSize * sampleSize);
            Utils.addFloat3Array(inferInputs, inferInputsBatch);
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("x", inferInputs);

            FloatBuffer inferProbs = FloatBuffer.allocate(batchSize * classNum);
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("probabilities", inferProbs);

            interpreter.runSignature(inputs, outputs, "infer");

            inferProbs.rewind();

            for (int b = 0; b < batchSize && (i + b) < inferSize; ++b) {
                int index = 0;
                for (int c = 1; c < classNum; ++c) {
                    if (inferProbs.get(b * classNum + index) < inferProbs.get(b * classNum + c))
                        index = c;
                }
                inferLabels[i + b] = index;
            }
            long elapsedTimeMillis = System.currentTimeMillis() - start;

            Log.i("Recognizer infer time", "millis: " + elapsedTimeMillis);
        }
        return inferLabels;
    }

    @Override
    public float train(float[][][] trainingData, int numEpochs) {
        return (float) 0.0;
    }
}
