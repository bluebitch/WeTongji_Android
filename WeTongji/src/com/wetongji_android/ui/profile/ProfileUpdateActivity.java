package com.wetongji_android.ui.profile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.wetongji_android.R;
import com.wetongji_android.data.User;
import com.wetongji_android.net.NetworkLoader;
import com.wetongji_android.net.http.HttpMethod;
import com.wetongji_android.util.common.WTApplication;
import com.wetongji_android.util.exception.ExceptionToast;
import com.wetongji_android.util.net.ApiHelper;
import com.wetongji_android.util.net.HttpRequestResult;

public class ProfileUpdateActivity extends SherlockFragmentActivity implements OnClickListener, 
LoaderCallbacks<HttpRequestResult>{

	private EditText mEtMotto;
	private EditText mEtPhone;
	private EditText mEtEmail;
	private EditText mEtQQ;
	private EditText mEtWeibo;
	private EditText mEtDorm;
	
	private User mUser;
	private User mNewUser;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_update);
		
		// Not show the soft keyboard
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		setUpUI();
		setUpActionBar();
	}
	
	private void setUpUI() {
		mUser = getIntent().getParcelableExtra(ProfileFragment.BUNDLE_USER);
		mEtMotto = (EditText) findViewById(R.id.et_profile_motto);
		mEtPhone = (EditText) findViewById(R.id.et_profile_phone);
		mEtEmail = (EditText) findViewById(R.id.et_profile_email);
		mEtQQ = (EditText) findViewById(R.id.et_profile_qq);
		mEtWeibo = (EditText) findViewById(R.id.et_profile_weibo);
		mEtDorm = (EditText) findViewById(R.id.et_profile_dorm);

        mEtDorm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    queryDormBuilding();
                }
            }
        });
		
		if (mUser != null) {
			mEtMotto.setText(mUser.getWords());
			mEtPhone.setText(mUser.getPhone());
			mEtEmail.setText(mUser.getEmail());
			mEtQQ.setText(mUser.getQQ());
			mEtWeibo.setText(mUser.getSinaWeibo());
			mEtDorm.setText(mUser.getRoom());
		}
	}

    private void queryDormBuilding(){
        mEtDorm.setText("");
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("微同济");
        final String[] types = getResources().getStringArray(R.array.dorm_array);
        b.setItems(types, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mEtDorm.setText(types[which] + " ");
                queryDormId();
            }
        });

        b.show();
    }

    private void queryDormId(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("微同济");
        alert.setMessage("请输入宿舍号");


        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                mEtDorm.setText(mEtDorm.getText() + value);
            }
        });


        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }
	
	private void setUpActionBar() {
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(false);
		ab.setDisplayShowCustomEnabled(true);
		ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayShowHomeEnabled(false);
		
		View v = getLayoutInflater().inflate(R.layout.actionbar_update_profile, null);
		ab.setCustomView(v);
		Button btnCancel = (Button) v.findViewById(R.id.btn_ac_cancel_update);
		Button btnSave = (Button) v.findViewById(R.id.btn_ac_save_profile);
		btnCancel.setOnClickListener(this);
		btnSave.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btn_ac_cancel_update) {
			finish();
		} else if (view.getId() == R.id.btn_ac_save_profile) {
			updateProfile();
		}
	}

	private void updateProfile() {
		mNewUser = new User();
		mNewUser.setBirthday(mUser.getBirthday());
		mNewUser.setMajor(mUser.getMajor());
		mNewUser.setNO(mUser.getNO());
		
		mNewUser.setWords(mEtMotto.getText().toString());
		mNewUser.setPhone(mEtPhone.getText().toString());
		mNewUser.setEmail(mEtEmail.getText().toString());
		mNewUser.setQQ(mEtQQ.getText().toString());
		mNewUser.setSinaWeibo(mEtWeibo.getText().toString());
		mNewUser.setRoom(mEtDorm.getText().toString());
		
		getSupportLoaderManager().restartLoader(WTApplication.NETWORK_LOADER_DEFAULT, null, this);
	}

	@Override
	public Loader<HttpRequestResult> onCreateLoader(int arg0, Bundle arg1) {
		ApiHelper apiHelper = ApiHelper.getInstance(this);
		Bundle b = apiHelper.postUserUpdate(mNewUser);
		return new NetworkLoader(this, HttpMethod.Post, b);
	}

	@Override
	public void onLoadFinished(Loader<HttpRequestResult> loader,
			HttpRequestResult result) {
		if (result.getResponseCode() == 0) {
			Toast.makeText(this, R.string.text_save_success, Toast.LENGTH_SHORT).show();
			Intent intent = getIntent();
			Bundle bundle = new Bundle();
			bundle.putParcelable(ProfileFragment.BUNDLE_USER, mNewUser);
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
		} else {
			ExceptionToast.show(this, result.getResponseCode());
		}
	}

	@Override
	public void onLoaderReset(Loader<HttpRequestResult> arg0) {
	}
	
}
