package com.wetongji_android.ui.event;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.wetongji_android.R;
import com.wetongji_android.data.Activity;
import com.wetongji_android.data.ActivityList;
import com.wetongji_android.factory.ActivityFactory;
import com.wetongji_android.net.NetworkLoader;
import com.wetongji_android.net.http.HttpMethod;
import com.wetongji_android.ui.main.MainActivity;
import com.wetongji_android.ui.main.NotificationHandler;
import com.wetongji_android.util.common.WTApplication;
import com.wetongji_android.util.common.WTBaseDetailActivity;
import com.wetongji_android.util.common.WTBaseFragment;
import com.wetongji_android.util.data.QueryHelper;
import com.wetongji_android.util.exception.ExceptionToast;
import com.wetongji_android.util.net.ApiHelper;
import com.wetongji_android.util.net.HttpRequestResult;
import com.wetongji_android.util.net.HttpUtil;

public class EventsFragment extends WTBaseFragment implements
		LoaderCallbacks<HttpRequestResult>, OnScrollListener {
	public static final String BUNDLE_KEY_ACTIVITY = "bundle_key_activity";
	public static final String BUNDLE_KEY_ACTIVITY_LIST = "bundle_key_activity_list";
	public static final String BUNDLE_KEY_LOAD_FROM_DB_FINISHED = "bundle_key_load_from_db_finished";
	public static final String SHARE_PREFERENCE_EVENT = "EventSettings";
	public static final String PREFERENCE_EVENT_EXPIRE = "EventExpire";
	public static final String PREFERENCE_EVENT_SORT = "EventSort";
	public static final String PREFERENCE_EVENT_TYPE = "EventType";
	private static final int USER_SELECT_TYPE = 15;

	// mUID may be used in USERS mode
	private String mUID;
	public ListView mListActivity;
	public EventListAdapter mAdapter;
	private int mCurrentPage = 0;
	private boolean isRefresh = false;
	private ActivityFactory mFactory;
	private boolean mShouldRequest = true;

	/** Sort preferences by default **/
	private boolean mFilterExpired = false;
	private int mSortType = 1;
	private int mSelectedType = 15;

	// Widgets on bottom action bar
	private LinearLayout llBottomActionbar;
	private LinearLayout llActionSort;
	private LinearLayout llActionType;
	private CheckBox cbActionExpired;

	public static EventsFragment newInstance(StartMode startMode, Bundle args) {
		EventsFragment f = new EventsFragment();
		Bundle bundle;

		if (args != null) {
			bundle = args;
		} else {
			bundle = new Bundle();
		}

		switch (startMode) {
		case BASIC:
			bundle.putInt(BUNDLE_KEY_START_MODE, 1);
			break;
		case USERS:
			bundle.putInt(BUNDLE_KEY_START_MODE, 2);
			break;
		case LIKE:
			bundle.putInt(BUNDLE_KEY_START_MODE, 3);
			break;
		case FRIENDS:
			break;
		case ATTEND:
			bundle.putInt(BUNDLE_KEY_START_MODE, 4);
			break;
		case TODAY:
			bundle.putInt(BUNDLE_KEY_START_MODE, 5);
			break;
		}

		f.setArguments(bundle);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_events, null);

		llBottomActionbar = (LinearLayout) view
				.findViewById(R.id.bottom_action_bar);
		llActionSort = (LinearLayout) view
				.findViewById(R.id.btn_event_detail_invite);
		llActionType = (LinearLayout) view
				.findViewById(R.id.btn_event_detail_friends);
		cbActionExpired = (CheckBox) view.findViewById(R.id.cb_event_expired);
		llActionSort.setOnClickListener(bottomActionItemClikListener);
		llActionType.setOnClickListener(bottomActionItemClikListener);
		if(mStartMode == StartMode.BASIC) {
			readPreference();
		} else {
			view.findViewById(R.id.events_list_space).setVisibility(View.GONE);
		}
		cbActionExpired.setChecked(mFilterExpired);
		cbActionExpired.setOnCheckedChangeListener(new OnTypeCheckedListener());

		mListActivity = (ListView) view.findViewById(R.id.lst_events);
		mAdapter = new EventListAdapter(this, mListActivity);
		mListActivity.setAdapter(mAdapter);
		mListActivity.setOnItemClickListener(onItemClickListener);
		AQuery aq = WTApplication.getInstance().getAq(getActivity());
		aq.id(mListActivity).scrolled(this);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		switch (getCurrentState(savedInstanceState)) {
		case FIRST_TIME_START:
			if (mStartMode == StartMode.BASIC) {
				mAdapter.loadDataFromDB(getQueryArgs());
			} else if (mStartMode == StartMode.USERS) {
				loadDataByUser(1);
			} else if (mStartMode == StartMode.LIKE) {
				loadDataLiked(1);
			} else if (mStartMode == StartMode.ATTEND) {
				loadDataByAccount(1);
			} else if (mStartMode == StartMode.TODAY) {
				mAdapter.loadDataFromDB(getQueryArgs());
			}
			break;
		case SCREEN_ROTATE:
			break;
		case ACTIVITY_DESTROY_AND_CREATE:
			ActivityList activityList = (ActivityList) savedInstanceState
					.getSerializable(BUNDLE_KEY_ACTIVITY_LIST);
			mAdapter.addAll(activityList.getList());
			break;
		}

		if (mStartMode != StartMode.BASIC) {
			llBottomActionbar.setVisibility(View.GONE);
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle b = getArguments();
		if (b != null) {
			int modeCode = b.getInt(BUNDLE_KEY_START_MODE);
			switch (modeCode) {
			case 1:
				mStartMode = StartMode.BASIC;
				break;
			case 2:
				mStartMode = StartMode.USERS;
				break;
			case 3:
				mStartMode = StartMode.LIKE;
				break;
			case 4:
				mStartMode = StartMode.ATTEND;
				break;
			case 5:
				mStartMode = StartMode.TODAY;
				break;
			}

			mUID = b.getString(BUNDLE_KEY_UID);
			mSelectedType = b.getInt(BUNDLE_KEY_SELECT_TYPE, 15);
		}

		setRetainInstance(true);

		setHasOptionsMenu(true);

		getSherlockActivity().getSupportActionBar().setTitle(
				R.string.title_mainmenu_events);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save activity list for next time resume;
		ActivityList activityList = new ActivityList();
		activityList.setItems(mAdapter.getData());
		outState.putSerializable(BUNDLE_KEY_ACTIVITY_LIST, activityList);
	}
	

	@Override
	public void onResume() {
		super.onResume();
		NotificationHandler.getInstance().checkNotification();
	}

	@Override
	public Loader<HttpRequestResult> onCreateLoader(int id, Bundle args) {
		return new NetworkLoader(getActivity(), HttpMethod.Get, args);
	}

	@Override
	public void onLoadFinished(Loader<HttpRequestResult> arg0,
			HttpRequestResult result) {
		if (result.getResponseCode() == 0) {
			if (mFactory == null) {
				mFactory = new ActivityFactory(this);
			}

			List<Activity> activities = mFactory.createObjects(
					result.getStrResponseCon(), isRefresh);

			if (mCurrentPage == 0) {
				mAdapter.getData().clear();
			}

			mAdapter.addAll(activities);

			mCurrentPage++;
			mAdapter.setIsLoadingData(false);
			if (mFactory.getNextPage() == 0) {
				mShouldRequest = false;
			}
		} else {
			ExceptionToast.show(getActivity(), result.getResponseCode());
		}
	}

	@Override
	public void onLoaderReset(Loader<HttpRequestResult> arg0) {
	}

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long id) {

			Intent intent = new Intent(getActivity(), EventDetailActivity.class);
			Activity activity = mAdapter.getItem(position);
			Bundle bundle = new Bundle();
			bundle.putParcelable(BUNDLE_KEY_ACTIVITY, activity);
			intent.putExtras(bundle);
			startActivityForResult(intent, 1);
			getActivity().overridePendingTransition(R.anim.slide_right_in,
					R.anim.slide_left_out);
		}

	};

	public void refreshData() {
		isRefresh = true;

		mAdapter.clear();
		mAdapter.setIsLoadingData(true);
		// scroll the listview to top
		mListActivity.setSelection(0);
		mCurrentPage = 0;
		ApiHelper apiHelper = ApiHelper.getInstance(getActivity());
		Bundle args = apiHelper.getActivities(1, mSelectedType, mSortType,
				mFilterExpired);
		getLoaderManager().restartLoader(WTApplication.NETWORK_LOADER_DEFAULT,
				args, this);
	}

	private void loadMoreData(int page) {
		isRefresh = false;
		mAdapter.setIsLoadingData(true);
		ApiHelper apiHelper = ApiHelper.getInstance(getActivity());
		Bundle args = apiHelper.getActivities(page, mSelectedType, mSortType,
				mFilterExpired);
		getLoaderManager().restartLoader(WTApplication.NETWORK_LOADER_DEFAULT,
				args, this);
	}

	private void loadDataByUser(int page) {
		isRefresh = false;
		mAdapter.setIsLoadingData(true);
		ApiHelper apiHelper = ApiHelper.getInstance(getActivity());
		Bundle args = apiHelper.getActivityByUser(mUID, page, USER_SELECT_TYPE,
				ApiHelper.API_ARGS_SORT_BY_PUBLISH_DESC, mFilterExpired);
		getLoaderManager().restartLoader(WTApplication.NETWORK_LOADER_DEFAULT,
				args, this);
	}

	private void loadDataLiked(int page) {
		isRefresh = false;
		mAdapter.setIsLoadingData(true);
		
		if(HttpUtil.isConnected(getActivity())) {
			ApiHelper apiHelper = ApiHelper.getInstance(getActivity());
			Bundle args = apiHelper.getLikedObjectsListWithModelType(page,
					"Activity");
			getLoaderManager().restartLoader(WTApplication.NETWORK_LOADER_DEFAULT,
					args, this);
		} else {
			mAdapter.setIsLoadingData(false);
			Toast.makeText(getActivity(), getResources().getString(R.string.text_error_check_network), 
					Toast.LENGTH_SHORT).show();
		}
	}

	private void loadDataByAccount(int page) {
		isRefresh = false;
		mAdapter.setIsLoadingData(true);
		ApiHelper apiHelper = ApiHelper.getInstance(getActivity());
		Bundle args = apiHelper
				.getActivityByAccount(mUID, page, USER_SELECT_TYPE,
						ApiHelper.API_ARGS_SORT_BY_PUBLISH_DESC, true);
		getLoaderManager().restartLoader(WTApplication.NETWORK_LOADER_DEFAULT,
				args, this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getLoaderManager().destroyLoader(WTApplication.NETWORK_LOADER_DEFAULT);
		getLoaderManager().destroyLoader(WTApplication.ACTIVITIES_LOADER);
	}

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
		if (mAdapter.shouldRequestNextPage(arg1, arg2, arg3) && mShouldRequest) {
			if (mStartMode == StartMode.BASIC) {
				loadMoreData(mCurrentPage + 1);
			} else if (mStartMode == StartMode.USERS) {
				loadDataByUser(mCurrentPage + 1);
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		String id = data.getStringExtra(WTBaseDetailActivity.KEY_OBJECT_ID);
		int like = data.getIntExtra(WTBaseDetailActivity.KEY_LIKE_NUMBER, 0);
		boolean canLike = data.getBooleanExtra(
				WTBaseDetailActivity.KEY_CAN_LIKE, true);
		boolean bSechedule = data.getBooleanExtra(WTBaseDetailActivity.KEY_ATTEND, false);

		for (int i = 0; i < mAdapter.getCount(); i++) {
			Activity activity = (Activity) mAdapter.getItem(i);
			if (activity.getId() == Integer.valueOf(id)) {
				activity.setCanSchedule(bSechedule);
				activity.setLike(like);
				activity.setCanLike(canLike);
				mAdapter.setObjectAtPosition(i, activity);
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			com.actionbarsherlock.view.MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		if (mStartMode == StartMode.BASIC) {
			getSherlockActivity().getSupportActionBar()
					.setDisplayShowCustomEnabled(true);
			getSherlockActivity().getSupportActionBar().setCustomView(
					R.layout.actionbar_events);
			getActivity().findViewById(R.id.notification_button)
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							NotificationHandler.getInstance().finish();
							if (WTApplication.getInstance().hasAccount) {
								((MainActivity) getActivity()).showRightMenu();
							} else {
								Toast.makeText(
										getActivity(),
										getResources().getText(
												R.string.no_account_error),
										Toast.LENGTH_SHORT).show();
							}
						}
					});
			getActivity().findViewById(R.id.eventlist_reload_button)
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							refreshData();
						}
					});

			//readPreference();
		} else {
			inflater.inflate(R.menu.menu_eventlist_nonotification, menu);

			ActionBar ab = getSherlockActivity().getSupportActionBar();
			ab.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().finish();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}

		writePreference();
		return true;
	}

	/*
	 * Read preference and by default the sort type is by publish time
	 * descending, the expire is true and also we select all the type
	 */
	private void readPreference() {
		SharedPreferences sp = getActivity().getSharedPreferences(
				SHARE_PREFERENCE_EVENT, Context.MODE_PRIVATE);
		mFilterExpired = sp.getBoolean(PREFERENCE_EVENT_EXPIRE, false);
		mSortType = sp.getInt(PREFERENCE_EVENT_SORT,
				ApiHelper.API_ARGS_SORT_BY_PUBLISH_DESC);
		mSelectedType = sp.getInt(PREFERENCE_EVENT_TYPE, 15);
	}

	private void writePreference() {
		SharedPreferences.Editor edit = getActivity().getSharedPreferences(
				SHARE_PREFERENCE_EVENT, Context.MODE_PRIVATE).edit();
		edit.putBoolean(PREFERENCE_EVENT_EXPIRE, mFilterExpired);
		edit.putInt(PREFERENCE_EVENT_SORT, mSortType);
		edit.putInt(PREFERENCE_EVENT_TYPE, mSelectedType);

		edit.commit();
	}

	private Bundle getQueryArgs() {
		String orderBy = QueryHelper.ARGS_ORDER_BY_PUBLISH_TIME;
		boolean assending = false;

		if (mSortType == ApiHelper.API_ARGS_SORT_BY_PUBLISH_DESC) {
			orderBy = QueryHelper.ARGS_ORDER_BY_PUBLISH_TIME;
			assending = false;
		} else if (mSortType == ApiHelper.API_ARGS_SORT_BY_LIKE_DESC) {
			orderBy = QueryHelper.ARGS_ORDER_BY_LIKE;
			assending = false;
		} else if (mSortType == ApiHelper.API_ARGS_SORT_BY_PUBLISH_ASC) {
			orderBy = QueryHelper.ARGS_ORDER_BY_PUBLISH_TIME;
			assending = true;
		}

		boolean hasCH1 = (mSelectedType & ApiHelper.API_ARGS_CHANNEL_ACADEMIC_MASK) != 0;
		boolean hasCH2 = (mSelectedType & ApiHelper.API_ARGS_CHANNEL_COMPETITION_MASK) != 0;
		boolean hasCH3 = (mSelectedType & ApiHelper.API_ARGS_CHANNEL_ENTERTAINMENT_MASK) != 0;
		boolean hasCH4 = (mSelectedType & ApiHelper.API_ARGS_CHANNEL_EMPLOYMENT_MASK) != 0;

		Bundle b = QueryHelper.getActivitiesQueryArgs(orderBy, assending,
				!mFilterExpired, hasCH1, hasCH2, hasCH3, hasCH4);
		return b;
	}

	private OnClickListener bottomActionItemClikListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if (view.getId() == R.id.btn_event_detail_invite) {
				openSortDialog();
			} else if (view.getId() == R.id.btn_event_detail_friends) {
				openTypeSelectDailog();
			}
		}

	};

	private void openSortDialog() {
		final Dialog dialog = new Dialog(getSherlockActivity(), R.style.WTDialog);
		dialog.setTitle(R.string.events_sort_dialog_title);
		dialog.setContentView(R.layout.dialog_events_sort);
		dialog.setCanceledOnTouchOutside(true);
		LinearLayout llCancel = (LinearLayout) dialog
				.findViewById(R.id.btn_sort_dialog_cancel);
		llCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		RadioGroup rgSort = (RadioGroup) dialog
				.findViewById(R.id.rg_dialog_events_sort);
		int checkedId = 0;
		switch (mSortType) {
		case ApiHelper.API_ARGS_SORT_BY_PUBLISH_DESC:
			checkedId = R.id.sort_rb_publish;
			break;
		case ApiHelper.API_ARGS_SORT_BY_LIKE_DESC:
			checkedId = R.id.sort_rb_popularity;
			break;
		case ApiHelper.API_ARGS_SORT_BY_BEGIN_DESC:
			checkedId = R.id.sort_rb_start_date;
			break;
		}
		rgSort.check(checkedId);
		rgSort.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.sort_rb_publish:
					mSortType = ApiHelper.API_ARGS_SORT_BY_PUBLISH_DESC;
					break;
				case R.id.sort_rb_popularity:
					mSortType = ApiHelper.API_ARGS_SORT_BY_LIKE_DESC;
					break;
				case R.id.sort_rb_start_date:
					mSortType = ApiHelper.API_ARGS_SORT_BY_BEGIN_DESC;
					break;
				}
				refreshData();
				dialog.dismiss();
				writePreference();
			}
		});
		dialog.show();
	}

	private void openTypeSelectDailog() {
		final Dialog dialog = new Dialog(getSherlockActivity(), R.style.WTDialog);
		dialog.setTitle(R.string.events_type_dialog_title);
		dialog.setContentView(R.layout.dialog_events_type);
		dialog.setCanceledOnTouchOutside(true);
		LinearLayout llCancel = (LinearLayout) dialog
				.findViewById(R.id.btn_sort_dialog_cancel);
		llCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				refreshData();
				dialog.dismiss();
			}
		});
		CheckBox cbAcademic = (CheckBox) dialog.findViewById(R.id.cb_academic);
		CheckBox cbCompetition = (CheckBox) dialog
				.findViewById(R.id.cb_competition);
		CheckBox cbEnter = (CheckBox) dialog
				.findViewById(R.id.cb_entertainment);
		CheckBox cbEmploy = (CheckBox) dialog.findViewById(R.id.cb_employ);
		cbAcademic
				.setChecked((mSelectedType & ApiHelper.API_ARGS_CHANNEL_ACADEMIC_MASK) != 0);
		cbCompetition
				.setChecked((mSelectedType & ApiHelper.API_ARGS_CHANNEL_COMPETITION_MASK) != 0);
		cbEnter.setChecked((mSelectedType & ApiHelper.API_ARGS_CHANNEL_ENTERTAINMENT_MASK) != 0);
		cbEmploy.setChecked((mSelectedType & ApiHelper.API_ARGS_CHANNEL_EMPLOYMENT_MASK) != 0);
		OnTypeCheckedListener onCheckedListener = new OnTypeCheckedListener();
		cbAcademic.setOnCheckedChangeListener(onCheckedListener);
		cbCompetition.setOnCheckedChangeListener(onCheckedListener);
		cbEnter.setOnCheckedChangeListener(onCheckedListener);
		cbEmploy.setOnCheckedChangeListener(onCheckedListener);
		dialog.show();
	}

	private class OnTypeCheckedListener implements
			android.widget.CompoundButton.OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			switch (buttonView.getId()) {
			case R.id.cb_academic:
				if (buttonView.isChecked()) {
					mSelectedType += ApiHelper.API_ARGS_CHANNEL_ACADEMIC_MASK;
				} else {
					mSelectedType -= ApiHelper.API_ARGS_CHANNEL_ACADEMIC_MASK;
				}
				break;
			case R.id.cb_competition:
				if (buttonView.isChecked()) {
					mSelectedType += ApiHelper.API_ARGS_CHANNEL_COMPETITION_MASK;
				} else {
					mSelectedType -= ApiHelper.API_ARGS_CHANNEL_COMPETITION_MASK;
				}
				break;
			case R.id.cb_entertainment:
				if (buttonView.isChecked()) {
					mSelectedType += ApiHelper.API_ARGS_CHANNEL_ENTERTAINMENT_MASK;
				} else {
					mSelectedType -= ApiHelper.API_ARGS_CHANNEL_ENTERTAINMENT_MASK;
				}
				break;
			case R.id.cb_employ:
				if (buttonView.isChecked()) {
					mSelectedType += ApiHelper.API_ARGS_CHANNEL_EMPLOYMENT_MASK;
				} else {
					mSelectedType -= ApiHelper.API_ARGS_CHANNEL_EMPLOYMENT_MASK;
				}
				break;
			case R.id.cb_event_expired:
				mFilterExpired = buttonView.isChecked();
				refreshData();
				break;
			}

			writePreference();
		}
	}

}
