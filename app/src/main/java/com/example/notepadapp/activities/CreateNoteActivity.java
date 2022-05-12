package com.example.notepadapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.example.notepadapp.Constants;
import com.example.notepadapp.R;
import com.example.notepadapp.sqlite.NotesDB;
import com.example.notepadapp.models.Note;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

import static com.example.notepadapp.R.*;

public class CreateNoteActivity extends AppCompatActivity {

    private ImageView backIV, saveNoteIV;
    private EditText mNoteTitleET, mNoteSubTitleET, mNoteTextET;
    private TextView mDateTimeTV, mWEbUrlTV;
    private LinearLayout webUrlLayout;
    private View subtitleIndicatorView;
    private VoicePlayerView saveAudioVP;
    RecordButton recordButton;
    RecordView recordView;

//    NotesDB myDataBase;
//    private String selectedNoteColor;

     private String selectedNoteColor;
     private String selectedImagePath;
     private String audioPath;
     private boolean isNoteLocked, toLock, toUnLock;
     private String notePassword;

    private MediaRecorder mediaRecorder;


    private AlertDialog addUrlDialog, deleteNoteDialog, addLockDialog;


     private Note alreadyAvailableNote;

     private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_AUDIO_PERMISSION = 3;

    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
     private ImageView imageNoteIV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_create_note);
        backIV = findViewById(id.iv_back);
        saveNoteIV = findViewById(id.iv_save);
        subtitleIndicatorView = findViewById(id.view_subtitle_indicator);
        imageNoteIV = findViewById(id.iv_image_note);
        mWEbUrlTV = findViewById(id.tv_web_url);
        webUrlLayout = findViewById(id.layout_web_url);
        saveAudioVP = findViewById(id.voicePlayerView);


        backIV.setOnClickListener(v->{
            onBackPressed();
        });

        mNoteTitleET = findViewById(id.et_note_title);
        mNoteSubTitleET = findViewById(id.et_note_subtitle);
        mNoteTextET = findViewById(id.et_note);
        mDateTimeTV = findViewById(id.tv_date_time);
//        myDataBase = NotesDB.getDataBase(this);

        //Saturday, 13 march 2022 21:09 PM
        mDateTimeTV.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date())
        );
        saveNoteIV.setOnClickListener(v->{
            saveNote();
        });

        selectedNoteColor = Constants.COLOR_NOTE_DEFAULT;
