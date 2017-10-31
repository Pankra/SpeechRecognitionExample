package com.test.pankra.speechexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
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

    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "RecognitionService busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Error from server";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Didn't understand, please try again.";
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
            textView.setText("error " + error + ": " + getErrorText(error));
            Log.d(TAG, new Date().getTime() + "onError: " + getErrorText(error));
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

    class LogRecognitionListener implements RecognitionListener {
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
}
