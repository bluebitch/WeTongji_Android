package com.wetongji_android.factory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wetongji_android.data.Banner;

public class BannerFactory {

	private Gson gson = new Gson();

	public List<Banner> createObjects(String jsonStr) {
		List<Banner> result = new ArrayList<Banner>();
		JSONObject bannersJson;
		try {
			bannersJson = new JSONObject(jsonStr);
			result.addAll(createBannerAds(bannersJson
					.getString("BannerAdvertisements")));
			result.add(createBannerInfo(bannersJson
					.getString("BannerInformation")));
			result.add(createBannerActivity(bannersJson
					.getString("BannerActivity")));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	private List<Banner> createBannerAds(String jsonStr) {
		List<Banner> result = new ArrayList<Banner>();
		Type type = new TypeToken<List<Banner>>() {
		}.getType();
		result = gson.fromJson(jsonStr, type);
		return result;
	}

	private Banner createBannerInfo(String jsonStr) throws JSONException {
		Banner result = new Banner();
		JSONObject bannerJson = new JSONObject(jsonStr);
		result.setId(bannerJson.getInt("Id"));
		result.setTitle(bannerJson.getString("Title"));
		result.setPublisher(bannerJson.getString("Organizer"));
		result.setBgColor(Banner.DEFAULT_BG_COLOR);
		JSONArray images = bannerJson.getJSONArray("Images");
		result.setImage(images.get(0).toString());
		return result;
	}

	private Banner createBannerActivity(String jsonStr) throws JSONException {
		Banner result = new Banner();
		JSONObject bannerJson = new JSONObject(jsonStr);
		result.setId(bannerJson.getInt("Id"));
		result.setTitle(bannerJson.getString("Title"));
		result.setPublisher(bannerJson.getString("Organizer"));
		result.setBgColor(Banner.DEFAULT_BG_COLOR);
		result.setImage(bannerJson.getString("Image"));
		return result;
	}

}
