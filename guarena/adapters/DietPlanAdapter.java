package com.example.guarena.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guarena.R;
import com.example.guarena.models.DietPlan;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DietPlanAdapter extends RecyclerView.Adapter<DietPlanAdapter.DietPlanViewHolder> {

    private List<DietPlan> dietPlans;
    private OnDietPlanClickListener listener;
    private String userRole;

    public interface OnDietPlanClickListener {
        void onDietPlanClick(DietPlan dietPlan);
        void onDietPlanEdit(DietPlan dietPlan);
        void onDietPlanDelete(DietPlan dietPlan);
    }

    public DietPlanAdapter(List<DietPlan> dietPlans, OnDietPlanClickListener listener, String userRole) {
        this.dietPlans = dietPlans;
        this.listener = listener;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public DietPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diet_plan, parent, false);
        return new DietPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DietPlanViewHolder holder, int position) {
        DietPlan dietPlan = dietPlans.get(position);
        holder.bind(dietPlan);
    }

    @Override
    public int getItemCount() {
        return dietPlans.size();
    }

    class DietPlanViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardDietPlan;
        private TextView tvPlanName, tvCalories, tvCreatedBy, tvCreatedDate;
        private ImageView ivDietPlanMenu;

        public DietPlanViewHolder(@NonNull View itemView) {
            super(itemView);
            cardDietPlan = itemView.findViewById(R.id.card_diet_plan);
            tvPlanName = itemView.findViewById(R.id.tv_plan_name);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvCreatedBy = itemView.findViewById(R.id.tv_created_by);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);
            ivDietPlanMenu = itemView.findViewById(R.id.iv_diet_plan_menu);

            cardDietPlan.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDietPlanClick(dietPlans.get(getAdapterPosition()));
                }
            });

            ivDietPlanMenu.setOnClickListener(v -> {
                showDietPlanMenu(getAdapterPosition());
            });
        }

        public void bind(DietPlan dietPlan) {
            tvPlanName.setText(dietPlan.getPlanName());
            tvCalories.setText(dietPlan.getCalories() + " Cal");
            tvCreatedBy.setText("By " + (dietPlan.getCreatedByName() != null ?
                    dietPlan.getCreatedByName() : "Unknown"));

            // Format date
            if (dietPlan.getCreatedAt() != null) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                    Date date = inputFormat.parse(dietPlan.getCreatedAt());
                    tvCreatedDate.setText(outputFormat.format(date));
                } catch (Exception e) {
                    tvCreatedDate.setText(dietPlan.getCreatedAt());
                }
            }

            // Show/hide menu based on user role
            if (canEditDietPlan(dietPlan)) {
                ivDietPlanMenu.setVisibility(View.VISIBLE);
            } else {
                ivDietPlanMenu.setVisibility(View.GONE);
            }
        }

        private boolean canEditDietPlan(DietPlan dietPlan) {
            return userRole.equals("admin") || userRole.equals("coach");
        }

        private void showDietPlanMenu(int position) {
            if (listener != null && canEditDietPlan(dietPlans.get(position))) {
                DietPlan dietPlan = dietPlans.get(position);

                androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(
                        itemView.getContext(), ivDietPlanMenu);
                popup.getMenuInflater().inflate(R.menu.menu_diet_plan_item, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit_diet_plan) {
                        listener.onDietPlanEdit(dietPlan);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete_diet_plan) {
                        listener.onDietPlanDelete(dietPlan);
                        return true;
                    }
                    return false;
                });

                popup.show();
            }
        }
    }

    public void updateDietPlans(List<DietPlan> newDietPlans) {
        this.dietPlans = newDietPlans;
        notifyDataSetChanged();
    }
}
