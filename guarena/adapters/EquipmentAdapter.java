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
import com.example.guarena.models.Equipment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder> {

    private List<Equipment> equipmentList;
    private OnEquipmentClickListener listener;
    private String userRole;


    public interface OnEquipmentClickListener {
        void onEquipmentClick(Equipment equipment);
        void onEquipmentBorrow(Equipment equipment); // ✅ Changed from CheckOut
        void onEquipmentReturn(Equipment equipment); // ✅ Changed from CheckIn
        void onEquipmentEdit(Equipment equipment);
        void onEquipmentDelete(Equipment equipment);
    }



    public EquipmentAdapter(List<Equipment> equipmentList, OnEquipmentClickListener listener, String userRole) {
        this.equipmentList = equipmentList;
        this.listener = listener;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipment, parent, false);
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        Equipment equipment = equipmentList.get(position);
        holder.bind(equipment);
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    class EquipmentViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardEquipment;
        private TextView tvEquipmentName, tvEquipmentCategory, tvEquipmentDescription;
        private TextView tvBorrowedBy, tvDueDate, tvQuantity;
        private Chip chipStatus;
        private MaterialButton btnBorrow, btnReturn, btnEdit, btnDelete;
        private ImageView ivEquipmentIcon;

        public EquipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardEquipment = itemView.findViewById(R.id.card_equipment);
            tvEquipmentName = itemView.findViewById(R.id.tv_equipment_name);
            tvEquipmentCategory = itemView.findViewById(R.id.tv_equipment_category);
            tvEquipmentDescription = itemView.findViewById(R.id.tv_equipment_description);
            tvBorrowedBy = itemView.findViewById(R.id.tv_borrowed_by);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            chipStatus = itemView.findViewById(R.id.chip_status);
            btnBorrow = itemView.findViewById(R.id.btn_borrow);
            btnReturn = itemView.findViewById(R.id.btn_return);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            ivEquipmentIcon = itemView.findViewById(R.id.iv_equipment_icon);

            // Click listeners
            cardEquipment.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEquipmentClick(equipmentList.get(getAdapterPosition()));
                }
            });

            btnBorrow.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEquipmentBorrow(equipmentList.get(getAdapterPosition()));
                }
            });

            btnReturn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEquipmentReturn(equipmentList.get(getAdapterPosition()));
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEquipmentEdit(equipmentList.get(getAdapterPosition()));
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEquipmentDelete(equipmentList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Equipment equipment) {
            tvEquipmentName.setText(equipment.getName());
            tvEquipmentCategory.setText(equipment.getCategory());
            tvEquipmentDescription.setText(equipment.getDescription());

            // Show quantity
            if (equipment.getQuantity() > 0) {
                tvQuantity.setText(equipment.getAvailableQuantity() + "/" + equipment.getQuantity() + " available");
                tvQuantity.setVisibility(View.VISIBLE);
            } else {
                tvQuantity.setVisibility(View.GONE);
            }

            // Set status chip
            String status = equipment.getStatus();
            chipStatus.setText(status.toUpperCase());

            switch (status.toLowerCase()) {
                case "available":
                    chipStatus.setChipBackgroundColorResource(R.color.status_available);
                    chipStatus.setTextColor(itemView.getContext().getColor(R.color.white));
                    break;
                case "borrowed":
                    chipStatus.setChipBackgroundColorResource(R.color.status_borrowed);
                    chipStatus.setTextColor(itemView.getContext().getColor(R.color.white));
                    break;
                case "maintenance":
                    chipStatus.setChipBackgroundColorResource(R.color.status_maintenance);
                    chipStatus.setTextColor(itemView.getContext().getColor(R.color.white));
                    break;
            }

            // Show borrowed info
            if ("borrowed".equals(status)) {
                String borrowedBy = equipment.getBorrowedUserName();
                if (borrowedBy != null && !borrowedBy.isEmpty()) {
                    tvBorrowedBy.setText("Borrowed by: " + borrowedBy);
                    tvBorrowedBy.setVisibility(View.VISIBLE);
                } else {
                    tvBorrowedBy.setVisibility(View.GONE);
                }

                // Show due date
                String dueDate = equipment.getDueDate();
                if (dueDate != null && !dueDate.isEmpty()) {
                    try {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
                        Date date = inputFormat.parse(dueDate);
                        tvDueDate.setText("Due: " + outputFormat.format(date));
                        tvDueDate.setVisibility(View.VISIBLE);

                        // Check if overdue
                        if (date.before(new Date())) {
                            tvDueDate.setTextColor(Color.RED);
                        } else {
                            tvDueDate.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
                        }
                    } catch (Exception e) {
                        tvDueDate.setVisibility(View.GONE);
                    }
                } else {
                    tvDueDate.setVisibility(View.GONE);
                }
            } else {
                tvBorrowedBy.setVisibility(View.GONE);
                tvDueDate.setVisibility(View.GONE);
            }

            // Show/hide buttons based on role and status
            setupButtons(equipment);
        }

        private void setupButtons(Equipment equipment) {
            String status = equipment.getStatus();

            if ("student".equals(userRole)) {
                // Students can only borrow/return
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);

                // ✅ FIXED: Check if THIS user has borrowed this equipment
                if (equipment.isBorrowedByCurrentUser()) {
                    // User has borrowed this - show Return button
                    btnBorrow.setVisibility(View.GONE);
                    btnReturn.setVisibility(View.VISIBLE);
                } else if (equipment.getAvailableQuantity() > 0 && !"maintenance".equals(status)) {
                    // ✅ FIXED: Equipment has units available AND not in maintenance - show Borrow button
                    btnBorrow.setVisibility(View.VISIBLE);
                    btnReturn.setVisibility(View.GONE);
                } else {
                    // Either no units available, in maintenance, or user hasn't borrowed
                    btnBorrow.setVisibility(View.GONE);
                    btnReturn.setVisibility(View.GONE);
                }
            } else {
                // Coaches and admins can edit/delete
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                btnBorrow.setVisibility(View.GONE);
                btnReturn.setVisibility(View.GONE);
            }
        }



