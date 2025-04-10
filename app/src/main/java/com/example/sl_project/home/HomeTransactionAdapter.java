package com.example.sl_project.home;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;

import java.util.List;

public class HomeTransactionAdapter extends RecyclerView.Adapter<HomeTransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<HomeTransaction> homeTransactionList;

    public HomeTransactionAdapter(Context context, List<HomeTransaction> homeTransactionList) {
        this.context = context;
        this.homeTransactionList = homeTransactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        HomeTransaction homeTransaction = homeTransactionList.get(position);
        holder.amountTextView.setText(String.valueOf(homeTransaction.getAmount()));
        holder.descriptionTextView.setText(homeTransaction.getDescription());
        holder.categoryTextView.setText(homeTransaction.getCategory());

        if ("Income".equalsIgnoreCase(homeTransaction.getType())) {
            holder.amountTextView.setTextColor(ContextCompat.getColor(context, R.color.homeGreen));
        } else {
            holder.amountTextView.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return homeTransactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView amountTextView;
        TextView descriptionTextView;
        TextView categoryTextView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            amountTextView = itemView.findViewById(R.id.amount);
            descriptionTextView = itemView.findViewById(R.id.expenseName);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
        }
    }
}