package com.example.guarena.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guarena.R;
import com.example.guarena.models.User;

import java.util.List;

public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.TeamMemberViewHolder> {

    private List<User> teamMembers;

    public TeamMemberAdapter(List<User> teamMembers) {
        this.teamMembers = teamMembers;
    }

    @NonNull
    @Override
    public TeamMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_member, parent, false);
        return new TeamMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamMemberViewHolder holder, int position) {
        User member = teamMembers.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return teamMembers != null ? teamMembers.size() : 0;
    }

    static class TeamMemberViewHolder extends RecyclerView.ViewHolder {

        private TextView tvMemberName, tvMemberPosition, tvMemberJersey, tvMemberStats;

        public TeamMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tv_member_name);
            tvMemberPosition = itemView.findViewById(R.id.tv_member_position);
            tvMemberJersey = itemView.findViewById(R.id.tv_member_jersey);
            tvMemberStats = itemView.findViewById(R.id.tv_member_stats);
        }

        public void bind(User member) {
            tvMemberName.setText(member.getFullName());

            if (member.getPosition() != null && !member.getPosition().isEmpty()) {
                tvMemberPosition.setText(member.getPosition());
            } else {
                tvMemberPosition.setText("Position: N/A");
            }

            if (member.getJerseyNumber() > 0) {
                tvMemberJersey.setText("#" + member.getJerseyNumber());
            } else {
                tvMemberJersey.setText("#--");
            }

            // Display height and weight if available
            if (member.getHeight() > 0 && member.getWeight() > 0) {
                String stats = String.format("%.1f cm | %.1f kg", member.getHeight(), member.getWeight());
                tvMemberStats.setText(stats);
                tvMemberStats.setVisibility(View.VISIBLE);
            } else {
                tvMemberStats.setVisibility(View.GONE);
            }
        }
    }
}