//        selectedNoteColor = getResources().getColor(color.colorDefaultNoteColor);
        selectedImagePath = "";
        isNoteLocked = false;
        toLock = false;
        toUnLock = false;
        notePassword = "";

        if(getIntent().getBooleanExtra(Constants.IS_VIEW_OR_UPDATE, false)){
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra(Constants.NOTE);
            setViewOrUpdateNote();
        }

        findViewById(id.iv_delete_url).setOnClickListener(v->{
            mWEbUrlTV.setText(null);
            webUrlLayout.setVisibility(View.GONE);

        });

        findViewById(id.iv_delete_img).setOnClickListener(v->{
            imageNoteIV.setImageBitmap(null);
            imageNoteIV.setVisibility(View.GONE);
            findViewById(id.iv_delete_img).setVisibility(View.GONE);
            selectedImagePath = "";

        });

        findViewById(id.iv_delete_audio).setOnClickListener(v->{
            findViewById(id.iv_audio).setVisibility(View.GONE);
            audioPath = "";
            saveAudioVP.onPause();
            saveAudioVP.onStop();

        });
        initMiscellaneous();
        setSubtitleIndicatorColor();


    }
    private void setViewOrUpdateNote(){
        mNoteTitleET.setText(alreadyAvailableNote.getTitle());
        mNoteSubTitleET.setText(alreadyAvailableNote.getSubtitle());
        mNoteTextET.setText(alreadyAvailableNote.getNoteText());
        mDateTimeTV.setText(alreadyAvailableNote.getDateTime());
        if(alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()){
            imageNoteIV.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
            imageNoteIV.setVisibility(View.VISIBLE);
            findViewById(id.iv_delete_img).setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableNote.getImagePath();
        }

        if(alreadyAvailableNote.getAudioPath() != null && !alreadyAvailableNote.getAudioPath().trim().isEmpty()){
            saveAudioVP.setAudio(alreadyAvailableNote.getAudioPath());
            saveAudioVP.setVisibility(View.VISIBLE);
            findViewById(id.iv_delete_audio).setVisibility(View.VISIBLE);
            findViewById(id.iv_audio).setVisibility(View.VISIBLE);
            audioPath = alreadyAvailableNote.getAudioPath();

        }


        if(alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()){
            Log.e("link", alreadyAvailableNote.getWebLink() + "hh" );
            mWEbUrlTV.setText(alreadyAvailableNote.getWebLink());
            mWEbUrlTV.setVisibility(View.VISIBLE);
            webUrlLayout.setVisibility(View.VISIBLE);

        }
        if(alreadyAvailableNote.isLocked()){
            isNoteLocked = true;
            notePassword = alreadyAvailableNote.getPassword();
        }
    }
    private void saveNote(){
        if(mNoteTitleET.getText().toString().trim().isEmpty()){
            Toast.makeText(this, getResources().getString(R.string.empty_note_title), Toast.LENGTH_SHORT).show();
            return;
        }else if(mNoteSubTitleET.getText().toString().trim().isEmpty() && mNoteTextET.getText().toString().trim().isEmpty()){
            Toast.makeText(this, getResources().getString(R.string.empty_note), Toast.LENGTH_SHORT).show();
            return;
        }
        final Note note = new Note();
        note.setTitle(mNoteTitleET.getText().toString().trim());
        note.setSubtitle(mNoteSubTitleET.getText().toString().trim());
        note.setNoteText(mNoteTextET.getText().toString().trim());
        note.setDateTime(mDateTimeTV.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImagePath);
        note.setAudioPath(audioPath);
        Log.e("lock  ", String.valueOf(toLock) + "unlock :" + String.valueOf(toUnLock) +"isUnLocked" + String.valueOf(isNoteLocked));

        if(toLock){
            note.setLocked(toLock);
            note.setPassword(notePassword);
        }
        else if(toUnLock){
            //if variable toUnlock is true this means that we unlocked this note so the tolock
            //variable is still false for sure and we can use it
            note.setLocked(toLock);
            note.setPassword(notePassword);
        }else{
            note.setLocked(isNoteLocked);
            note.setPassword(notePassword);
        }
        if(webUrlLayout.getVisibility() == View.VISIBLE){
            note.setWebLink(mWEbUrlTV.getText().toString());
        }

        //update note by giving the new one the same id an ddepend on onConflictStrategy
        if(alreadyAvailableNote != null){
            note.setId(alreadyAvailableNote.getId());
        }
//        myDataBase.noteDao().insertNote(note);

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void>{
            @Override
            protected Void doInBackground(Void... params) {
                NotesDB.getDataBase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveNoteTask().execute();
    }

    private void initMiscellaneous(){
        final LinearLayout linearLayoutMisc = findViewById(id.layout_miscellaneous);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutMisc);
        linearLayoutMisc.findViewById(id.tv_miscellaneous).setOnClickListener(v->{
            if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            }else{
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            }
        });
        final ImageView ivColor1 = linearLayoutMisc.findViewById(id.iv_color_1);
        final ImageView ivColor2 = linearLayoutMisc.findViewById(id.iv_color_2);
        final ImageView ivColor3 = linearLayoutMisc.findViewById(id.iv_color_3);
        final ImageView ivColor4 = linearLayoutMisc.findViewById(id.iv_color_4);
        final ImageView ivColor5 = linearLayoutMisc.findViewById(id.iv_color_5);

        linearLayoutMisc.findViewById(id.view_color_1).setOnClickListener(v->{
//            selectedNoteColor = getResources().getColor(color.colorDefaultNoteColor);
            selectedNoteColor = Constants.COLOR_NOTE_DEFAULT;
            ivColor1.setImageResource(drawable.ic_done);
            ivColor2.setImageResource(0);
            ivColor3.setImageResource(0);
            ivColor4.setImageResource(0);
            ivColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        linearLayoutMisc.findViewById(id.view_color_2).setOnClickListener(v->{
//            selectedNoteColor = getResources().getColor(color.colorNoteColor2);
            selectedNoteColor = Constants.COLOR_NOTE_2;
            ivColor2.setImageResource(drawable.ic_done);
            ivColor1.setImageResource(0);
            ivColor3.setImageResource(0);
            ivColor4.setImageResource(0);
            ivColor5.setImageResource(0);
            setSubtitleIndicatorColor();

        });
        linearLayoutMisc.findViewById(id.view_color_3).setOnClickListener(v->{
            selectedNoteColor = Constants.COLOR_NOTE_3;
            ivColor3.setImageResource(drawable.ic_done);
            ivColor1.setImageResource(0);
            ivColor2.setImageResource(0);
            ivColor4.setImageResource(0);
            ivColor5.setImageResource(0);
            setSubtitleIndicatorColor();

        });
        linearLayoutMisc.findViewById(id.view_color_4).setOnClickListener(v->{
            selectedNoteColor = Constants.COLOR_NOTE_4;
            ivColor4.setImageResource(drawable.ic_done);
            ivColor1.setImageResource(0);
            ivColor2.setImageResource(0);
            ivColor3.setImageResource(0);
            ivColor5.setImageResource(0);
            setSubtitleIndicatorColor();

        });
        linearLayoutMisc.findViewById(id.view_color_5).setOnClickListener(v->{
            selectedNoteColor = Constants.COLOR_NOTE_5;
            ivColor5.setImageResource(drawable.ic_done);
            ivColor1.setImageResource(0);
            ivColor2.setImageResource(0);
            ivColor3.setImageResource(0);
            ivColor4.setImageResource(0);
            setSubtitleIndicatorColor();

        });

        if(alreadyAvailableNote != null && alreadyAvailableNote.getColor() !=  null && !alreadyAvailableNote.getColor().trim().isEmpty()){
            switch (alreadyAvailableNote.getColor()){
                case Constants.COLOR_NOTE_2 :
                    linearLayoutMisc.findViewById(id.view_color_2).performClick();
                    break;
                case Constants.COLOR_NOTE_3 :
                    linearLayoutMisc.findViewById(id.view_color_3).performClick();
                    break;
                case Constants.COLOR_NOTE_4 :
                    linearLayoutMisc.findViewById(id.view_color_4).performClick();
                    break;
                case Constants.COLOR_NOTE_5 :
                    linearLayoutMisc.findViewById(id.view_color_5).performClick();
                    break;

            }
        }
        linearLayoutMisc.findViewById(id.layout_add_image_misc).setOnClickListener(v->{
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        CreateNoteActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            }else{
                selectImage();
            }
        });

        recordButton = linearLayoutMisc.findViewById(id.recordButton);
        recordView = linearLayoutMisc.findViewById(id.recordView);
        recordButton.setRecordView(recordView);

        recordButton.setListenForRecord(false);

        recordButton.setOnClickListener(view -> {

            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        CreateNoteActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_AUDIO_PERMISSION
                );

            }else{
                recordButton.setListenForRecord(true);
            }
        });

        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart");

                setUpRecording();

                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                linearLayoutMisc.findViewById(id.record_audio_text).setVisibility(View.GONE);
                recordView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel");

                mediaRecorder.reset();
                mediaRecorder.release();
                File file = new File(audioPath);
                if (file.exists())
                    file.delete();

                audioPath = "";

                recordView.setVisibility(View.GONE);
                linearLayoutMisc.findViewById(id.record_audio_text).setVisibility(View.VISIBLE);

            }

            @Override
            public void onFinish(long recordTime) {
                //Stop Recording..
                Log.d("RecordView", "onFinish");

                try {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                recordView.setVisibility(View.GONE);
                linearLayoutMisc.findViewById(id.record_audio_text).setVisibility(View.VISIBLE);

                saveAudioVP.refreshPlayer(audioPath);
                saveAudioVP.setVisibility(View.VISIBLE);
                findViewById(id.iv_delete_audio).setVisibility(View.VISIBLE);
                findViewById(id.iv_audio).setVisibility(View.VISIBLE);


            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond");

                mediaRecorder.reset();
                mediaRecorder.release();

                File file = new File(audioPath);
                if (file.exists())
                    file.delete();

                audioPath = "";



                recordView.setVisibility(View.GONE);
                linearLayoutMisc.findViewById(id.record_audio_text).setVisibility(View.VISIBLE);
            }
        });

        linearLayoutMisc.findViewById(id.layout_add_url_misc).setOnClickListener(v->{
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddUrlDialog();
        });

        //not null means it is in viewing or updating state
        if(alreadyAvailableNote != null){
            linearLayoutMisc.findViewById(id.layout_delete_note_misc).setVisibility(View.VISIBLE);
            linearLayoutMisc.findViewById(id.layout_delete_note_misc).setOnClickListener(v->{
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showDeleteNoteDialog();

            });

        }

        if(alreadyAvailableNote != null){
            linearLayoutMisc.findViewById(id.layout_lock_note_misc).setVisibility(View.VISIBLE);
            TextView miscLockTV= findViewById(id.tv_misc_lock);
            ImageView miscLockIV = findViewById(id.iv_misc_lock);
            //this mean that it is now unlocked and we can lock it
            if(isNoteLocked){
                miscLockTV.setText(Constants.UN_LOCK);
                miscLockIV.setImageResource(R.drawable.ic_unlock);
            }else{
                miscLockTV.setText(Constants.LOCK);
                miscLockIV.setImageResource(R.drawable.ic_lock);
            }
            linearLayoutMisc.findViewById(id.layout_lock_note_misc).setOnClickListener(v->{
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showLockDialog();

            });

        }
    }


    private void setUpRecording() {

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "NotesApp/Media/Recording");

        if (!file.exists())
            file.mkdirs();
        audioPath = file.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".3gp";

        mediaRecorder.setOutputFile(audioPath);
    }

    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) subtitleIndicatorView.getBackground();
