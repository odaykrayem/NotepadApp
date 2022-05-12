package com.example.notepadapp.NoteLockMVP;

public class LockNotePresenter implements LockNoteInteractor.OnLoginFinishedListener {

    private LockNoteView lockNoteView;
    private LockNoteInteractor lockNoteInteractor;

    LockNotePresenter(LockNoteView lockNoteView, LockNoteInteractor lockNoteInteractor) {
        this.lockNoteView = lockNoteView;
        this.lockNoteInteractor = lockNoteInteractor;
    }

    public void validateCredentials(String notePassword, String password) {
        if (lockNoteView != null) {
            lockNoteView.showProgress();
        }

        lockNoteInteractor.goToNote(notePassword, password, this);
    }

    public void onDestroy() {
        lockNoteView = null;
    }

    @Override
    public void onPasswordValidationError() {
        if (lockNoteView != null) {
            lockNoteView.setPasswordValidationError();
            lockNoteView.hideProgress();
        }
    }

    @Override
    public void onPasswordVerificationError() {
        if (lockNoteView != null) {
            lockNoteView.setPasswordVerificationError();
            lockNoteView.hideProgress();
        }
    }

    @Override
    public void onSuccess() {
        if (lockNoteView != null) {
            lockNoteView.navigateToNote();
        }
    }
}
