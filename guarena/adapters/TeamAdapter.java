package com.example.guarena.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guarena.R;
import com.example.guarena.models.Team;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private List<Team> teams;
    private OnTeamClickListener listener;
    private String userRole;

    public interface OnTeamClickListener {
        void onTeamClick(Team team);
        void onTeamEdit(Team team);
        void onTeamDelete(Team team);
    }

    public TeamAdapter(List<Team> teams, OnTeamClickListener listener, String userRole) {
        this.teams = teams;
        this.listener = listener;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = teams.get(position);
        holder.bind(team);
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    class TeamViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardTeam;
        private TextView tvTeamName, tvTeamDescription, tvPlayerCount, tvCoachName;
        private Chip chipSport;
        private ImageView ivTeamIcon, ivTeamMenu;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTeam = itemView.findViewById(R.id.card_team);
            tvTeamName = itemView.findViewById(R.id.tv_team_name);
            tvTeamDescription = itemView.findViewById(R.id.tv_team_description);
            tvPlayerCount = itemView.findViewById(R.id.tv_player_count);
            tvCoachName = itemView.findViewById(R.id.tv_coach_name);
            chipSport = itemView.findViewById(R.id.chip_sport);
            ivTeamIcon = itemView.findViewById(R.id.iv_team_icon);
            ivTeamMenu = itemView.findViewById(R.id.iv_team_menu);

            // Click listeners
            cardTeam.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTeamClick(teams.get(getAdapterPosition()));
                }
            });

            ivTeamMenu.setOnClickListener(v -> {
                showTeamMenu(getAdapterPosition());
            });
        }

        public void bind(Team team) {
            tvTeamName.setText(team.getName());
            tvTeamDescription.setText(team.getDescription());
            tvPlayerCount.setText(team.getPlayerCount() + " Players");
            tvCoachName.setText("Coach: " + (team.getCoachName() != null ? team.getCoachName() : "Not Assigned"));

            // Set sport chip
            chipSport.setText(team.getSport());
            setSportChipStyle(chipSport, team.getSport());

            // Set team icon based on sport
            setTeamIcon(ivTeamIcon, team.getSport());

            // Show/hide menu based on user role and permissions
            if (canEditTeam(team)) {
                ivTeamMenu.setVisibility(View.VISIBLE);
            } else {
                ivTeamMenu.setVisibility(View.GONE);
            }
        }

        private void setSportChipStyle(Chip chip, String sport) {
            switch (sport.toLowerCase()) {
                case "football":
                    chip.setChipBackgroundColorResource(R.color.chip_football_bg);
                    chip.setTextColor(itemView.getContext().getColor(R.color.chip_football_text));
                    break;
                case "basketball":
                    chip.setChipBackgroundColorResource(R.color.chip_basketball_bg);
                    chip.setTextColor(itemView.getContext().getColor(R.color.chip_basketball_text));
                    break;
                case "cricket":
                    chip.setChipBackgroundColorResource(R.color.chip_cricket_bg);
                    chip.setTextColor(itemView.getContext().getColor(R.color.chip_cricket_text));
                    break;
                case "volleyball":
                    chip.setChipBackgroundColorResource(R.color.chip_volleyball_bg);
                    chip.setTextColor(itemView.getContext().getColor(R.color.chip_volleyball_text));
                    break;
                default:
                    chip.setChipBackgroundColorResource(R.color.chip_default_bg);
                    chip.setTextColor(itemView.getContext().getColor(R.color.chip_default_text));
                    break;
            }
        }

        private void setTeamIcon(ImageView imageView, String sport) {
            switch (sport.toLowerCase()) {
                case "football":
                    imageView.setImageResource(R.drawable.ic_sports_football);
                    break;
                case "basketball":
                    imageView.setImageResource(R.drawable.ic_sports_basketball);
                    break;
                case "cricket":
                    imageView.setImageResource(R.drawable.ic_sports_cricket);
                    break;
                case "volleyball":
                    imageView.setImageResource(R.drawable.ic_sports_volleyball);
                    break;
                default:
                    imageView.setImageResource(R.drawable.ic_people);
                    break;
            }
        }

        private boolean canEditTeam(Team team) {
            return userRole.equals("admin") || (userRole.equals("coach") && team.getCoachId() > 0);
        }

        private void showTeamMenu(int position) {
            if (listener != null && canEditTeam(teams.get(position))) {
                Team team = teams.get(position);

                // Show popup menu
                androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(
                        itemView.getContext(), ivTeamMenu);
                popup.getMenuInflater().inflate(R.menu.menu_team_item, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit_team) {
                        listener.onTeamEdit(team);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete_team) {
                        listener.onTeamDelete(team);
                        return true;
                    }
                    return false;
                });

                popup.show();
            }
        }
    }

    public void updateTeams(List<Team> newTeams) {
        this.teams = newTeams;
        notifyDataSetChanged();
    }
}
