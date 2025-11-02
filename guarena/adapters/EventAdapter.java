package com.example.guarena.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guarena.R;
import com.example.guarena.models.Event;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events;
    private OnEventClickListener listener;
    private String userRole;

    public interface OnEventClickListener {
        void onEventClick(Event event);
        void onEventEdit(Event event);
        void onEventDelete(Event event);
    }

    public EventAdapter(List<Event> events, OnEventClickListener listener, String userRole) {
        this.events = events;
        this.listener = listener;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardEvent;
        private TextView tvEventTitle, tvEventDescription, tvEventLocation, tvEventDateTime;
        private TextView tvParticipationStatus; // ✅ ADDED: Participation status
        private Chip chipEventType;
        private ImageView ivEventMenu;
        private MaterialButton btnEdit, btnDelete; // ✅ ADDED: Action buttons

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            cardEvent = itemView.findViewById(R.id.card_event);
            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
            tvEventDescription = itemView.findViewById(R.id.tv_event_description);
            tvEventLocation = itemView.findViewById(R.id.tv_event_location);
            tvEventDateTime = itemView.findViewById(R.id.tv_event_date_time);
            tvParticipationStatus = itemView.findViewById(R.id.tv_participation_status); // ✅ ADDED
            chipEventType = itemView.findViewById(R.id.chip_event_type);
            ivEventMenu = itemView.findViewById(R.id.iv_event_menu);
            btnEdit = itemView.findViewById(R.id.btn_edit_event); // ✅ ADDED
            btnDelete = itemView.findViewById(R.id.btn_delete_event); // ✅ ADDED

            // Click listeners
            cardEvent.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(events.get(getAdapterPosition()));
                }
            });

            ivEventMenu.setOnClickListener(v -> {
                showEventMenu(getAdapterPosition());
            });

            // ✅ ADDED: Button click listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventEdit(events.get(getAdapterPosition()));
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventDelete(events.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Event event) {
            tvEventTitle.setText(event.getTitle());
            tvEventDescription.setText(event.getDescription());
            tvEventLocation.setText(event.getLocation());

            // Format date and time
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
                Date date = inputFormat.parse(event.getDateTime());
                tvEventDateTime.setText(outputFormat.format(date));
            } catch (Exception e) {
                tvEventDateTime.setText(event.getDateTime());
            }

            // Set event type chip
            chipEventType.setText(event.getType()); // ✅ FIXED: Use getType() instead of getEventType()
            setEventTypeChipStyle(chipEventType, event.getType());

            // ✅ FIXED: Show participation status
            if (event.isUserParticipating()) {
                // Show "You're participating" indicator
                tvParticipationStatus.setText("🎯 You're participating!");
                tvParticipationStatus.setTextColor(Color.GREEN);
                tvParticipationStatus.setVisibility(View.VISIBLE);
                cardEvent.setCardBackgroundColor(Color.parseColor("#E8F5E8")); // Light green
            } else {
                // Show "Open event" indicator
                tvParticipationStatus.setText("📢 Open Event");
                tvParticipationStatus.setTextColor(Color.BLUE);
                tvParticipationStatus.setVisibility(View.VISIBLE);
                cardEvent.setCardBackgroundColor(Color.WHITE);
            }

            // ✅ FIXED: Show role-based actions
            if (event.getUserRole() != null &&
                    (event.getUserRole().equals("admin") ||
                            (event.getUserRole().equals("coach") && event.getCreatedBy() == event.getCurrentUserId()))) {
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                ivEventMenu.setVisibility(View.VISIBLE);
            } else {
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                ivEventMenu.setVisibility(View.GONE);
            }
        }

        private void setEventTypeChipStyle(Chip chip, String eventType) {
            if (eventType == null) return;

            switch (eventType.toLowerCase()) {
                case "practice":
                    chip.setChipBackgroundColorResource(R.color.chip_practice_bg);
                    chip.setTextColor(itemView.getContext().getColor(R.color.chip_practice_text));
                    break;
                case "match":
                    chip.setChipBackgroundColorResource(R.color.chip_match_bg);
                    chip.setTextColor(itemView.getContext().getColor(R.color.chip_match_text));
                    break;
                case "tournament":
                    chip.setChipBackgroundColorResource(R.color.chip_tournament_bg);
                    chip.setTextColor(itemView.getContext().getColor(R.color.chip_tournament_text));
                    break;
                default:
                    chip.setChipBackgroundColorResource(R.color.chip_default_bg);
                    chip.setTextColor(itemView.getContext().getColor(R.color.chip_default_text));
                    break;
            }
        }

        private void showEventMenu(int position) {
            if (listener != null && (userRole.equals("coach") || userRole.equals("admin"))) {
                Event event = events.get(position);
                // Show popup menu
                androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(
                        itemView.getContext(), ivEventMenu);
                popup.getMenuInflater().inflate(R.menu.menu_event_item, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit) {
                        listener.onEventEdit(event);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete) {
                        listener.onEventDelete(event);
                        return true;
                    }
                    return false;
                });
                popup.show();
            }
        }
    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }
}


