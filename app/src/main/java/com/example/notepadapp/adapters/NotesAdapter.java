package com.example.notepadapp.adapters;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.example.notepadapp.R;
import com.example.notepadapp.activities.ShareNoteActivity;
import com.example.notepadapp.models.Note;
import com.example.notepadapp.listeners.NoteListener;
import com.example.notepadapp.sqlite.NotesDB;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> implements Filterable {

    Context ctx;
    private ArrayList<Note> list;
    private NoteListener noteListener;
    private Timer timer;
    private ArrayList<Note> notesSearch;
    NotesDB notesDB;

    public NotesAdapter(Context ctx, ArrayList<Note> list, NoteListener noteListener) {
        this.ctx = ctx;
        this.list = list;
        this.noteListener = noteListener;
        notesSearch = list;
        notesDB = NotesDB.getDataBase(ctx);
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

        holder.share.setOnClickListener(v -> {
            Intent intent = new Intent(ctx, ShareNoteActivity.class);
            intent.putExtra("share", list.get(position));
            ctx.startActivity(intent);
        });

        if(list.get(position).isFavorite()){
            holder.fav.setImageDrawable(ctx.getResources().getDrawable(R.drawable.star_filled));
        }else {
            holder.fav.setImageDrawable(ctx.getResources().getDrawable(R.drawable.star_outline));
        }

        holder.fav.setOnClickListener(v -> {
            if(list.get(position).isFavorite()){
                holder.fav.setImageDrawable(ctx.getResources().getDrawable(R.drawable.star_outline));
                list.get(position).setFavorite(false);
                notifyDataSetChanged();
            }else {
                holder.fav.setImageDrawable(ctx.getResources().getDrawable(R.drawable.star_filled));
                list.get(position).setFavorite(true);
                notifyDataSetChanged();
            }
            notesDB.noteDao().updateNote(list.get(position));
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
        ImageView share, record, lock, unlock, fav;


        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            dateTime = itemView.findViewById(R.id.date_time);
            noteLayout = itemView.findViewById(R.id.layout_note);
            imageNoteRIV = itemView.findViewById(R.id.riv_image_note);
            share = itemView.findViewById(R.id.share_btn);
            record = itemView.findViewById(R.id.record);
            lock = itemView.findViewById(R.id.lock);
            unlock = itemView.findViewById(R.id.unlock);
            fav = itemView.findViewById(R.id.fav_btn);
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

            if(note.getAudioPath() != null){
                record.setVisibility(View.VISIBLE);
            }else {
                record.setVisibility(View.GONE);
            }
            if(note.isLocked()){
                lock.setVisibility(View.VISIBLE);
                unlock.setVisibility(View.GONE);
            }else{
                lock.setVisibility(View.GONE);
                unlock.setVisibility(View.VISIBLE);
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
