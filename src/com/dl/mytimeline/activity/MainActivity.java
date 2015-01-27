package com.dl.mytimeline.activity;

import java.util.List;

import com.dl.mytimeline.R;
import com.dl.mytimeline.activity.TimelineAdapter.OnStatusDeletedListener;
import com.dl.mytimeline.autostatusendpoint.Autostatusendpoint;
import com.dl.mytimeline.autostatusendpoint.model.AutoStatus;
import com.dl.mytimeline.autostatusendpoint.model.CollectionResponseAutoStatus;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnStatusDeletedListener {

    private TextView mTimelineStatusCount;
    private ListView mTimelineList;
    private TimelineAdapter mTimelineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshTimeline();
    }

    private void refreshTimeline() {
        new StatusesListAsyncTask(this).execute();
    }

    private void initialize() {
        findViews();
        initTimeline();
    }

    private void initTimeline() {
        mTimelineAdapter = new TimelineAdapter(this);
        mTimelineAdapter.setOnStatusDeletedListener(this);
        mTimelineList.setAdapter(mTimelineAdapter);
    }

    private void findViews() {
        mTimelineList = (ListView)findViewById(R.id.timeline_list);
        mTimelineStatusCount = (TextView)findViewById(R.id.timeline_status_count);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
        } else if(id == R.id.menu_add_status) {
            navigateToAddStatusPage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToAddStatusPage() {
        Intent intent = new Intent(this, AddStatusActivity.class);
        startActivity(intent);
    }

    private class StatusesListAsyncTask extends AsyncTask<Void, Void, CollectionResponseAutoStatus> {
        Context mContext;

        public StatusesListAsyncTask(Context context) {
            mContext = context;
        }

        protected CollectionResponseAutoStatus doInBackground(Void... unused) {
            CollectionResponseAutoStatus statuses = null;
            try {
                Autostatusendpoint.Builder builder = new Autostatusendpoint.Builder(
                        AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
                Autostatusendpoint service = builder.build();
                statuses = service.listAutoStatus().execute();
            } catch (Exception e) {
                Log.d("Could not retrieve Statuses", e.getMessage(), e);
            }
            return statuses;
        }

        protected void onPostExecute(CollectionResponseAutoStatus statuses) {
            List<AutoStatus> statusList = statuses.getItems();
            if(statusList != null && !statusList.isEmpty()) {
                mTimelineStatusCount.setText(getString(R.string.total_text) + statusList.size());
                mTimelineAdapter.setData(statusList);
                mTimelineAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onStatusDeleted(int totalStatusCount) {
        mTimelineStatusCount.setText(getString(R.string.total_text) + totalStatusCount);
    }

}
