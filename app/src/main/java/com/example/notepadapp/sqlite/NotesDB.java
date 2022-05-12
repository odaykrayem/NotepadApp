package com.example.notepadapp.sqlite;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.notepadapp.models.Note;

@Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract  class NotesDB extends RoomDatabase {

    private static  NotesDB notesDB;
    //Room doesn't allow database operations on the main thread that's why we are using async task to save note
    public static synchronized NotesDB getDataBase(Context ctx){
        if(notesDB == null){
            notesDB = Room.databaseBuilder(
                    ctx,
                    NotesDB.class,
                    "notes_db"
            ).allowMainThreadQueries()
                    .build();
        }
        return notesDB;
    }

//    public static  NotesDB getDataBase(Context ctx){
//        if(notesDB == null){
//            notesDB = Room.databaseBuilder(
//                    ctx,
//                    NotesDB.class,
//                    "notes_db"
//            ).allowMainThreadQueries()
//                    .build();
//        }
//        return notesDB;
//    }
    public abstract NoteDao noteDao();

}
