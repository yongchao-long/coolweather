package com.chaochaoweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import net.youmi.android.AdManager;

import com.chaochaoweather.app.R;
import com.chaochaoweather.app.model.City;
import com.chaochaoweather.app.model.CoolWeatherDB;
import com.chaochaoweather.app.model.Country;
import com.chaochaoweather.app.model.Province;
import com.chaochaoweather.app.util.HttpCallbackListener;
import com.chaochaoweather.app.util.HttpUtil;
import com.chaochaoweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity{

	private static final int LEVEL_PROVINCE = 0;
	private static final int LEVEL_CITY = 1;
	private static final int LEVEL_COUNTRY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	//ʡ�б�
	private List<Province> provinceList;
	//���б�
	private List<City> cityList;
	//���б�
	private List<Country> countryList;
	
	//ѡ�е�ʡ��
	private Province selectedProvince;
	//ѡ�еĳ���
	private City selectedCity;
	//��ǰѡ�еļ���
	private int currentLevel;
	//�Ƿ��WeatherActivity����ת������
	private Boolean isFromWeatherActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		AdManager.getInstance(getApplicationContext()).init("3d25f255f9937359", "a90cc647f82069c9", false);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//�Ѿ�ѡ���˳��ж��Ҳ��Ǵ�WeatherActivity����ת�������Ż���ת��WeatherActivity
		if(prefs.getBoolean("city_selected", false) && !isFromWeatherActivity){
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int idex,
					long arg3) {
				// TODO Auto-generated method stub
				if(currentLevel == LEVEL_PROVINCE){
					selectedProvince = provinceList.get(idex);
					queryCities();
				}else if(currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(idex);
					queryCountries();
				}else if(currentLevel == LEVEL_COUNTRY){
					String countryCode = countryList.get(idex).getCountryCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("country_code", countryCode);
					startActivity(intent);
					finish();
				}
			}

		});
		queryProvinces();
	}

	    private void queryProvinces() {
		// TODO Auto-generated method stub
		provinceList = coolWeatherDB.loadProvince();
		if(provinceList.size() > 0){
			dataList.clear();
			for(Province province : provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		} else{
			queryFromServer(null,"province");
		}
	   }
	    
		private void queryCities() {
			// TODO Auto-generated method stub
			cityList = coolWeatherDB.loadCity(selectedProvince.getId());
			if(cityList.size() > 0){
				dataList.clear();
				for(City city : cityList){
					dataList.add(city.getCityName());
				}
				adapter.notifyDataSetChanged();
				listView.setSelection(0);
				titleText.setText(selectedProvince.getProvinceName());
				currentLevel = LEVEL_CITY;
			} else{
				queryFromServer(selectedProvince.getProvinceCode(),"city");
			}
		}
		private void queryCountries() {
			// TODO Auto-generated method stub
			countryList = coolWeatherDB.loadCountry(selectedCity.getId());
			if(countryList.size() > 0){
				dataList.clear();
				for(Country country : countryList){
					dataList.add(country.getCountryName());
				}
				adapter.notifyDataSetChanged();
				listView.setSelection(0);
				titleText.setText(selectedCity.getCityName());
				currentLevel = LEVEL_COUNTRY;
			} else{
				queryFromServer(selectedCity.getCityCode(),"country");
			}
		}
		private void queryFromServer(final String code,final String type) {
			// TODO Auto-generated method stub
			String address;
			if(!TextUtils.isEmpty(code)){
				address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
			}else{
				address = "http://www.weather.com.cn/data/list3/city.xml";
			}
			showProgressDialog();
			HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
				
				@Override
				public void onError(Exception e) {
					// TODO Auto-generated method stub
					//ͨ��runOnUiThread()�����ص����̴߳����߼�
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
							
						}
					});
				}
				
				@Override
				public void finish(String response) {
					// TODO Auto-generated method stub
					Boolean result = false;
					if("province".equals(type)){
						result = Utility.handleProvincesResponse(coolWeatherDB, response);
					}else if("city".equals(type)){
						result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
					}else if("country".equals(type)){
						result = Utility.handleCountriesResponse(coolWeatherDB, response, selectedCity.getId());
					}
					if(result){
						//ͨ��runOnUiThread()�����ص����̴߳����߼�
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								closeProgressDialog();
								if("province".equals(type)){
									queryProvinces();
								}
								else if("city".equals(type)){
									queryCities();
								}
								else if("country".equals(type)){
									queryCountries();
								}
							}
						});
					}
				}
			});
		}
		
		//��ʾ���ȶԻ���
		private void showProgressDialog(){
			if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ��ء�����");
			progressDialog.setCanceledOnTouchOutside(false);
			}
			progressDialog.show();
		}
		//�رս��ȶ̻���
		private void closeProgressDialog(){
			if(progressDialog != null){
				progressDialog.dismiss();
			}
		}
		
		@Override
		public void onBackPressed() {
			// TODO Auto-generated method stub
			if(currentLevel == LEVEL_COUNTRY){
				queryCities();
			}
			else if(currentLevel == LEVEL_CITY){
				queryProvinces();
			}
			else{
				if(isFromWeatherActivity){
					Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
					startActivity(intent);
				}
				finish();
			}
		}
}
