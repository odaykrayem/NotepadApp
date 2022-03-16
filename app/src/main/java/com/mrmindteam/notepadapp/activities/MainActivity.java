package com.mrmindteam.notepadapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import com.mrmindteam.notepadapp.Constants;
import com.mrmindteam.notepadapp.NoteLockMVP.LockNoteActivity;
import com.mrmindteam.notepadapp.R;
import com.mrmindteam.notepadapp.adapters.NotesAdapter;
import com.mrmindteam.notepadapp.sqlite.NotesDB;
import com.mrmindteam.notepadapp.models.Note;
import com.mrmindteam.notepadapp.listeners.NoteListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    private static final int REQUEST_CODE_CHECK_PASS = 4;

    private RecyclerView mRV;
    private NotesAdapter mAdapter;
    private ArrayList<Note> list;
    ImageView addNoteMainIV;

    //    EditText mSearchET;
    SearchView searchView;
    private int noteClickedPosition = -1;

    Note noteObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addNoteMainIV = findViewById(R.id.add_note_main);
        mRV = findViewById(R.id.rv_notes);
        addNoteMainIV.setOnClickListener(v -> {
            startActivityForResult(
                    new Intent(this, CreateNoteActivity.class),
                    REQUEST_CODE_ADD_NOTE
            );

        });

        mRV.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );
        list = new ArrayList<>();
        mAdapter = new NotesAdapter(MainActivity.this, list, this);
        mRV.setAdapter(mAdapter);
        getNotes(REQUEST_CODE_SHOW_NOTES, false);

//        mSearchET = findViewById(R.id.input_search);
//        mSearchET.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                mAdapter.cancelTimer();
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if(list.size() != 0){
//                    mAdapter.searchNotes(s.toString());
//                }
//            }
//        });
        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }
        });


    }


    private void getNotes(final int requestCode, final boolean isNoteDeleted) {

        @SuppressLint("StaticFieldLeak")
        class getNotesTask extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {

                return NotesDB.getDataBase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                //first time app opened
                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    list.addAll(notes);
                    mAdapter.notifyDataSetChanged();
                    //here we are adding only newly added note from db to list add scrolling to top
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    list.add(0, notes.get(0));
                    mAdapter.notifyItemInserted(0);
                    mRV.smoothScrollToPosition(0);
                    //remove note from the clicked position  and add latest updated note to same position
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    list.remove(noteClickedPosition);
                    if (isNoteDeleted) {
                        mAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        list.add(noteClickedPosition, notes.get(noteClickedPosition));
                        mAdapter.notifyItemChanged(noteClickedPosition);
                    }


                }
//                Log.e("Notes : ",notes.toString());
//                if (list.size() == 0){
//                    list.addAll(notes);
//                    mAdapter.notifyDataSetChanged();
//                }else {
//                    list.add(0, notes.get(0));
//                    mAdapter.notifyItemInserted(0);
//                }
//                mRV.smoothScrollToPosition(0);
            }
        }
        new getNotesTask().execute();

//        ArrayList<Note> notes = (ArrayList<Note>) NotesDB.getDataBase(getApplicationContext()).noteDao().getAllNotes();
    }
    //Update the note list after adding a note from CreateNote Activity

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);

        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra(Constants.IS_NOTE_DELETED, false));
            }
        } else if (requestCode == REQUEST_CODE_CHECK_PASS && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, CreateNoteActivity.class);
            intent.putExtra(Constants.IS_VIEW_OR_UPDATE, true);
            intent.putExtra(Constants.NOTE, noteObject);
            startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
        }
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        noteObject = note;

        if (note.isLocked()) {
            Intent intent = new Intent(this, LockNoteActivity.class);
            intent.putExtra(Constants.NOTE_PASS, note.getPassword());
            startActivityForResult(intent, REQUEST_CODE_CHECK_PASS);
        } else {
            Intent intent = new Intent(this, CreateNoteActivity.class);
            intent.putExtra(Constants.IS_VIEW_OR_UPDATE, true);
            intent.putExtra(Constants.NOTE, note);
            startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
        }


    }


}