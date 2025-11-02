package com.example.guarena.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guarena.R;
import com.example.guarena.models.Player;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    private List<Player> players;
    private OnPlayerClickListener listener;
    private String userRole;

    public interface OnPlayerClickListener {
        void onPlayerClick(Player player);
        void onPlayerEdit(Player player);
        void onPlayerRemove(Player player);
    }

    public PlayerAdapter(List<Player> players, OnPlayerClickListener listener, String userRole) {
        this.players = players;
        this.listener = listener;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player player = players.get(position);
        holder.bind(player);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    class PlayerViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardPlayer;
        private TextView tvPlayerName, tvPlayerEmail, tvJerseyNumber, tvPosition, tvPlayerStats;
        private Chip chipJersey;
        private ImageView ivPlayerAvatar, ivPlayerMenu;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPlayer = itemView.findViewById(R.id.card_player);
            tvPlayerName = itemView.findViewById(R.id.tv_player_name);
            tvPlayerEmail = itemView.findViewById(R.id.tv_player_email);
            tvJerseyNumber = itemView.findViewById(R.id.tv_jersey_number);
            tvPosition = itemView.findViewById(R.id.tv_position);
            tvPlayerStats = itemView.findViewById(R.id.tv_player_stats);
            chipJersey = itemView.findViewById(R.id.chip_jersey);
            ivPlayerAvatar = itemView.findViewById(R.id.iv_player_avatar);
            ivPlayerMenu = itemView.findViewById(R.id.iv_player_menu);

            // Click listeners
            cardPlayer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayerClick(players.get(getAdapterPosition()));
                }
            });

            ivPlayerMenu.setOnClickListener(v -> {
                showPlayerMenu(getAdapterPosition());
            });
        }

        public void bind(Player player) {
            tvPlayerName.setText(player.getUserName());
            tvPlayerEmail.setText(player.getUserEmail());

            // Jersey number and position
            if (player.getJerseyNumber() > 0) {
                tvJerseyNumber.setText("#" + player.getJerseyNumber());
                chipJersey.setText("#" + player.getJerseyNumber());
                chipJersey.setVisibility(View.VISIBLE);
            } else {
                tvJerseyNumber.setText("No Jersey");
                chipJersey.setVisibility(View.GONE);
            }

            tvPosition.setText(player.getPosition() != null ? player.getPosition() : "Not Assigned");

            // Player stats (height, weight)
            String stats = "";
            if (player.getHeight() > 0 && player.getWeight() > 0) {
                stats = String.format("%.1fcm • %.1fkg", player.getHeight(), player.getWeight());
            } else if (player.getHeight() > 0) {
                stats = String.format("%.1fcm", player.getHeight());
            } else if (player.getWeight() > 0) {
                stats = String.format("%.1fkg", player.getWeight());
            } else {
                stats = "No physical stats";
            }
            tvPlayerStats.setText(stats);

            // Set player avatar (using initials for now)
            setPlayerAvatar(player.getUserName());

            // Show/hide menu based on user role
            if (userRole.equals("coach") || userRole.equals("admin")) {
                ivPlayerMenu.setVisibility(View.VISIBLE);
            } else {
                ivPlayerMenu.setVisibility(View.GONE);
            }
        }

        private void setPlayerAvatar(String playerName) {
            // Generate initials for avatar
            String initials = "";
            if (playerName != null && !playerName.isEmpty()) {
                String[] parts = playerName.split(" ");
                if (parts.length >= 2) {
                    initials = parts[0].substring(0, 1).toUpperCase() +
                            parts[1].substring(0, 1).toUpperCase();
                } else {
                    initials = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
                }
            } else {
                initials = "P";
            }

            // For now, just set a default avatar icon
            // In a real app, you'd load from profile image or generate avatar with initials
            ivPlayerAvatar.setImageResource(R.drawable.ic_profile);
        }

        private void showPlayerMenu(int position) {
            if (listener != null && (userRole.equals("coach") || userRole.equals("admin"))) {
                Player player = players.get(position);

                // Show popup menu
                androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(
                        itemView.getContext(), ivPlayerMenu);
                popup.getMenuInflater().inflate(R.menu.menu_player_item, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit_player) {
                        listener.onPlayerEdit(player);
                        return true;
                    } else if (item.getItemId() == R.id.action_remove_player) {
                        listener.onPlayerRemove(player);
                        return true;
                    }
                    return false;
                });

                popup.show();
            }
        }
    }

    public void updatePlayers(List<Player> newPlayers) {
        this.players = newPlayers;
        notifyDataSetChanged();
    }

    public void addPlayer(Player player) {
        players.add(player);
        notifyItemInserted(players.size() - 1);
    }

    public void removePlayer(int position) {
        if (position >= 0 && position < players.size()) {
            players.remove(position);
            notifyItemRemoved(position);
        }
    }
}