//        private void setupButtons(Equipment equipment) {
//            String status = equipment.getStatus();
//
//            if ("student".equals(userRole)) {
//                // Students can only borrow/return
//                btnEdit.setVisibility(View.GONE);
//                btnDelete.setVisibility(View.GONE);
//
//                if ("available".equals(status) && equipment.getAvailableQuantity() > 0) {
//                    btnBorrow.setVisibility(View.VISIBLE);
//                    btnReturn.setVisibility(View.GONE);
//                } else if ("borrowed".equals(status)) {
//                    // Check if current user borrowed it
//                    btnBorrow.setVisibility(View.GONE);
//                    btnReturn.setVisibility(View.VISIBLE);
//                } else {
//                    btnBorrow.setVisibility(View.GONE);
//                    btnReturn.setVisibility(View.GONE);
//                }
//            } else {
//                // Coaches and admins can edit/delete
//                btnEdit.setVisibility(View.VISIBLE);
//                btnDelete.setVisibility(View.VISIBLE);
//                btnBorrow.setVisibility(View.GONE);
//                btnReturn.setVisibility(View.GONE);
//            }
//        }
    }

    public void updateEquipmentList(List<Equipment> newList) {
        this.equipmentList = newList;
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
//import com.example.guarena.models.Equipment;
//import com.google.android.material.card.MaterialCardView;
//import com.google.android.material.chip.Chip;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder> {
//
//    private List<Equipment> equipmentList;
//    private OnEquipmentClickListener listener;
//    private String userRole;
//
//    public interface OnEquipmentClickListener {
//        void onEquipmentClick(Equipment equipment);
//        void onEquipmentCheckOut(Equipment equipment);
//        void onEquipmentCheckIn(Equipment equipment);
//        void onEquipmentEdit(Equipment equipment);
//        void onEquipmentDelete(Equipment equipment);
//    }
//
//    public EquipmentAdapter(List<Equipment> equipmentList, OnEquipmentClickListener listener, String userRole) {
//        this.equipmentList = equipmentList;
//        this.listener = listener;
//        this.userRole = userRole;
//    }
//
//    @NonNull
//    @Override
//    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_equipment, parent, false);
//        return new EquipmentViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
//        Equipment equipment = equipmentList.get(position);
//        holder.bind(equipment);
//    }
//
//    @Override
//    public int getItemCount() {
//        return equipmentList.size();
//    }
//
//    class EquipmentViewHolder extends RecyclerView.ViewHolder {
//        private MaterialCardView cardEquipment;
//        private TextView tvEquipmentName, tvEquipmentDescription, tvEquipmentCategory;
//        private TextView tvCheckedOutBy, tvCheckedOutDate;
//        private Chip chipStatus;
//        private ImageView ivEquipmentIcon, ivEquipmentMenu;
//
//        public EquipmentViewHolder(@NonNull View itemView) {
//            super(itemView);
//            cardEquipment = itemView.findViewById(R.id.card_equipment);
//            tvEquipmentName = itemView.findViewById(R.id.tv_equipment_name);
//            tvEquipmentDescription = itemView.findViewById(R.id.tv_equipment_description);
//            tvEquipmentCategory = itemView.findViewById(R.id.tv_equipment_category);
//            tvCheckedOutBy = itemView.findViewById(R.id.tv_checked_out_by);
//            tvCheckedOutDate = itemView.findViewById(R.id.tv_checked_out_date);
//            chipStatus = itemView.findViewById(R.id.chip_status);
//            ivEquipmentIcon = itemView.findViewById(R.id.iv_equipment_icon);
//            ivEquipmentMenu = itemView.findViewById(R.id.iv_equipment_menu);
//
//            // Click listeners
//            cardEquipment.setOnClickListener(v -> {
//                if (listener != null) {
//                    listener.onEquipmentClick(equipmentList.get(getAdapterPosition()));
//                }
//            });
//
//            ivEquipmentMenu.setOnClickListener(v -> {
//                showEquipmentMenu(getAdapterPosition());
//            });
//        }
//
//        public void bind(Equipment equipment) {
//            tvEquipmentName.setText(equipment.getName());
//            tvEquipmentDescription.setText(equipment.getDescription());
//            tvEquipmentCategory.setText(equipment.getCategory());
//
//            // Set equipment icon based on category
//            setEquipmentIcon(equipment.getCategory());
//
//            // Set status chip
//            setStatusChip(equipment);
//
//            // Set checkout info
//            setCheckoutInfo(equipment);
//
//            // Show/hide menu based on user role
//            if (userRole.equals("coach") || userRole.equals("admin")) {
//                ivEquipmentMenu.setVisibility(View.VISIBLE);
//            } else {
//                ivEquipmentMenu.setVisibility(View.GONE);
//            }
//        }
//
//        private void setEquipmentIcon(String category) {
//            switch (category.toLowerCase()) {
//                case "balls":
//                    ivEquipmentIcon.setImageResource(R.drawable.ic_sports_football);
//                    break;
//                case "fitness":
//                    ivEquipmentIcon.setImageResource(R.drawable.ic_fitness_center);
//                    break;
//                case "protective":
//                    ivEquipmentIcon.setImageResource(R.drawable.ic_privacy);
//                    break;
//                case "training":
//                    ivEquipmentIcon.setImageResource(R.drawable.ic_directions_run);
//                    break;
//                default:
//                    ivEquipmentIcon.setImageResource(R.drawable.ic_sports);
//                    break;
//            }
//        }
//
//        private void setStatusChip(Equipment equipment) {
//            chipStatus.setText(equipment.getStatus().toUpperCase());
//
//            switch (equipment.getStatus().toLowerCase()) {
//                case "available":
//                    chipStatus.setChipBackgroundColorResource(R.color.status_available_bg);
//                    chipStatus.setTextColor(itemView.getContext().getColor(R.color.status_available_text));
//                    break;
//                case "checked_out":
//                    chipStatus.setChipBackgroundColorResource(R.color.status_checked_out_bg);
//                    chipStatus.setTextColor(itemView.getContext().getColor(R.color.status_checked_out_text));
//                    break;
//                case "maintenance":
//                    chipStatus.setChipBackgroundColorResource(R.color.status_maintenance_bg);
//                    chipStatus.setTextColor(itemView.getContext().getColor(R.color.status_maintenance_text));
//                    break;
//                default:
//                    chipStatus.setChipBackgroundColorResource(R.color.chip_default_bg);
//                    chipStatus.setTextColor(itemView.getContext().getColor(R.color.chip_default_text));
//                    break;
//            }
//        }
//
//        private void setCheckoutInfo(Equipment equipment) {
//            if (equipment.isCheckedOut()) {
//                tvCheckedOutBy.setVisibility(View.VISIBLE);
//                tvCheckedOutDate.setVisibility(View.VISIBLE);
//
//                tvCheckedOutBy.setText("Checked out by: " +
//                        (equipment.getCheckedOutUserName() != null ? equipment.getCheckedOutUserName() : "Unknown"));
//
//                // Format checkout date
//                if (equipment.getCheckedOutDate() != null) {
//                    try {
//                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//                        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
//                        Date date = inputFormat.parse(equipment.getCheckedOutDate());
//                        tvCheckedOutDate.setText("Since: " + outputFormat.format(date));
//                    } catch (Exception e) {
//                        tvCheckedOutDate.setText("Since: " + equipment.getCheckedOutDate());
//                    }
//                } else {
//                    tvCheckedOutDate.setText("");
//                }
//            } else {
//                tvCheckedOutBy.setVisibility(View.GONE);
//                tvCheckedOutDate.setVisibility(View.GONE);
//            }
//        }
//
//        private void showEquipmentMenu(int position) {
//            if (listener != null && (userRole.equals("coach") || userRole.equals("admin"))) {
//                Equipment equipment = equipmentList.get(position);
//
//                // Show popup menu
//                androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(
//                        itemView.getContext(), ivEquipmentMenu);
//
//                // Inflate different menus based on equipment status
//                if (equipment.isAvailable()) {
//                    popup.getMenuInflater().inflate(R.menu.menu_equipment_available, popup.getMenu());
//                } else if (equipment.isCheckedOut()) {
//                    popup.getMenuInflater().inflate(R.menu.menu_equipment_checked_out, popup.getMenu());
//                } else {
//                    popup.getMenuInflater().inflate(R.menu.menu_equipment_maintenance, popup.getMenu());
//                }
//
//                popup.setOnMenuItemClickListener(item -> {
//                    if (item.getItemId() == R.id.action_check_out) {
//                        listener.onEquipmentCheckOut(equipment);
//                        return true;
//                    } else if (item.getItemId() == R.id.action_check_in) {
//                        listener.onEquipmentCheckIn(equipment);
//                        return true;
//                    } else if (item.getItemId() == R.id.action_edit_equipment) {
//                        listener.onEquipmentEdit(equipment);
//                        return true;
//                    } else if (item.getItemId() == R.id.action_delete_equipment) {
//                        listener.onEquipmentDelete(equipment);
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
//    public void updateEquipment(List<Equipment> newEquipmentList) {
//        this.equipmentList = newEquipmentList;
//        notifyDataSetChanged();
//    }
//
//    public void addEquipment(Equipment equipment) {
//        equipmentList.add(equipment);
//        notifyItemInserted(equipmentList.size() - 1);
//    }
//
//    public void removeEquipment(int position) {
//        if (position >= 0 && position < equipmentList.size()) {
//            equipmentList.remove(position);
//            notifyItemRemoved(position);
//        }
//    }
//}
