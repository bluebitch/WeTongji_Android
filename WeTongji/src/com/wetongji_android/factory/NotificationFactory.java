package com.wetongji_android.factory;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wetongji_android.data.Notification;
import com.wetongji_android.util.date.DateParser;

public class NotificationFactory 
{
	private int nextPage;
	
	public NotificationFactory()
	{
		
	}
	
	public List<Notification> createObjects(String jsonStr)
	{
		List<Notification> results = new ArrayList<Notification>();
		int nextPage = 0;
		
		try 
		{
			JSONObject data = new JSONObject(jsonStr);
			nextPage = data.getInt("NextPager");
			setNextPage(nextPage);
			JSONArray notifications = data.getJSONArray("Notifications");
			for(int i = 0; i < notifications.length(); i++)
			{
				results.add(createObject(notifications.getJSONObject(i)));
			}
		} catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
		return results;
	}
	
	private Notification createObject(JSONObject json)
	{
		Notification notification = new Notification();
		
		try 
		{
			notification.setId(json.getInt("Id"));
			notification.setDescription(json.getString("Description"));
			notification.setTitle(json.getString("Title"));
			notification.setRead(json.getBoolean("UnRead"));
			String type = json.getString("SourceType");
			if(type.equals("CourseInvite"))
			{
				notification.setType(1);
			}else if(type.equals("FriendInvite"))
			{
				if (notification.getTitle().contains("已经接受")) {
					notification.setType(4);
				} else {
					notification.setType(2);
				}
			}else
			{
				notification.setType(3);
			}
			notification.setSourceId(json.getInt("SourceId"));
			JSONObject detail = json.getJSONObject("SourceDetails");
			notification.setSentAt(DateParser.parseDateAndTime(detail.getString("SentAt")));
			notification
				.setAcceptedAt(detail.getString("AcceptedAt").equals("null") ? null
						: DateParser.parseDateAndTime(detail
								.getString("AcceptedAt")));
			
			notification.setAccepted(!detail.getString("AcceptedAt").equals("null"));
			notification.setRejectedAt(DateParser.parseDateAndTime(detail.getString("RejectedAt")));
			notification.setFrom(detail.getString("From"));
		} catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
		return notification;
	}

	public int getNextPage() 
	{
		return nextPage;
	}

	public void setNextPage(int nextPage) 
	{
		this.nextPage = nextPage;
	}
}
