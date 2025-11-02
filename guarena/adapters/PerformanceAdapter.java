package com.example.guarena.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guarena.R;
import com.example.guarena.models.Performance;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PerformanceAdapter extends RecyclerView.Adapter<PerformanceAdapter.PerformanceViewHolder> {

    private List<Performance> performanceList;
    private OnPerformanceClickListener listener;

    public interface OnPerformanceClickListener {
        void onPerformanceClick(Performance performance);
    }

    public PerformanceAdapter(List<Performance> performanceList, OnPerformanceClickListener listener) {
        this.performanceList = performanceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PerformanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_performance_simple, parent, false);
        return new PerformanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PerformanceViewHolder holder, int position) {
        Performance performance = performanceList.get(position);
        holder.bind(performance);
    }

    @Override
    public int getItemCount() {
        return performanceList.size();
    }

    class PerformanceViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardPerformance;
        private TextView tvEventTitle, tvPerformanceDate, tvPerformanceScore;

        public PerformanceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPerformance = itemView.findViewById(R.id.card_performance);
            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
            tvPerformanceDate = itemView.findViewById(R.id.tv_performance_date);
            tvPerformanceScore = itemView.findViewById(R.id.tv_performance_score);

            cardPerformance.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPerformanceClick(performanceList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Performance performance) {
            tvEventTitle.setText(performance.getEventTitle());
            tvPerformanceDate.setText(performance.getDateRecorded());
            tvPerformanceScore.setText(String.format("%.1f", performance.getScore()));
        }
    }

    public void updatePerformance(List<Performance> newPerformanceList) {
        this.performanceList = newPerformanceList;
        notifyDataSetChanged();
    }
}
