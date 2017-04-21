package com.example.sreer.sdsuchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListAndFilterActivity extends AppCompatActivity implements FilterFragment.FilterFragmentInterface {

    ViewRecordsFragment viewRecords;
    FilterFragment filterFragment;
    MapPlotterFragment mapFragment;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRefDatabase;
    FragmentManager fragments;
    private String email;
    FragmentTransaction fragmentTransaction;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //SQLiteDatabase db = new DatabaseHelper(this);
        //DatabaseHelperTest db = new DatabaseHelperTest(this);
        setContentView(R.layout.activity_list_and_filter);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUserRefDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        fragments = getSupportFragmentManager();
        fragmentTransaction = fragments.beginTransaction();
        email = mAuth.getCurrentUser().getEmail();
        viewRecords = new ViewRecordsFragment();
        filterFragment = new FilterFragment();
        fragmentTransaction.add(R.id.fragment_filter, filterFragment);
        fragmentTransaction.add(R.id.fragment_list_map, viewRecords);
        fragmentTransaction.commit();



//        mapFragment = new MapPlotterFragment();
//        fragmentTransaction.add(R.id.fragment_list, mapFragment);
//        fragmentTransaction.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_mapView:
                setTitle("SDSU CHAT - Map View");
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_list_map);
                if(fragment instanceof ViewRecordsFragment) {
                    Fragment fragmentForMap = getSupportFragmentManager().findFragmentById(R.id.fragment_filter);
                    ((FilterFragment) fragmentForMap).resetSpinners();
                    mapFragmentLoader();
                }
                break;
            case R.id.menu_listView:
                setTitle("SDSU CHAT - List View");
                Fragment fragments = getSupportFragmentManager().findFragmentById(R.id.fragment_list_map);
                if(fragments instanceof MapPlotterFragment) {
                    Fragment fragmentForList = getSupportFragmentManager().findFragmentById(R.id.fragment_filter);
                    ((FilterFragment) fragmentForList).resetSpinners();
                    listFragmentLoader();
                }
                break;
            case R.id.menu_chat:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_logout:
                logout();
                break;
            default:
                Log.i("Test", "None");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!isChangingConfigurations())
            logout();
    }

    public void mapFragmentLoader(){
        //Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_list_map);
        fragmentTransaction = fragments.beginTransaction();
        mapFragment = new MapPlotterFragment();
        fragmentTransaction.replace(R.id.fragment_list_map, mapFragment);
        fragmentTransaction.commit();

    }

    public void listFragmentLoader(){
        //Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_list_map);
        fragmentTransaction = fragments.beginTransaction();
        viewRecords = new ViewRecordsFragment();
        fragmentTransaction.replace(R.id.fragment_list_map, viewRecords);
        fragmentTransaction.commit();
        //if (!(fragment instanceof ViewRecordsFragment)) {
            //Bundle listBundle = new Bundle();
            //Fragment fragmentList = getSupportFragmentManager().findFragmentById(R.id.fragment_filter);
            //String filterURL = ((FilterFragment) fragmentList).getFilterURL();
            //listBundle.putString("filter", filterURL);
            //viewRecords.setArguments(listBundle);
        //}
    }

    private void logout() {
        setUserOffline();
        mAuth.signOut();
        finish();
    }

    private void setUserOffline() {
        if(mAuth.getCurrentUser()!=null ) {
            String userId = mAuth.getCurrentUser().getUid();
            mUserRefDatabase.child(userId).child("connection").setValue(UsersChatAdapter.OFFLINE);
        }
    }



    @Override
    public void apply(String dbFilter,String filterURL){
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_list_map);
        if(fragment instanceof ViewRecordsFragment)
            ((ViewRecordsFragment) fragment) .applyListFilter(dbFilter,filterURL);
        if(fragment instanceof MapPlotterFragment)
            ((MapPlotterFragment) fragment).applyMapFilter(dbFilter,filterURL);
    }

    @Override
    public void clear(){
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_list_map);
        if(fragment instanceof ViewRecordsFragment)
            ((ViewRecordsFragment) fragment).clearFilter();
        if(fragment instanceof MapPlotterFragment)
            ((MapPlotterFragment) fragment).clearFilter();
    }


    public void showProgressDialog(){
        progressDialog.setMessage("Loading...");
        progressDialog.show();
    }

    public void dismissProgressDialog(){
        progressDialog.dismiss();
    }

}
