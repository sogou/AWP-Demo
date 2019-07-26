// Copyright 2019 The Sogou Inc. All rights reserved.
// Author: AWP TEAM.

package com.awp.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

public class SharedPrefsUtils {
    public static final String PROXY = "proxy_override";

    private static SharedPrefsUtils sInstance;
    private SharedPreferences mPrefs;

    public static void initialize(Context context) {
        sInstance = new SharedPrefsUtils(context);
    }

    public static SharedPrefsUtils getInstance() {
        return sInstance;
    }

    private SharedPrefsUtils(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public void put(String key, Object obj) {
        SharedPreferences.Editor editor = mPrefs.edit();

        if (obj instanceof Boolean) {
            editor.putBoolean(key, (Boolean) obj);
        } else if (obj instanceof Float) {
            editor.putFloat(key, (Float) obj);
        } else if (obj instanceof Integer) {
            editor.putInt(key, (Integer) obj);
        } else if (obj instanceof Long) {
            editor.putLong(key, (Long) obj);
        } else {
            editor.putString(key, (String) obj);
        }
        editor.commit();
    }

    public Object get(String key, Object defaultObj) {
        Object obj = null;
        if (defaultObj instanceof Boolean) {
            obj = mPrefs.getBoolean(key, (Boolean) defaultObj);
        } else if (defaultObj instanceof Float) {
            obj = mPrefs.getFloat(key, (Float) defaultObj);
        } else if (defaultObj instanceof Integer) {
            obj = mPrefs.getInt(key, (Integer) defaultObj);
        } else if (defaultObj instanceof Long) {
            obj = mPrefs.getLong(key, (Long) defaultObj);
        } else {
            obj = mPrefs.getString(key, (String) defaultObj);
        }
        return obj == null ? defaultObj : obj;
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.remove(key);
        editor.commit();
    }

    public void clear() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.clear();
        editor.commit();
    }
}
