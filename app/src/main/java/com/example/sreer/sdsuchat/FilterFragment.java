package com.example.sreer.sdsuchat;


import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FilterFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    Spinner countrySpinner,stateSpinner,yearSpinner;
    List<String> countryList = new ArrayList<String>();
    List<String> stateList = new ArrayList<String>();
    ArrayList<String> yearList = new ArrayList<>();
    List<User> filterUser = new ArrayList<>();
    int pageCount =0;
    Button apply, clear;
    int dbFilterDataCount;
    private DatabaseHelper db;
    int filterDataCount;


    public FilterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
        stateSpinner = (Spinner) view.findViewById(R.id.spinner_state);
        countrySpinner = (Spinner) view.findViewById(R.id.spinner_country);
        yearSpinner = (Spinner) view.findViewById(R.id.spinner_year);
        apply = (Button) view.findViewById(R.id.button_filter_done);
        clear = (Button) view.findViewById(R.id.button_filter_clear);
//        countryListLoader();
//        yearLoader();
        countrySpinner.setOnItemSelectedListener(this);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // applyFilters();
                String countryName = String.valueOf(countrySpinner.getSelectedItem());
                String stateName = String.valueOf(stateSpinner.getSelectedItem());
                String year = String.valueOf(yearSpinner.getSelectedItem());
                if(!(year.equals("None")&&countryName.equals("None")&&stateName.equals("None")))
                {
                    ((ListAndFilterActivity) getActivity()).showProgressDialog();
                    ((ListAndFilterActivity) getActivity()).apply(getFilterString(countryName,stateName,year),getFilterURL(countryName,stateName,year));
                }
                else
                    Toast.makeText(getActivity(),"No filter selected to apply",Toast.LENGTH_SHORT);
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String countryName = String.valueOf(countrySpinner.getSelectedItem());
                String stateName = String.valueOf(stateSpinner.getSelectedItem());
                String year = String.valueOf(yearSpinner.getSelectedItem());
                if(!(year.equals("None")&&countryName.equals("None")&&stateName.equals("None"))) {
                    countrySpinner.setSelection(0, true);
                    stateSpinner.setSelection(0, true);
                    yearSpinner.setSelection(0, true);
                    ((ListAndFilterActivity) getActivity()).clear();
                }
                else
                    Toast.makeText(getActivity(),"No filter applied to clear",Toast.LENGTH_SHORT);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        db = new DatabaseHelper(getActivity());
        yearLoader();
        new CountryLoader().execute();
    }

    public void countryListLoader(){
        String url = "http://bismarck.sdsu.edu/hometown/countries";
        countryList.add("None");
        stateList.add("None");
        VolleySingleton requestQueue = VolleySingleton.getInstance(getActivity().getApplicationContext());
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                Log.i("test",String.valueOf(response.length()));
                for (int i = 0; i < response.length(); i++)
                    try {
                        countryList.add(response.getString(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, countryList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                countrySpinner.setAdapter(adapter);
                ArrayAdapter<String> stateadapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, stateList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                stateSpinner.setAdapter(stateadapter);
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

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        //stateListLoader(String.valueOf(parent.getItemAtPosition(pos)));
        new StateLoader().execute(String.valueOf(parent.getItemAtPosition(pos)));

    }

    public void onNothingSelected(AdapterView<?> parent) {
        stateSpinner.setAdapter(null);
    }

    public void stateListLoader(String country){
        String url = "http://bismarck.sdsu.edu/hometown/states?country="+country;
        VolleySingleton requestQueue = VolleySingleton.getInstance(getActivity().getApplicationContext());
        stateList.clear();
        stateList.add("None");
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++)
                    try {
                        stateList.add(response.getString(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, stateList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                stateSpinner.setAdapter(adapter);
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


    public void yearLoader(){
        yearList.add("None");
        for(int i=1970;i<=2017;i++)
            yearList.add(String.valueOf(i));
        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, yearList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);
    }

    class CountryLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            countryListLoader();
            return null;
        }

        @Override
        protected void onPreExecute() {
            // Runs on the UI thread before doInBackground
            // Good for toggling visibility of a progress indicator
        }

        @Override
        protected void onPostExecute(Void result) {
        }

    }

    class StateLoader extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            stateListLoader(params[0]);
            return null;
        }

        @Override
        protected void onPreExecute() {
            // Runs on the UI thread before doInBackground
            // Good for toggling visibility of a progress indicator
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

    public static interface FilterFragmentInterface{
        void apply(String dBfilter,String filterURL);
        void clear();
    }

    public String getFilterURL(String countryName, String stateName, String year){
        String filter = "";
        if(countryName != null && !countryName.contains("None"))
            filter = "country="+countryName;
        if(stateName != null && !stateName.contains("None"))
            filter = filter.length()>0 ? filter+"&state="+stateName : filter+"state="+stateName;
        if(year != null && !year.contains("None"))
            filter = filter.length()>0 ? filter+"&year="+year : filter+"year="+year;
        Log.i("Filter Return","http://bismarck.sdsu.edu/hometown/users?"+filter);
        return filter.length() > 0 ? "http://bismarck.sdsu.edu/hometown/users?"+filter : "http://bismarck.sdsu.edu/hometown/users";
    }

    public String getFilterString(){
        String filter = "";
        String countryName = String.valueOf(countrySpinner.getSelectedItem());
        String stateName = String.valueOf(stateSpinner.getSelectedItem());
        String year = String.valueOf(yearSpinner.getSelectedItem());
        if(countryName != null && !countryName.contains("None"))
            filter = "country="+"'"+countryName+"'";
        if(stateName != null && !stateName.contains("None"))
            filter = filter.length()>0 ? filter+" AND state="+"'"+stateName+"'" : filter+"state="+"'"+stateName+"'";
        if(year != null && !year.contains("None"))
            filter = filter.length()>0 ? filter+" AND year="+year : filter+"year="+year;
        Log.i("String Filter Return",filter);
        return filter.length() > 0 ? filter : null;
    }

    public String getFilterString(String countryName, String stateName, String year){
        String filter = "";
        if(countryName != null && !countryName.contains("None"))
            filter = "country="+"'"+countryName+"'";
        if(stateName != null && !stateName.contains("None"))
            filter = filter.length()>0 ? filter+" AND state="+"'"+stateName+"'" : filter+"state="+"'"+stateName+"'";
        if(year != null && !year.contains("None"))
            filter = filter.length()>0 ? filter+" AND year="+year : filter+"year="+year;
        Log.i("String Filter Return",filter);
        return filter.length() > 0 ? filter : null;
    }

    public void resetSpinners(){
        countrySpinner.setSelection(0,true);
        stateSpinner.setSelection(0,true);
        yearSpinner.setSelection(0,true);
    }

}
