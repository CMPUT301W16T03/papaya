package ca.ualberta.papaya;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import ca.ualberta.papaya.controllers.ThingDetailController;
import ca.ualberta.papaya.controllers.ThrowawayElasticSearchController;
import ca.ualberta.papaya.data.ThrowawayDataManager;
import ca.ualberta.papaya.interfaces.IObserver;
import ca.ualberta.papaya.models.ElasticModel;
import ca.ualberta.papaya.models.Thing;
import ca.ualberta.papaya.util.Observable;

/**
 * An activity representing a single Thing detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ThingListActivity}.
 */
public class ThingDetailActivity extends AbstractPapayaActivity {

    public static final String THING_EXTRA = "ca.papaya.ualberta.thing.detail.thing.extra";

    // Test
    public static final String ID_EXTRA = "ca.papaya.ualberta.thing.detail.id.extra";

    Intent intent = null;
    Thing thing = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thing_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        intent = getIntent();
        thing = (Thing) intent.getSerializableExtra(THING_EXTRA);

        // Alternatively
        String id = intent.getStringExtra(ID_EXTRA);
        System.out.println("Intent passed id: " + id);

        ElasticModel.getById(new IObserver<Thing>() {
            @Override
            public void update(Thing data) {
                System.out.println("In observer: Thing id: " + data.getId());
            }
        }, Thing.class, id);

//        Observable<Thing> observable = new Observable<>();
//        observable.addObserver(new IObserver<Thing>() {
//            @Override
//            public void update(Thing data) {
//                System.out.println("In other obersver: Thing id: " + thing.getId());
//            }
//        });
//
//        ThrowawayElasticSearchController.GetThingTask getThingTask =
//                new ThrowawayElasticSearchController.GetThingTask(observable);
//        getThingTask.execute(id);

        TextView thingDetailTextView = (TextView) findViewById(R.id.thing_detail);
        thingDetailTextView.setText(thing.getDescription());

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ThingDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(ThingDetailFragment.ARG_ITEM_ID));
            ThingDetailFragment fragment = new ThingDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.thing_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_thing_detail, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.goToEdit).setOnMenuItemClickListener(ThingDetailController.getInstance()
                .getEditItemOnClickListener(this, thing));
        menu.findItem(R.id.delete).setOnMenuItemClickListener(ThingDetailController.getInstance()
                .getDeleteItemOnClickListener(this, thing));
        //menu.findItem(R.id.viewPicture).setOnMenuItemClickListener(ThingDetailController.getInstance()
        //        .getPictureOnClickListener(this, thing)); //Todo fill in button id

        return true;
    }
}
