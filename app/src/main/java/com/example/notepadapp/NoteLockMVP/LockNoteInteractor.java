package com.example.notepadapp.NoteLockMVP;

import android.os.Handler;
import android.text.TextUtils;

public class LockNoteInteractor {

    interface OnLoginFinishedListener {
//        void onUsernameError();
        void onPasswordValidationError();

        void onPasswordVerificationError();

        void onSuccess();
    }

    public void goToNote(final String notePassword, final String password, final OnLoginFinishedListener listener) {
        new Handler().postDelayed(() -> {

            if (TextUtils.isEmpty(password)) {
                listener.onPasswordValidationError();
                return;
            }else {
                if(notePassword.equals(password)){
                    listener.onSuccess();
                }else{
                    listener.onPasswordVerificationError();
                }
            }
        }, 2000);
    }
}
