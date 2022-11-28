package unihar.mobile.model;

import android.app.Activity;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import unihar.mobile.Utils;

public class ClassificationModelHelper extends ModelHelper{

    public ClassificationModelHelper(Activity activity){
        super(activity);
    }

    public void train(float[][][] trainingData, float[][] trainingLabels, int numEpochs){
        int trainingSize = trainingData.length;
        int sampleSize = trainingData[0].length * trainingData[0][0].length;
        float[] trainingLosses = new float[numEpochs];

        for (int e = 0; e < numEpochs; ++e) {
            double[] epochLosses = new double[(int)Math.ceil(trainingSize * 1.0 / batchSize)];
            for (int i = 0; i < trainingSize; i += batchSize) {
                int index_end = Math.min(i + batchSize, trainingSize);
                float[][][] trainingInputsBatch1 = Utils.copyFloat3Array(trainingData, i, index_end);
                FloatBuffer trainingInputs1 = FloatBuffer.allocate(batchSize * sampleSize);
                trainingInputs1 = Utils.addFloat3Array(trainingInputs1, trainingInputsBatch1);

                float[][] trainingInputsBatch2 = Utils.copyFloat2Array(trainingLabels, i, index_end);
                FloatBuffer trainingInputs2 = FloatBuffer.allocate(batchSize * classNum);
                trainingInputs2 = Utils.addFloat2Array(trainingInputs2, trainingInputsBatch2);

                Map<String, Object> inputs = new HashMap<>();
                inputs.put("x", trainingInputs1);
                inputs.put("y", trainingInputs2);

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

    public int[] infer(float[][][] inferData){
        int inferSize = inferData.length;
        int sampleSize = inferData[0].length * inferData[0][0].length;
        int[] inferLabels = new int[inferSize];

        for (int i = 0; i < inferSize; i += batchSize) {
            int index_end = Math.min(i + batchSize, inferSize);
            float[][][] inferInputsBatch = Utils.copyFloat3Array(inferData, i, index_end);

            FloatBuffer inferInputs = FloatBuffer.allocate(batchSize * sampleSize);
            inferInputs = Utils.addFloat3Array(inferInputs, inferInputsBatch);
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("x", inferInputs);

            FloatBuffer inferProbs = FloatBuffer.allocate(batchSize * classNum);
            FloatBuffer inferLogits = FloatBuffer.allocate(batchSize * classNum);
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("output", inferProbs);
            outputs.put("logits", inferLogits);

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
        }
        return inferLabels;
    }

    @Override
    public void train(float[][][] trainingData, int numEpochs) {

    }
}
