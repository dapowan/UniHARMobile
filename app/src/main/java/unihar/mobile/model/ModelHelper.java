package unihar.mobile.model;

import android.app.Activity;

import org.tensorflow.lite.Interpreter;
//import org.tensorflow.lite.gpu.CompatibilityList;
//import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.Map;

import unihar.mobile.Config;
import unihar.mobile.Utils;

public abstract class ModelHelper {

    protected Interpreter interpreter;
    protected int batchSize = 10;
    protected String saveModelPath;
    protected Activity activity;

    public ModelHelper(Activity activity){
        this.activity = activity;
    }

    public void initFromAsset(String assetName){
        MappedByteBuffer modelBuffer = null;
        try {
            modelBuffer = Utils.loadModelFile(activity, assetName);
            interpreter = new Interpreter(modelBuffer); // , getOptions()
        } catch (IOException e) {
            e.printStackTrace();
        }
        restore();
    }

    public void initFromFile(String modelName){
        File modelFile = new File(Config.RECORD_PATH + File.separator + modelName);
        interpreter = new Interpreter(modelFile); // , getOptions()
        restore();
    }

//    private Interpreter.Options getOptions(){
//        Interpreter.Options options = new Interpreter.Options();
//        CompatibilityList compatList = new CompatibilityList();
//
//        if(compatList.isDelegateSupportedOnThisDevice()){
//            // if the device has a supported GPU, add the GPU delegate
//            GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
//            GpuDelegate gpuDelegate = new GpuDelegate(delegateOptions);
//            options.addDelegate(gpuDelegate);
//        } else {
//            // if the GPU is not supported, run on 4 threads
//            options.setNumThreads(4);
//        }
//        return null;
//    }

    public abstract void train(float[][][] trainingData, float[][] trainingLabels, int numEpochs);

    public abstract void train(float[][][] trainingData, int numEpochs);


    public abstract int[] infer(float[][][] inferData);

    public int infer(float[][] inferData){
        int BATCH_SIZE = 1;
        float[][][] newInferData = new float[BATCH_SIZE][inferData.length][inferData[0].length];
        newInferData[0] = inferData;
        return infer(newInferData)[0];
    }

    public void save(){
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("checkpoint_path", saveModelPath);
        Map<String, Object> outputs = new HashMap<>();
        interpreter.runSignature(inputs, outputs, "save");
    }

    public void restore(){
        if (new File(saveModelPath).isFile()) {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("checkpoint_path", saveModelPath);
            Map<String, Object> outputs = new HashMap<>();
            interpreter.runSignature(inputs, outputs, "restore");
        }
    }
}
