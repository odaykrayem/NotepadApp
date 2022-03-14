package com.mrmindteam.notepadapp.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.mrmindteam.notepadapp.R;
import com.mrmindteam.notepadapp.entities.Note;
import com.mrmindteam.notepadapp.listeners.NoteListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> implements Filterable {

    Context ctx;
    private ArrayList<Note> list;
    private NoteListener noteListener;
    private Timer timer;
    private ArrayList<Note> notesSearch;

    public NotesAdapter(Context ctx, ArrayList<Note> list, NoteListener noteListener) {
        this.ctx = ctx;
        this.list = list;
        this.noteListener = noteListener;
        notesSearch = list;
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

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();

                    if(charSequence == null | charSequence.toString().length() == 0){
                        filterResults.count = notesSearch.size();
                        filterResults.values = notesSearch;
                    }else{

                        ArrayList<Note> temp = new ArrayList<>();
                        for(Note note : notesSearch ){
                            if(note.getTitle().toLowerCase().contains(charSequence.toString().toLowerCase())
                                    || note.getSubtitle().toLowerCase().contains(charSequence.toString().toLowerCase())
                                    || note.getNoteText().toLowerCase().contains(charSequence.toString().toLowerCase())){
                                temp.add(note);

                            }
                        }
                        filterResults.count = temp.size();
                        filterResults.values = temp;
                    }


                return filterResults;
            }
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                list = (ArrayList<Note>) filterResults.values;
                notifyDataSetChanged();
            }
        };
        return filter;
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
    public void searchNotes(final String searchKeyWord){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeyWord.trim().isEmpty()){
                    list = notesSearch;

                }else{
                    ArrayList<Note> temp = new ArrayList<>();
                    for(Note note : notesSearch ){
                        if(note.getTitle().toLowerCase().contains(searchKeyWord.toLowerCase())|| note.getSubtitle().toLowerCase().contains(searchKeyWord.toLowerCase()) || note.getNoteText().toLowerCase().contains(searchKeyWord.toLowerCase())){
                            temp.add(note);

                        }
                    }
                    list = temp;

                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancelTimer(){
        if(timer != null){
            timer.cancel();
        }
    }
}
