package com.example.speechinterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private TextView textView;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private SpeechRecognizer objectSpeechRecognizer;
    private Intent objectSpeechRecognizerIntent;
    private Vibrator vibrator;
    private final ArrayList<String> objectsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Integrate the layout and text view
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
        textView = findViewById(R.id.textView);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);



        // Add objects
        objectsList.add("table");
        objectsList.add("laptop");
        objectsList.add("bowl");

        // Ask permission for microphone
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        // Initialize speech recognizer for the commands (find, settings, stop)
        speechRecognizer= SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Initialize speech recognizer for finding objects
        objectSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        objectSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Set listener for speech recognizers
        speechRecognizer.setRecognitionListener(commandListener);
        objectSpeechRecognizer.setRecognitionListener(objectListener);

        // Initialize text to speech for voice feedback
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS){
                    int success = textToSpeech.setLanguage(Locale.US);
                    if (success == TextToSpeech.LANG_MISSING_DATA || success == TextToSpeech.LANG_NOT_SUPPORTED){
                        textView.setText("Language Not Supported");
                    }
                    else {
                        // Ask the user to touch the screen to start
                        feedback("Tap the screen for menu options");
                    }
                }
                else {
                    textView.setText("Initialization Failed");
                }
            }
        });

        // Once screen is touched, voice assistant asks for command
        constraintLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                functionality("options");
                return false;
            }

        });
    }

    // This method asks and takes command
    public void functionality(String command){
        switch (command) {
                case "options":
                case "repeat":
                    feedback("Say find to find an object, settings to go to the settings page, or stop to end voice assistant.");
                    speechRecognizer.startListening(speechRecognizerIntent);
                    break;
                case "find":
                    feedback("Say the object you want to find or say back go back to the commands.");
                    objectSpeechRecognizer.startListening(objectSpeechRecognizerIntent);
                    break;
                case "settings":
                    feedback("Okay, opening the settings page now.");
                    return;
                // go to settings page
                case "stop":
                    feedback("Okay, tap the screen anytime if you want to start again");
                    return;
                // stop
                default:
                    feedback("I didn't get that. Can you say it again?");
                    speechRecognizer.startListening(speechRecognizerIntent);
                    break;
        }
    }

    // This method asks for/finds the object the user wants to find
    public void find(String str){
        if (str.equalsIgnoreCase("back")){
            functionality("options");
        }
        else if(objectsList.contains(str.toLowerCase())) {
            feedback("Okay, let's find the " + str);
            // Find
        }
        else {
            feedback("The object "+ str +"  is not in the system. Say the object you want to find or say back to go back to the commands.");
            objectSpeechRecognizer.startListening(objectSpeechRecognizerIntent);
        }
    }

    // This method displays and speaks the feedback
    private void feedback(String str){
        textView.setText(str);
        textToSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null, null);
        while(textToSpeech.isSpeaking());
    }


    @Override
    protected void onDestroy() {
        if (textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Listener for commands
    RecognitionListener commandListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            else {
                vibrator.vibrate(300);
            }

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int i) {
            
        }

        @Override
        public void onResults(Bundle bundle) {
            ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            speechRecognizer.stopListening();
            vibrator.vibrate(300);
            functionality(data.get(0));
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    };

    // Listener for objects
    RecognitionListener objectListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            else {
                vibrator.vibrate(300);
            }
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int i) {

        }

        @Override
        public void onResults(Bundle bundle) {
            ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            objectSpeechRecognizer.stopListening();
            vibrator.vibrate(300);
            find(data.get(0));
        }

        @Override
        public void onPartialResults(Bundle bundle) {
        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    };

}