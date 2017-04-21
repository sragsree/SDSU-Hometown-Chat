package com.example.sreer.sdsuchat;


import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewRecordsFragment extends Fragment {
    private List<User> userList = new ArrayList<>();
    private RecyclerView recyclerView;
    private UserAdapter userAdapter, filterUserAdapter;
    private static final int FILTER =123;
    private String mCurrentUserEmail;
    private long mCurrentUserCreatedAt;
    private String mCurrentUserId;
    private FirebaseAuth mAuth;
    private int pageCount=0,filterPageCount=-1;
    private DatabaseHelper db;
    private String nextUserId;
    private View fragmentView;
    private int databaseid = 0,databaseFilterIndex=0;
    private String webFilterURL,dbFilterString;


    public ViewRecordsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_view_records, container, false);
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_view);
        mAuth = FirebaseAuth.getInstance();
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        currentUserInfo();
        db = new DatabaseHelper(getActivity());
            if (db.getTotalCount() <= 0)
                new WebListLoader().execute();
            else
                checkNewUsers();
    }

    class WebListLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String url = "http://bismarck.sdsu.edu/hometown/users?page="+pageCount+"&reverse=true";
            VolleySingleton requestQueue = VolleySingleton.getInstance(getActivity().getApplicationContext());
            Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
                public void onResponse(JSONArray response) {
                    Log.i("test",String.valueOf(response.length()));
                    try {
                        for (int i = 0; i < response.length(); i++){
                            JSONObject userObject = response.getJSONObject(i);
                            User user = new User();
                            user.setId(userObject.getInt("id"));
                            user.setNickname(userObject.getString("nickname"));
                            user.setCity(userObject.getString("city"));
                            user.setTimeStamp(userObject.getString("time-stamp"));
                            user.setCountry(userObject.getString("country"));
                            user.setState(userObject.getString("state"));
                            user.setYear(Integer.valueOf(userObject.getString("year")));
                            if(userObject.getDouble("latitude")!=0.0 && userObject.getDouble("longitude")!=0.0) {
                                user.setLatitude(userObject.getDouble("latitude"));
                                user.setLongitude(userObject.getDouble("longitude"));
                            }
                            else{
                                LatLng userLocation = getGeoCoordinates(user.getCity(),user.getState(),user.getCountry());
                                user.setLatitude(userLocation.latitude);
                                user.setLongitude(userLocation.longitude);
                            }

                            db.addUser(user);
                            db.addUserToFilter(user);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i("Total users",String.valueOf(userList.size()));
                    userList = db.getAllUsers();
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    userAdapter = new UserAdapter(userList,getActivity(),mCurrentUserId,mCurrentUserEmail,mCurrentUserCreatedAt,recyclerView);
                    recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
                    recyclerView.setAdapter(userAdapter);
                    pageCount = (db.getTotalCount()/25)-1;
                    User user = userList.get(userList.size()-1);
                    databaseid=user.getId();
                    ((ListAndFilterActivity) getActivity()).dismissProgressDialog();
                   loadMore(null,null);
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

    public void filter(final String dbFilter, final String filterURL){
        userList.clear();
        userAdapter.clear();
        if(filterUserAdapter!=null)
            filterUserAdapter.clear();
        filterPageCount = -1;
        filterPageCount++;
        String url = filterURL.replaceAll("\\s","%20")+"&page="+filterPageCount+"&reverse=true";
        VolleySingleton requestQueue = VolleySingleton.getInstance(getActivity().getApplicationContext());
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                Log.i("test",String.valueOf(response.length()));
                try {
                    for (int i = 0; i < response.length(); i++){
                        JSONObject userObject = response.getJSONObject(i);
                        User user = new User();
                        user.setId(userObject.getInt("id"));
                        user.setNickname(userObject.getString("nickname"));
                        user.setCity(userObject.getString("city"));
                        user.setTimeStamp(userObject.getString("time-stamp"));
                        user.setCountry(userObject.getString("country"));
                        user.setState(userObject.getString("state"));
                        user.setYear(Integer.valueOf(userObject.getString("year")));
                        user.setLatitude(userObject.getDouble("latitude"));
                        user.setLongitude(userObject.getDouble("longitude"));
                        userList.add(user);
                        if(user.getLatitude()!=0.0 && user.getLongitude()!=0.0) {
                            db.addUserToFilter(user);
                        }
                        else{
                            new GeocodeUserDetails().execute(user);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                userAdapter.setOnLoadMoreListener(null);
                filterUserAdapter = new UserAdapter(userList,getActivity(),mCurrentUserId,mCurrentUserEmail,mCurrentUserCreatedAt,recyclerView);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
                recyclerView.setAdapter(filterUserAdapter);
                ((ListAndFilterActivity) getActivity()).dismissProgressDialog();
                loadMore(dbFilter,filterURL);
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
                filterUserAdapter.setLoaded();
            }
        };
        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        requestQueue.addToRequestQueue(getRequest);
    }


    class FilterList extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
//            filter(params[0]);
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

    public void applyListFilter(String dBFilter,String filterURL) {
        if (dBFilter != null) {
            webFilterURL = filterURL;
            dbFilterString = dBFilter;
            filter(dBFilter, filterURL);
        }
    }

    public void clearFilter(){
        if(dbFilterString!=null) {
            filterUserAdapter.setOnLoadMoreListener(null);
            webFilterURL = null;
            dbFilterString = null;
            databaseFilterIndex = 0;
            databaseListLoader();
            databaseid = 0;
        }
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

    public void increasePageCount(){
        pageCount++;
    }


    public void databaseListLoader(){
        userList = db.getAllUsers();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        userAdapter = new UserAdapter(userList,getActivity(),mCurrentUserId,mCurrentUserEmail,mCurrentUserCreatedAt,recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(userAdapter);
        ((ListAndFilterActivity) getActivity()).dismissProgressDialog();
        pageCount = (db.getTotalCount()/25)-1;
        User user=userAdapter.getUser(userAdapter.getItemCount()-1);
        databaseid=user.getId();
        loadMore(null,null);
    }

    public void webLoadMore(){
        userAdapter.setLoading();
        increasePageCount();
        userAdapter.add(null);
        String URL = "http://bismarck.sdsu.edu/hometown/users?page="+pageCount+"&reverse=true";
        VolleySingleton requestQueue = VolleySingleton.getInstance(getActivity().getApplicationContext());
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                Log.i("test",String.valueOf(response.length()));
                userAdapter.remove();
                try {
                    for (int i = 0; i < response.length(); i++){
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
                        userAdapter.add(user);
                        if(user.getLatitude() != 0.0 && user.getLongitude() != 0.0){
                            db.addUser(user);
                            db.addUserToFilter(user);
                        }
                        else
                            new GeocodeUserDetails().execute(user);
                    }
                } catch (JSONException e) {
                    userAdapter.remove();
                    e.printStackTrace();
                }
                userAdapter.setLoaded();

            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
                userAdapter.setLoaded();
            }
        };
        JsonArrayRequest getRequest = new JsonArrayRequest(URL, success, failure);
        requestQueue.addToRequestQueue(getRequest);
    }

    public void databaseLoadMore(){
        userAdapter.setLoading();
        userAdapter.add(null);
        List<User> reloadList = db.getUsers(databaseid);
        userAdapter.remove();
        for(User user:reloadList){
            userAdapter.add(user);
            databaseid = user.getId();
        }
        userAdapter.setLoaded();
    }

    public void loadMore(final String dbFilter, final String filterURL) {
        if(filterURL == null) {
            userAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    if (db.isDataAvailable(databaseid))
                        databaseLoadMore();
                    else
                        webLoadMore();
                }
            });
        }
        else
            filterUserAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                        webLoadMore(webFilterURL);
                }
            });
    }

    public void databaseLoadMore (String dbfilter){
        filterUserAdapter.setLoading();
        filterUserAdapter.add(null);
        List<User> reloadList = db.getUsersFromFilter(dbfilter,databaseFilterIndex);
        filterUserAdapter.remove();
        for(User user:reloadList){
            filterUserAdapter.add(user);
            databaseFilterIndex=user.getId();
        }
        filterUserAdapter.setLoaded();
    }

    public void webLoadMore(final String filterURL){
        filterUserAdapter.setLoading();
        filterPageCount++;
        String URLFilter = filterURL.replaceAll("\\s","%20")+"&page="+filterPageCount+"&reverse=true";
        VolleySingleton requestQueue = VolleySingleton.getInstance(getActivity().getApplicationContext());
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                Log.i("test",String.valueOf(response.length()));
                try {
                    for (int i = 0; i < response.length(); i++){
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
                        filterUserAdapter.add(user);
                        if(user.getLatitude() != 0.0 && user.getLongitude() != 0.0){
                            db.addUserToFilter(user);
                        }
                        else
                            new GeocodeUserDetails().execute(user);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                filterUserAdapter.setLoaded();
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
                filterUserAdapter.setLoaded();
            }
        };
        JsonArrayRequest getRequest = new JsonArrayRequest(URLFilter, success, failure);
        requestQueue.addToRequestQueue(getRequest);
    }


    public void checkNewUsers(){
        ((ListAndFilterActivity) getActivity()).showProgressDialog();
        String url = "http://bismarck.sdsu.edu/hometown/nextid";
        VolleySingleton requestQueue = VolleySingleton.getInstance(getActivity().getApplicationContext());
        Response.Listener<String> success = new Response.Listener<String>() {
            public void onResponse(String response) {
                //Log.i("test",String.valueOf(response.length()));
                try {
                     nextUserId = response;
                    }
                 catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("NextId is",nextUserId);
                if(db.getMaxId()==(Integer.valueOf(nextUserId)-1))
                    databaseListLoader();
                else {
                    int difference = ((Integer.valueOf(nextUserId)-1)-db.getMaxId());
                    int totalFetches = difference/25;
                    for(int i=0;i<=totalFetches;i++){
                        dbEqualizer();
                        increasePageCount();
                    }
                    pageCount=0;
                    databaseListLoader();
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
            }
        };
        StringRequest getRequest = new StringRequest(url, success, failure);
        requestQueue.addToRequestQueue(getRequest);
    }

    public void dbEqualizer(){
        String url = "http://bismarck.sdsu.edu/hometown/users?page="+pageCount+"&reverse=true";
        VolleySingleton requestQueue = VolleySingleton.getInstance(getActivity().getApplicationContext());
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                Log.i("test",String.valueOf(response.length()));
                try {
                    for (int i = 0; i < response.length(); i++){
                        JSONObject userObject = response.getJSONObject(i);
                        User user = new User();
                        user.setId(userObject.getInt("id"));
                        user.setNickname(userObject.getString("nickname"));
                        user.setCity(userObject.getString("city"));
                        user.setTimeStamp(userObject.getString("time-stamp"));
                        user.setYear(Integer.valueOf(userObject.getString("year")));
                        user.setCountry(userObject.getString("country"));
                        user.setState(userObject.getString("state"));
                        if(userObject.getDouble("latitude")!=0.0 && userObject.getDouble("longitude")!=0.0) {
                            user.setLatitude(userObject.getDouble("latitude"));
                            user.setLongitude(userObject.getDouble("longitude"));
                        }
                        else{
                            LatLng userLocation = getGeoCoordinates(user.getCity(),user.getState(),user.getCountry());
                            user.setLatitude(userLocation.latitude);
                            user.setLongitude(userLocation.longitude);
                        }

                        db.addUser(user);
                        db.addUserToFilter(user);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
            }
        };
        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        requestQueue.addToRequestQueue(getRequest);
    }

    public LatLng getGeoCoordinates(String city,String state, String country){
        LatLng userLocation = null;
        if (city != null)
            userLocation = getCoordinates(city + "," + state + "," + country, 3);
        else
            userLocation = getCoordinates(state + "," + country, 2);
        if (userLocation == null) {
            userLocation = new LatLng(0.0, 0.0);
        }
        return userLocation;
    }


    class GeocodeUserDetails extends AsyncTask<User, Void, Void> {
        @Override
        protected Void doInBackground(User... params) {
            User user = params[0];
            LatLng userLocation = null;
            if (user.getCity() != null)
                userLocation = getCoordinates(user.getCity() + "," + user.getState() + "," + user.getCountry(), 3);
            else
                userLocation = getCoordinates(user.getState() + "," + user.getCountry(), 2);
            if (userLocation == null) {
                userLocation = new LatLng(0.0, 0.0);
            }
            user.setLatitude(userLocation.latitude);
            user.setLongitude(userLocation.longitude);
            db.addUser(user);
            db.addUserToFilter(user);
            return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }


    public LatLng getCoordinates(String address, int args) {
        LatLng point = null;
        Geocoder gc = new Geocoder(getActivity());
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
}
