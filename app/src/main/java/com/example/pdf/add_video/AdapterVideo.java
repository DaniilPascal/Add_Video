package com.example.pdf.add_video;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class AdapterVideo extends RecyclerView.Adapter<AdapterVideo.HolderVideo>{

    private Context context;
    private ArrayList<ModelVideo> videoArrayList;

    public AdapterVideo(Context context, ArrayList<ModelVideo> videoArrayList) {
        this.context = context;
        this.videoArrayList = videoArrayList;
    }

    @NonNull
    @Override
    public HolderVideo onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_video, parent, false);
        return new HolderVideo(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderVideo holder, int position) {
        ModelVideo modelVideo = videoArrayList.get(position);

        String id = modelVideo.getId();
        String title = modelVideo.getTitle();
        long timestamp = Long.parseLong(modelVideo.getTimestamp());
        String videoUrl = modelVideo.getVideoUrl();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm"); //yyyy-MM-dd HH:mm
        String time = formatter.format(LocalDateTime.ofEpochSecond(timestamp / 1000, 0, OffsetDateTime.now().getOffset()));

        holder.titleTv.setText(title);
        holder.timeTv.setText(time);
        setVideoUrl(modelVideo, holder);

        holder.downloadFab.setOnClickListener(view -> downloadVideo(modelVideo));
        holder.deleteFab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete").setMessage("Are you sure you want to delete video: "+ title).setPositiveButton("DELETE", (dialogInterface, i) ->
                    deleteVideo(modelVideo)).setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss()).show();
        });
    }

    private void setVideoUrl(ModelVideo modelVideo, HolderVideo holder) {
//        holder.progressBar.setVisibility(View.VISIBLE);

        String videoUrl = modelVideo.getVideoUrl();
        MediaController mediaController = new MediaController(context);

        Uri videoUri = Uri.parse(videoUrl);
        holder.videoView.setMediaController(mediaController);
        holder.videoView.setVideoURI(videoUri);

        holder.videoView.requestFocus();
        holder.videoView.setOnPreparedListener(mediaPlayer -> mediaPlayer.start());

        holder.videoView.setOnInfoListener((mediaPlayer, what, extra) -> {
            switch (what){
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:{
//                    holder.progressBar.setVisibility(View.VISIBLE);
                    return true;
                }
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:{
//                    holder.progressBar.setVisibility(View.VISIBLE);
                    return true;
                }
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:{
//                    holder.progressBar.setVisibility(View.GONE);
                    return true;
                }
            }
            return false;
        });

        holder.videoView.setOnCompletionListener(mediaPlayer -> mediaPlayer.start());
    }

    private void deleteVideo(ModelVideo modelVideo) {
        String videoId = modelVideo.getId();
        String videoUrl = modelVideo.getVideoUrl();

        StorageReference reference = FirebaseStorage.getInstance().getReference(videoUrl);
        reference.delete().addOnSuccessListener(aVoid -> {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Videos");
            databaseReference.child(videoId).removeValue().addOnSuccessListener(aVoid1 ->
                    Toast.makeText(context, "Video deleted Successfully!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e ->
                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void downloadVideo(ModelVideo modelVideo) {
        String videoUrl = modelVideo.getVideoUrl();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);
        storageReference.getMetadata().addOnSuccessListener(storageMetadata -> {
            String fileName = storageMetadata.getName();
            String fileType = storageMetadata.getContentType();
            String fileDirectory = Environment.DIRECTORY_DOWNLOADS;

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(videoUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir("" + fileDirectory, "" + fileName + ".mp4");
            downloadManager.enqueue(request);
        }).addOnFailureListener(e -> Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return videoArrayList.size();
    }


    static class HolderVideo extends RecyclerView.ViewHolder{
        VideoView videoView;
        TextView titleTv, timeTv;
//        ProgressBar progressBar;
        FloatingActionButton deleteFab, downloadFab;

        public HolderVideo(@NonNull View itemView){
            super(itemView);

            downloadFab = itemView.findViewById(R.id.downloadFab);
            deleteFab = itemView.findViewById(R.id.deleteFab);
//            progressBar = itemView.findViewById(R.id.progressBar);
            videoView = itemView.findViewById(R.id.videoView);
            titleTv = itemView.findViewById(R.id.titleTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
