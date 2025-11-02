package com.example.a202sgitodoapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a202sgitodoapp.AddNewCategory;
import com.example.a202sgitodoapp.HomeActivity;
import com.example.a202sgitodoapp.MainActivity;
import com.example.a202sgitodoapp.Model.CategoryModel;
import com.example.a202sgitodoapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // --- NEW: Define constants for our two card types ---
    private static final int VIEW_TYPE_CATEGORY = 1;
    private static final int VIEW_TYPE_ADD = 2;
    public interface OnCategoryInteractionListener {
        void onRenameCategory(CategoryModel category);
        void onDeleteCategory(CategoryModel category);
    }
    private Context context;
    private List<CategoryModel> categoryList;
    private String userId;
    private FirebaseFirestore firestore;
    private OnCategoryInteractionListener listener;

    public CategoryAdapter(Context context, List<CategoryModel> categoryList, String userId) {
        this.context = context;
        this.categoryList = categoryList;
        this.userId = userId;
        this.firestore = FirebaseFirestore.getInstance();
        if (context instanceof OnCategoryInteractionListener) {
            this.listener = (OnCategoryInteractionListener) context;
        }
    }
    @Override
    public int getItemViewType(int position) {
        // The last item in the list will always be the "Add" button
        if (position == categoryList.size()) {
            return VIEW_TYPE_ADD;
        }
        return VIEW_TYPE_CATEGORY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the correct layout based on the view type
        if (viewType == VIEW_TYPE_CATEGORY) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
            return new CategoryViewHolder(view);
        } else { // viewType == VIEW_TYPE_ADD
            View view = LayoutInflater.from(context).inflate(R.layout.item_add_category, parent, false);
            return new AddViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // First, check what type of card we are currently creating
        if (holder.getItemViewType() == VIEW_TYPE_CATEGORY) {
            // We can safely cast the holder to our CategoryViewHolder
            CategoryViewHolder categoryHolder = (CategoryViewHolder) holder;
            // We get the category data. This is safe because we know this isn't the "Add" button's position.
            CategoryModel category = categoryList.get(position);
            categoryHolder.categoryName.setText(category.getName());
            categoryHolder.categoryIcon.setText(category.getIcon());
            // Get task count for this category
            updateTaskCount(category.getId(), categoryHolder.taskCount);
            // Click the main content to navigate to the task list
            categoryHolder.mainContent.setOnClickListener(v -> {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("CATEGORY_ID", category.getId());
                intent.putExtra("CATEGORY_NAME", category.getName());
                context.startActivity(intent);
            });
            // Click the menu button to show options
            categoryHolder.menuButton.setOnClickListener(v -> {
                if (listener != null) {
                    // Create a PopupMenu
                    PopupMenu popup = new PopupMenu(context, categoryHolder.menuButton);
                    popup.getMenuInflater().inflate(R.menu.category_menu, popup.getMenu());
                    // Set a listener for menu item clicks
                    popup.setOnMenuItemClickListener(item -> {
                        int itemId = item.getItemId();
                        if (itemId == R.id.menu_rename_category) {
                            listener.onRenameCategory(category);
                            return true;
                        } else if (itemId == R.id.menu_delete_category) {
                            listener.onDeleteCategory(category);
                            return true;
                        }
                        return false;
                    });
                    popup.show(); // Show the popup menu
                }
            });
        } else if (holder.getItemViewType() == VIEW_TYPE_ADD) {
            // We don't need to cast here, we just need the click listener
            holder.itemView.setOnClickListener(v -> {
                // When the "Add New" card is clicked, show the AddNewCategory dialog
                AddNewCategory.newInstance(userId).show(((HomeActivity) context).getSupportFragmentManager(), "AddNewCategory");
            });
        }
    }

    private void updateTaskCount(String categoryId, TextView taskCountView) {
        firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .whereEqualTo("categoryId", categoryId)
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        int count = value.size();
                        taskCountView.setText(count + " tasks");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return categoryList.size() + 1;
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        View mainContent; // Reference to the clickable area
        ImageView menuButton; // The three-dot menu icon
        TextView categoryIcon;
        TextView categoryName;
        TextView taskCount;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            mainContent = itemView.findViewById(R.id.main_content_layout);
            menuButton = itemView.findViewById(R.id.category_menu_button);
            categoryIcon = itemView.findViewById(R.id.category_icon);
            categoryName = itemView.findViewById(R.id.category_name);
            taskCount = itemView.findViewById(R.id.task_count);
        }
    }
    public static class AddViewHolder extends RecyclerView.ViewHolder {
        public AddViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}