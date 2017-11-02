package com.test.pankra.speechexample.listener;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.util.Log;

/**
 * Created by User on 02.11.2017.
 */

public class LogRecognitionListener implements RecognitionListener {
    private static final String TAG = "LogRecognitionListener";

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.d(TAG, "onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float v) {
        Log.d(TAG, "onRmsChanged");
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        Log.d(TAG, "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndofSpeech");
    }

    @Override
    public void onError(int error) {
        Log.d(TAG, "error " + error);
    }

    @Override
    public void onResults(Bundle results) {
        Log.d(TAG, "onResults " + results);
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        Log.d(TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int eventType, Bundle bundle) {
        Log.d(TAG, "onEvent " + eventType);
    }
}
