package com.chaochaoweather.app.activity;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;

import com.chaochaoweather.app.R;
import com.chaochaoweather.app.service.AutoUpdateService;
import com.chaochaoweather.app.util.HttpCallbackListener;
import com.chaochaoweather.app.util.HttpUtil;
import com.chaochaoweather.app.util.Utility;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements android.view.View.OnClickListener{

	//切换城市按钮
	private Button switchCity;
	//更新天气按钮
	private Button refreshWeather;
	
	private LinearLayout weatherInfoLayout;
	//用于显示城市名
	private TextView cityNameText;
	//用于显示发布时间
	private TextView publishText;
	//用于显示天气描述信息
	private TextView weatherDespText;
	//用于显示温度1
	private TextView temp1Text;
	//用于显示温度2
	private TextView temp2Text;
	//用于显示当前时间
	private TextView currentDateText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//初始化控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.public_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener( this);
		
		String countryCode = getIntent().getStringExtra("country_code");
		
		if(!TextUtils.isEmpty(countryCode)){
			//有县级代号就去查询天气
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countryCode);
		}else {
			//没有县级代号是直接显示本地天气
			showWeather();
		}
		
		//实例化广告条
		AdView adView = new AdView(getApplicationContext(), AdSize.FIT_SCREEN);
		//获取要嵌入广告条的布局
		LinearLayout adLayout = (LinearLayout) findViewById(R.id.adLayout);
		//将广告条加入布局中
		adLayout.addView(adView);
	}
    //查询县级代号所对应的天气代号
	private void queryWeatherCode(String countryCode) {
		// TODO Auto-generated method stub
		String address = "http://www.weather.com.cn/data/list3/city"+countryCode+".xml";
		queryFromServer(address,"countryCode");
	}
	//查询天气代号所对应的天气
	private void queryWeatherInfo(String weatherCode){
		String address = "http://www.weather.com.cn/adat/cityinfo/" + weatherCode+".html";
		queryFromServer(address,"weatherCode");
	}
	
	//根据传入的地址和类型去服务器查询天气代号和天气信息
	private void queryFromServer(final String address , final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						publishText.setText("同步失败");
					}
				});
			}
			
			@Override
			public void finish(final String response) {
				// TODO Auto-generated method stub
				if("countryCode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						String[] array = response.split("\\|");
						if(array != null && array.length == 2){
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if("weatherCode".equals(type)){
					//处理服务器返回的天气信息
					Utility.handleWeatherResponse(getApplicationContext(), response);
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							showWeather();
						}
					});
				}
			}
		});
	}
	//从SharedPreferences文件中读取存储的天气信息，并显示到界面上
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		//String name = prefs.getString("city_name", "");
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("今天"+prefs.getString("publish_time", "")+"发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
		Intent intent = new Intent(getApplicationContext(), AutoUpdateService.class);
		startService(intent);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			String weatherCode = prefs.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)){
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}
	
}
