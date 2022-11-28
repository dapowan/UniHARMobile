package unihar.mobile.model;

import android.app.Activity;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import unihar.mobile.Utils;

public class RegressionModelHelper extends ModelHelper{

    public RegressionModelHelper(Activity activity){
        super(activity);
    }

    @Override
    public void train(float[][][] trainingData, int numEpochs) {
        int trainingSize = trainingData.length;
        int sampleSize = trainingData[0].length * trainingData[0][0].length;
        float[] trainingLosses = new float[numEpochs];

        for (int e = 0; e < numEpochs; ++e) {
            double[] epochLosses = new double[(int)Math.ceil(trainingSize * 1.0 / batchSize)];
            for (int i = 0; i < trainingSize; i += batchSize) {
                int index_end = Math.min(i + batchSize, trainingSize);
                float[][][] trainingInputsBatch1 = Utils.copyFloat3Array(trainingData, i, index_end);
                FloatBuffer trainingInputs = FloatBuffer.allocate(batchSize * sampleSize);
                trainingInputs = Utils.addFloat3Array(trainingInputs, trainingInputsBatch1);

                Map<String, Object> inputs = new HashMap<>();
                inputs.put("x", trainingInputs);

                FloatBuffer trainingLoss = FloatBuffer.allocate(1);
                Map<String, Object> outputs = new HashMap<>();
                outputs.put("loss", trainingLoss);

                interpreter.runSignature(inputs, outputs, "train");

                trainingLoss.rewind();

                epochLosses[i / batchSize] = trainingLoss.get(0);
            }
            trainingLosses[e] = (float)Arrays.stream(epochLosses).average().getAsDouble();
        }
        save();
    }

    public void train(float[][][] trainingData, float[][] trainingLabels, int numEpochs){}
    public int[] infer(float[][][] inferData){
        return null;
    }

}
