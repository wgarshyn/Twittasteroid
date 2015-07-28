package com.vgarshyn.twittasteroid.core;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.vgarshyn.twittasteroid.contentprovider.tweet.TweetColumns;
import com.vgarshyn.twittasteroid.contentprovider.tweet.TweetContentValues;

import java.util.List;

public class TweetIntentService extends IntentService {
    public static final int REQUEST_TWEET_COUNT = 50;
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_REFRESH = "twittasteroid.action.REFRESH";
    public static final String ACTION_CANCEL_REFRESH = "twittasteroid.action.CANCEL_REFRESH";
    private static final String TAG = TweetIntentService.class.getSimpleName();
    private static final String ACTION_LOAD = "twittasteroid.core.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.vgarshyn.twittasteroid.core.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.vgarshyn.twittasteroid.core.extra.PARAM2";

    private Gson gson = new Gson();

    public TweetIntentService() {
        super("TweetIntentService");
    }

    public static void startActionRefresh(Context context) {
        Intent intent = new Intent(context, TweetIntentService.class);
        intent.setAction(ACTION_REFRESH);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, TweetIntentService.class);
        intent.setAction(ACTION_LOAD);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REFRESH.equals(action)) {
                handleActionRefresh();
            } else if (ACTION_LOAD.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    private void handleActionRefresh() {
        StatusesService service = Twitter.getInstance().getApiClient().getStatusesService();
        service.homeTimeline(REQUEST_TWEET_COUNT, null, null, null, null, null, true, new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> result) {
                        List<Tweet> data = result.data;
                        storeData(data);
                        cancelRefresh();
                    }

                    @Override
                    public void failure(TwitterException error) {
//                        Toast.makeText(getActivity(), "Failed to retrieve timeline",
//                                Toast.LENGTH_SHORT).show();
                        cancelRefresh();
                    }
                }
        );
    }

    private void storeData(List<Tweet> data) {
        ContentValues[] bulkDataset = new ContentValues[data.size()];
        int index = 0;
        for (Tweet tweet : data) {
            TweetContentValues tweetContentValues = new TweetContentValues()
                    .putTweetId(tweet.getId())
                    .putCreatedAt(tweet.createdAt)
                    .putOriginalJson(gson.toJson(tweet));
            bulkDataset[index] = tweetContentValues.values();
            index++;
        }
        getContentResolver().bulkInsert(TweetColumns.CONTENT_URI, bulkDataset);
    }

    private void cancelRefresh() {
        Intent intent = new Intent(ACTION_CANCEL_REFRESH);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
