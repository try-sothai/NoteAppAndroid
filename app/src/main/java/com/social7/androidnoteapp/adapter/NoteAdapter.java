package com.social7.androidnoteapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.social7.androidnoteapp.R;
import com.social7.androidnoteapp.model.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView Adapter for displaying note cards with category badges and icons.
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onFavoriteToggle(Note note);
        void onNoteLongClick(Note note, View view);
    }

    private final Context context;
    private final List<Note> noteList;
    private final OnNoteClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault());

    public NoteAdapter(Context context, OnNoteClickListener listener) {
        this.context = context;
        this.noteList = new ArrayList<>();
        this.listener = listener;
    }

    public void setNotes(List<Note> notes) {
        this.noteList.clear();
        if (notes != null) {
            this.noteList.addAll(notes);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_note_adapter, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);

        holder.tvTitle.setText(TextUtils.isEmpty(note.getTitle()) ? "(Untitled Note)" : note.getTitle());
        holder.tvContent.setText(TextUtils.isEmpty(note.getContent()) ? "" : note.getContent());
        holder.tvCategory.setText(note.getCategory());
        holder.tvDate.setText(dateFormat.format(new Date(note.getUpdatedAt())));

        // Apply Category Badge Style dynamically
        applyCategoryStyle(holder.tvCategory, note.getCategory());

        // Pin indicator
        holder.ivPin.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);

        // Favorite icon star
        if (note.isFavorite()) {
            holder.btnFavorite.setImageResource(R.drawable.ic_star_filled);
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_star_border);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(note);
            }
        });

        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteToggle(note);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onNoteLongClick(note, v);
                return true;
            }
            return false;
        });
    }

    private void applyCategoryStyle(TextView tvCategory, String category) {
        int bgColor;
        int textColor;

        if ("Study".equalsIgnoreCase(category)) {
            bgColor = ContextCompat.getColor(context, R.color.cat_study_bg);
            textColor = ContextCompat.getColor(context, R.color.cat_study_text);
        } else if ("Personal".equalsIgnoreCase(category)) {
            bgColor = ContextCompat.getColor(context, R.color.cat_personal_bg);
            textColor = ContextCompat.getColor(context, R.color.cat_personal_text);
        } else if ("Work".equalsIgnoreCase(category)) {
            bgColor = ContextCompat.getColor(context, R.color.cat_work_bg);
            textColor = ContextCompat.getColor(context, R.color.cat_work_text);
        } else if ("Shopping".equalsIgnoreCase(category)) {
            bgColor = ContextCompat.getColor(context, R.color.cat_shopping_bg);
            textColor = ContextCompat.getColor(context, R.color.cat_shopping_text);
        } else {
            bgColor = ContextCompat.getColor(context, R.color.cat_other_bg);
            textColor = ContextCompat.getColor(context, R.color.cat_other_text);
        }

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(30f);
        drawable.setColor(bgColor);

        tvCategory.setBackground(drawable);
        tvCategory.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvCategory, tvDate;
        ImageView ivPin;
        ImageButton btnFavorite;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivPin = itemView.findViewById(R.id.ivPin);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
