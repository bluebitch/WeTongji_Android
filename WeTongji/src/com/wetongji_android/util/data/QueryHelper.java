package com.wetongji_android.util.data;

import java.util.Calendar;

import android.os.Bundle;

public class QueryHelper {
	
	public static final String ARGS_ORDER_BY="order";
	public static final String ARGS_ORDER_BY_LIKE="Like";
	public static final String ARGS_ORDER_BY_TIME="Begin";
	public static final String ARGS_ORDER_BY_PUBLISH_TIME="Id";
	public static final String ARGS_ASCENDING="ascending";
	public static final String ARGS_HAS_EXPIRED="hasExpired";
	public static final String ARGS_HAS_CHANNEL_1="hasChannel1";
	public static final String ARGS_HAS_CHANNEL_2="hasChannel2";
	public static final String ARGS_HAS_CHANNEL_3="hasChannel3";
	public static final String ARGS_HAS_CHANNEL_4="hasChannel4";
	public static final String ARGS_BEGIN="begin";
	public static final String ARGS_END="end";
	
	public static Bundle getActivityQueryArgs(String orderBy, boolean ascending, boolean hasExpired, boolean hasChannel1, boolean hasChannel2, boolean hasChannel3, boolean hasChannel4){
		Bundle bundle=new Bundle();
		bundle.putString(ARGS_ORDER_BY, orderBy);
		bundle.putBoolean(ARGS_HAS_CHANNEL_1, hasChannel1);
		bundle.putBoolean(ARGS_HAS_CHANNEL_2, hasChannel2);
		bundle.putBoolean(ARGS_HAS_CHANNEL_3, hasChannel3);
		bundle.putBoolean(ARGS_HAS_CHANNEL_4, hasChannel4);
		bundle.putBoolean(ARGS_ASCENDING, ascending);
		bundle.putBoolean(ARGS_HAS_EXPIRED, hasExpired);
		return bundle;
	}
	
	public static Bundle getEventQueryArgs(Calendar begin){
		Bundle bundle=new Bundle();
		begin.set(Calendar.HOUR_OF_DAY, 0);
		begin.set(Calendar.MINUTE, 0);
		begin.set(Calendar.SECOND, 0);
		bundle.putSerializable(ARGS_BEGIN, begin);
		Calendar end=Calendar.getInstance();
		end=begin;
		end.add(Calendar.DAY_OF_YEAR, 7);
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);
		bundle.putSerializable(ARGS_END, end);
		return bundle;
	}
	
}
