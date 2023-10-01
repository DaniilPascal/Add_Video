package com.example.pdf.add_video;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class VideosActivity extends AppCompatActivity {

    FloatingActionButton addVideoBtn;
    private RecyclerView videosRv;
    private ArrayList<ModelVideo> videoArrayList;
    private AdapterVideo adapterVideo;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        addVideoBtn = findViewById(R.id.addVideosBtn);
        videosRv = findViewById(R.id.videosRv);
        loadVideosFromFirebase();

        addVideoBtn.setOnClickListener(view -> startActivity(new Intent(VideosActivity.this, AddVideoActivity.class)));
    }

    private void loadVideosFromFirebase() {
        videoArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Videos");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                videoArrayList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelVideo modelVideo = ds.getValue(ModelVideo.class);
                    videoArrayList.add(modelVideo);
                }

                adapterVideo = new AdapterVideo(VideosActivity.this, videoArrayList);
                videosRv.setAdapter(adapterVideo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}