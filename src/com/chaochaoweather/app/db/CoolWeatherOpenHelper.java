package com.chaochaoweather.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class CoolWeatherOpenHelper extends SQLiteOpenHelper{

	//Province���������
			public static final String CREATE_PROVINCE = "create table Province ("
			+ "id integer primary key autoincrement, "
		    + "province_name text, "
			+ "province_code text)";
			//City���������
			public static final String CREATE_CITY = "create table City ("
			+"id integer primary key autoincrement, "
			+"city_name text, "
			+"city_code text, "
			+"province_id integer)";
			//Country���������
			public static final String CREATE_COUNTRY = "create table Country ("
					+"id integer primary key autoincrement, "
					+"country_name text, "
					+"country_code text, "
					+"city_id integer)";
			
			public CoolWeatherOpenHelper(Context context, String name,
					CursorFactory factory, int version) {
				super(context, name, factory, version);
				// TODO Auto-generated constructor stub
			}

			@Override
			public void onCreate(SQLiteDatabase db) {
				// TODO Auto-generated method stub
				db.execSQL(CREATE_PROVINCE);//����Province��
				db.execSQL(CREATE_CITY);//����City��
				db.execSQL(CREATE_COUNTRY);//����Country��
			}

			@Override
			public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
}