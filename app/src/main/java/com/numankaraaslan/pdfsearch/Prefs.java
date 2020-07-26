package com.numankaraaslan.pdfsearch;

import android.content.*;

public class Prefs
{
	private static SharedPreferences sharedpreferences;

	public static void set_statics(Context context)
	{
		sharedpreferences = context.getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
	}

	public static void put_string(String key, String val)
	{
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.putString(key, val);
		editor.apply();
	}

	public static String get_string(String key)
	{
		return sharedpreferences.getString(key, "");
	}
}
