package ca.ualberta.papaya;

import android.location.Location;
import android.location.LocationManager;
import android.test.ActivityInstrumentationTestCase2;

import com.google.android.gms.maps.model.LatLng;

import ca.ualberta.papaya.models.Thing;
import ca.ualberta.papaya.models.User;


/**
 * Created by bgodley on 3/11/16.
 */

public class LocationTest extends ActivityInstrumentationTestCase2{

    public LocationTest() {
        super(ca.ualberta.papaya.ThingListActivity.class);
    }

    public void testSetLocation(){
        Thing thing = new Thing(new User());

        LatLng location = new LatLng(24,59);


        thing.setLocation(location);

        assertEquals(thing.getLocation(), location);

    }

    public void testGetLocation(){
        Thing thing = new Thing(new User());

        LatLng location = new LatLng(24,59);

        thing.setLocation(location);

        assertEquals(thing.getLocation(), location);

    }
}