//package com.example.guarena.adapters;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.guarena.R;
//import com.example.guarena.models.Event;
//import com.google.android.material.card.MaterialCardView;
//import com.google.android.material.chip.Chip;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
//
//    private List<Event> events;
//    private OnEventClickListener listener;
//    private String userRole;
//
//    public interface OnEventClickListener {
//        void onEventClick(Event event);
//        void onEventEdit(Event event);
//        void onEventDelete(Event event);
//    }
//
//    public EventAdapter(List<Event> events, OnEventClickListener listener, String userRole) {
//        this.events = events;
//        this.listener = listener;
//        this.userRole = userRole;
//    }
//
//    @NonNull
//    @Override
//    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_event, parent, false);
//        return new EventViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
//        Event event = events.get(position);
//        holder.bind(event);
//    }
//
//    @Override
//    public int getItemCount() {
//        return events.size();
//    }
//
//    class EventViewHolder extends RecyclerView.ViewHolder {
//        private MaterialCardView cardEvent;
//        private TextView tvEventTitle, tvEventDescription, tvEventLocation, tvEventDateTime;
//        private Chip chipEventType;
//        private ImageView ivEventMenu;
//
//        public EventViewHolder(@NonNull View itemView) {
//            super(itemView);
//            cardEvent = itemView.findViewById(R.id.card_event);
//            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
//            tvEventDescription = itemView.findViewById(R.id.tv_event_description);
//            tvEventLocation = itemView.findViewById(R.id.tv_event_location);
//            tvEventDateTime = itemView.findViewById(R.id.tv_event_date_time);
//            chipEventType = itemView.findViewById(R.id.chip_event_type);
//            ivEventMenu = itemView.findViewById(R.id.iv_event_menu);
//
//            // Click listeners
//            cardEvent.setOnClickListener(v -> {
//                if (listener != null) {
//                    listener.onEventClick(events.get(getAdapterPosition()));
//                }
//            });
//
//            ivEventMenu.setOnClickListener(v -> {
//                showEventMenu(getAdapterPosition());
//            });
//        }
//
//        public void bind(Event event) {
//            tvEventTitle.setText(event.getTitle());
//            tvEventDescription.setText(event.getDescription());
//            tvEventLocation.setText(event.getLocation());
//
//            // Format date and time
//            try {
//                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
//                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
//                Date date = inputFormat.parse(event.getDateTime());
//                tvEventDateTime.setText(outputFormat.format(date));
//            } catch (Exception e) {
//                tvEventDateTime.setText(event.getDateTime());
//            }
//
//            // Set event type chip
//            chipEventType.setText(event.getEventType());
//            setEventTypeChipStyle(chipEventType, event.getEventType());
//
//            // Show/hide menu based on user role
//            if (userRole.equals("coach") || userRole.equals("admin")) {
//                ivEventMenu.setVisibility(View.VISIBLE);
//            } else {
//                ivEventMenu.setVisibility(View.GONE);
//            }
//
//
//        }
//
//        private void setEventTypeChipStyle(Chip chip, String eventType) {
//            switch (eventType.toLowerCase()) {
//                case "practice":
//                    chip.setChipBackgroundColorResource(R.color.chip_practice_bg);
//                    chip.setTextColor(itemView.getContext().getColor(R.color.chip_practice_text));
//                    break;
//                case "match":
//                    chip.setChipBackgroundColorResource(R.color.chip_match_bg);
//                    chip.setTextColor(itemView.getContext().getColor(R.color.chip_match_text));
//                    break;
//                case "tournament":
//                    chip.setChipBackgroundColorResource(R.color.chip_tournament_bg);
//                    chip.setTextColor(itemView.getContext().getColor(R.color.chip_tournament_text));
//                    break;
//                default:
//                    chip.setChipBackgroundColorResource(R.color.chip_default_bg);
//                    chip.setTextColor(itemView.getContext().getColor(R.color.chip_default_text));
//                    break;
//            }
//        }
//
//        private void showEventMenu(int position) {
//            if (listener != null && (userRole.equals("coach") || userRole.equals("admin"))) {
//                Event event = events.get(position);
//
//                // Show popup menu
//                androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(
//                        itemView.getContext(), ivEventMenu);
//                popup.getMenuInflater().inflate(R.menu.menu_event_item, popup.getMenu());
//
//                popup.setOnMenuItemClickListener(item -> {
//                    if (item.getItemId() == R.id.action_edit) {
//                        listener.onEventEdit(event);
//                        return true;
//                    } else if (item.getItemId() == R.id.action_delete) {
//                        listener.onEventDelete(event);
//                        return true;
//                    }
//                    return false;
//                });
//
//                popup.show();
//            }
//        }
//    }
//
//    public void updateEvents(List<Event> newEvents) {
//        this.events = newEvents;
//        notifyDataSetChanged();
//    }
//}
