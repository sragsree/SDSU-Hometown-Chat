package com.example.sreer.sdsuchat;


import android.content.Intent;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.location.Address;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapPlotterFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap mMap;
    private List<User> userList = new ArrayList<>();
    private LatLng location;
    private String mCurrentUserEmail;
    private long mCurrentUserCreatedAt;
    private String mCurrentUserId;
    private FirebaseAuth mAuth;
    private static final int FILTER = 123;
    private String webFilterURL = null,dataBaseFilter=null;
    private String stateFilter, yearFilter, countryFilter;
    private TextView progress;
    private Button loadMore;
    private int pageCount=0,filterPageCount=-1;
    private DatabaseHelper db;
    private String nextUserId;
    private int databaseid=0;



    public void setRequestComplete(boolean requestComplete) {
        isRequestComplete = requestComplete;
    }

    boolean isRequestComplete = false;


    public MapPlotterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mapInflater = inflater.inflate(R.layout.fragment_map_plotter, container, false);
        loadMore = (Button) mapInflater.findViewById(R.id.button_map_load);
        // Inflate the layout for this fragment
        return mapInflater;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();
        Log.i("test","Map View Created");
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        db = new DatabaseHelper(getActivity());
        currentUserInfo();
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                chatLauncher(marker);
            }
        });
        Log.i("test","Map is Ready");
        userList.clear();
        databaseMapPlotter();
        LatLng currentLocation = new LatLng(32.77, -117.07);
        CameraPosition myPosition = new CameraPosition.Builder()
                .target(currentLocation)
                .zoom(mMap.getCameraPosition().zoom)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition));
        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(webFilterURL==null) {
                    pageCount = (db.getTotalCount() / 25) - 1;
                    loadMore.setEnabled(false);
                    ((ListAndFilterActivity) getActivity()).showProgressDialog();
                    loadMoreData();
                }
                else{
                    new MapFilterPlotter().execute(webFilterURL);
                }

            }
        });

    }

    public void geocodeUsers() {
        LatLng userLocation = null;
        for (User user : userList) {
            if (user.getLatitude() == 0.0 && user.getLongitude() == 0.0) {
                if (user.getCity() != null) {
                    userLocation = getCoordinates(user.getCity() + "," + user.getState() + "," + user.getCountry(), 3);
                    if (userLocation != null) {
                        user.setLatitude(userLocation.latitude);
                        user.setLongitude(userLocation.longitude);
                    }
                } else {
                    userLocation = getCoordinates(user.getState() + "," + user.getCountry(), 2);
                    if (userLocation != null) {
                        user.setLatitude(userLocation.latitude);
                        user.setLongitude(userLocation.longitude);
                    }
                }
            }
            location = new LatLng(user.getLatitude(), user.getLongitude());
            mMap.addMarker(new MarkerOptions().position(location).title(user.getNickname()));
            db.addUserToFilter(user);
            if(dataBaseFilter==null)
                db.addUser(user);
        }
        userList.clear();
        ((ListAndFilterActivity) getActivity()).dismissProgressDialog();
    }


    public LatLng getCoordinates(String address, int args) {
        LatLng point = null;
        Geocoder gc = new Geocoder(getActivity().getApplication());
        try {
            List<Address> list = gc.getFromLocationName(address, args);
            if (list.size() <= 0)
                return new LatLng(0.0, 0.0);
            else {
                Address add = list.get(0);
                point = new LatLng(add.getLatitude(), add.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return point;
    }

    public void databaseLoadMore(){
        userList.clear();
        userList = db.getUsers(databaseid);
        for (User user : userList) {
            location = new LatLng(user.getLatitude(), user.getLongitude());
            mMap.addMarker(new MarkerOptions().position(location).title(user.getNickname()));
            databaseid = user.getId();
        }
        userList.clear();

    }

    public void loadMoreData(){
        if(db.isDataAvailable(databaseid)) {
            databaseLoadMore();
            loadMore.setEnabled(true);
            ((ListAndFilterActivity)getActivity()).dismissProgressDialog();
        }
        else {
            new MapPlotter().execute();

        }
    }

    class MapPlotter extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            increasePageCount();
            String url = "http://bismarck.sdsu.edu/hometown/users?page="+pageCount+"&reverse=true";
            VolleySingleton requestQueue = VolleySingleton.getInstance(getActivity().getApplicationContext());
            Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
                public void onResponse(JSONArray response) {
                    Log.i("test", String.valueOf(response.length()));
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject userObject = response.getJSONObject(i);
                            User user = new User();
                            user.setId(userObject.getInt("id"));
                            user.setNickname(userObject.getString("nickname"));
                            user.setCity(userObject.getString("city"));
                            user.setTimeStamp(userObject.getString("time-stamp"));
                            user.setYear(Integer.valueOf(userObject.getString("year")));
                            user.setCountry(userObject.getString("country"));
                            user.setState(userObject.getString("state"));
                            user.setLatitude(userObject.getDouble("latitude"));
                            user.setLongitude(userObject.getDouble("longitude"));
                            userList.add(user);
                        }
                        geocodeUsers();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        setRequestComplete(true);
                    }
                    loadMore.setEnabled(true);
                }
            };

            Response.ErrorListener failure = new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    Log.d("rew", error.toString());
                }
            };
            JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
            requestQueue.addToRequestQueue(getRequest);
            return null;
        }

        @Override
        protected void onPreExecute() {
            ((ListAndFilterActivity) getActivity()).showProgressDialog();
        }

        @Override
        protected void onPostExecute(Void result) {
        }

    }

    class MapFilterPlotter extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            filterPageCount++;
            String URLfilter = params[0].replaceAll("\\s","%20")+"&page="+filterPageCount;
            VolleySingleton requestQueue = VolleySingleton.getInstance(getActivity().getApplicationContext());
            Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
                public void onResponse(JSONArray response) {
                    Log.i("test", String.valueOf(response.length()));
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject userObject = response.getJSONObject(i);
                            User user = new User();
                            user.setNickname(userObject.getString("nickname"));
                            user.setCity(userObject.getString("city"));
                            user.setYear(Integer.valueOf(userObject.getString("year")));
                            user.setCountry(userObject.getString("country"));
                            user.setState(userObject.getString("state"));
                            user.setLatitude(userObject.getDouble("latitude"));
                            user.setLongitude(userObject.getDouble("longitude"));
                            userList.add(user);
                        }
                        Log.i("test","size:"+String.valueOf(userList.size()));
                        Log.i("test","Ploting Started");
                        geocodeUsers();
                        Log.i("test","Ploting completed");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ((ListAndFilterActivity) getActivity()).dismissProgressDialog();
                }
            };
            Response.ErrorListener failure = new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    Log.d("rew", error.toString());
                }
            };
            Log.i("URL", URLfilter);
            JsonArrayRequest getRequest = new JsonArrayRequest(URLfilter, success, failure);
            requestQueue.addToRequestQueue(getRequest);
            return null;
        }

        @Override
        protected void onPreExecute() {
            ((ListAndFilterActivity) getActivity()).showProgressDialog();
        }

        @Override
        protected void onPostExecute(Void result) {
        }

    }

    public void applyMapFilter(String dbfilter,String filterURL){
            userList.clear();
            dataBaseFilter=dbfilter;
            mMap.clear();
            ((ListAndFilterActivity) getActivity()).showProgressDialog();
            webFilterURL = filterURL;
            userList = db.getUsersFromFilter(dbfilter);
            for (User user : userList) {
                location = new LatLng(user.getLatitude(), user.getLongitude());
                mMap.addMarker(new MarkerOptions().position(location).title(user.getNickname()));
            }
            userList.clear();
            ((ListAndFilterActivity) getActivity()).dismissProgressDialog();
    }

    public void clearFilter() {
        if (dataBaseFilter != null) {
            webFilterURL = null;
            dataBaseFilter = null;
            databaseMapPlotter();
        }
    }

    public void increasePageCount(){
        pageCount++;
    }

    public void databaseMapPlotter(){
        userList = db.getAllUsers();
        for (User user : userList) {
            location = new LatLng(user.getLatitude(), user.getLongitude());
            mMap.addMarker(new MarkerOptions().position(location).title(user.getNickname()));
            databaseid = user.getId();
        }
        pageCount = ((db.getTotalCount() / 25)-1);
        userList.clear();
    }

    public void currentUserInfo(){
        User dummyUser = new User();
        if(mAuth.getCurrentUser()!=null ) {
            mCurrentUserEmail = mAuth.getCurrentUser().getEmail();
            mCurrentUserId = mAuth.getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference()
                    .child("Identifier")
                    .child(dummyUser.cleanEmailAddress(mCurrentUserEmail))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                User user = dataSnapshot.getValue(User.class);
                                mCurrentUserCreatedAt = user.getCreatedAt();

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

    }

    public void chatLauncher(Marker marker){
        FirebaseDatabase.getInstance().getReference().child("meta-data")
                .child(marker.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    User user = dataSnapshot.getValue(User.class);
                    if (TextUtils.equals(user.getFirebaseID(), mCurrentUserId))
                        Toast.makeText(getActivity(), "That is You !", Toast.LENGTH_SHORT).show();
                    else {
                        String chatRef = user.createUniqueChatRef(mCurrentUserCreatedAt, mCurrentUserEmail);
                        Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                        chatIntent.putExtra(ExtraIntent.EXTRA_CURRENT_USER_ID, mCurrentUserId);
                        chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_ID, user.getFirebaseID());
                        chatIntent.putExtra(ExtraIntent.EXTRA_CHAT_REF, chatRef);
                        chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_NAME, user.getNickname());
                        //Start new activity
                        startActivity(chatIntent);
                    }
                } else {
                    Toast.makeText(getActivity(), "User not registered in firebase", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getActivity(), "Error:Data fetch failed from firebase", Toast.LENGTH_SHORT).show();

            }
        });
    }

}
