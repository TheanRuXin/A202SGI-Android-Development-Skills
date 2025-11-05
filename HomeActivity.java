package com.example.a202sgitodoapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a202sgitodoapp.Adapter.CategoryAdapter;
import com.example.a202sgitodoapp.Adapter.ToDoAdapter;
import com.example.a202sgitodoapp.Model.CategoryModel;
import com.example.a202sgitodoapp.Model.ToDoModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryInteractionListener {
    private RecyclerView categoryRecyclerView;
    private RecyclerView searchResultsRecyclerView;
    private CategoryAdapter categoryAdapter;
    private ToDoAdapter searchAdapter;
    private List<CategoryModel> categoryList;
    private List<ToDoModel> searchResultsList;
    private EditText searchBar;
    private TextView myListsTitle;
    private FirebaseFirestore firestore;
    private String userId;
    private boolean isSearching = false;
    private ListenerRegistration categoryListener;
    private ListenerRegistration searchListener;
    private ImageButton logout;
    String greetingMessage;
    String emojiIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        userId = getIntent().getStringExtra("USER_ID");
        firestore = FirebaseFirestore.getInstance();

        searchBar = findViewById(R.id.search_bar);
        categoryRecyclerView = findViewById(R.id.category_recycler_view);
        searchResultsRecyclerView = findViewById(R.id.search_results_recycler_view);
        myListsTitle = findViewById(R.id.my_lists_title);
        categoryList = new ArrayList<>();
        searchResultsList = new ArrayList<>();
        logout = findViewById(R.id.logout_button);
        TextView greetingIcon = findViewById(R.id.greeting_icon_textview);
        TextView greetingText = findViewById(R.id.greeting_textview);
        int hourOfDay = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);

        if (hourOfDay >= 5 && hourOfDay < 12) {
            greetingMessage = "Good morning!";
            emojiIcon = "☀️";
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            greetingMessage = "Good afternoon!";
            emojiIcon = "🕶️";
        } else {
            greetingMessage = "Good evening!";
            emojiIcon = "🌙";
        }
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("'It''s' EEEE, MMMM d", java.util.Locale.getDefault());
        String dateString = dateFormat.format(new java.util.Date());
        greetingIcon.setText(emojiIcon);
        greetingText.setText(greetingMessage + "\n" + dateString);

        // Setup Category RecyclerView with Grid Layout (2 columns)
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        categoryAdapter = new CategoryAdapter(this, categoryList, userId);
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Setup Search Results RecyclerView with Linear Layout
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new ToDoAdapter(this, searchResultsList, userId, true);
        searchResultsRecyclerView.setAdapter(searchAdapter);

        // Initially hide search results
        searchResultsRecyclerView.setVisibility(View.GONE);

        // Search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    showCategories();
                } else {
                    showSearchResults();
                    // attach the listener to get live results
                    attachSearchListener(query); // <-- CALL THE NEW METHOD
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_all_tasks) {
                // This is the code to open the "All Tasks" page
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("CATEGORY_ID", "all_tasks_id");
                intent.putExtra("CATEGORY_NAME", "All Tasks");
                startActivity(intent);
                return true; // Return true to show the item as selected
            }
            return false;
        });

        logout.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // clears back stack
            startActivity(intent);
            Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadCategories(); // Attach listener here
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (categoryListener != null) {
            categoryListener.remove(); // Detach listener here to prevent leaks
        }
        if (searchListener != null) {
            searchListener.remove();
        }
    }

    private void loadCategories() {
        categoryListener = firestore.collection("users")
                .document(userId)
                .collection("categories")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading categories", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    categoryList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            CategoryModel category = doc.toObject(CategoryModel.class);
                            category.setId(doc.getId());
                            categoryList.add(category);
                        }
                    }

                    // Check for emptiness *after* the list is built
                    if (categoryList.isEmpty()) {
                        addDefaultCategories();
                    } else {
                        categoryAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void addDefaultCategories() {
        String[] defaultCategories = {"Personal", "Work", "Study", "Grocery List"};
        String[] defaultIcons = {"📝", "💼", "📚", "🛒"};

        for (int i = 0; i < defaultCategories.length; i++) {
            CategoryModel category = new CategoryModel(defaultCategories[i], defaultIcons[i], 0);
            firestore.collection("users")
                    .document(userId)
                    .collection("categories")
                    .add(category);
        }
    }

    private void attachSearchListener(String query) {
        isSearching = true;
        String searchLower = query.toLowerCase();

        // Detach any previous search listener to start fresh
        if (searchListener != null) {
            searchListener.remove();
        }

        // Attach a new listener to the tasks collection
        searchListener = firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error searching tasks", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    searchResultsList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            ToDoModel task = doc.toObject(ToDoModel.class).withId(doc.getId());
                            String taskName = task.getTask();
                            if (taskName != null && taskName.toLowerCase().contains(searchLower)) {
                                searchResultsList.add(task);
                            }
                        }
                    }

                    // You are already in search mode, so just update the data
                    searchAdapter.notifyDataSetChanged();
                    // Also update the title with the latest count
                    myListsTitle.setText("Search Results (" + searchResultsList.size() + ")");
                });
    }

    private void showCategories() {
        isSearching = false;
        myListsTitle.setText("My lists");

        if (searchListener != null) {
            searchListener.remove();
        }
        // Hide the search results view and show the category view
        searchResultsRecyclerView.setVisibility(View.GONE);
        categoryRecyclerView.setVisibility(View.VISIBLE);

        // Clear the search results list and notify the adapter
        searchResultsList.clear();
        searchAdapter.notifyDataSetChanged();
    }

    private void showSearchResults() {
        isSearching = true;
        categoryRecyclerView.setVisibility(View.GONE);
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        myListsTitle.setText("Search Results (" + searchResultsList.size() + ")");
    }
    @Override
    public void onRenameCategory(CategoryModel category) {
        AddNewCategory.newInstanceForUpdate(
                userId, category.getId(), category.getName(), category.getIcon()
        ).show(getSupportFragmentManager(), "UpdateCategory");
    }

    @Override
    public void onDeleteCategory(CategoryModel category) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure? This will permanently delete the '" + category.getName() + "' category and ALL tasks inside it. This action cannot be undone.")
                .setPositiveButton("DELETE", (dialogInterface, which) -> {
                    performCascadingDelete(category);
                })
                .setNegativeButton("CANCEL", null)
                .show();
        int blackColor = getResources().getColor(R.color.black);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(blackColor);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(blackColor);
    }

    private void performCascadingDelete(CategoryModel category) {
        WriteBatch batch = firestore.batch();

        // 1. Find all tasks belonging to the category
        firestore.collection("users").document(userId).collection("tasks")
                .whereEqualTo("categoryId", category.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // 2. Add delete operations for each task to the batch
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }

                    // 3. Add the delete operation for the category itself
                    DocumentReference categoryRef = firestore.collection("users").document(userId)
                            .collection("categories").document(category.getId());
                    batch.delete(categoryRef);

                    // 4. Commit the batch
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(HomeActivity.this, "Category and tasks deleted", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(HomeActivity.this, "Error: Could not delete", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "Error: Could not find tasks to delete", Toast.LENGTH_SHORT).show();
                });
    }
}
