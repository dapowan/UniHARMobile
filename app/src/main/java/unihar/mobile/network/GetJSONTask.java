package unihar.mobile.network;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GetJSONTask extends AsyncTask<String, Void, JSONObject>
{
    protected int responseCode;

    @Override
    protected JSONObject doInBackground(String... params)
    {

        String str = params[0];
        HttpURLConnection urlConn;
        BufferedReader bufferedReader = null;
        try
        {
            URL url = new URL(str);
            urlConn = (HttpURLConnection)url.openConnection();
//            urlConn.setReadTimeout(2000);
//            urlConn.setConnectTimeout(2000);
            bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            responseCode = urlConn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String re = stringBuilder.toString();
                return new JSONObject(re);
            }else{
                Log.e("message", "get json fail");
                return null;
            }
        }
        catch(Exception ex)
        {
            Log.e("Fetch Error", "Can not get json data.", ex);
            return null;
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
    }

    protected boolean isNotModified(){
        return responseCode == HttpsURLConnection.HTTP_NOT_MODIFIED
                || responseCode == HttpsURLConnection.HTTP_OK;
    }
}