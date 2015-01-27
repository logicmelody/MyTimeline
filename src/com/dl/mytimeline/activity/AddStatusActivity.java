package com.dl.mytimeline.activity;

import com.dl.mytimeline.R;
import com.dl.mytimeline.autostatusendpoint.Autostatusendpoint;
import com.dl.mytimeline.autostatusendpoint.model.AutoStatus;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

    private EditText mStatus;
    private Button mPostButton;
    private CheckBox mWithQuoteCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_status);
        initialize();
    }

    private void initialize() {
        findViews();
        initButtons();
    }

    private void initButtons() {
        mPostButton.setOnClickListener(this);
    }

    private void findViews() {
        mStatus = (EditText)findViewById(R.id.status);
        mPostButton = (Button)findViewById(R.id.post_button);
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
                postStatus();
                break;
        }
    }

    private void postStatus() {
        String status = mStatus.getText().toString().trim();
        Long timeStamp = System.currentTimeMillis();

        if(TextUtils.isEmpty(status)) {
            Toast.makeText(this, getString(R.string.empty_status_warning), Toast.LENGTH_SHORT).show();
            return;
        }

        new PostStatusAsyncTask(this).execute(status, timeStamp.toString(), "No subject");
    }

    private class PostStatusAsyncTask extends AsyncTask<String, Void, AutoStatus> {
        private Context mContext;
        private ProgressDialog mProgressDialog;

        public PostStatusAsyncTask(Context context) {
            mContext = context;
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
                Autostatusendpoint.Builder builder = new Autostatusendpoint.Builder(
                        AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
                Autostatusendpoint service = builder.build();
                AutoStatus status = new AutoStatus();
                status.setContent(params[0]);
                status.setTimestamp(Long.valueOf(params[1]));
                status.setSubject(params[2]);
                if(mWithQuoteCheckBox.isChecked()) {
                    response = service.quoteAndInsertAutoStatus(status).execute();
                } else {
                    response = service.insertAutoStatus(status).execute();
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
}
