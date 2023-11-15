package com.example.kiosk_electric_charger.SharedPreferences;


import android.content.Context;
import android.content.SharedPreferences;
import android.provider.MediaStore;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PrefsHelper {
    public static final String PREFERENCE_NAME="prefs";
    private Context mContext;
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor prefsEditor;
    private static PrefsHelper instance;
    
    private static final String FolderName_ad = "k_charger";
    private static final String FolderName_apk = "k_charger_apk";
    public static String ad_path;

    public static String apk_path;

    //광고 리스트 키
    public  static final String ad_url_list_key = "adurl";

    //최근 광고 시청 index 키
    public  static final String recent_ad_count_key = "adindex";
    
    public static synchronized PrefsHelper init(Context context){
        if(instance == null)
            instance = new PrefsHelper(context);
        return instance;
    }

    private PrefsHelper(Context context) {
        mContext = context;
        prefs = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE );
        prefsEditor = prefs.edit();
        // 광고 저장 루트
        ad_path = context.getExternalFilesDir(null)+ File.separator+ FolderName_ad;
        apk_path = context.getExternalFilesDir(null)+ File.separator+ FolderName_apk;

    }

    public static String read(String key, String defValue) {
        return prefs.getString(key, defValue);
    }

    public static void write(String key, String value) {
        prefsEditor.putString(key, value);
        prefsEditor.commit();
    }

    public static Integer read(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    public static void write(String key, Integer value) {
        prefsEditor.putInt(key, value).commit();
    }

    public static boolean read(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    public static void write(String key, boolean value) {
        prefsEditor.putBoolean(key, value);
        prefsEditor.commit();
    }


    // clear
    public static void destroyPref() {
        prefsEditor.clear().apply();
    }

    public static void setStringArrayPref(String key, List<String> values) {

        JSONArray a = new JSONArray();

        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }

        if (!values.isEmpty()) {
            prefsEditor.putString(key, a.toString());
        } else {
            prefsEditor.putString(key, null);
        }

        prefsEditor.apply();
    }


    public static List<String> getStringArrayPref(String key) {

        String json = prefs.getString(key, null);
        List<String> urls = new ArrayList<>();

        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);

                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }
}