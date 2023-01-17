package unihar.mobile.network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class PostJSONTask extends AsyncTask<String, Void, String>
{
    protected String fileName;

    @Override
    protected String doInBackground(String... params)
    {

        String str = params[0];
        String record = params[1];
        fileName = params[2];
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

            OutputStream os = urlConn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(record);
            writer.flush();
            writer.close();
            os.close();

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