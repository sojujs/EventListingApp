package com.application.test;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.application.test.databinding.ActivityHomeBinding;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityHomeBinding binding;
    private Context context;
    private boolean backFlag = false;
    private EventAdapter adapter;
    private ArrayList<EventListModel> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        init();
    }

    private void init() {
        context = this;
        retrieveData();
        binding.fabAddEvent.setOnClickListener(this);
        binding.rvEvents.setHasFixedSize(true);
        binding.rvEvents.setLayoutManager(new LinearLayoutManager(context));
        adapter = new EventAdapter(list, context);
        binding.rvEvents.setAdapter(adapter);
    }

    private void retrieveData() {
        if (list.size() == 0) {
            binding.rvEvents.setVisibility(View.GONE);
            binding.tvEmptyText.setVisibility(View.VISIBLE);
        } else {
            binding.rvEvents.setVisibility(View.VISIBLE);
            binding.tvEmptyText.setVisibility(View.GONE);
        }
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference().child("events");
        eventsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Map<String, String> newEvent = (Map<String, String>) dataSnapshot.getValue();
                EventListModel model = new EventListModel();
                if (newEvent != null) {
                    model.setDate(newEvent.get("date"));
                    model.setDescription(newEvent.get("description"));
                    model.setLocation(newEvent.get("location"));
                    model.setPrice(newEvent.get("price"));
                    model.setTime(newEvent.get("time"));
                    model.setTitle(newEvent.get("title"));
                    model.setEmail(newEvent.get("email"));
                    model.setPicture(newEvent.get("picture"));
                    list.add(model);
                    adapter.notifyDataSetChanged();
                    if (list.size() == 0) {
                        binding.rvEvents.setVisibility(View.GONE);
                        binding.tvEmptyText.setVisibility(View.VISIBLE);
                    } else {
                        binding.rvEvents.setVisibility(View.VISIBLE);
                        binding.tvEmptyText.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(HomeActivity.class.getSimpleName(), "onCancelled: " + databaseError.getCode());
            }
        });




    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabAddEvent:
                startActivity(new Intent(this, CreateEventActivity.class));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (backFlag) {
            finishAffinity();
            System.exit(0);
            return;
        }
        backFlag = true;
        Toast.makeText(context, "Press back again to exit from the app.", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backFlag = false;
            }
        }, 3000);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
