package com.example.a202sgitodoapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTask";

    private TextView setDueDate;
    private EditText mTaskEdit;
    private Button mSaveBtn;
    private FirebaseFirestore firestore;
    private String userId;
    private String categoryId;
    private Context context;
    private String dueDate = "";
    private String id = "";
    private String dueDateUpdate = "";
    private Calendar selectedDateCalendar;

    public static AddNewTask newInstance(String userId, String categoryId) {
        AddNewTask fragment = new AddNewTask();
        Bundle args = new Bundle();
        args.putString("USER_ID", userId);
        args.putString("CATEGORY_ID", categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_new_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setDueDate = view.findViewById(R.id.set_due_tv);
        mTaskEdit = view.findViewById(R.id.task_edittext);
        mSaveBtn = view.findViewById(R.id.save_btn);

        firestore = FirebaseFirestore.getInstance();
        selectedDateCalendar = null;
        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getString("USER_ID", null);
            categoryId = bundle.getString("CATEGORY_ID", null);

            if (bundle.containsKey("task")) {
                isUpdate = true;
                String task = bundle.getString("task", "");
                id = bundle.getString("id", "");
                String dueDateUpdate = bundle.getString("due", "");

                mTaskEdit.setText(task);
                setDueDate.setText(dueDateUpdate);

                if (task.trim().isEmpty()) {
                    mSaveBtn.setEnabled(false);
                    mSaveBtn.setBackgroundColor(Color.GRAY);
                }
            }
        }

        mTaskEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    mSaveBtn.setEnabled(false);
                    mSaveBtn.setBackgroundColor(Color.GRAY);
                } else {
                    mSaveBtn.setEnabled(true);
                    mSaveBtn.setBackgroundColor(getResources().getColor(R.color.pink));
                }
            }
        });

        setDueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                // Initialize the calendar object if it's null
                if (selectedDateCalendar == null) {
                    selectedDateCalendar = Calendar.getInstance();
                }
                // Store the selected date in our Calendar object
                selectedDateCalendar.set(year, month, dayOfMonth);

                // Format a string for display purposes ONLY
                String displayDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                setDueDate.setText(displayDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

            datePickerDialog.show();
            // ... (your styling code for the dialog is fine)
        });

        boolean finalIsUpdate = isUpdate;
        mSaveBtn.setOnClickListener(v -> {
            String task = mTaskEdit.getText().toString().trim();
            if (task.isEmpty()) {
                Toast.makeText(getContext(), "Task is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("task", task);

            // --- THIS IS THE CRUCIAL, CORRECTED LOGIC ---
            // We ONLY add the 'due' field to the map if a date has been selected by the user.
            if (selectedDateCalendar != null) {
                // Create a proper Date object from our calendar
                Date dueDate = selectedDateCalendar.getTime();
                // Put the Date object into the map. This will be saved as a Timestamp.
                taskMap.put("due", dueDate);
            }

            if (finalIsUpdate) {
                // UPDATE the existing task document with the new data
                firestore.collection("users").document(userId).collection("tasks").document(id)
                        .update(taskMap) // Use the same map for updates
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Task Updated", Toast.LENGTH_SHORT).show();
                            dismiss();
                        });
            } else {
                // CREATE a new task document
                taskMap.put("status", 0);
                taskMap.put("time", FieldValue.serverTimestamp());
                taskMap.put("categoryId", categoryId); // This will be null if adding from "All Tasks"

                firestore.collection("users").document(userId).collection("tasks")
                        .add(taskMap)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(getContext(), "Task Saved", Toast.LENGTH_SHORT).show();
                            dismiss();
                        });
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener) activity).onDialogClose(dialog);
        }
    }
}