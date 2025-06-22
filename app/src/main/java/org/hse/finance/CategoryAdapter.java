package org.hse.finance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(String categoryName);
    }

    private List<String> categories;
    private OnDeleteClickListener deleteClickListener;

    public CategoryAdapter(List<String> categories, OnDeleteClickListener listener) {
        this.categories = categories;
        this.deleteClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        Button deleteButton;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.text_category_name);
            deleteButton = view.findViewById(R.id.button_delete);
        }
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String categoryName = categories.get(position);
        holder.textView.setText(categoryName);
        holder.deleteButton.setOnClickListener(v -> deleteClickListener.onDelete(categoryName));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateData(List<String> newCategories) {
        categories = newCategories;
        notifyDataSetChanged();
    }
}
