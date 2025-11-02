package com.example.a202sgitodoapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a202sgitodoapp.Adapter.GroupedTaskAdapter;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class GroupedTouchHelper extends ItemTouchHelper.SimpleCallback {

    private GroupedTaskAdapter adapter;

    public GroupedTouchHelper(GroupedTaskAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAbsoluteAdapterPosition();

        // ✅ Only allow swipe actions for actual tasks, not headers
        if (adapter.getItemViewType(position) == com.example.a202sgitodoapp.Model.ListItem.TYPE_HEADER) {
            adapter.notifyItemChanged(position);
            return;
        }

        if (direction == ItemTouchHelper.RIGHT) {
            // 👉 Swipe Right to DELETE
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
            builder.setMessage("Are you sure you want to delete this task?")
                    .setTitle("Delete Task")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.deleteTask(position);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.notifyItemChanged(position);
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();

            int deepPink = Color.parseColor("#FFB6C1");
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(deepPink);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(deepPink);

        } else if (direction == ItemTouchHelper.LEFT) {
            // 👈 Swipe Left to EDIT
            adapter.editTask(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {

        // ✅ Only draw swipe background for tasks (not headers)
        if (adapter.getItemViewType(viewHolder.getAbsoluteAdapterPosition()) ==
                com.example.a202sgitodoapp.Model.ListItem.TYPE_HEADER) {
            super.onChildDraw(c, recyclerView, viewHolder, 0, dY, actionState, isCurrentlyActive);
            return;
        }

        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addSwipeRightBackgroundColor(recyclerView.getContext().getResources().getColor(android.R.color.holo_red_light))
                .addSwipeRightActionIcon(R.drawable.baseline_delete_24)
                .addSwipeLeftBackgroundColor(recyclerView.getContext().getResources().getColor(android.R.color.holo_blue_light))
                .addSwipeLeftActionIcon(R.drawable.outline_edit_24)
                .create()
                .decorate();

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
