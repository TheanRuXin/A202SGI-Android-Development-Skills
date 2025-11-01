package com.example.a202sgitodoapp;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.a202sgitodoapp.Model.CategoryModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddNewCategory extends BottomSheetDialogFragment {

    private EditText categoryNameEdit;
    private EditText categoryIconEdit;
    private Button saveCategoryBtn;
    private FirebaseFirestore firestore;
    private String userId;
    private String categoryId;
    private String categoryName;
    private String categoryIcon;
    private String id = "";

    public static AddNewCategory newInstanceForUpdate(String userId, String id, String name, String icon) {
        AddNewCategory fragment = new AddNewCategory();
        Bundle args = new Bundle();
        args.putString("USER_ID", userId);
        args.putString("id", id);  // Store the ID
        args.putString("name", name);
        args.putString("icon", icon);
        fragment.setArguments(args);
        return fragment;
    }
    public static AddNewCategory newInstance(String userId) {
        AddNewCategory fragment = new AddNewCategory();
        Bundle args = new Bundle();
        args.putString("USER_ID", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_new_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoryNameEdit = view.findViewById(R.id.category_name_edit);
        categoryIconEdit = view.findViewById(R.id.category_icon_edit);
        saveCategoryBtn = view.findViewById(R.id.save_category_btn);

        firestore = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            userId = getArguments().getString("USER_ID");

            if (getArguments().containsKey("id")) {
                id = getArguments().getString("id"); // get the ID
                categoryName = getArguments().getString("name");
                categoryIcon = getArguments().getString("icon");

                // PRE-FILL FIELDS
                categoryNameEdit.setText(categoryName);
                categoryIconEdit.setText(categoryIcon);
                saveCategoryBtn.setText("Save"); // Change the button text

            }
        }

        // Disable save button initially
        saveCategoryBtn.setEnabled(false);
        saveCategoryBtn.setBackgroundColor(Color.GRAY);

        // Enable save button when category name is entered
        categoryNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    saveCategoryBtn.setEnabled(false);
                    saveCategoryBtn.setBackgroundColor(Color.GRAY);
                } else {
                    saveCategoryBtn.setEnabled(true);
                    saveCategoryBtn.setBackgroundColor(getResources().getColor(R.color.pink));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        saveCategoryBtn.setOnClickListener(v -> {
            String newName = categoryNameEdit.getText().toString().trim();
            String newIcon = categoryIconEdit.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Default icon if not provided
            if (newIcon.isEmpty()) {
                newIcon = "📁";
            }

            if (id.isEmpty()) {
                // --- CREATE (If no ID, it's a new category) ---
                CategoryModel category = new CategoryModel(newName, newIcon, 0);
                firestore.collection("users")
                        .document(userId)
                        .collection("categories")
                        .add(category)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(requireContext(), "Category created", Toast.LENGTH_SHORT).show();
                            dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Error creating category", Toast.LENGTH_SHORT).show();
                        });

            } else {
                // --- UPDATE (If an ID exists, we are updating) ---
                firestore.collection("users")
                        .document(userId)
                        .collection("categories")
                        .document(id) // use the ID to update the correct document
                        .update("name", newName, "icon", newIcon)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(requireContext(), "Category updated", Toast.LENGTH_SHORT).show();
                            dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Error updating category", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}