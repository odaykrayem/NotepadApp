package com.example.notepadapp.listeners;

import com.example.notepadapp.models.Note;

public interface NoteListener {

    void onNoteClicked(Note note, int position);

}
