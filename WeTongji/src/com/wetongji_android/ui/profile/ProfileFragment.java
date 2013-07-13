package com.wetongji_android.ui.profile;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.wetongji_android.R;
import com.wetongji_android.data.User;
import com.wetongji_android.factory.UserFactory;
import com.wetongji_android.net.NetworkLoader;
import com.wetongji_android.net.http.HttpMethod;
import com.wetongji_android.ui.friend.FriendListActivity;
import com.wetongji_android.ui.main.MainActivity;
import com.wetongji_android.util.common.WTApplication;
import com.wetongji_android.util.data.DbListLoader;
import com.wetongji_android.util.data.user.UserLoader;
import com.wetongji_android.util.image.ImageUtil;
import com.wetongji_android.util.net.ApiHelper;
import com.wetongji_android.util.net.HttpRequestResult;

public class ProfileFragment extends SherlockFragment implements LoaderCallbacks<HttpRequestResult>{

	private User mUser;
	
	private TextView mTvWords;
	private TextView mTvCollege;
	private TextView mTvFriendsNum;
	private TextView mTvEventsLikes;
	private TextView mTvNewsLikes;
	private TextView mTvPeopleLikes;
	private TextView mTvOrgsLikes;
	private TextView mTvParActivities;
	private TextView mTvParCourse;
	
	private RelativeLayout rlFriendsList;
	private RelativeLayout rlMyProfile;
	private ImageButton btnFriendAdd;
	
	private Activity mActivity;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_profile, null);
		
		setHeadBluredBg(view);
		initWidgets(view);
		
		this.getLoaderManager().initLoader(WTApplication.USER_LOADER, null, this);
		
		return view;
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
		mActivity = activity;
		((MainActivity)mActivity).getSupportActionBar().setDisplayShowTitleEnabled(true);
	}

	@SuppressWarnings("deprecation")
	private void setHeadBluredBg(View view) {
		RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.layout_profile_header);
		Bitmap resource = BitmapFactory.decodeResource(getResources(), R.drawable.test_avatar);
		int tH =  (496 * 200 / 1080);
		Bitmap bm = Bitmap.createBitmap(resource, 0, (100 - tH / 2), 200, tH);
		Bitmap bg = ImageUtil.fastblur(bm, 10);
		rl.setBackgroundDrawable(new BitmapDrawable(getActivity().getResources(), bg));
	}
	
	private void initWidgets(View v) {
		mTvWords = (TextView) v.findViewById(R.id.text_profile_words);
		mTvCollege = (TextView) v.findViewById(R.id.text_profile_gender);
		mTvFriendsNum = (TextView) v.findViewById(R.id.text_profile_friend_num);
		mTvEventsLikes = (TextView) v.findViewById(R.id.text_profile_events_num);
		mTvNewsLikes = (TextView) v.findViewById(R.id.text_profile_news_num);
		mTvPeopleLikes = (TextView) v.findViewById(R.id.text_profile_people_num);
		mTvOrgsLikes = (TextView) v.findViewById(R.id.text_profile_orgs_num);
		mTvParActivities = (TextView) v
				.findViewById(R.id.text_profile_par_activities_num);
		mTvParCourse = (TextView) v.findViewById(R.id.text_profile_par_course_num);
		
		rlFriendsList = (RelativeLayout)v.findViewById(R.id.ll_profile_friend_list);
		rlFriendsList.setOnClickListener(new ClickListener());
		
		rlMyProfile = (RelativeLayout) v.findViewById(R.id.layout_profile_my_profile);
	}
	
	private void setWidgets(User user) {
		if (!TextUtils.isEmpty(user.getWords())) {
			mTvWords.setText("\"" + user.getWords() + "\"");
		}
		
		mTvCollege.setText(user.getDepartment());
		String fmt = getResources().getString(R.string.text_friends_counter);
		mTvFriendsNum.setText(String.format(fmt, user.getFriendCount()));
		
		String format = getResources().getString(R.string.format_likes);
		mTvEventsLikes.setText(String.format(format, user.getLikeCount().getActivity()));
		mTvNewsLikes.setText(String.format(format, user.getLikeCount().getInformation()));
		mTvPeopleLikes.setText(String.format(format, user.getLikeCount().getPerson()));
		mTvOrgsLikes.setText(String.format(format, user.getLikeCount().getAccount()));
		/*mTvParActivities.setText(String.format(format, user.get));
		mTvParCourse.setText(String.format(format, user));*/
		
		((MainActivity)mActivity).getSupportActionBar().setTitle(user.getName());
	}

	@Override
	public Loader<HttpRequestResult> onCreateLoader(int arg0, Bundle arg1) {
		Bundle bundle = ApiHelper.getInstance(getActivity()).getUserGet();
		
		NetworkLoader loader = new NetworkLoader(getActivity(), HttpMethod.Get, bundle);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<HttpRequestResult> arg0,
			HttpRequestResult result) 
	{
		JSONObject json = null;
		String strUser = null;
		if (result.getResponseCode() == 0) {
			try {
				json = new JSONObject(result.getStrResponseCon());
				strUser = json.getString("User");
			} catch (JSONException e) {
			}
			
			UserFactory factory = new UserFactory(this);
			mUser = factory.createSingleObject(strUser);
			setWidgets(mUser);
		}
	}

	@Override
	public void onLoaderReset(Loader<HttpRequestResult> arg0) {
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.menu_profile, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.notification_button_profile) {
			if (WTApplication.getInstance().hasAccount) {
				((MainActivity)getActivity()).showRightMenu();
			} else {
				Toast.makeText(getActivity(), getResources().getText(R.string.no_account_error),
						Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	class ClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.ll_profile_friend_list) {
				Intent intent = new Intent(mActivity, FriendListActivity.class);
				startActivity(intent);
				mActivity.overridePendingTransition(R.anim.slide_right_in,
						R.anim.slide_left_out);
			} else if (v.getId() == R.id.layout_profile_my_profile) {
				Intent intent = new Intent();
				startActivity(intent);
				mActivity.overridePendingTransition(R.anim.slide_right_in,
						R.anim.slide_left_out);
			}
		}
	}
}
