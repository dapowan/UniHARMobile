package unihar.mobile;

import android.Manifest;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import java.io.File;
import java.util.Date;

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
}
