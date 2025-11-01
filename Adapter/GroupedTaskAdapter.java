package com.example.a202sgitodoapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a202sgitodoapp.Model.ListItem;
import com.example.a202sgitodoapp.Model.ToDoModel;
import com.example.a202sgitodoapp.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupedTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ListItem> items;
    private Context context;
    private String userId;

    // This is the constructor that MainActivity is looking for
    public GroupedTaskAdapter(Context context, List<ListItem> items, String userId) {
        this.context = context;
        this.items = items;
        this.userId = userId;
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
            taskHolder.mCheckBox.setChecked(task.getStatus() != 0);

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
}