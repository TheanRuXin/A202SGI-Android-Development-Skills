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
import java.util.HashMap;
import java.util.Map;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTask";

    private TextView setDueDate;
    private EditText mTaskEdit;
    private Button mSaveBtn;
    private FirebaseFirestore firestore;
    private String userId;
    private Context context;
    private String dueDate = "";
    private String id = "";
    private String dueDateUpdate = "";

    public static AddNewTask newInstance(String userId) {
        AddNewTask fragment = new AddNewTask();
        Bundle args = new Bundle();
        args.putString("USER_ID", userId);
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

        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getString("USER_ID", null);
            if (bundle.containsKey("task")) {
                isUpdate = true;
                String task = bundle.getString("task", "");
                id = bundle.getString("id", "");
                dueDateUpdate = bundle.getString("due", "");

                mTaskEdit.setText(task);
                setDueDate.setText(dueDateUpdate);

                if (task.trim().isEmpty()) {
                    mSaveBtn.setEnabled(false);
                    mSaveBtn.setBackgroundColor(Color.GRAY);
                }
            }
        }

        if (getArguments() != null) {
            userId = getArguments().getString("USER_ID");
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
            int MONTH = calendar.get(Calendar.MONTH);
            int YEAR = calendar.get(Calendar.YEAR);
            int DAY = calendar.get(Calendar.DATE);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                month = month + 1;
                dueDate = dayOfMonth + "/" + month + "/" + year;
                setDueDate.setText(dueDate);
            }, YEAR, MONTH, DAY);

            datePickerDialog.show();

            int deepPink = Color.parseColor("#FFB6C1");
            datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(deepPink);
            datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(deepPink);
        });

        boolean finalIsUpdate = isUpdate;
        mSaveBtn.setOnClickListener(v -> {
            String task = mTaskEdit.getText().toString().trim();
            if (task.isEmpty()) {
                Toast.makeText(requireContext(), "Task cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (finalIsUpdate) {
                // Update existing task
                firestore.collection("users")
                        .document(userId)
                        .collection("tasks")
                        .document(id)
                        .update("task", task, "due", dueDate.isEmpty() ? dueDateUpdate : dueDate)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(requireContext(), "Task Updated", Toast.LENGTH_SHORT).show();
                            dismiss(); // closes the dialog -> triggers MainActivity.onDialogClose()
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            } else {
                // Add new task
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("task", task);
                taskMap.put("due", dueDate);
                taskMap.put("status", 0);
                taskMap.put("time", FieldValue.serverTimestamp());

                firestore.collection("users")
                        .document(userId)
                        .collection("tasks")
                        .add(taskMap)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(requireContext(), "Task saved", Toast.LENGTH_SHORT).show();
                            dismiss(); // closes the dialog -> triggers MainActivity.onDialogClose()
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Error saving task", Toast.LENGTH_SHORT).show();
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
