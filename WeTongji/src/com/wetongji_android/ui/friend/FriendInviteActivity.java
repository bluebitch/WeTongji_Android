package com.wetongji_android.ui.friend;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.wetongji_android.R;
import com.wetongji_android.util.net.HttpRequestResult;

public class FriendInviteActivity extends SherlockFragmentActivity implements
		OnClickListener, OnCheckedChangeListener, LoaderCallbacks<HttpRequestResult> 
{	
	private static final String TAG_FRIEND_INVITE_FRAGMENT = "FRIEND_INVITE_FRAGMENT";
	
	private ToggleButton btnInvite;
	
	@Override
	protected void onCreate(Bundle arg0) 
	{
		super.onCreate(arg0);
		setContentView(R.layout.activity_friend_invite);
		
		setupActionBar();
		
		FriendListFragment fragment = new FriendListFragment();
		getSupportFragmentManager().beginTransaction()
			.add(R.id.friend_invite_list_container, fragment, TAG_FRIEND_INVITE_FRAGMENT)
			.commit();
	}
	
	@Override
	protected void onPause() 
	{
		super.onPause();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
	}

	private void setupActionBar()
	{
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowCustomEnabled(true);
		ab.setDisplayShowHomeEnabled(true);
		ab.setDisplayShowTitleEnabled(true);
		View v = getLayoutInflater().inflate(R.layout.actionbar_invite_friend, null);
		ab.setCustomView(v);
		btnInvite = (ToggleButton)v.findViewById(R.id.btn_on_invite);
		btnInvite.setOnCheckedChangeListener(this);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) 
	{

	}

	@Override
	public void onClick(View v) 
	{

	}

	@Override
	public Loader<HttpRequestResult> onCreateLoader(int arg0, Bundle arg1) 
	{
		return null;
	}

	@Override
	public void onLoadFinished(Loader<HttpRequestResult> arg0,
			HttpRequestResult arg1) 
	{
		
	}

	@Override
	public void onLoaderReset(Loader<HttpRequestResult> arg0) 
	{
		
	}
}