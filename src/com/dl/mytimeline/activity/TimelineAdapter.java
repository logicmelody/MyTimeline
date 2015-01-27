package com.dl.mytimeline.activity;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.dl.mytimeline.R;
import com.dl.mytimeline.autostatusendpoint.Autostatusendpoint;
import com.dl.mytimeline.autostatusendpoint.model.AutoStatus;
import com.dl.mytimeline.autostatusendpoint.model.CollectionResponseAutoStatus;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TimelineAdapter extends ArrayAdapter<AutoStatus> {

    public interface OnStatusDeletedListener {
        public void onStatusDeleted(int totalStatusCount);
    }

    private Context mContext;
    private OnStatusDeletedListener mOnStatusDeletedListener;

    public TimelineAdapter(Context context) {
        super(context, 0);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        final AutoStatus status = getItem(position);

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.timeline_list_item, parent, false);
        } else {
            view = convertView;
        }

        TextView subject = (TextView)view.findViewById(R.id.subject);
        TextView content = (TextView)view.findViewById(R.id.content);
        TextView timestamp = (TextView)view.findViewById(R.id.timestamp);
        Button deleteButton = (Button)view.findViewById(R.id.delete_button);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss"); 
        String dateString = formatter.format(new Date(status.getTimestamp()));

        if(TextUtils.isEmpty(status.getSubject())) {
            subject.setVisibility(View.GONE);
        } else {
            subject.setVisibility(View.VISIBLE);
            subject.setText(status.getSubject());
        }
        content.setText(status.getContent());
        timestamp.setText(dateString);
        deleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new RemoveStatusAsyncTask(mContext).execute(status.getId());
                remove(status);
                notifyDataSetChanged();
                mOnStatusDeletedListener.onStatusDeleted(getCount());
            }

        });

        return view;
    }

    public void setData(List<AutoStatus> data) {
        clear();
        sortTimelineData(data);
        if (data != null) {
            addAll(data);
        }
    }

    private void sortTimelineData(List<AutoStatus> data) {
        Collections.sort(data, new Comparator<AutoStatus>() {

            @Override
            public int compare(AutoStatus status1, AutoStatus status2) {
                return -(status1.getTimestamp().compareTo(status2.getTimestamp()));
            }

        });
    }

    public void setOnStatusDeletedListener(OnStatusDeletedListener onStatusDeletedListener) {
        mOnStatusDeletedListener = onStatusDeletedListener;
    }

    private class RemoveStatusAsyncTask extends AsyncTask<Long, Void, AutoStatus> {
        Context mContext;

        public RemoveStatusAsyncTask(Context context) {
            mContext = context;
        }

        protected AutoStatus doInBackground(Long... params) {
            try {
                Autostatusendpoint.Builder builder = new Autostatusendpoint.Builder(
                        AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
                Autostatusendpoint service = builder.build();
                service.removeAutoStatus(params[0]).execute();
            } catch (Exception e) {
                Log.d("Could not Remove Status", e.getMessage(), e);
            }
            return null;
        }

        protected void onPostExecute(AutoStatus status) {
            Toast.makeText(mContext, "Status removed succesfully", Toast.LENGTH_SHORT).show();
        }
    }
}
