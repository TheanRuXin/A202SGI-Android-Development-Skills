package com.example.a202sgitodoapp.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a202sgitodoapp.AddNewTask;
import com.example.a202sgitodoapp.Model.ListItem;
import com.example.a202sgitodoapp.Model.ToDoModel;
import com.example.a202sgitodoapp.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupedTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ListItem> items;
    private Context context;
    private String userId;
    private FirebaseFirestore firestore;

    // This is the constructor that MainActivity is looking for
    public GroupedTaskAdapter(Context context, List<ListItem> items, String userId) {
        this.context = context;
        this.items = items;
        this.userId = userId;
        this.firestore = FirebaseFirestore.getInstance();
    }

    // This method is the key: it tells the adapter which layout to use for which position
    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ListItem.TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_task_header, parent, false);
            return new HeaderViewHolder(view);
        } else { // It must be a task
            View view = LayoutInflater.from(context).inflate(R.layout.each_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == ListItem.TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            ListItem.HeaderItem header = (ListItem.HeaderItem) items.get(position);
            headerHolder.headerTitle.setText(header.getCategoryName());
        } else {
            TaskViewHolder taskHolder = (TaskViewHolder) holder;
            ListItem.TaskItem taskItem = (ListItem.TaskItem) items.get(position);
            ToDoModel task = taskItem.getTask();

            // This is the binding logic from your original ToDoAdapter
            taskHolder.mCheckBox.setText(task.getTask());
            taskHolder.mCheckBox.setOnCheckedChangeListener(null);
            taskHolder.mCheckBox.setChecked(task.getStatus() != 0);

            taskHolder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) { // ensure user triggered it
                    int newStatus = isChecked ? 1 : 0;
                    firestore.collection("users")
                            .document(userId)
                            .collection("tasks")
                            .document(task.getTaskId())
                            .update("status", newStatus)
                            .addOnSuccessListener(aVoid -> {
                                task.setStatus(newStatus);
                                Toast.makeText(context, isChecked ? "Task completed" : "Task marked incomplete", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show();
                            });
                }
            });

            taskHolder.itemView.setOnClickListener(v -> editTask(position));

            // Format the due date
            if (task.getDue() instanceof Date) { // Check if it's a real Date
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                taskHolder.mDueDateTv.setText("Due on " + sdf.format(task.getDue()));
            } else {
                taskHolder.mDueDateTv.setText(""); // If not, show nothing
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public Context getContext() {
        return context;
    }

    // A ViewHolder for the category headers
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.header_title);
        }
    }

    // A ViewHolder for the tasks (same as your ToDoAdapter's MyViewHolder)
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView mDueDateTv;
        MaterialCheckBox mCheckBox;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            mDueDateTv = itemView.findViewById(R.id.due_date_tv);
            mCheckBox = itemView.findViewById(R.id.mcheckbox);
        }
    }

    public void editTask(int position) {
        ListItem item = items.get(position);
        if (item instanceof ListItem.TaskItem) {
            ToDoModel model = ((ListItem.TaskItem) item).getTask();
            Bundle bundle = new Bundle();
            bundle.putString("task", model.getTask());
            bundle.putString("id", model.getTaskId());
            bundle.putString("USER_ID", userId);
            bundle.putString("CATEGORY_ID", model.getCategoryId());

            AddNewTask addNewTask = new AddNewTask();
            addNewTask.setArguments(bundle);
            addNewTask.show(((FragmentActivity) context).getSupportFragmentManager(), addNewTask.getTag());
        }
    }

    public void deleteTask(int position) {
        ListItem item = items.get(position);
        if (item instanceof ListItem.TaskItem) {
            ToDoModel model = ((ListItem.TaskItem) item).getTask();

            firestore.collection("users")
                    .document(userId)
                    .collection("tasks")
                    .document(model.getTaskId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
                    });

            // Remove task from list
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void setTasks(List<ListItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
}