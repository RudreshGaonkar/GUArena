package com.example.guarena.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guarena.R;
import com.example.guarena.models.Exercise;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<Exercise> exercises;
    private OnExerciseClickListener listener;
    private String userRole;

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
        void onExerciseEdit(Exercise exercise);
        void onExerciseDelete(Exercise exercise);
        void onExerciseStart(Exercise exercise);
    }

    public ExerciseAdapter(List<Exercise> exercises, OnExerciseClickListener listener, String userRole) {
        this.exercises = exercises;
        this.listener = listener;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.bind(exercise);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardExercise;
        private TextView tvExerciseName, tvExerciseDescription, tvExerciseDuration, tvExerciseReps;
        private Chip chipCategory;
        private ImageView ivExerciseIcon, ivExerciseMenu;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardExercise = itemView.findViewById(R.id.card_exercise);
            tvExerciseName = itemView.findViewById(R.id.tv_exercise_name);
            tvExerciseDescription = itemView.findViewById(R.id.tv_exercise_description);
            tvExerciseDuration = itemView.findViewById(R.id.tv_exercise_duration);
            tvExerciseReps = itemView.findViewById(R.id.tv_exercise_reps);
            chipCategory = itemView.findViewById(R.id.chip_category);
            ivExerciseIcon = itemView.findViewById(R.id.iv_exercise_icon);
            ivExerciseMenu = itemView.findViewById(R.id.iv_exercise_menu);

            cardExercise.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseClick(exercises.get(getAdapterPosition()));
                }
            });

            ivExerciseMenu.setOnClickListener(v -> {
                showExerciseMenu(getAdapterPosition());
            });
        }

        public void bind(Exercise exercise) {
            tvExerciseName.setText(exercise.getName());
            tvExerciseDescription.setText(exercise.getDescription());

            // Set category chip
            chipCategory.setText(exercise.getCategory());
            setCategoryStyle(exercise.getCategory());

            // Set exercise icon based on category
            setExerciseIcon(exercise.getCategory());

            // Set duration and reps
            if (exercise.getDuration() > 0) {
                tvExerciseDuration.setText(exercise.getDuration() + " min");
                tvExerciseDuration.setVisibility(View.VISIBLE);
            } else {
                tvExerciseDuration.setVisibility(View.GONE);
            }

            if (exercise.getReps() != null && !exercise.getReps().isEmpty()) {
                tvExerciseReps.setText(exercise.getReps());
                tvExerciseReps.setVisibility(View.VISIBLE);
            } else {
                tvExerciseReps.setVisibility(View.GONE);
            }

            // Show/hide menu based on user role
            if (canEditExercise()) {
                ivExerciseMenu.setVisibility(View.VISIBLE);
            } else {
                ivExerciseMenu.setVisibility(View.GONE);
            }
        }

        private void setExerciseIcon(String category) {
            switch (category.toLowerCase()) {
                case "strength":
                    ivExerciseIcon.setImageResource(R.drawable.ic_fitness_center);
                    break;
                case "cardio":
                    ivExerciseIcon.setImageResource(R.drawable.ic_directions_run);
                    break;
                case "flexibility":
                    ivExerciseIcon.setImageResource(R.drawable.ic_self_improvement);
                    break;
                case "sport specific":
                    ivExerciseIcon.setImageResource(R.drawable.ic_sports);
                    break;
                default:
                    ivExerciseIcon.setImageResource(R.drawable.ic_fitness_center);
                    break;
            }
        }

        private void setCategoryStyle(String category) {
            switch (category.toLowerCase()) {
                case "strength":
                    chipCategory.setChipBackgroundColorResource(R.color.category_strength_bg);
                    chipCategory.setTextColor(itemView.getContext().getColor(R.color.category_strength_text));
                    break;
                case "cardio":
                    chipCategory.setChipBackgroundColorResource(R.color.category_cardio_bg);
                    chipCategory.setTextColor(itemView.getContext().getColor(R.color.category_cardio_text));
                    break;
                case "flexibility":
                    chipCategory.setChipBackgroundColorResource(R.color.category_flexibility_bg);
                    chipCategory.setTextColor(itemView.getContext().getColor(R.color.category_flexibility_text));
                    break;
                case "sport specific":
                    chipCategory.setChipBackgroundColorResource(R.color.category_sport_bg);
                    chipCategory.setTextColor(itemView.getContext().getColor(R.color.category_sport_text));
                    break;
                default:
                    chipCategory.setChipBackgroundColorResource(R.color.chip_default_bg);
                    chipCategory.setTextColor(itemView.getContext().getColor(R.color.chip_default_text));
                    break;
            }
        }

        private boolean canEditExercise() {
            return userRole.equals("admin") || userRole.equals("coach");
        }

        private void showExerciseMenu(int position) {
            if (listener != null && canEditExercise()) {
                Exercise exercise = exercises.get(position);

                androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(
                        itemView.getContext(), ivExerciseMenu);
                popup.getMenuInflater().inflate(R.menu.menu_exercise_item, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_start_exercise) {
                        listener.onExerciseStart(exercise);
                        return true;
                    } else if (item.getItemId() == R.id.action_edit_exercise) {
                        listener.onExerciseEdit(exercise);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete_exercise) {
                        listener.onExerciseDelete(exercise);
                        return true;
                    }
                    return false;
                });

                popup.show();
            }
        }
    }

    public void updateExercises(List<Exercise> newExercises) {
        this.exercises = newExercises;
        notifyDataSetChanged();
    }

    public void filterByCategory(String category) {
        // TODO: Implement category filtering
        notifyDataSetChanged();
    }
}
