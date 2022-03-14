package com.mrmindteam.notepadapp.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.mrmindteam.notepadapp.R;
import com.mrmindteam.notepadapp.entities.Note;
import com.mrmindteam.notepadapp.listeners.NoteListener;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder>{

    Context ctx;
    private ArrayList<Note> list;
    private NoteListener noteListener;
    public NotesAdapter(Context ctx, ArrayList<Note> list, NoteListener noteListener) {
        this.ctx = ctx;
        this.list = list;
        this.noteListener = noteListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_note,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(list.get(position));
        holder.noteLayout.setOnClickListener(v->{
            noteListener.onNoteClicked(list.get(position), position);

        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder{

        TextView title , subtitle, dateTime;
        LinearLayout noteLayout;
        RoundedImageView  imageNoteRIV;


        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            dateTime = itemView.findViewById(R.id.date_time);
            noteLayout = itemView.findViewById(R.id.layout_note);
            imageNoteRIV = itemView.findViewById(R.id.riv_image_note);

        }

        void setNote(Note note){
            title.setText(note.getTitle());
            if(note.getSubtitle().trim().isEmpty()){
                subtitle.setVisibility(View.GONE);
            }else{
                subtitle.setText(note.getSubtitle());
            }
            dateTime.setText(note.getDateTime());
            GradientDrawable gradientDrawable = (GradientDrawable) noteLayout.getBackground();
            if(note.getColor() != null){
                gradientDrawable.setColor(Color.parseColor(note.getColor()));

            }
            if(note.getImagePath() != null){
                imageNoteRIV.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNoteRIV.setVisibility(View.VISIBLE);

            }else {
                imageNoteRIV.setVisibility(View.GONE);
            }
        }
    }
}
