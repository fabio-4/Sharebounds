package com.sharebounds.sharebounds.keyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import static android.content.Context.AUDIO_SERVICE;

class KeyboardTextManager {

    private AudioManager mAudioManager;

    boolean playSound;
    boolean vibration;

    KeyboardTextManager(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
    }

    void onKey(int i, InputConnection inputConnection) {
        if (inputConnection == null) return;
        switch(i){
            case Keyboard.KEYCODE_DELETE:
                keyDownUp(KeyEvent.KEYCODE_DEL, inputConnection);
                break;
            case Keyboard.KEYCODE_DONE:
                keyDownUp(KeyEvent.KEYCODE_ENTER, inputConnection);
                break;
            default:
                char code = (char)i;
                inputConnection.commitText(String.valueOf(code),1);
        }
    }

    void insertText(String text, InputConnection inputConnection, View view) {
        soundClick(0, view);
        if (inputConnection == null) return;
        inputConnection.commitText(text, 1);
    }

    private void keyDownUp(int keyEventCode, InputConnection inputConnection) {
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    void soundClick(int i, View view) {
        if (playSound) {
            playClick(i);
        }
        if (vibration) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        }
    }

    private void playClick(int keyCode){
        int sound = AudioManager.FX_KEYPRESS_STANDARD;
        switch(keyCode){
            case 32:
                sound = AudioManager.FX_KEYPRESS_SPACEBAR;
                break;
            case Keyboard.KEYCODE_DONE:
                sound = AudioManager.FX_KEYPRESS_RETURN;
                break;
            case Keyboard.KEYCODE_DELETE:
                sound = AudioManager.FX_KEYPRESS_DELETE;
        }
        mAudioManager.playSoundEffect(sound);
    }
}
