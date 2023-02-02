package unihar.mobile.network;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.net.ssl.HttpsURLConnection;

public class GetFileTask extends AsyncTask<String, Void, Boolean>
{
    protected String filePath;
    protected int responseCode;

    public GetFileTask(String filePath){
        this.filePath = filePath;
    }

    @Override
    protected Boolean doInBackground(String... params)
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
//            bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            responseCode = urlConn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
//                FileWriter fw = new FileWriter(filePath);
//                BufferedWriter bw = new BufferedWriter(fw);
//                char data[] = new char[1024];
//                int count;
//                while((count = bufferedReader.read(data, 0, 1024)) != -1)
//                {
//                    bw.write(data, 0, count);
//                }
//                bw.flush();
//                bw.close();
                Long re = Files.copy(urlConn.getInputStream(), new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                if (re > 0) return true;
            }else{
                Log.e("message", "get file failed!");
                return false;
            }
        }
        catch(Exception ex)
        {
            Log.e("Fetch Error", "Can not get file data.", ex);
        }
        return false;
    }

    protected boolean isNotModified(){
        return responseCode == HttpsURLConnection.HTTP_NOT_MODIFIED
                || responseCode == HttpsURLConnection.HTTP_OK;
    }
}