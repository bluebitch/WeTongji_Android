/**
 * 
 */
package com.wetongji_android.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

import com.wetongji_android.util.exception.WTException;
import com.wetongji_android.util.net.WTHttpUtil;

import android.text.TextUtils;

/**
 * @author nankonami
 *
 */
public class WTHttpClient 
{
	//Constant values
	private static final int CONNECT_TIMEOUT = 10*1000;
	private static final int READ_TIMEOUT = 10*1000;
	private static final String API_DOMAIN = "http://we.tongji.edu.cn/api/call";
	
	
	public String execute(WTHttpMethod httpMethod, Map<String, String> params) throws WTException
	{
		switch(httpMethod)
		{
		case Post:
			return doPost(params);
		case Get:
			return doGet(params);
		}
		
		return "";
	}
	
	//Implement custom proxy in case proxy server
	private Proxy getProxy()
	{
		String strProxyHost = System.getProperty("http:proxyHost");
		String strProxyPort = System.getProperty("http:proxyPort");
		
		if(!TextUtils.isEmpty(strProxyHost) && !TextUtils.isEmpty(strProxyPort))
			return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(strProxyHost, Integer.valueOf(strProxyPort)));
		else
			return null;
	}
	
	//implement http get request
	public String doGet(Map<String, String> params) throws WTException
	{
		try
		{
			StringBuilder sb = new StringBuilder(API_DOMAIN);
			sb.append("?").append(WTHttpUtil.encodeUrl(params));
			Proxy proxy = getProxy();
			URL url = new URL(sb.toString());
			HttpURLConnection urlConnection;
			
			if(proxy != null)
				urlConnection = (HttpURLConnection)url.openConnection(proxy);
			else
				urlConnection = (HttpURLConnection)url.openConnection();
			
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(false);
			urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(READ_TIMEOUT);
			urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            
            urlConnection.connect();
            
            return handleResponse(urlConnection);
		}catch(IOException e)
		{
			e.printStackTrace();
			throw new WTException("", e);
		}
	}
	
	//implement http post request
	public String doPost(Map<String, String> params) throws WTException
	{
		try
		{
			URL url = new URL(API_DOMAIN);
			Proxy proxy = getProxy();
			HttpURLConnection urlConnection;
			
			if(proxy != null)
				urlConnection = (HttpURLConnection)url.openConnection(proxy);
			else
				urlConnection = (HttpURLConnection)url.openConnection();
			
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoInput(true);
			urlConnection.setUseCaches(false);
			urlConnection.setInstanceFollowRedirects(false);
			urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(READ_TIMEOUT);
			urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            urlConnection.connect();
            
            DataOutputStream outStream = new DataOutputStream(urlConnection.getOutputStream());
            outStream.write(WTHttpUtil.encodeUrl(params).getBytes());
            outStream.flush();
            outStream.close();
            
            return handleResponse(urlConnection);
		}catch(IOException e)
		{
			e.printStackTrace();
			throw new WTException("", e);
		}
	}
	
	private String handleResponse(HttpURLConnection urlConnection) throws WTException
	{
		int iStatus;
		
		try
		{
			iStatus = urlConnection.getResponseCode();
		}catch(IOException e)
		{
			e.printStackTrace();
			urlConnection.disconnect();
			throw new WTException("", e);
		}
		
		if(iStatus != HttpURLConnection.HTTP_OK)
		{
			return handleError(urlConnection);
		}
		
		return null;
	}
	
	private String handleResult(HttpURLConnection urlConnection) throws WTException
	{
		return null;
	}
	
	private String handleError(HttpURLConnection urlConnection) throws WTException
	{
		return null;
	}
	
}

