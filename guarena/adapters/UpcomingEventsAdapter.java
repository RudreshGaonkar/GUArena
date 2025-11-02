package com.example.guarena.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guarena.R;
import com.example.guarena.models.Event;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpcomingEventsAdapter extends RecyclerView.Adapter<UpcomingEventsAdapter.EventViewHolder> {

    private List<Event> events;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public UpcomingEventsAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upcoming_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return Math.min(events.size(), 5); // Show max 5 upcoming events
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardEvent;
        private TextView tvEventTitle, tvEventTime, tvEventLocation;
        private ImageView ivEventIcon;
        private View viewEventTypeIndicator;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            cardEvent = itemView.findViewById(R.id.card_event);
            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
            tvEventTime = itemView.findViewById(R.id.tv_event_time);
            tvEventLocation = itemView.findViewById(R.id.tv_event_location);
            ivEventIcon = itemView.findViewById(R.id.iv_event_icon);
            viewEventTypeIndicator = itemView.findViewById(R.id.view_event_type_indicator);

            // Click listener
            cardEvent.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(events.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Event event) {
            tvEventTitle.setText(event.getTitle());
            tvEventLocation.setText(event.getLocation());

            // Format time
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date date = inputFormat.parse(event.getDateTime());
                tvEventTime.setText(outputFormat.format(date));
            } catch (Exception e) {
                tvEventTime.setText(event.getDateTime());
            }

            // Set event type indicator and icon
            setEventTypeStyle(event.getEventType());
        }

        private void setEventTypeStyle(String eventType) {
            // ✅ FIXED: Add null check to prevent crash
            if (eventType == null || eventType.isEmpty()) {
                // Default style for null/empty event types
                viewEventTypeIndicator.setBackgroundColor(
                        itemView.getContext().getColor(R.color.indicator_default));
                ivEventIcon.setImageResource(R.drawable.ic_calendar);
                return;
            }

            switch (eventType.toLowerCase()) {
                case "practice":
                    viewEventTypeIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.indicator_practice));
                    ivEventIcon.setImageResource(R.drawable.ic_fitness_center);
                    break;
                case "match":
                    viewEventTypeIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.indicator_match));
                    ivEventIcon.setImageResource(R.drawable.ic_sports);
                    break;
                case "tournament":
                    viewEventTypeIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.indicator_tournament));
                    ivEventIcon.setImageResource(R.drawable.ic_emoji_events);
                    break;
                default:
                    viewEventTypeIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.indicator_default));
                    ivEventIcon.setImageResource(R.drawable.ic_calendar);
                    break;
            }
        }

    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }
}
