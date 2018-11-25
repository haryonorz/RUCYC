package com.example.zenoinc.rucyc.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zenoinc.rucyc.R;
import com.example.zenoinc.rucyc.activity.ActivityWithInfo;
import com.example.zenoinc.rucyc.adapter.FeedAdapter;
import com.example.zenoinc.rucyc.connection.AuthUser;
import com.example.zenoinc.rucyc.connection.HTTPTools;
import com.example.zenoinc.rucyc.model.Feed;
import com.example.zenoinc.rucyc.utilities.DataInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NewsFeed extends Fragment implements AdapterView.OnItemClickListener{
    private Activity activity;
    private Context context;

    private ListView lvItem;
    private SwipeRefreshLayout swipe;
    private View ftView;
    private FeedAdapter adapter;
    private Handler feedhandler;
    private TextView mTitle;
    private ImageView bgFeed, noFeed;
    private int page = 1;
    private Boolean isLoading = false, end = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);
        activity = getActivity();
        context = getContext();

        Glide.with(activity).load(R.drawable.bg_feed).into((ImageView) view.findViewById(R.id.bg_feed));

        swipe = view.findViewById(R.id.swipe);
        lvItem = view.findViewById(R.id.list_feed);
        mTitle = view.findViewById(R.id.newsFeed_title);
        bgFeed = view.findViewById(R.id.bg_feed);
        noFeed = view.findViewById(R.id.bg_no_feed);
        initComponent();
        return view;
    }

    private void initComponent() {
        Shader textShader=new LinearGradient(0, 0, 0, mTitle.getPaint().getTextSize()+25,
                new int[]{
                        ContextCompat.getColor(getActivity(), R.color.colorGStart2),
                        ContextCompat.getColor(getActivity(), R.color.colorGEnd2)},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mTitle.getPaint().setShader(textShader);

        swipe.setColorSchemeResources(R.color.colorPrimary);
        swipe.setRefreshing(true);
        swipe.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFeed();
            }
        });

        lvItem.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(view.getLastVisiblePosition() == totalItemCount-1 && lvItem.getCount() >=10 && !isLoading && !end) {
                    isLoading = true; page++;
                    getFeed();
                }
            }
        });
        lvItem.setOnItemClickListener(this);
        LayoutInflater li = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ftView = li.inflate(R.layout.list_footer_view, null);
        feedhandler = new FeedHandler();
        getFeed();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(activity, ActivityWithInfo.class);
        intent.putExtra("news_id", Integer.parseInt(((Feed)adapter.getItem(i)).getHistory().get(0)));
        startActivity(intent);
    }

    public void refreshFeed(){
        page=1;
        swipe.setRefreshing(true);
        getFeed();
    }

    private void getFeed(){
        new GetFeed().execute();
    }

    private class FeedHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case 0:
                    if(lvItem.getFooterViewsCount()==0) lvItem.addFooterView(ftView);
                    break;
                case 1:
                    adapter = new FeedAdapter(context, (List<Feed>) msg.obj);
                    adapter.notifyDataSetChanged();
                    lvItem.setAdapter(adapter);
                    isLoading=false;
                    end=false;
                    break;
                case 2:
                    adapter.addFeedItemToAdapter((List<Feed>) msg.obj);
                    end=false;
                    isLoading=false;
                    break;
                case 3:
                    adapter.addFeedItemToAdapter((List<Feed>) msg.obj);
                    lvItem.removeFooterView(ftView);
                    isLoading=false;
                    end=true;
                    break;
                case 4:
                    lvItem.removeFooterView(ftView);
                    isLoading=false;
                    end=true;
                    break;
                case 5:
                    lvItem.removeFooterView(ftView);
                    lvItem.setAdapter(null);
                    isLoading=false;
                    end=true;
                    break;
                case 6:
                    if(lvItem.getFooterViewsCount()>0) lvItem.removeFooterView(ftView);
                default:
                    break;
            }
        }
    }

    private class GetFeed extends AsyncTask<String, Void, Integer > {
        private List<Feed> feedm;
        private int count;

        private String base = DataInfo.url;
        private int responseCode;
        private int statusCode;

        private URL url;
        private HttpURLConnection con = null;
        private ArrayNode ANode;
        private Iterator<JsonNode> AIterator;
        private JsonNode rootnode;
        private ObjectMapper mapper;

        private String tagError = "Sending to Server Error";

        protected Integer doInBackground(String... params) {
            getFeed();
            return responseCode;
        }

        protected void onPostExecute(Integer result) {
            swipe.setRefreshing(false);
            if(page == 1) feedhandler.sendEmptyMessage(0);
            if(result == HttpURLConnection.HTTP_OK){
                if(statusCode == 260) {
                    if(count!=0){
                        bgFeed.setVisibility(View.VISIBLE);
                        noFeed.setVisibility(View.GONE);
                        if(page==1) {
                            Message msg = feedhandler.obtainMessage(1, feedm);
                            feedhandler.sendMessage(msg);
                            if(feedm.size()<10) feedhandler.sendEmptyMessage(6);
                        } else if(feedm.size()<10){
                            Message msg = feedhandler.obtainMessage(3, feedm);
                            feedhandler.sendMessage(msg);
                        } else {
                            Message msg = feedhandler.obtainMessage(2, feedm);
                            feedhandler.sendMessage(msg);
                        }
                    } else {
                        if(page==1) {
                            bgFeed.setVisibility(View.GONE);
                            noFeed.setVisibility(View.VISIBLE);
                            feedhandler.sendEmptyMessage(5);
                        } else {
                            feedhandler.sendEmptyMessage(4);
                        }
                    }
                } else {
                    feedhandler.sendEmptyMessage(5);
                }
            } else {
                feedhandler.sendEmptyMessage(4);
            }
        }

        private void getFeed(){
            try {
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("token", DataInfo.token);
                params.put("page", page);

                byte[] postDataBytes = HTTPTools.PostParamaters(params).getBytes("UTF-8");

                url=new URL(base+"/feed");

                HttpURLConnection.setFollowRedirects(false);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(10000);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                con.setDoOutput(true);
                con.getOutputStream().write(postDataBytes);

                responseCode = con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    mapper = new ObjectMapper();
                    rootnode = mapper.readTree(con.getInputStream());
                    statusCode = rootnode.get("status").asInt();
                    if(statusCode == 260) {
                        ANode = (ArrayNode) rootnode.get("feed");
                        AIterator = ANode.elements();
                        count = ANode.size();
                        if(count!=0) {
                            feedm = new ArrayList<>();
                            while (AIterator.hasNext()) {
                                JsonNode node = AIterator.next();
                                Feed feed = null;
                                List<String> data = new ArrayList<>();
                                data.add(node.get("news_id").asText());
                                data.add(node.get("activity").asText());
                                data.add(node.get("created_at").asText());
                                data.add(node.get("time").asText());
                                data.add(node.get("distance").asText());
                                feed = new Feed(data);
                                feedm.add(feed);
                            }
                        }
                    }
                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    viewErrorDesc(getString(R.string.get_feed_error_0));
                    new AuthUser(context).execute();
                    viewErrorDesc(getString(R.string.auth_time_out));
                } else {
                    viewErrorDesc(getString(R.string.get_feed_error_0));
                }
            } catch (SocketException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.get_feed_error_0));
            } catch (SocketTimeoutException e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.get_feed_error_0));
                viewErrorDesc(getString(R.string.send_time_out));
            } catch (Exception e){
                Log.e(tagError, e.getMessage());
                viewErrorDesc(getString(R.string.get_feed_error_0));
            } finally {
                if(con != null) con.disconnect();
            }
        }
    }

    private void viewErrorDesc(final String desc){
        if(DataInfo.tabposition==0) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast toast = Toast.makeText(context, desc, Toast.LENGTH_SHORT);
                    TextView v = toast.getView().findViewById(android.R.id.message);
                    if (v != null) v.setGravity(Gravity.CENTER);
                    toast.show();
                }
            });
        }
    }
}
