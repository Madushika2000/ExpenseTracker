package com.example.expensetracker.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.activities.AddExpenseActivity;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.Expense;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    public interface OnExpenseActionListener {
        void onExpenseDeleted();
    }

    private Context context;
    private List<Expense> expenses;
    private DatabaseHelper db;
    private OnExpenseActionListener listener;

    public ExpenseAdapter(Context context, List<Expense> expenses, OnExpenseActionListener listener) {
        this.context = context;
        this.expenses = expenses;
        this.db = new DatabaseHelper(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenses.get(position);

        holder.tvIcon.setText(expense.getCategoryIcon() != null ? expense.getCategoryIcon() : "💰");
        holder.tvCategory.setText(expense.getCategoryName() != null ? expense.getCategoryName() : "Other");
        holder.tvDescription.setText(expense.getDescription() != null && !expense.getDescription().isEmpty()
            ? expense.getDescription() : "No description");
        holder.tvAmount.setText(String.format("Rs. %.2f", expense.getAmount()));
        holder.tvDate.setText(expense.getDate());

        // Set color indicator
        try {
            String color = expense.getCategoryColor();
            if (color != null) holder.tvIcon.setBackgroundColor(Color.parseColor(color + "33")); // 20% opacity
        } catch (Exception ignored) {}

        if (listener == null) {
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    showDeleteDialog(expense, adapterPosition);
                }
            });
        }

        // Long press to edit/delete
        holder.itemView.setOnLongClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                showOptionsDialog(expense, adapterPosition);
            }
            return true;
        });
    }

    private void showOptionsDialog(Expense expense, int position) {
        new AlertDialog.Builder(context)
            .setTitle("Expense Options")
            .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                if (which == 0) {
                    // Edit
                    Intent intent = new Intent(context, AddExpenseActivity.class);
                    intent.putExtra("expense_id", expense.getId());
                    intent.putExtra("expense_amount", expense.getAmount());
                    intent.putExtra("expense_date", expense.getDate());
                    intent.putExtra("expense_description", expense.getDescription());
                    intent.putExtra("expense_category_id", expense.getCategoryId());
                    context.startActivity(intent);
                } else {
                    showDeleteDialog(expense, position);
                }
            })
            .show();
    }

    private void showDeleteDialog(Expense expense, int position) {
        new AlertDialog.Builder(context)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Delete", (d, w) -> {
                db.deleteExpense(expense.getId());
                expenses.remove(position);
                notifyItemRemoved(position);
                if (listener != null) listener.onExpenseDeleted();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public int getItemCount() { return expenses.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvCategory, tvDescription, tvAmount, tvDate;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvExpenseIcon);
            tvCategory = itemView.findViewById(R.id.tvExpenseCategory);
            tvDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvDate = itemView.findViewById(R.id.tvExpenseDate);
            btnDelete = itemView.findViewById(R.id.btnDeleteExpense);
        }
    }
}