//        gradientDrawable.setColor(selectedNoteColor);
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void selectImage(){
         Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
         if(intent.resolveActivity(getPackageManager()) != null){
             startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
         }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else{
                Toast.makeText(this, getResources().getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //this code for selecting image from the gallery
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNoteIV.setImageBitmap(bitmap);
                        imageNoteIV.setVisibility(View.VISIBLE);
                        findViewById(id.iv_delete_img).setVisibility(View.VISIBLE);
                        selectedImagePath = getPathFromUri(selectedImageUri);

                    }catch (Exception e){
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);
        if(cursor == null){
            filePath = contentUri.getPath();
        }else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();

        }
        return filePath;
    }

    private void showAddUrlDialog(){
        if(addUrlDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    layout.dialog_add_url,
                    (ViewGroup) findViewById(id.layout_add_url_dialog)
            );
            builder.setView(view);
            addUrlDialog = builder.create();
            if(addUrlDialog.getWindow() != null){
                addUrlDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText urlET = view.findViewById(R.id.et_add_web_link);
            urlET.requestFocus();

            view.findViewById(id.tv_add_url).setOnClickListener(v->{
                if(urlET.getText().toString().trim().isEmpty()){
                    Toast.makeText(this, getResources().getString(R.string.please_enter_url), Toast.LENGTH_SHORT).show();
                }else if(!Patterns.WEB_URL.matcher(urlET.getText().toString()).matches()){
                    Toast.makeText(this, getResources().getString(R.string.please_enter_valid_url), Toast.LENGTH_SHORT).show();
                }else{
                    mWEbUrlTV.setText(urlET.getText().toString());
                    webUrlLayout.setVisibility(View.VISIBLE);
                    addUrlDialog.dismiss();
                }
            });

            view.findViewById(id.tv_cancel).setOnClickListener(v->{
                addUrlDialog.dismiss();
            });
        }
        addUrlDialog.show();
    }

    private void showLockDialog(){
        if(addLockDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    layout.dialog_lock_unlock,
                    (ViewGroup) findViewById(id.layout_add_lock_dialog)
            );
            builder.setView(view);
            addLockDialog = builder.create();
            if(addLockDialog.getWindow() != null){
                addLockDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText passET = view.findViewById(id.et_password);
            final EditText confirmET = view.findViewById(id.et_confirm_password);
            if(isNoteLocked){
                confirmET.setVisibility(View.GONE);
            }else{
                confirmET.setVisibility(View.VISIBLE);
            }
            final TextView lockNoteTV = view.findViewById(id.tv_lock_note);
            final TextView lockTV = view.findViewById(id.tv_lock);
            final ImageView lockNoteIV = view.findViewById(id.iv_lock_note);

            if (alreadyAvailableNote.isLocked()) {
                lockNoteTV.setText(Constants.UN_LOCK);
                lockTV.setText("UnLock");
                lockNoteIV.setImageResource(R.drawable.ic_unlock);
            } else {
                lockNoteTV.setText(Constants.LOCK);
                lockTV.setText("Lock");
                lockNoteIV.setImageResource(drawable.ic_lock);
            }
            passET.requestFocus();

            if(isNoteLocked){
                view.findViewById(id.tv_lock).setOnClickListener(v->{
                    if(passET.getText().toString().trim().isEmpty()){
                        Toast.makeText(this, getResources().getString(string.password_empty_error), Toast.LENGTH_SHORT).show();
                    }else{
                        String pass = passET.getText().toString().trim();
                        if(notePassword.equals(pass)){
                            toUnLock = true;
                            toLock = false;
                            notePassword = "";
                            addLockDialog.dismiss();
                        }else{
                            Toast.makeText(this, getResources().getString(string.password_error), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }else{
                view.findViewById(id.tv_lock).setOnClickListener(v->{
                    if(passET.getText().toString().trim().isEmpty()){
                        Toast.makeText(this, getResources().getString(string.password_empty_error), Toast.LENGTH_SHORT).show();
                    }else if(confirmET.getText().toString().trim().isEmpty()){
                        Toast.makeText(this, getResources().getString(string.confirm_password_empty_error), Toast.LENGTH_SHORT).show();
                    }else if(passET.getText().toString().trim().equals(confirmET.getText().toString().trim())){
                        toLock = true;
                        toUnLock = false;
                        notePassword = passET.getText().toString().trim();
                        addLockDialog.dismiss();
                    }else{
                        Toast.makeText(this, getResources().getString(string.confirm_password_equal_error), Toast.LENGTH_SHORT).show();
                        confirmET.setText("");
                        confirmET.requestFocus();
                    }
                });
            }

            view.findViewById(id.tv_cancel).setOnClickListener(v->{
                addLockDialog.dismiss();
            });
        }
        addLockDialog.show();
    }

    private void showDeleteNoteDialog(){
        if(deleteNoteDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    layout.dialog_delete_note,
                    (ViewGroup) findViewById(id.layout_delete_note)
            );
            builder.setView(view);
            deleteNoteDialog = builder.create();
            if(deleteNoteDialog.getWindow() != null){
                deleteNoteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(id.tv_delete).setOnClickListener(v->{

                @SuppressLint("StaticFieldLeak")
                class DeleteNoteTask extends AsyncTask<Void, Void, Void>{

                    @Override
                    protected Void doInBackground(Void... voids) {
                        NotesDB.getDataBase(getApplicationContext()).noteDao().deleteNote(
                                alreadyAvailableNote
                        );
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        Intent intent = new Intent();
                        intent.putExtra(Constants.IS_NOTE_DELETED, true);
                        setResult(RESULT_OK, intent);
                        finish();

                    }
                }
                new DeleteNoteTask().execute();

            });

            view.findViewById(id.tv_cancel).setOnClickListener(v->{
                deleteNoteDialog.dismiss();
            });
        }
        deleteNoteDialog.show();
    }

}

