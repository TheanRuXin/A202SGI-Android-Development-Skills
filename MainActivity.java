package com.example.a202sgitodoapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a202sgitodoapp.Adapter.GroupedTaskAdapter;
import com.example.a202sgitodoapp.Adapter.ToDoAdapter;
import com.example.a202sgitodoapp.Model.ToDoModel;
import com.example.a202sgitodoapp.Model.ListItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;


import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnDialogCloseListener {

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private Toolbar toolbar;
    private FirebaseFirestore firestore;
    private ToDoAdapter adapter;
    private List<ToDoModel> mList;
    private ListenerRegistration listenerRegistration;
    private String userId;
    private String categoryId;
    private String categoryName;
    private List<ListItem> groupedList;
    private GroupedTaskAdapter groupedAdapter;
    private ToDoAdapter simpleAdapter;
    private Map<String, String> categoryNameMap = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recyclerViewTasks);
        mFab = findViewById(R.id.floatingActionButton);
        toolbar = findViewById(R.id.topAppBar);

        firestore = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("USER_ID");
        categoryId = getIntent().getStringExtra("CATEGORY_ID");
        categoryName = getIntent().getStringExtra("CATEGORY_NAME");

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(categoryName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        mList = new ArrayList<>();
        groupedList = new ArrayList<>();
        if ("all_tasks_id".equals(categoryId)) {
            // If it's "All Tasks", use the new GroupedTaskAdapter
            groupedAdapter = new GroupedTaskAdapter(this, groupedList, userId);
            mRecyclerView.setAdapter(groupedAdapter);
            // We can disable swipe-to-delete for the grouped list for simplicity
        } else {
            // Otherwise, use the simple ToDoAdapter
            simpleAdapter = new ToDoAdapter(this, mList);
            mRecyclerView.setAdapter(simpleAdapter);
            // Attach swipe helper only for the simple list
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(simpleAdapter));
            itemTouchHelper.attachToRecyclerView(mRecyclerView);
        }

        // Fetch all category names once for the titles in the grouped list
        fetchAllCategoryNames();

        mFab.setOnClickListener(v -> {
            if ("all_tasks_id".equals(categoryId)) {
                AddNewTask.newInstance(userId, null).show(getSupportFragmentManager(), AddNewTask.TAG);
            } else {
                AddNewTask.newInstance(userId, categoryId).show(getSupportFragmentManager(), AddNewTask.TAG);
            }
        });

        attachDataListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Safety check to disable editing for "All Tasks"
        if ("all_tasks_id".equals(categoryId)) {
            if (itemId == R.id.menu_rename_category || itemId == R.id.menu_delete_category) {
                Toast.makeText(this, "Cannot edit the 'All Tasks' view", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        if (itemId == R.id.menu_rename_category) {
            showRenameDialog();
            return true;
        } else if (itemId == R.id.menu_delete_category) {
            showDeleteConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // This handles the back arrow click
        return true;
    }

    // THIS IS THE METHOD YOU NEED TO ADD/REPLACE
    private void attachDataListener() {
        if (userId == null) return;
        if (listenerRegistration != null) { listenerRegistration.remove(); }

        Query query;
        if ("all_tasks_id".equals(categoryId)) {
            query = firestore.collection("users").document(userId).collection("tasks")
                    .orderBy("time", Query.Direction.DESCENDING);
        } else {
            query = firestore.collection("users").document(userId).collection("tasks")
                    .whereEqualTo("categoryId", categoryId)
                    .orderBy("time", Query.Direction.DESCENDING);
        }

        listenerRegistration = query.addSnapshotListener((value, error) -> {
            if (error != null) { return; }
            if (value == null) return;
            mList.clear();
            for (QueryDocumentSnapshot doc : value) {
                mList.add(doc.toObject(ToDoModel.class).withId(doc.getId()));
            }
            if ("all_tasks_id".equals(categoryId)) {
                prepareGroupedData(mList);
                groupedAdapter.notifyDataSetChanged();
            } else {
                // For normal categories, the simple adapter works as before
                simpleAdapter.notifyDataSetChanged();
            }
        });
    }
    private void fetchAllCategoryNames() {
        firestore.collection("users").document(userId).collection("categories").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryNameMap.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        categoryNameMap.put(doc.getId(), doc.getString("name"));
                    }
                    // Once names are fetched, refresh the grouped list if it's visible
                    if ("all_tasks_id".equals(categoryId)) {
                        prepareGroupedData(mList);
                        groupedAdapter.notifyDataSetChanged();
                    }
                });
    }
    private void prepareGroupedData(List<ToDoModel> tasks) {
        groupedList.clear();
        // Use a Map to group tasks by their category ID
        Map<String, List<ToDoModel>> groupedMap = new LinkedHashMap<>();
        for (ToDoModel task : tasks) {
            String catId = task.getCategoryId() != null ? task.getCategoryId() : "uncategorized";
            if (!groupedMap.containsKey(catId)) {
                groupedMap.put(catId, new ArrayList<>());
            }
            groupedMap.get(catId).add(task);
        }
        for (Map.Entry<String, List<ToDoModel>> entry : groupedMap.entrySet()) {
            // Here, you would look up the category name from its ID for a better title
            String categoryName = getCategoryNameFromId(entry.getKey());
            groupedList.add(new ListItem.HeaderItem(categoryName));
            for (ToDoModel task : entry.getValue()) {
                groupedList.add(new ListItem.TaskItem(task));
            }
        }
    }
    private String getCategoryNameFromId(String catId) {
        if ("uncategorized".equals(catId)) {
            return "Uncategorized";
        }
        return categoryNameMap.getOrDefault(catId, "Unknown Category");
    }
    private void showRenameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Category");
        final EditText input = new EditText(this);
        input.setText(categoryName);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equals(categoryName)) {
                renameCategoryInFirestore(newName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void renameCategoryInFirestore(String newName) {
        firestore.collection("users").document(userId).collection("categories").document(categoryId)
                .update("name", newName)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Category renamed", Toast.LENGTH_SHORT).show();
                    getSupportActionBar().setTitle(newName);
                    categoryName = newName;
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error renaming", Toast.LENGTH_SHORT).show());
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure? This will permanently delete the '" + categoryName + "' category and ALL tasks inside it. This action cannot be undone.")
                .setPositiveButton("DELETE", (dialog, which) -> performCascadingDelete())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performCascadingDelete() {
        WriteBatch batch = firestore.batch();
        firestore.collection("users").document(userId).collection("tasks")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }
                    DocumentReference categoryRef = firestore.collection("users").document(userId)
                            .collection("categories").document(categoryId);
                    batch.delete(categoryRef);

                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Category and all tasks deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e -> Toast.makeText(this, "Error deleting", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error finding tasks", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        // This method is now correctly empty. The listener handles everything.
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}