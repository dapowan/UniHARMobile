package unihar.mobile.model;

import static unihar.mobile.Utils.contains;

import android.app.Activity;
import android.util.Log;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.Console;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import unihar.mobile.Config;
import unihar.mobile.Utils;

public class AutoencoderModelHelper extends ModelHelper{

    private float maskRatio = Config.MASK_RATIO;

    public AutoencoderModelHelper(Activity activity, String saveModelPath){
        super(activity, saveModelPath);
    }

    @Override
    public float train(float[][][] trainingData, int numEpochs) {
        shuffle(trainingData);
        int trainingSize = trainingData.length;
        int seqLength = trainingData[0].length;
        int featureSize = trainingData[0][0].length;
        float[] trainingLosses = new float[numEpochs];
        long timeTag = (long) 0;

        for (int e = 0; e < numEpochs; ++e) {
            float[] epochLosses = new float[(int)Math.floor(trainingSize * 1.0 / batchSize)];
            for (int i = 0; i + batchSize <= trainingSize; i += batchSize) {
                long start = System.currentTimeMillis();
                float[][][] trainingDataBatch = Utils.copyFloat3Array(trainingData, i, i + batchSize);

                AutoencoderTrainingSample sample = preprocess(trainingDataBatch.clone(), maskRatio);


                FloatBuffer trainingMaskedInput = FloatBuffer.allocate(batchSize * seqLength * featureSize);
                Utils.addFloat3Array(trainingMaskedInput, sample.maskedInput);
                IntBuffer trainingPos = IntBuffer.allocate(batchSize * sample.maskedLength * 2);
                Utils.addInt3Array(trainingPos, sample.maskPos);
                FloatBuffer trainingMaskedTarget = FloatBuffer.allocate(batchSize * sample.maskedLength * featureSize);
                Utils.addFloat3Array(trainingMaskedTarget, sample.maskedTarget);

                Map<String, Object> inputs = new HashMap<>();
                inputs.put("x", trainingMaskedInput);
                inputs.put("y", trainingPos);
                inputs.put("z", trainingMaskedTarget);

                FloatBuffer trainingLoss = FloatBuffer.allocate(1);
                Map<String, Object> outputs = new HashMap<>();
                outputs.put("loss", trainingLoss);

                interpreter.runSignature(inputs, outputs, "train");

                trainingLoss.rewind();

                epochLosses[i / batchSize] = trainingLoss.get(0);

                long elapsedTimeMillis = System.currentTimeMillis() - start;
                timeTag += elapsedTimeMillis;
                Log.i("Autoencoder training time", "millis: " + elapsedTimeMillis);
            }
            trainingLosses[e] = Utils.average(epochLosses);
        }
        Log.i("Autoencoder total training time", "millis: " + timeTag);
        save();
        return Utils.average(trainingLosses);
    }

    public AutoencoderTrainingSample preprocess(float[][][] data, float maskRadio){
        int batchSize = data.length;
        int seqLength = data[0].length;

        data = rotate(data);
        int[][] maskPos = randomMask(batchSize, seqLength, maskRadio);
        int[][][] maskPosWrapped = wrapMaskPos(maskPos);
        float[][][] maskedTarget = gather(data, maskPos);
        float[][][] maskedInput = mask(data, maskPos);
        return new AutoencoderTrainingSample(maskedInput, maskPosWrapped, maskedTarget, maskPos[0].length);
    }

    public float[][][] rotate(float[][][] data){
        int batchSize = data.length;
        int seqLength = data[0].length;
        double[][][] rotations = Utils.randomRotation(batchSize);
        for (int i = 0; i < batchSize; i++) {
            RealMatrix rotation = MatrixUtils.createRealMatrix(rotations[i]);
            RealMatrix sample = MatrixUtils.createRealMatrix(Utils.float2Double(data[i]));
            RealMatrix rotatedAcc = sample.getSubMatrix(0, seqLength - 1, 0, 2).multiply(rotation);
            RealMatrix rotatedGyr = sample.getSubMatrix(0, seqLength - 1, 3, 5).multiply(rotation);
            float[][] tmp = Utils.stackFloatArray(Utils.double2Float(rotatedAcc.getData()), Utils.double2Float(rotatedGyr.getData()));
            data[i] = tmp;
        }
        return data;
    }


    public float[][][] mask(float[][][] data, int[][] maskPos){
        int maskedSeqLength = maskPos[0].length;
        int featureSize = data[0][0].length;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < maskedSeqLength; j++) {
                data[i][maskPos[i][j]] = new float[featureSize];
            }
        }
        return data;
    }


    public float[][][] gather(float[][][] data, int[][] maskPos){
        int maskedSeqLength = maskPos[0].length;
        float[][][] dataGathered = new float[data.length][maskedSeqLength][data[0][0].length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < maskedSeqLength; j++) {
                dataGathered[i][j] = data[i][maskPos[i][j]];
            }
        }
        return dataGathered;
    }

    public int[][] randomMask(int batchSize, int length, float maskRadio){
        int maskNum = (int) Math.floor(length * maskRadio);
        int[][] maskPos = new int[batchSize][maskNum];
        Arrays.stream(maskPos).forEach(a -> Arrays.fill(a, -1));
        Random rd = new Random();
        for (int i = 0; i < batchSize; i++) {
            int j = 0;
            while (j < maskNum){
                int m = rd.nextInt(length);
                if (!contains(maskPos[i], m)){
                    maskPos[i][j] = m;
                    j++;
                }
            }
        }
        return maskPos;
    }

    public int[][][] wrapMaskPos(int[][] maskPos){
        int[][][] maskPosWrapped = new int[maskPos.length][maskPos[0].length][2];
        for (int i = 0; i < maskPos.length; i++) {
            for (int j = 0; j < maskPos[0].length; j++) {
                maskPosWrapped[i][j][0] = i;
                maskPosWrapped[i][j][1] = maskPos[i][j];
            }
        }
        return maskPosWrapped;
    }

    public float train(float[][][] trainingData, float[][] trainingLabels, int numEpochs){
        Log.w("Encoder training doesn't require labeled data", "");
        return train(trainingData, numEpochs);
    }
    public int[] infer(float[][][] inferData){
        return null;
    }

    private float[][][] shuffle(float[][][] data){
        Random rand = new Random();

        for (int i = 0; i < data.length; i++) {
            int randomIndexToSwap = rand.nextInt(data.length);
            float[][] temp = data[randomIndexToSwap];
            data[randomIndexToSwap] = data[i];
            data[i] = temp;
        }
        return data;
    }

    private class AutoencoderTrainingSample{
        public float[][][] maskedInput;
        public int[][][] maskPos;
        public float[][][] maskedTarget;
        public int maskedLength;

        public AutoencoderTrainingSample(float[][][] maskedInput, int[][][] maskPos
                , float[][][] maskedTarget, int maskedLength){
            this.maskedInput = maskedInput;
            this.maskPos = maskPos;
            this.maskedTarget = maskedTarget;
            this.maskedLength = maskedLength;
        }
    }

}
