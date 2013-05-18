/**
 * 
 */
package com.wetongji_android.net.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.wetongji_android.util.common.WTUtility;
import com.wetongji_android.util.exception.WTException;
import com.wetongji_android.util.net.ApiHelper;
import com.wetongji_android.util.net.HttpRequestResult;
import com.wetongji_android.util.net.HttpUtil;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author nankonami
 *
 */
public class HttpClient 
{
	private static final String TAG = "HttpClient";
	//Constant values
	private static final int CONNECT_TIMEOUT = 10*1000;
	private static final int READ_TIMEOUT = 10*1000;
	//private static final String API_DOMAIN = "http://we.tongji.edu.cn/api/call";
	private static final String API_DOMAIN="http://leiz.name:8080/api/call";
	
	private static final String HTTP_TIMEOUT = "HttpTimeout";
	
	private String strCurrentMethod;
	
	public HttpRequestResult execute(HttpMethod httpMethod, Bundle params) throws WTException
	{
		switch(httpMethod)
		{
		case Post:
			return doPost(params);
		case Get:
			return doGet(params);
		}
		
		return null;
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
	
	//Set common http property
	private void setRequestProperty(HttpURLConnection httpURLConnection)
	{
		httpURLConnection.setConnectTimeout(CONNECT_TIMEOUT);
		httpURLConnection.setReadTimeout(READ_TIMEOUT);
		httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
		httpURLConnection.setRequestProperty("Charset", "UTF-8");
		httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
		httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
	}
	
	//Implement http get request
	public HttpRequestResult doGet(Bundle params) throws WTException
	{
		strCurrentMethod = params.getString(ApiHelper.API_ARGS_METHOD);
		
		try
		{
			StringBuilder sb = new StringBuilder(API_DOMAIN);
			sb.append("?").append(HttpUtil.encodeUrl(params));
			Log.d("HttpClient", "URL="+sb.toString());
			Proxy proxy = getProxy();
			URL url = new URL(sb.toString());
			HttpURLConnection urlConnection;
			
			if(proxy != null)
				urlConnection = (HttpURLConnection)url.openConnection(proxy);
			else
				urlConnection = (HttpURLConnection)url.openConnection();
			
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(false);
			
			setRequestProperty(urlConnection);
            
            urlConnection.connect();
            
            return handleResponse(urlConnection);
		}catch(IOException e)
		{
			e.printStackTrace();
			throw new WTException("GET", params.getString(ApiHelper.API_ARGS_METHOD), HTTP_TIMEOUT, e);
		}
	}
	
	//Implement http post request
	public HttpRequestResult doPost(Bundle params) throws WTException
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
            urlConnection.setRequestProperty("Content-Type", "application/json");
            
            setRequestProperty(urlConnection);
            
            urlConnection.connect();
            
            DataOutputStream outStream = new DataOutputStream(urlConnection.getOutputStream());
            outStream.write(HttpUtil.encodeUrl(params).getBytes());
            outStream.flush();
            outStream.close();
            
            return handleResponse(urlConnection);
		}catch(IOException e)
		{
			e.printStackTrace();
			throw new WTException("POST", params.getString(ApiHelper.API_ARGS_METHOD), HTTP_TIMEOUT, e);
		}
	}
	
	private HttpRequestResult handleResponse(HttpURLConnection urlConnection) throws WTException
	{
		int iStatus = 200;
		
		try
		{
			iStatus = urlConnection.getResponseCode();
			WTUtility.log(TAG, "Response Code: " + iStatus);
		}catch(IOException e)
		{
			urlConnection.disconnect();
			e.printStackTrace();
			throw new WTException(urlConnection.getRequestMethod(), strCurrentMethod, 
								HTTP_TIMEOUT, iStatus, e);
		}
		
		//The connection is not successful
		if(iStatus != HttpURLConnection.HTTP_OK)
		{
			return handleError(urlConnection);
		}
		
		return handleResult(urlConnection);
	}
	
	private HttpRequestResult handleResult(HttpURLConnection urlConnection) throws WTException
	{
		String strResponse = readResponse(urlConnection);
		
		try 
		{
			JSONObject json = new JSONObject(strResponse);
			JSONObject status = json.getJSONObject("Status");
			int id = Integer.valueOf(status.getString("Id"));
			String memo = status.getString("Memo");
			
			if(id != 0)
			{
				return new HttpRequestResult(id, memo);
			}
			
			return new HttpRequestResult(id, json.getJSONObject("Data").toString());
		} catch (JSONException e) 
		{
			e.printStackTrace();
			throw new WTException();
		}
	}
	
	private HttpRequestResult handleError(HttpURLConnection urlConnection) throws WTException
	{
		String strError = readResponse(urlConnection);
		
		try 
		{
			return new HttpRequestResult(urlConnection.getResponseCode(), strError);
		} catch (IOException e) 
		{
			e.printStackTrace();
			throw new WTException(strError, e);
		}
	}
	
	private String readResponse(HttpURLConnection urlConnection) throws WTException
	{
		InputStream is = null;
		BufferedReader bfReader = null;
		
		try
		{
			is = urlConnection.getInputStream();
			String strConEncode = urlConnection.getContentEncoding();
			
			if(strConEncode != null && !strConEncode.equals("") && strConEncode.equals("gzip"))
			{
				is = new GZIPInputStream(is);
			}
			
			bfReader = new BufferedReader(new InputStreamReader(is));
			StringBuilder sbError = new StringBuilder();
			String strLine;
			
			while((strLine = bfReader.readLine()) != null)
			{
				sbError.append(strLine);
			}
			
			return sbError.toString();
		}catch(IOException e)
		{
			e.printStackTrace();
			throw new WTException(HTTP_TIMEOUT, e);
		}finally
		{
			WTUtility.closeResource(is);
			WTUtility.closeResource(bfReader);
			urlConnection.disconnect();
		}
	}
}

