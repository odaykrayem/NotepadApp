package com.example.notepadapp.NoteLockMVP;

public interface LockNoteView {

    void showProgress();

    void hideProgress();

//    void setUsernameError();

    void setPasswordValidationError();

    void setPasswordVerificationError();

    void navigateToNote();


}
