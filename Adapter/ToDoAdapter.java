package com.example.a202sgitodoapp.Adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a202sgitodoapp.AddNewTask;
import com.example.a202sgitodoapp.MainActivity;
import com.example.a202sgitodoapp.Model.ToDoModel;
import com.example.a202sgitodoapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Context;
import android.content.Intent;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;
public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {

    private List<ToDoModel> todoList;
    private Context context;
    private FirebaseFirestore firestore;
    private String userId;
    private boolean isSearchMode = false; // <-- ADD THIS NEW FLAG

    // Constructor for MainActivity (normal mode)
    public ToDoAdapter(MainActivity mainActivity, List<ToDoModel> todoList){
        this.todoList = todoList;
        this.context = mainActivity;
        this.firestore = FirebaseFirestore.getInstance();
        this.userId = mainActivity.getIntent().getStringExtra("USER_ID");
    }
    // THIS IS THE NEW CONSTRUCTOR FOR HomeActivity (search mode)
    public ToDoAdapter(Context context, List<ToDoModel> todoList, String userId, boolean isSearchMode){
        this.todoList = todoList;
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.userId = userId;
        this.isSearchMode = isSearchMode; // <-- This sets the mode
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.each_task, parent, false);
        return new MyViewHolder(view);
    }
    public void deleteTask(int position) {
        ToDoModel toDoModel = todoList.get(position);
        firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .document(toDoModel.getTaskId())
                .delete();
        todoList.remove(position);
        notifyItemRemoved(position);
    }
    public Context getContext(){
        return context;
    }
    public void editTask(int position) {
        ToDoModel toDoModel = todoList.get(position);
        Bundle bundle = new Bundle();
        bundle.putString("task", toDoModel.getTask());
        bundle.putString("id", toDoModel.getTaskId());
        bundle.putString("USER_ID", userId);
        bundle.putString("CATEGORY_ID", toDoModel.getCategoryId());
        if (toDoModel.getDue() != null) {
            // If it does, format the Date object into a "dd/MM/yyyy" string
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateString = sdf.format(toDoModel.getDue());
            // Put the formatted STRING into the bundle
            bundle.putString("due", dateString);
        } else {
            // If there is no due date, put an empty string
            bundle.putString("due", "");
        }

        AddNewTask addNewTask = new AddNewTask();
        addNewTask.setArguments(bundle);

        if (context instanceof FragmentActivity) {
            addNewTask.show(((FragmentActivity) context).getSupportFragmentManager(), addNewTask.getTag());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ToDoModel toDoModel = todoList.get(position);
        holder.mCheckBox.setText(toDoModel.getTask());

        if (toDoModel.getDue() != null) { // A simple null check is even better than 'instanceof' here
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateString = sdf.format(toDoModel.getDue());
            holder.mDueDateTv.setText("Due on " + dateString);
        } else {
            holder.mDueDateTv.setText(""); // Show nothing if there's no due date
        }

        holder.mCheckBox.setOnCheckedChangeListener(null);
        holder.mCheckBox.setChecked(toBoolean(toDoModel.getStatus()));
        holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            firestore.collection("users")
                    .document(userId)
                    .collection("tasks")
                    .document(toDoModel.getTaskId())
                    .update("status", isChecked ? 1 : 0);
        });

        // Your existing click listener logic is perfect and does not need to change
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            if (isSearchMode) {
                // BEHAVIOR 1: If searching, navigate to the task's category list
                ToDoModel clickedTask = todoList.get(currentPosition);
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("CATEGORY_ID", clickedTask.getCategoryId());
                context.startActivity(intent);
            } else {
                // BEHAVIOR 2: If not searching (in a normal list), open the edit dialog
                editTask(currentPosition);
            }
        });
    }

    private boolean toBoolean(int status){
        return status != 0;
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView mDueDateTv;
        CheckBox mCheckBox;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            mDueDateTv = itemView.findViewById((R.id.due_date_tv));
            mCheckBox = itemView.findViewById((R.id.mcheckbox));
        }
    }
}