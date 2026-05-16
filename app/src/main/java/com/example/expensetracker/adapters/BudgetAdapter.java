package com.example.expensetracker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.models.Budget;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    private Context context;
    private List<Budget> budgets;

    public BudgetAdapter(Context context, List<Budget> budgets) {
        this.context = context;
        this.budgets = budgets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        double pct = budget.getPercentage();

        holder.tvIcon.setText(budget.getCategoryIcon() != null ? budget.getCategoryIcon() : "💰");
        holder.tvCategory.setText(budget.getCategoryName() != null ? budget.getCategoryName() : "Category");
        holder.tvBudgetAmount.setText(String.format("Budget: Rs. %.2f", budget.getAmount()));
        holder.tvSpent.setText(String.format("Spent: Rs. %.2f", budget.getSpent()));
        holder.progressBar.setProgress((int) Math.min(pct, 100));

        String status;
        int color;
        if (pct >= 100) {
            status = "⚠️ Budget exceeded!";
            color = Color.parseColor("#FF4444");
            holder.progressBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (pct >= 80) {
            status = "⚠️ Nearing limit (" + String.format("%.0f", pct) + "%)";
            color = Color.parseColor("#FF8800");
            holder.progressBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            status = String.format("%.0f%% used", pct);
            color = Color.parseColor("#4CAF50");
            holder.progressBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        holder.tvStatus.setText(status);
        holder.tvStatus.setTextColor(color);
    }

    @Override
    public int getItemCount() { return budgets.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvCategory, tvBudgetAmount, tvSpent, tvStatus;
        ProgressBar progressBar;

        ViewHolder(View v) {
            super(v);
            tvIcon = v.findViewById(R.id.tvBudgetIcon);
            tvCategory = v.findViewById(R.id.tvBudgetCategory);
            tvBudgetAmount = v.findViewById(R.id.tvBudgetAmount);
            tvSpent = v.findViewById(R.id.tvBudgetSpent);
            tvStatus = v.findViewById(R.id.tvBudgetStatus);
            progressBar = v.findViewById(R.id.progressBudget);
        }
    }
}
