package com.test.pankra.speechexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

/**
 * Пример controller'a для управления распознаванием голоса
 */

public class VoiceController {
    private static final String TAG = "VoiceController";

    private Activity context;
    private VoiceView UI;
    private SpeechRecognizer speechRecognizer;
    private RecognitionListener recognitionListener;
    private Intent recognitionIntent;
    private boolean recognitionRunning;
    private AudioManager audioManager;

    public interface VoiceView {
        void showRecognitionResult(String result);

        void stopRecognition();

        void startRecognition();
    }

    public VoiceController(Activity context, VoiceView ui) {
        this.context = context;
        this.UI = ui;
    }

    public void init() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        recognitionListener = new ContinuousRecognitionListener();
        initSpeechRecognizer();
    }

    private void initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(recognitionListener);
    }

    public void toggleRecognition() {
        if (recognitionRunning) {
            stopRecognition();
        } else {
            startRecognition();
        }
    }

    public void startRecognition() {
        Log.d(TAG, "startRecognition start");
        recognitionRunning = true;
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true); // turn off beep sound
        speechRecognizer.startListening(getRecognitionIntent());
        UI.startRecognition();
    }

    public void stopRecognition() {
        recognitionRunning = false;
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        speechRecognizer.stopListening();
        speechRecognizer.cancel();
        UI.stopRecognition();
    }

    public void finishRecognition() {
        Log.d(TAG, "finishRecognition: ");
        speechRecognizer.destroy();
    }

    private void restartRecognition() {
        finishRecognition();
        initSpeechRecognizer();
        startRecognition();
    }


    @NonNull
    private Intent getRecognitionIntent() {
        if (recognitionIntent == null) {
            recognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
            recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            recognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            recognitionIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        }
        return recognitionIntent;
    }

    private void receiveResults(Bundle results, boolean isPartial) {
        if (results == null || !results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)) {
            Log.d(TAG, "receive NO results");
            return;
        }

        ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (data == null || data.isEmpty()) {
            Log.d(TAG, "receive EMPTY results");
            return;
        }

        UI.showRecognitionResult(data.get(0));

        if (!isPartial) {
            recognitionRunning = false;
            if ("Выход".equalsIgnoreCase(data.get(0)) || "Отмена".equalsIgnoreCase(data.get(0))) {
                context.onBackPressed();
            }
            //todo handle results: analyse result, perform server query or quit...
        }
    }

    private class ContinuousRecognitionListener implements RecognitionListener {
        @Override
        public void onError(int error) {
            UI.showRecognitionResult("error " + error + ": " + SpeechUtil.getErrorText(error));
            Log.d(TAG, "onError: " + SpeechUtil.getErrorText(error));
            if (recognitionRunning) {
                restartRecognition();
            }
        }

        @Override
        public void onResults(Bundle results) {
            receiveResults(results, false);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            receiveResults(partialResults, true);
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    }
}
