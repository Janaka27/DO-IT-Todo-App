package com.example.todo_app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private final List<TaskItem> taskItems = new ArrayList<>();
    private TaskAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private DBHelper dbHelper;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DBHelper(requireContext());
        recyclerView = view.findViewById(R.id.taskRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        View addTaskButton = view.findViewById(R.id.addTaskButton);

        adapter = new TaskAdapter(
                requireContext(),
                taskItems,
                this::updateEmptyState,
                new TaskAdapter.TaskPersistenceListener() {
                    @Override
                    public boolean onDeleteTask(TaskItem task) {
                        if (AppState.currentUser == null || dbHelper == null) {
                            return false;
                        }
                        return dbHelper.deleteTask(task.id, AppState.currentUser.id) > 0;
                    }

                    @Override
                    public boolean onUpdateTask(TaskItem task, String updatedTitle, String updatedDate, String updatedTime) {
                        if (AppState.currentUser == null || dbHelper == null) {
                            return false;
                        }
                        return dbHelper.updateTask(task.id, updatedTitle, updatedDate, updatedTime, AppState.currentUser.id) > 0;
                    }
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        if (addTaskButton != null) {
            addTaskButton.setOnClickListener(v -> showAddTaskDialog());
        }

        loadTasksForCurrentUser();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null && dbHelper != null) {
            loadTasksForCurrentUser();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }
    }

    private void showAddTaskDialog() {
        if (!isAdded()) {
            return;
        }
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null, false);
        EditText taskInput = dialogView.findViewById(R.id.taskInput);
        EditText dateInput = dialogView.findViewById(R.id.dateInput);
        EditText timeInput = dialogView.findViewById(R.id.timeInput);
        TextView addAction = dialogView.findViewById(R.id.addAction);
        TextView cancelAction = dialogView.findViewById(R.id.cancelAction);

        setupDateTimeInputs(dateInput, timeInput);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext()).setView(dialogView).create();
        dialog.setCanceledOnTouchOutside(false);
        cancelAction.setOnClickListener(v -> dialog.dismiss());
        addAction.setOnClickListener(v -> {
            String title = taskInput.getText().toString().trim();
            String date = normalizeDateOrTime(dateInput.getText().toString());
            String time = normalizeDateOrTime(timeInput.getText().toString());
            if (title.isEmpty()) {
                taskInput.setError("Enter task");
                return;
            }
            if (adapter == null || dbHelper == null || AppState.currentUser == null) {
                Toast.makeText(requireContext(), "Unable to add task right now", Toast.LENGTH_SHORT).show();
                return;
            }

            long insertedId = dbHelper.insertTask(title, date, time, AppState.currentUser.id);
            if (insertedId == -1) {
                Toast.makeText(requireContext(), "Failed to save task", Toast.LENGTH_SHORT).show();
                return;
            }

            taskItems.add(0, new TaskItem((int) insertedId, AppState.currentUser.id, title, date, time));
            adapter.notifyItemInserted(0);
            if (recyclerView != null) {
                recyclerView.scrollToPosition(0);
            }
            updateEmptyState();
            dialog.dismiss();
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private void loadTasksForCurrentUser() {
        taskItems.clear();
        if (AppState.currentUser != null && dbHelper != null) {
            taskItems.addAll(dbHelper.getTaskListByUser(AppState.currentUser.id));
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateEmptyState();
    }

    private void setupDateTimeInputs(EditText dateInput, EditText timeInput) {
        configureAsPickerField(dateInput, v -> showDatePicker(dateInput));
        configureAsPickerField(timeInput, v -> showTimePicker(timeInput));
    }

    private void configureAsPickerField(EditText input, View.OnClickListener clickListener) {
        input.setFocusable(false);
        input.setFocusableInTouchMode(false);
        input.setCursorVisible(false);
        input.setOnClickListener(clickListener);
    }

    private void showDatePicker(EditText dateInput) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog pickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth, 0, 0, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selected.getTime());
                    dateInput.setText(formattedDate);
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        pickerDialog.show();
    }

    private void showTimePicker(EditText timeInput) {
        Calendar now = Calendar.getInstance();
        boolean is24Hour = android.text.format.DateFormat.is24HourFormat(requireContext());
        TimePickerDialog pickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selected.set(Calendar.MINUTE, minute);
                    selected.set(Calendar.SECOND, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    String pattern = is24Hour ? "HH:mm" : "hh:mm a";
                    String formattedTime = new SimpleDateFormat(pattern, Locale.getDefault()).format(selected.getTime());
                    timeInput.setText(formattedTime);
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                is24Hour
        );
        pickerDialog.show();
    }

    private String normalizeDateOrTime(String value) {
        if (value == null) {
            return "-";
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? "-" : normalized;
    }

    private void updateEmptyState() {
        if (emptyStateText == null) {
            return;
        }
        emptyStateText.setVisibility(taskItems.isEmpty() ? View.VISIBLE : View.GONE);
    }
}