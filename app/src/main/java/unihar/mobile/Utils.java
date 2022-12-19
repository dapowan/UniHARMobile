package unihar.mobile;

import android.Manifest;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;


public class Utils {

    public static String unifyIDText(int id){
        return String.format("%03d", id);
    }

    public static String dataFolderPath(String recordName){
        String date = Config.DATE_FORMAT.format(new Date());
        return Config.RECORD_PATH + File.separator + date + File.separator + recordName;
    }

    public static void checkPermission(Activity activity){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                //do when permission is granted
            } else {
                //request for the permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            }
        }else {
            if(activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED){ // && activity.checkSelfPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                //do whatever you want here
            }else{
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE}; // , Manifest.permission.MANAGE_EXTERNAL_STORAGE
                activity.requestPermissions(permissions, 101);
            }
        }
    }

    public static boolean createFolder(String folderPath){
        File folder = new File(folderPath);
        if (!folder.exists()) {
            return folder.mkdirs();
        }
        return true;
    }

    public static MappedByteBuffer loadModelFile(Activity activity, String path) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(path);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public static float[][][] randomFloat3Array(int[] shape){
        Random rd = new Random();
        float[][][] result = new float[shape[0]][shape[1]][shape[2]];
        for (int i = 0; i < shape[0]; i++) {
            for (int j = 0; j < shape[1]; j++) {
                for (int k = 0; k < shape[2]; k++) {
                    result[i][j][k] = rd.nextFloat();
//                    if (i > 5) {
////                        result[i][j][k] = rd.nextFloat();
//                        result[i][j][k] = 0.0f;
//                    }else {
//                        result[i][j][k] = 1.0f;
//                    }
                }
            }
        }
        return result;
    }

    public static ByteBuffer float3ArrayToFloatBuffer(float[][][] data){
        int d1 = data.length;
        int d2 = data[0].length;
        int d3 = data[0][0].length;
        int fsize = java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(d1 * d2 * d3 * fsize).order(ByteOrder.nativeOrder());
        for (int i = 0; i < d1; i++) {
            for (int j = 0; j < d2; j++) {
                for (int k = 0; k < d3; k++) {
                    buffer.putFloat(data[i][j][k]);
                }
            }
        }
        buffer.rewind();
        return buffer;
    }

    public static FloatBuffer addFloat2Array(FloatBuffer buffer, float[][] data){
        int d1 = data.length;
        int d2 = data[0].length;
        for (int i = 0; i < d1; i++) {
            for (int j = 0; j < d2; j++) {
                buffer.put(data[i][j]);
            }
        }
        buffer.rewind();
        return buffer;
    }

    public static FloatBuffer addFloat3Array(FloatBuffer buffer, float[][][] data){
        int d1 = data.length;
        int d2 = data[0].length;
        int d3 = data[0][0].length;
        for (int i = 0; i < d1; i++) {
            for (int j = 0; j < d2; j++) {
                for (int k = 0; k < d3; k++) {
                    buffer.put(data[i][j][k]);
                }
            }
        }
        buffer.rewind();
        return buffer;
    }

    public static IntBuffer addInt3Array(IntBuffer buffer, int[][][] data){
        int d1 = data.length;
        int d2 = data[0].length;
        int d3 = data[0][0].length;
        for (int i = 0; i < d1; i++) {
            for (int j = 0; j < d2; j++) {
                for (int k = 0; k < d3; k++) {
                    buffer.put(data[i][j][k]);
                }
            }
        }
        buffer.rewind();
        return buffer;
    }

    public static float[][]  copyFloat2Array(float[][] data, int start, int end){
        int d1 = data[0].length;
        float[][] newData = new float[end - start][d1];
        if (end - start >= 0) System.arraycopy(data, start, newData, 0, end - start);
        return newData;
    }

    public static float[][][]  copyFloat3Array(float[][][] data, int start, int end){
        int d2 = data[0].length;
        int d3 = data[0][0].length;
        float[][][] newData = new float[end - start][d2][d3];
        if (end - start >= 0) System.arraycopy(data, start, newData, 0, end - start);
        return newData;
    }

    public static float[][] randomFloat2Array(int[] shape, int max){
        Random rd = new Random();
        float[][] result = new float[shape[0]][shape[1]];
        for (int i = 0; i < shape[0]; i++) {
            result[i][rd.nextInt(max)] = 1.0f;
        }
        return result;
    }

    public static double[][] float2Double(float[][] data){
        int d1 = data.length;
        int d2 = data[0].length;
        double[][] dataNew = new double[d1][d2];
        for (int i = 0; i < d1; i++) {
            float[] fa = data[i];
            double[] da = new double[d2];
            IntStream.range(0, d2).forEach(index -> da[index] = fa[index]);
            dataNew[i] = da;
        }
        return dataNew;
    }

    public static float[][] double2Float(double[][] data){
        int d1 = data.length;
        int d2 = data[0].length;
        float[][] dataNew = new float[d1][d2];
        for (int i = 0; i < d1; i++) {
            double[] fa = data[i];
            float[] da = new float[d2];
            IntStream.range(0, d2).forEach(index -> da[index] = (float) fa[index]);
            dataNew[i] = da;
        }
        return dataNew;
    }

    public static float[][] stackFloatArray(float[][] data1, float[][] data2){
        int d1 = data1.length;
        int d2 = data1[0].length;
        float[][] dataNew = new float[d1][d2 * 2];
        for (int i = 0; i < d1; i++) {
            for (int j = 0; j < d2 * 2; j++) {
                if (j < d2){
                    dataNew[i][j] = data1[i][j];
                }else{
                    dataNew[i][j] = data2[i][j - d2];
                }
            }
        }
        return dataNew;
    }

    public static boolean contains(final int[] arr, final int key) {
        return Arrays.asList(arr).contains(key);
    }

    public static double[][][] randomRotation(int size){
        double[][][] rotations = new double[size][3][3];
        Random rd1 = new Random();
        Random rd2 = new Random();
        Random rd3 = new Random();
        Random rd4 = new Random();
        double eps = Math.pow(10, -6);
        for (int i = 0; i < size; i++) {
            double q1 = rd1.nextFloat();
            double q2 = rd2.nextFloat();
            double q3 = rd3.nextFloat();
            double q4 = rd4.nextFloat();
            double qn = q1 * q1 + q2 * q2+ q3 * q3+ q4 * q4 + eps;
            rotations[i] = new Rotation(q1 / qn, q2 / qn, q3 / qn, q4 / qn
                    , false).getMatrix();
        }
        return rotations;
    }
}
