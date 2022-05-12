package com.example.notepadapp.activities;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.notepadapp.Constants;
import com.example.notepadapp.NoteLockMVP.LockNoteActivity;
import com.example.notepadapp.R;
import com.example.notepadapp.adapters.NotesAdapter;
import com.example.notepadapp.sqlite.NotesDB;
import com.example.notepadapp.models.Note;
import com.example.notepadapp.listeners.NoteListener;

import java.util.ArrayList;
import java.util.List;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class MainActivity extends AppCompatActivity implements NoteListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    private static final int REQUEST_CODE_CHECK_PASS = 4;

    private RecyclerView mRV;
    private NotesAdapter mAdapter;
    private ArrayList<Note> list;
    ImageView addNoteMainIV;

    ImageView mShareAppBtn, mAboutUsBtn;

    //    EditText mSearchET;
    SearchView searchView;
    private int noteClickedPosition = -1;

    Note noteObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mShareAppBtn = findViewById(R.id.share_btn);
        mAboutUsBtn = findViewById(R.id.about_us_btn);

        mShareAppBtn.setOnClickListener(v -> {
            showQrDialog();
        });

        mAboutUsBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, AboutUsActivity.class));
        });

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

    private void showQrDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.dialog_qr_code, null);
        final AlertDialog qr_code_dialog = new AlertDialog.Builder(this).create();

        qr_code_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        qr_code_dialog.setView(view);

        ImageView qrCodeIV = view.findViewById(R.id.idIVQrcode);
        Bitmap bitmap;
        QRGEncoder qrgEncoder;

        // the windowmanager service.
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        // initializing a variable for default display.
        Display display = manager.getDefaultDisplay();
        // creating a variable for point which
        // is to be displayed in QR Code.
        Point point = new Point();
        display.getSize(point);
        // getting width and
        // height of a point
        int width = point.x;
        int height = point.y;
        // generating dimension from width and height.
        int dimen = width < height ? width : height;
        dimen = dimen * 3 / 4;

        // setting this dimensions inside our qr code
        // encoder to generate our qr code.
        qrgEncoder = new QRGEncoder("https://play.google.com/store/apps/details?id="+getPackageName(), null, QRGContents.Type.TEXT, dimen);

        try {
            // getting our qrcode in the form of bitmap.
            bitmap = qrgEncoder.encodeAsBitmap();
            // the bitmap is set inside our image
            // view using .setimagebitmap method.
            qrCodeIV.setImageBitmap(bitmap);
        } catch (com.google.zxing.WriterException e) {
            // this method is called for
            // exception handling.
            Log.e("Tag", e.toString());
        }
        qr_code_dialog.show();
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
                    if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        show_Notification(notes.get(0));
                    }else {
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(MainActivity.this)
                                        .setSmallIcon(R.drawable.ic_notification)
                                        .setContentTitle(notes.get(0).getTitle())
                                        .setContentText(notes.get(0).getNoteText());
                        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(500, mBuilder.build());
                    }


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

            }
        }
        new getNotesTask().execute();

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void show_Notification(Note note){

        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        String CHANNEL_ID="MYCHANNEL";
        NotificationChannel notificationChannel=new NotificationChannel(CHANNEL_ID,"name",NotificationManager.IMPORTANCE_LOW);
        PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),1,intent,0);
        Notification notification = new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentText(note.getNoteText())
                .setContentTitle(note.getTitle())
                .setContentIntent(pendingIntent)
                .setChannelId(CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();

        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(1,notification);

    }


}