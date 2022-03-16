package com.mrmindteam.notepadapp.NoteLockMVP;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.mrmindteam.notepadapp.Constants;
import com.mrmindteam.notepadapp.R;
import com.mrmindteam.notepadapp.activities.CreateNoteActivity;
import com.mrmindteam.notepadapp.models.Note;

public class LockNoteActivity extends AppCompatActivity implements LockNoteView {

    private ProgressBar progressBar;
    private EditText password;
    private LockNotePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_note);


        progressBar = findViewById(R.id.progress);
        password = findViewById(R.id.et_lock_pass);
        findViewById(R.id.btn_check).setOnClickListener(v -> {
            validateCredentials();
        });

        presenter = new LockNotePresenter(this, new LockNoteInteractor());
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }


    @Override
    public void setPasswordValidationError() {
        password.setError(getString(R.string.password_empty_error));
    }

    @Override
    public void setPasswordVerificationError() {
        password.setError(getString(R.string.password_error));
    }

    @Override
    public void navigateToNote() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();

    }

    private void validateCredentials() {
        Intent i = getIntent();
        String notePassword = i.getStringExtra(Constants.NOTE_PASS);
        presenter.validateCredentials(notePassword, password.getText().toString());
    }

}
