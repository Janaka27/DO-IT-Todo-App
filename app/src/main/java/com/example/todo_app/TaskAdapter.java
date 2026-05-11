package com.example.todo_app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    public interface TaskChangeListener {
        void onTaskListChanged();
    }

    public interface TaskPersistenceListener {
        boolean onDeleteTask(TaskItem task);
        boolean onUpdateTask(TaskItem task, String updatedTitle, String updatedDate, String updatedTime);
    }

    private final Context context;
    private final List<TaskItem> tasks;
    @Nullable
    private final TaskChangeListener taskChangeListener;
    @Nullable
    private final TaskPersistenceListener taskPersistenceListener;

    public TaskAdapter(
            Context context,
            List<TaskItem> tasks,
            @Nullable TaskChangeListener taskChangeListener,
            @Nullable TaskPersistenceListener taskPersistenceListener
    ) {
        this.context = context;
        this.tasks = tasks;
        this.taskChangeListener = taskChangeListener;
        this.taskPersistenceListener = taskPersistenceListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskItem item = tasks.get(position);
        holder.avatar.setText(item.title.isEmpty() ? "?" : String.valueOf(Character.toUpperCase(item.title.charAt(0))));
        holder.taskName.setText(item.title);
        holder.taskDate.setText(item.date + "  " + item.time);

        holder.deleteButton.setOnClickListener(v -> new AlertDialog.Builder(context)
                .setTitle("Delete task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int adapterPos = holder.getBindingAdapterPosition();
                    if (adapterPos != RecyclerView.NO_POSITION) {
                        TaskItem selectedTask = tasks.get(adapterPos);
                        if (taskPersistenceListener != null && !taskPersistenceListener.onDeleteTask(selectedTask)) {
                            Toast.makeText(context, "Unable to delete task", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        tasks.remove(adapterPos);
                        notifyItemRemoved(adapterPos);
                        notifyTaskListChanged();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show());

        holder.editButton.setOnClickListener(v -> showEditDialog(holder.getBindingAdapterPosition()));
    }

    private void showEditDialog(int index) {
        if (index < 0 || index >= tasks.size()) {
            return;
        }
        TaskItem existing = tasks.get(index);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_task, null, false);
        TextView title = dialogView.findViewById(R.id.dialogTitle);
        EditText taskInput = dialogView.findViewById(R.id.taskInput);
        EditText dateInput = dialogView.findViewById(R.id.dateInput);
        EditText timeInput = dialogView.findViewById(R.id.timeInput);
        TextView addAction = dialogView.findViewById(R.id.addAction);
        TextView cancelAction = dialogView.findViewById(R.id.cancelAction);

        title.setText("EDIT TASK");
        addAction.setText("UPDATE");
        taskInput.setText(existing.title);
        dateInput.setText(existing.date);
        timeInput.setText(existing.time);

        setupDateTimeInputs(dateInput, timeInput);

        AlertDialog dialog = new AlertDialog.Builder(context).setView(dialogView).create();
        dialog.setCanceledOnTouchOutside(false);
        cancelAction.setOnClickListener(v -> dialog.dismiss());
        addAction.setOnClickListener(v -> {
            String updatedTitle = taskInput.getText().toString().trim();
            String updatedDate = dateInput.getText().toString().trim();
            String updatedTime = timeInput.getText().toString().trim();
            if (updatedTitle.isEmpty()) {
                taskInput.setError("Enter task");
                return;
            }

            String normalizedDate = normalizeDateOrTime(updatedDate);
            String normalizedTime = normalizeDateOrTime(updatedTime);

            if (taskPersistenceListener != null && !taskPersistenceListener.onUpdateTask(existing, updatedTitle, normalizedDate, normalizedTime)) {
                Toast.makeText(context, "Unable to update task", Toast.LENGTH_SHORT).show();
                return;
            }

            existing.title = updatedTitle;
            existing.date = normalizedDate;
            existing.time = normalizedTime;
            notifyItemChanged(index);
            notifyTaskListChanged();
            dialog.dismiss();
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private void notifyTaskListChanged() {
        if (taskChangeListener != null) {
            taskChangeListener.onTaskListChanged();
        }
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
                context,
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
        boolean is24Hour = android.text.format.DateFormat.is24HourFormat(context);
        TimePickerDialog pickerDialog = new TimePickerDialog(
                context,
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

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView avatar;
        TextView taskName;
        TextView taskDate;
        ImageButton editButton;
        ImageButton deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatarText);
            taskName = itemView.findViewById(R.id.taskNameText);
            taskDate = itemView.findViewById(R.id.taskDateText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
