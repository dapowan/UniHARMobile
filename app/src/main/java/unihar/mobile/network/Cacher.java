package unihar.mobile.network;

import android.content.Context;
import android.content.SharedPreferences;

public class Cacher {

    public static final String NAME_CONFIG = "cache_config";

    private Context context;


    public Cacher(Context context){
        this.context = context;
    }

    public void addString(String name, String data){
        SharedPreferences sharedPref = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.putString(name, data);
        editor.commit();
    }

    public String getString(String name){
        SharedPreferences sharedPref = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        return sharedPref.getString(name, null);
    }

}
