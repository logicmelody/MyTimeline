package com.dl.mytimeline.activity;

import com.dl.mytimeline.R;
import com.dl.mytimeline.autostatusendpoint.Autostatusendpoint;
import com.dl.mytimeline.autostatusendpoint.model.AutoStatus;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class AddStatusActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private static final String KEY_SETTINGS_SHAREDPREFERENCE = "key_settings_sharedpreference";
    private static final String KEY_ACCOUNT_NAME = "key_account_name";

    private SharedPreferences mSettings;
    private String mAccountName;
    private GoogleAccountCredential mCredential;

    private EditText mStatus;
    private Button mPostButton;
    private Button mPostProtectedButton;
    private CheckBox mWithQuoteCheckBox;

    private class PostStatusAsyncTask extends AsyncTask<String, Void, AutoStatus> {
        private Context mContext;
        private boolean mIsProtected = false;
        private ProgressDialog mProgressDialog;

        public PostStatusAsyncTask(Context context) {
            mContext = context;
        }

        public PostStatusAsyncTask(Context context, boolean isProtected) {
            mContext = context;
            mIsProtected = isProtected;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage("Posting the Status...");
            mProgressDialog.show();
        }

        protected AutoStatus doInBackground(String... params) {
            AutoStatus response = null;
            try {
                if(mIsProtected) {
                    Autostatusendpoint.Builder builder = new Autostatusendpoint.Builder(
                            AndroidHttp.newCompatibleTransport(), new GsonFactory(), mCredential);
                    Autostatusendpoint service = builder.build();
                    AutoStatus status = new AutoStatus();
                    status.setContent(params[0]);
                    status.setTimestamp(Long.valueOf(params[1]));
                    response = service.protectInsertAutoStatus(status).execute();
                } else {
                    Autostatusendpoint.Builder builder = new Autostatusendpoint.Builder(
                            AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
                    Autostatusendpoint service = builder.build();
                    AutoStatus status = new AutoStatus();
                    status.setContent(params[0]);
                    status.setTimestamp(Long.valueOf(params[1]));
                    if(mWithQuoteCheckBox.isChecked()) {
                        response = service.quoteAndInsertAutoStatus(status).execute();
                    } else {
                        response = service.insertAutoStatus(status).execute();
                    }
                }
            } catch (Exception e) {
                Log.d("Could not Add Status", e.getMessage(), e);
            }
            return response;
        }

        protected void onPostExecute(AutoStatus status) {
            // Clear the progress dialog and the fields
            mProgressDialog.dismiss();
            mStatus.setText("");

            // Display success message to user
            Toast.makeText(mContext, "Status added succesfully", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_status);
        initialize();
    }

    private void initialize() {
        findViews();
        initButtons();
        initAccount();
    }

    private void initAccount() {
        mSettings = getSharedPreferences(KEY_SETTINGS_SHAREDPREFERENCE, 0);
        mCredential = GoogleAccountCredential.usingAudience(this, getString(R.string.audience));
        setAccountName(mSettings.getString(KEY_ACCOUNT_NAME, null));
        if (mCredential.getSelectedAccountName() != null) {
            // Already signed in, begin app!
            Toast.makeText(getBaseContext(),
                    "Logged in with : " + mCredential.getSelectedAccountName(), Toast.LENGTH_SHORT)
                    .show();
            // Toast.makeText(getBaseContext(),
            // GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext()),Toast.LENGTH_SHORT).show();
        } else {
            // Not signed in, show login window or request an account.
            chooseAccount();
        }
    }

    private void chooseAccount() {
        startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private void setAccountName(String accountName) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(KEY_ACCOUNT_NAME, accountName);
        editor.commit();
        mCredential.setSelectedAccountName(accountName);
        mAccountName = accountName;
    }

    private void initButtons() {
        mPostButton.setOnClickListener(this);
        mPostProtectedButton.setOnClickListener(this);
    }

    private void findViews() {
        mStatus = (EditText)findViewById(R.id.status);
        mPostButton = (Button)findViewById(R.id.post_button);
        mPostProtectedButton = (Button)findViewById(R.id.post_protected_button);
        mWithQuoteCheckBox = (CheckBox)findViewById(R.id.with_quote_check_box);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.post_button:
                postStatus(false);
                break;
            case R.id.post_protected_button:
                postStatus(true);
                break;
        }
    }

    private void postStatus(boolean isProtected) {
        String status = mStatus.getText().toString().trim();
        Long timeStamp = System.currentTimeMillis();

        if(TextUtils.isEmpty(status)) {
            Toast.makeText(this, getString(R.string.empty_status_warning), Toast.LENGTH_SHORT).show();
            return;
        }

        new PostStatusAsyncTask(this, isProtected).execute(status, timeStamp.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (data != null && data.getExtras() != null) {
                    String accountName = data.getExtras()
                            .getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        setAccountName(accountName);
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putString(KEY_ACCOUNT_NAME, accountName);
                        editor.commit();
                        // User is authorized.
                    }
                }
                break;
        }
    }

}
