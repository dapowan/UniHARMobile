package unihar.mobile.network;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class PostFileTask extends AsyncTask<String, Void, Boolean>
{
    protected String fileName;
    protected String metaInfo;
    protected String filePath;
    protected int trainingSize;

    public PostFileTask(String fileName, String metaInfo, String filePath, int trainingSize){
        this.fileName = fileName;
        this.metaInfo = metaInfo;
        this.filePath = filePath;
        this.trainingSize = trainingSize;
    }

    @Override
    protected Boolean doInBackground(String... params)
    {
        String str = params[0];
        HttpURLConnection urlConn;
        BufferedReader bufferedReader = null;
        String result;
        try
        {
            URL url = new URL(str);
            urlConn = (HttpURLConnection)url.openConnection();
            urlConn.setReadTimeout(2000);
            urlConn.setConnectTimeout(2000);
            urlConn.setRequestMethod("POST");
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);

            String auth = "unihar_mobile";
            urlConn.setRequestProperty("authorization", auth);

            String boundary = UUID.randomUUID().toString();
            urlConn.setRequestProperty("content-Type", "multipart/form-data;boundary=" + boundary);
            urlConn.setRequestProperty("file-name", fileName);
            urlConn.setRequestProperty("other-info", metaInfo);
            urlConn.setRequestProperty("training-size", "" + trainingSize);
            DataOutputStream request = new DataOutputStream(urlConn.getOutputStream());

//            request.writeBytes("--" + boundary + "\r\n");
//            request.writeBytes("Content-Disposition: form-data; name=\"description\"\r\n\r\n");
//            request.writeBytes(fileInfo + "\r\n");
//
//            request.writeBytes("--" + boundary + "\r\n");
//            request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n\r\n");
            request.write(FileUtils.readFileToByteArray(new File(filePath)));
//            request.writeBytes("\r\n");
//
//            request.writeBytes("--" + boundary + "--\r\n");
            request.flush();
            request.close();

            int responseCode=urlConn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                return true;
            }
            else {
                Log.e("message", "Model upload fails");
            }

        }
        catch(Exception ex)
        {
            Log.e("Upload Error", "Can not upload json data.", ex);
        }
        return false;
    }

}