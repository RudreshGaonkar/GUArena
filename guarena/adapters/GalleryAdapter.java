package com.example.guarena.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.guarena.R;
import com.example.guarena.database.DatabaseHelper;
import com.example.guarena.models.GalleryItem;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private List<GalleryItem> galleryItems;
    private Context context;
    private String userRole;
    private DatabaseHelper databaseHelper;

    public GalleryAdapter(List<GalleryItem> galleryItems, Context context, String userRole) {
        this.galleryItems = galleryItems;
        this.context = context;
        this.userRole = userRole;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery_instagram, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        GalleryItem item = galleryItems.get(position);

        // Load image using Glide
        File imageFile = new File(item.getImagePath());
        if (imageFile.exists()) {
            Glide.with(context)
                    .load(imageFile)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(holder.ivPhoto);
        }

        // Set title
        holder.tvTitle.setText(item.getTitle());

        // Set event tag
        if (item.getEventName() != null && !item.getEventName().isEmpty()) {
            holder.tvEvent.setVisibility(View.VISIBLE);
            holder.tvEvent.setText(item.getEventName());
        } else {
            holder.tvEvent.setVisibility(View.GONE);
        }

        // Set description
        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(item.getDescription());
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Set uploader name
        String uploaderName = databaseHelper.getUserFullName(item.getUploadedBy());
        holder.tvUploader.setText("Posted by " + uploaderName);

        // Long press to delete (coaches only)
        if ("coach".equals(userRole) || "admin".equals(userRole)) {
            holder.cardView.setOnLongClickListener(v -> {
                showDeleteDialog(item, position);
                return true;
            });
        }
    }

    private void showDeleteDialog(GalleryItem item, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = databaseHelper.deleteGalleryItem(item.getId());
                    if (success) {
                        // Delete image file
                        File imageFile = new File(item.getImagePath());
                        if (imageFile.exists()) {
                            imageFile.delete();
                        }

                        galleryItems.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, galleryItems.size());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return galleryItems.size();
    }

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView ivPhoto;
        TextView tvTitle;
        TextView tvEvent;
        TextView tvDescription;
        TextView tvUploader;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvEvent = itemView.findViewById(R.id.tv_event);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvUploader = itemView.findViewById(R.id.tv_uploader);
        }
    }
}
