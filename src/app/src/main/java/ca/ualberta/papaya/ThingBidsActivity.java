package ca.ualberta.papaya;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.papaya.controllers.AddThingController;
import ca.ualberta.papaya.controllers.ThingBidsController;
import ca.ualberta.papaya.controllers.ThrowawayElasticSearchController;
import ca.ualberta.papaya.interfaces.IObserver;
import ca.ualberta.papaya.models.Bid;
import ca.ualberta.papaya.models.Thing;
import ca.ualberta.papaya.models.User;
import ca.ualberta.papaya.util.LocalUser;
import ca.ualberta.papaya.util.Observable;
import ca.ualberta.papaya.util.Observer;

/**
 * Created by hsbarker on 3/29/16.
 *
 * Main activity for displaying Things that a user has bid on
 *
 * Calls ThingBidsController for all of the button implementations.
 * @see ThingBidsController
 */


public class ThingBidsActivity extends AbstractPapayaActivity {

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_items_bids);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setTitle(getTitle());


        View recyclerView = findViewById(R.id.bidded_thing_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationIcon(R.drawable.ic_action_home);

        if (findViewById(R.id.thing_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_other_items_bids, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.goToBorrowings).setOnMenuItemClickListener(ThingBidsController.getInstance()
                .getBorrowingItemsOnClickListener(this));

        return true;
    }

    private void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        LocalUser.getUser(new Observer() {
            @Override
            public void update(Object data) {
                User user = (User) data;
                user.getBids(new IObserver() {
                    @Override
                    public void update(Object data) {
                        ArrayList<Bid> bids = (ArrayList<Bid>) data;

                        String[] ids = new String[ bids.size() ];
                        for (int i = 0; i < ids.length; ++i) {
                            ids[i] = bids.get(i).getThingId();
                        }

                        Observable<ArrayList<Thing>> thingsObservable = new Observable<>();
                        thingsObservable.addObserver(new IObserver<ArrayList<Thing>>() {
                            @Override
                            public void update(ArrayList<Thing> data) {
                                SimpleItemRecyclerViewAdapter va =
                                        new SimpleItemRecyclerViewAdapter(data);
                                recyclerView.setAdapter(va);
                            }
                        });

                        ThrowawayElasticSearchController.GetThingTask getThingTask =
                                new ThrowawayElasticSearchController.GetThingTask(thingsObservable);
                        getThingTask.execute(ids);
                    }
                });
            }
        });
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Thing> mValues;

        public SimpleItemRecyclerViewAdapter(List<Thing> items) {
            mValues = items;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bidded_thing_list_content, parent, false);
            return new ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(holder.mItem.getTitle()); // .getId()
            holder.mContentView.setText(holder.mItem.getDescription()); // .getTitle()
            holder.mPictureView.setImageBitmap(holder.mItem.getPhoto().getImage());
            holder.mMyBidView.setText("My Bid: ");

            Bid.search(new Observer<List<Bid>>() {
                @Override
                public void update(List<Bid> bids) {
                    if (bids.size() == 0) {
                        holder.mMyBidView.post(new Runnable() {
                            @Override
                            public void run() {
                                holder.mMyBidView.setText("My Bid: $0.00");
                            }
                        });
                    } else {
                        Bid maxBid = bids.get(0);
                        for (Bid bid : bids) {
                            if (bid.getAmount() > maxBid.getAmount()) {
                                maxBid = bid;
                            }
                        }
                        final Bid theMaxBid = maxBid;
                        holder.mMyBidView.post(new Runnable() {
                            @Override
                            public void run() {
                                holder.mMyBidView.setText("My Bid: " + theMaxBid.toString());
                            }
                        });
                    }
                }
            }, Bid.class, "{\"query\": {\"bool\": {\"must\": [" +
                    "{\"match\": {\"bidderId\": \"" + LocalUser.getId() + "\"}}," +
                    "{\"match\": {\"thingId\": \"" + holder.mItem.getId() + "\"}}" +
                    "]}}}");




            // method that is called when a Thing in the list is selected.
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ThingDetailFragment.ARG_ITEM_ID, holder.mItem.getId());
                        ThingDetailFragment fragment = new ThingDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.thing_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, AllInfoActivity.class);
                        intent.putExtra(AllInfoActivity.THING_EXTRA, holder.mItem);
                        intent.putExtra(ThingDetailFragment.ARG_ITEM_ID, holder.mItem.getId());

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public final ImageView mPictureView;
            public final TextView mMyBidView;
            public Thing mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
                mPictureView = (ImageView) view.findViewById(R.id.picture);
                mMyBidView = (TextView) view.findViewById(R.id.myBid);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
