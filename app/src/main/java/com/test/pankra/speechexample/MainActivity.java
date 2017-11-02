package com.test.pankra.speechexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.test.pankra.speechexample.listener.LogRecognitionListener;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_VOICE = 1;
    private SpeechRecognizer speechRecognizer;
    private TextView textView;
    private RecognitionListener recognitionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        //https://stackoverflow.com/questions/6316937/how-can-i-use-speech-recognition-without-the-annoying-dialog-in-android-phones --look for the answer in the question

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.label_speak_now));

                startActivityForResult(Intent.createChooser(intent, null), REQUEST_CODE_VOICE);
            }
        });

        findViewById(R.id.button_no_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechRecognizer.setRecognitionListener(new SimpleRecognitionListener());
                startRecognition();
            }
        });

        findViewById(R.id.button_continuous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechRecognizer.setRecognitionListener(new ContinuousRecognitionListener());
                startRecognition();
            }
        });

        findViewById(R.id.stream_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, ((CheckBox) v).isChecked());
            }
        });
        findViewById(R.id.stream_alarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamMute(AudioManager.STREAM_ALARM, ((CheckBox) v).isChecked());
            }
        });
        findViewById(R.id.stream_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, ((CheckBox) v).isChecked());
            }
        });
        findViewById(R.id.stream_ring).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamMute(AudioManager.STREAM_RING, ((CheckBox) v).isChecked());

            }
        });
        findViewById(R.id.stream_system).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, ((CheckBox) v).isChecked());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
        audioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        audioManager.setStreamMute(AudioManager.STREAM_RING, false);
        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_VOICE:
                    ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String result = matches.get(0);
                        textView.setText(result);
                    }
                    break;
            }
        }
    }

    private void restartRecognition() {
        Log.d(TAG, new Date().getTime() + "restartRecognition start");
        speechRecognizer.stopListening();
        speechRecognizer.cancel();
        speechRecognizer.destroy();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new ContinuousRecognitionListener());

        startRecognition();
    }

    private void startRecognition() {
        Log.d(TAG, new Date().getTime() + "startRecognition start");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 60 * 1000);

        Log.d(TAG, new Date().getTime() + "startRecognition before startListening");
        speechRecognizer.startListening(intent);
    }

    class ContinuousRecognitionListener extends LogRecognitionListener {
        @Override
        public void onRmsChanged(float v) {
        }

        @Override
        public void onError(int error) {
            super.onError(error);
            textView.setText("error " + error + ": " + SpeechUtil.getErrorText(error));
            Log.d(TAG, new Date().getTime() + "onError: " + SpeechUtil.getErrorText(error));
            restartRecognition();
        }

        @Override
        public void onResults(Bundle results) {
            super.onResults(results);
            receiveResults(results);
            startRecognition();
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            super.onPartialResults(partialResults);
            receiveResults(partialResults);
        }
    }

    private void receiveResults(Bundle results) {
        if (results == null || !results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)) {
            Log.d(TAG, "receive NO results");
            return;
        }
        String str = "\n";
        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (int i = 0; i < data.size(); i++) {
            Log.d(TAG, "result " + data.get(i));
            str += "\n" + data.get(i);
        }
        textView.setText("results: " + String.valueOf(data.size()) + str);
    }

    class SimpleRecognitionListener extends LogRecognitionListener {
        @Override
        public void onError(int error) {
            super.onError(error);
            textView.setText("error " + error);
        }

        @Override
        public void onResults(Bundle results) {
            super.onResults(results);
            String str = "\n";
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++) {
                Log.d(TAG, "result " + data.get(i));
                str += "\n" + data.get(i);
            }
            textView.setText("results: " + String.valueOf(data.size()) + str);
        }
    }

}
