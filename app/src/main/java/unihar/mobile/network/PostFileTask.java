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

public class PostFileTask extends AsyncTask<String, Void, String>
{

    @Override
    protected String doInBackground(String... params)
    {
        String str = params[0];
        String fileName = params[1];
        String fileInfo = params[2];
        String filePath = params[3];
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
            urlConn.setRequestProperty("Content-Type", "application/json");

            String auth = "unihar_mobile";
            urlConn.setRequestProperty("Authorization", auth);

            String boundary = UUID.randomUUID().toString();
            urlConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream request = new DataOutputStream(urlConn.getOutputStream());

            request.writeBytes("--" + boundary + "\r\n");
            request.writeBytes("Content-Disposition: form-data; name=\"description\"\r\n\r\n");
            request.writeBytes(fileInfo + "\r\n");

            request.writeBytes("--" + boundary + "\r\n");
            request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n\r\n");
            request.write(FileUtils.readFileToByteArray(new File(filePath)));
            request.writeBytes("\r\n");

            request.writeBytes("--" + boundary + "--\r\n");
            request.flush();
            request.close();

            int responseCode=urlConn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                result = stringBuilder.toString();
            }
            else {
                Log.e("message", "upload records fail");
                result = null;
            }

        }
        catch(Exception ex)
        {
            Log.e("Fetch Error", "Can not get json data.", ex);
            result = null;
        }
        finally
        {
            if(bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}