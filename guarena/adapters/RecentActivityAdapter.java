package com.example.guarena.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guarena.R;
import com.example.guarena.models.ActivityItem;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ActivityViewHolder> {

    private List<ActivityItem> activities;
    private OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onActivityClick(ActivityItem activity);
    }

    public RecentActivityAdapter(List<ActivityItem> activities) {
        this.activities = activities;
    }

    public RecentActivityAdapter(List<ActivityItem> activities, OnActivityClickListener listener) {
        this.activities = activities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityItem activity = activities.get(position);
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        return Math.min(activities.size(), 10); // Show max 10 recent activities
    }

    class ActivityViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardActivity;
        private ImageView ivActivityIcon;
        private TextView tvActivityTitle, tvActivityDescription, tvActivityTime;
        private View viewActivityTypeIndicator;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            cardActivity = itemView.findViewById(R.id.card_activity);
            ivActivityIcon = itemView.findViewById(R.id.iv_activity_icon);
            tvActivityTitle = itemView.findViewById(R.id.tv_activity_title);
            tvActivityDescription = itemView.findViewById(R.id.tv_activity_description);
            tvActivityTime = itemView.findViewById(R.id.tv_activity_time);
            viewActivityTypeIndicator = itemView.findViewById(R.id.view_activity_type_indicator);

            // Click listener (optional)
            if (listener != null) {
                cardActivity.setOnClickListener(v ->
                        listener.onActivityClick(activities.get(getAdapterPosition())));
            }
        }

        public void bind(ActivityItem activity) {
            tvActivityTitle.setText(activity.getTitle());
            tvActivityDescription.setText(activity.getDescription());

            // Format timestamp
            formatTimestamp(activity.getTimestamp());

            // Set activity type style
            setActivityTypeStyle(activity.getActivityType());
        }

        private void formatTimestamp(String timestamp) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(timestamp);

                long now = System.currentTimeMillis();
                long activityTime = date.getTime();
                long diff = now - activityTime;

                String timeText;
                if (diff < 60000) { // Less than 1 minute
                    timeText = "Just now";
                } else if (diff < 3600000) { // Less than 1 hour
                    int minutes = (int) (diff / 60000);
                    timeText = minutes + "m ago";
                } else if (diff < 86400000) { // Less than 1 day
                    int hours = (int) (diff / 3600000);
                    timeText = hours + "h ago";
                } else { // More than 1 day
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                    timeText = outputFormat.format(date);
                }

                tvActivityTime.setText(timeText);
            } catch (Exception e) {
                tvActivityTime.setText(timestamp);
            }
        }

        private void setActivityTypeStyle(String activityType) {
            switch (activityType.toLowerCase()) {
                case "event_created":
                    viewActivityTypeIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.activity_event_created));
                    ivActivityIcon.setImageResource(R.drawable.ic_calendar);
                    ivActivityIcon.setColorFilter(itemView.getContext().getColor(R.color.activity_event_created));
                    break;
                case "team_joined":
                    viewActivityTypeIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.activity_team_joined));
                    ivActivityIcon.setImageResource(R.drawable.ic_people);
                    ivActivityIcon.setColorFilter(itemView.getContext().getColor(R.color.activity_team_joined));
                    break;
                case "performance_recorded":
                    viewActivityTypeIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.activity_performance));
                    ivActivityIcon.setImageResource(R.drawable.ic_trending_up);
                    ivActivityIcon.setColorFilter(itemView.getContext().getColor(R.color.activity_performance));
                    break;
                case "equipment_borrowed":
                    viewActivityTypeIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.activity_equipment));
                    ivActivityIcon.setImageResource(R.drawable.ic_fitness_center);
                    ivActivityIcon.setColorFilter(itemView.getContext().getColor(R.color.activity_equipment));
                    break;
                case "photo_uploaded":
                    viewActivityTypeIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.activity_photo));
                    ivActivityIcon.setImageResource(R.drawable.ic_photo_library);
                    ivActivityIcon.setColorFilter(itemView.getContext().getColor(R.color.activity_photo));
                    break;
                default:
                    viewActivityTypeIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.activity_default));
                    ivActivityIcon.setImageResource(R.drawable.ic_info);
                    ivActivityIcon.setColorFilter(itemView.getContext().getColor(R.color.activity_default));
                    break;
            }
        }
    }

    public void updateActivity(List<ActivityItem> newActivities) {
        this.activities = newActivities;
        notifyDataSetChanged();
    }
}
