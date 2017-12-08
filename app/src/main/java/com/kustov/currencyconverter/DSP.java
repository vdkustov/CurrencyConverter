package com.kustov.currencyconverter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Vladislav Kustov on 07.12.2017.
 * Default Shared Preferences class
 */

public class DSP {
    private SharedPreferences sp;
    private SharedPreferences.Editor sp_editor;

    public DSP(Context context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp_editor = sp.edit();
    }

    public void clearDSP() {
        sp_editor.clear().commit();
    }

    public void setInt(String key, int value) {
        sp_editor.putInt(key, value).commit();
    }

    public void setString(String key, String value) {
        sp_editor.putString(key, value).commit();
    }

    public void setBoolean(String key, boolean value) {
        sp_editor.putBoolean(key, value).commit();
    }

    public int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }
}