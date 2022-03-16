package com.mrmindteam.notepadapp.listeners;

import com.mrmindteam.notepadapp.models.Note;

public interface NoteListener {

    void onNoteClicked(Note note, int position);

}
