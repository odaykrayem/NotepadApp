package com.mrmindteam.notepadapp.NoteLockMVP;

import com.mrmindteam.notepadapp.models.Note;

public interface LockNoteView {
    void showProgress();

    void hideProgress();

//    void setUsernameError();

    void setPasswordValidationError();

    void setPasswordVerificationError();

    void navigateToNote();


}
