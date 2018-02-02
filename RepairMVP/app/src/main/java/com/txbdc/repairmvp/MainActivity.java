package com.txbdc.repairmvp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.BinderThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;

//new
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.widget.Toast;
//new

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Random;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.view.View.VISIBLE;


public class MainActivity extends AppCompatActivity implements MVP_Main.RequiredViewOps{
    private MVP_Main.ProvidedPresenterOps mPresenter;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final int THIGH = 1;
    private final int SHIN = 2;
    private final int FOOT = 3;
    private final int connected = 2;
    public String TAG = "MainActivity";

    //new
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder ;
    Random random ;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    public static final int RequestPermissionCode = 2;
    MediaPlayer mediaPlayer ;
    //new

    SensorView thighView, shinView, footView;

    //Connect*Calibrate*Disconnect buttons
    @BindView(R.id.upperLegButton) ImageButton thighButton;
    @BindView(R.id.lowerLegButton) ImageButton shinButton;
    @BindView(R.id.footButton) ImageButton footButton;

    //Real Time Text Value
    @BindView(R.id.topAngleL) TextView negThighAngle;
    @BindView(R.id.topAngleR) TextView posThighAngle;
    @BindView(R.id.midAngleL) TextView negShinAngle;
    @BindView(R.id.midAngleR) TextView posShinAngle;
    @BindView(R.id.bottomAngleL) TextView negFootAngle;
    @BindView(R.id.bottomAngleR) TextView posFootAngle;

    //Thresholds
    @BindView(R.id.seekBarTopLeft) SeekBar negThighThreshold;
    @BindView(R.id.seekBarTopRight) SeekBar posThighThreshold;
    @BindView(R.id.seekBarMidLeft) SeekBar negShinThreshold;
    @BindView(R.id.seekBarMidRight) SeekBar posShinThreshold;
    @BindView(R.id.seekBarBottomLeft) SeekBar negFootThreshold;
    @BindView(R.id.seekBarBottomRight) SeekBar posFootThreshold;

    //Real Time Value Display
    @BindView(R.id.progressBarTopLeft) ProgressBar negThighProgress;
    @BindView(R.id.progressBarTopRight) ProgressBar posThighProgress;
    @BindView(R.id.progressBarMidLeft) ProgressBar negShinProgress;
    @BindView(R.id.progressBarMidRight) ProgressBar posShinProgress;
    @BindView(R.id.progressBarBottomLeft) ProgressBar negFootProgress;
    @BindView(R.id.progressBarBottomRight) ProgressBar posFootProgress;

    //Backgrounds
    @BindView(R.id.relativeHip) RelativeLayout thighBackground;
    @BindView(R.id.relativeKnee) RelativeLayout shinBackground;
    @BindView(R.id.relativeAnkle) RelativeLayout footBackground;

    //open details button
    @BindView(R.id.detailsButton) ImageButton detailsButton;

    //info text view
    @BindView(R.id.SensorStatus)TextView statusText;

    //record button
    @BindView(R.id.recordButton)FloatingActionButton recordButton;

    //pcm Button
    @BindView(R.id.pcmButton) FloatingActionButton pcmButton;

    EditText fileName;
    EditText notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPresenter = new MainPresenter(this);
        SensorView thighView = new SensorView(R.id.upperLegButton, R.id.progressBarTopRight, R.id.progressBarTopLeft, R.id.seekBarTopRight, R.id.seekBarTopLeft,
                R.id.topAngleR, R.id.topAngleL,R.id.relativeHip, R.drawable.hipwhite,R.drawable.hipyellow,R.drawable.hipgreen,this);
        SensorView shinView = new SensorView(R.id.lowerLegButton, R.id.progressBarMidRight, R.id.progressBarMidLeft, R.id.seekBarMidRight,R.id.seekBarMidLeft,
                R.id.midAngleR, R.id.midAngleL, R.id.relativeKnee,R.drawable.kneewhite,R.drawable.kneeyellow, R.drawable.kneegreen,this);
        SensorView footView = new SensorView(R.id.footButton,R.id.progressBarBottomRight,R.id.progressBarBottomLeft,R.id.seekBarBottomRight,R.id.seekBarBottomLeft,
                R.id.bottomAngleR,R.id.bottomAngleL, R.id.relativeAnkle,R.drawable.anklewhite,R.drawable.ankleyellow,R.drawable.anklegreen,this);

        // flip all progress bars on the left side
        thighView.negPB.setRotation(180);
        shinView.negPB.setRotation(180);
        footView.negPB.setRotation(180);

        detailsButton.setVisibility(View.VISIBLE);
        pcmButton.setVisibility(View.VISIBLE);

        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_COARSE_LOCATION);
    }

    @Override
    protected void onStop(){
        super.onStop();
        //mPresenter.mainActivityStopped();
        //for(int i = 1; i < 4; i++){
        //    int status = mPresenter.checkSensorStatus(i);
        //    if(status != connected) changeButtonToWhite(i);
        //}
    }

    public void changeButtonToWhite(final int sensor){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText("Select a sensor to connect");
                if(sensor == THIGH){
                    thighButton.setBackgroundResource(R.drawable.hipwhite);
                }
                else if(sensor == SHIN){
                    shinButton.setBackgroundResource(R.drawable.kneewhite);
                }
                else if(sensor == FOOT){
                    footButton.setBackgroundResource(R.drawable.anklewhite);
                }
            }
        });

    }
    //maybe belongs in presenter but is only run once atm
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_COARSE_LOCATION:
            {
                if ( grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(TAG, "coarse location permission granted");
                }
                else
                {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener()
                    {

                        @Override
                        public void onDismiss(DialogInterface dialog)
                        {
                            //add code to handle dismiss
                        }

                    });
                    builder.show();

                }
                return;
            }
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(MainActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    public Context getAppContext() {
        return getApplicationContext();
    }

    @Override
    public Context getActivityContext() {
        return this;
    }

    @Override
    public void notifyViewOfUpdatesFromRemoteService(Bundle bundle) {

    }

    @Override
    public void sensorConnected(final SensorView sensor){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sensor.sensorButton.setBackgroundResource(R.drawable.hipgreen);
                sensor.posTV.setVisibility(VISIBLE);
                sensor.negTV.setVisibility(VISIBLE);
                statusText.setText("Sensor Connected");
            }
        });
    }

    @Override
    public void thighConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                thighButton.setBackgroundResource(R.drawable.hipgreen);
                posThighAngle.setVisibility(VISIBLE);
                negThighAngle.setVisibility(VISIBLE);
                statusText.setText("Thigh Sensor Connected");
            }
        });
    }

    @Override
    public void shinConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shinButton.setBackgroundResource(R.drawable.kneegreen);
                posShinAngle.setVisibility(VISIBLE);
                negShinAngle.setVisibility(VISIBLE);
                statusText.setText("Shin Sensor Connected");
            }
        });
    }

    @Override
    public void footConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                footButton.setBackgroundResource(R.drawable.anklegreen);
                posFootAngle.setVisibility(VISIBLE);
                negFootAngle.setVisibility(VISIBLE);
                statusText.setText("Foot Sensor Connected");
            }
        });
    }

    @Override
    public void disconnected(SensorView sensor) {

    }


    @Override
    public void thighDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //set background grey in case the sensor was disconnected while above threshold
                thighButton.setBackgroundResource(R.drawable.hipwhite);
                negThighAngle.setVisibility(View.INVISIBLE);
                negThighProgress.setProgress(0);
                posThighAngle.setVisibility(View.INVISIBLE);
                posThighProgress.setProgress(0);
                statusText.setText("Thigh Sensor Disconnected");
            }
        });
    }


    @Override
    public void shinDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shinButton.setBackgroundResource(R.drawable.kneewhite);
                negShinAngle.setVisibility(View.INVISIBLE);
                negShinProgress.setProgress(0);
                posShinAngle.setVisibility(View.INVISIBLE);
                posShinProgress.setProgress(0);
                statusText.setText("Shin Sensor Disconnected");
            }
        });
    }

    @Override
    public void footDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                footButton.setBackgroundResource(R.drawable.anklewhite);
                negFootAngle.setVisibility(View.INVISIBLE);
                negFootProgress.setProgress(0);
                posFootAngle.setVisibility(View.INVISIBLE);
                posFootProgress.setProgress(0);
                statusText.setText("Foot Sensor Disconnected");
            }
        });
    }

    @Override
    public void setLongClickActionThigh() {
        thighButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.v(TAG, "long click detected 1");
                mPresenter.zeroSensor(1);
                return true;
            }
        });
    }

    @Override
    public void setLongClickActionShin() {
        shinButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.v(TAG, "long click detected 2");
                mPresenter.zeroSensor(2);
                return true;
            }
        });
    }

    @Override
    public void setLongClickActionFoot() {
        footButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.v(TAG, "long click detected 3");

                mPresenter.zeroSensor(3);
                return true;
            }
        });
    }

    @Override
    public void setLongClickFirefly() {
        pcmButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mPresenter.disconnectFF();
                fireflyDisconnected();
                return true;
            }
        });
    }

    @Override
    public void fireflyConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pcmButton.setImageResource(R.drawable.ic_flash_on_24dp);
                statusText.setText("PCM Connected");
            }
        });
    }

    @Override
    public void fireflyDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pcmButton.setImageResource(R.drawable.ic_flash_off_black_24dp);
                statusText.setText("PCM Disconnected");
            }
        });
    }


    @Override
    public void displayPositiveThighValue(final float value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                posThighAngle.setText(Integer.toString((int) value) + "/" + Integer.toString(posThighThreshold.getProgress()));
                negThighAngle.setText("0/" + Integer.toString(negThighThreshold.getProgress()));
                posThighProgress.setProgress((int)value);
                negThighProgress.setProgress(0);
                if((int)value > posThighThreshold.getProgress()){
                    thighBackground.setBackgroundColor(Color.parseColor("#008542"));
                    mPresenter.triggerFF();
                }
                else{
                    thighBackground.setBackgroundColor(Color.parseColor("#404040"));
                }
            }
        });
    }

    @Override
    public void displayNegativeThighValue(final float value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                negThighAngle.setText(Integer.toString((int)(-1*value)) + "/" + Integer.toString(negThighThreshold.getProgress()));
                posThighAngle.setText("0/" + Integer.toString(posThighThreshold.getProgress()));
                negThighProgress.setProgress((int)(-1*value));
                posThighProgress.setProgress(0);
                if((int)(-1*value) > negThighThreshold.getProgress()){
                    thighBackground.setBackgroundColor(Color.parseColor("#008542"));
                    mPresenter.triggerFF();

                }
                else{
                    thighBackground.setBackgroundColor(Color.parseColor("#404040"));
                }
            }
        });
    }

    @Override
    public void displayPositiveShinValue(final float value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                posShinAngle.setText(Integer.toString((int) value) + "/" + Integer.toString(posShinThreshold.getProgress()));
                negShinAngle.setText("0/" + Integer.toString(negShinThreshold.getProgress()));
                posShinProgress.setProgress((int)value);
                negShinProgress.setProgress(0);
                if((int)value > posShinThreshold.getProgress()){
                    shinBackground.setBackgroundColor(Color.parseColor("#008542"));
                    mPresenter.triggerFF();

                }
                else{
                    shinBackground.setBackgroundColor(Color.parseColor("#333333"));
                }
            }
        });
    }

    @Override
    public void displayNegativeShinValue(final float value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                negShinAngle.setText(Integer.toString((int)(-1*value)) + "/" + Integer.toString(negShinThreshold.getProgress()));
                posShinAngle.setText("0/" + Integer.toString(posShinThreshold.getProgress()));
                negShinProgress.setProgress((int)(-1*value));
                posShinProgress.setProgress(0);
                if((int)(-1*value) > negShinThreshold.getProgress()){
                    shinBackground.setBackgroundColor(Color.parseColor("#008542"));
                    mPresenter.triggerFF();

                }
                else{
                    shinBackground.setBackgroundColor(Color.parseColor("#333333"));
                }
            }
        });
    }

    @Override
    public void displayPositiveFootValue(final float value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                posFootAngle.setText(Integer.toString((int) value) + "/" + Integer.toString(posFootThreshold.getProgress()));
                negFootAngle.setText("0/" + Integer.toString(negFootThreshold.getProgress()));
                posFootProgress.setProgress((int)value);
                negFootProgress.setProgress(0);
                if((int)value > posFootThreshold.getProgress()){
                    footBackground.setBackgroundColor(Color.parseColor("#008542"));
                    mPresenter.triggerFF();

                }
                else{
                    footBackground.setBackgroundColor(Color.parseColor("#404040"));
                }
            }
        });
    }

    @Override
    public void displayNegativeFootValue(final float value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                negFootAngle.setText(Integer.toString((int)(-1*value)) + "/" + Integer.toString(negFootThreshold.getProgress()));
                posFootAngle.setText("0/" + Integer.toString(posFootThreshold.getProgress()));
                negFootProgress.setProgress((int)(-1*value));
                posFootProgress.setProgress(0);
                if((int)(-1*value) > negFootThreshold.getProgress()){
                    footBackground.setBackgroundColor(Color.parseColor("#008542"));
                    mPresenter.triggerFF();

                }
                else{
                    footBackground.setBackgroundColor(Color.parseColor("#404040"));
                }
            }
        });
    }

    @Override
    public void searchingForDevice(final SensorView view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.sensorButton.setBackgroundResource(view.yellow);
            }
        });
    }

    @Override
    public void startRecording() {
        if(checkPermission()) {
            String fileName = "trialData_";
            String path = "/storage/emulated/0/";
            String dateTime = DateFormat.getDateTimeInstance().format(new Date());
            String fullPath = path+fileName+dateTime;
            AudioSavePathInDevice =
                    fullPath
                             + "AudioRecording.3gp";

            MediaRecorderReady();

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //buttonStart.setEnabled(false);
            //buttonStop.setEnabled(true);

            Toast.makeText(MainActivity.this, "Recording started",
                    Toast.LENGTH_LONG).show();
        } else {
            requestPermission();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recordButton.setImageResource(R.drawable.stop);

            }
        });
        /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you want to record voice?")
                .setCancelable(true)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, ask for access
                        // to the microphone and begin recording voice
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just record data
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Application will begin recording data only.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener()
                        {

                            @Override
                            public void onDismiss(DialogInterface dialog)
                            {
                                //add code to handle dismiss
                            }

                        });
                        builder.show();

                        dialog.cancel();
                    }
                });


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();*/
    }

    public void MediaRecorderReady(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }



    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void stopRecording() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        LayoutInflater saveRecordingInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View saveRecordingLayout = saveRecordingInflater.inflate(R.layout.save_recording, null);
        alertDialogBuilder.setView(saveRecordingLayout);
        fileName = (EditText) saveRecordingLayout.findViewById(R.id.filenameET);
        notes = (EditText) saveRecordingLayout.findViewById(R.id.notesET);
        String fileNameString = "noName";
        String notesString = "\n";
        if(fileName != null){fileNameString = fileName.getText().toString();}
        if(notes != null){notesString = notes.getText().toString();}
        final String finalFileNameString1 = fileNameString;
        final String finalNotesString = notesString;
        alertDialogBuilder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        recordButton.setImageResource(R.drawable.record);
                    }
                });
                mPresenter.writeFile(fileName.getText().toString(), notes.getText().toString());
                //mPresenter.writeFile(fileName.getText().toString(), " ");
                /*mPresenter.writeFileAtStop(" ", THIGH);
                mPresenter.writeFileAtStop(" ", SHIN);
                mPresenter.writeFileAtStop(" ", FOOT);
                mPresenter.writeFileAtStop(" ",THIGH);
                mPresenter.writeFileAtStop(" ", SHIN);
                mPresenter.writeFileAtStop(" ", FOOT);*/
                mPresenter.writeFileAtStop(" ", 5);
                mPresenter.clearDataBuffers();
            }
        });

        if(mPresenter.getAudioRecordingStatus()) mediaRecorder.stop();

        Toast.makeText(MainActivity.this, "Recording Completed",
                Toast.LENGTH_LONG).show();

        //alertDialogBuilder.setNegativeButton("Cancel", null);

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();

    }

    @Override
    public void setTextDefault() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText("Select a device to connect");
            }
        });
    }

    @Override
    public void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }


    @Override
    public void searchingForThigh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                thighButton.setBackgroundResource(R.drawable.hipyellow);
                statusText.setText("Searching for Thigh Sensor");
            }
        });
    }

    @Override
    public void searchingForShin() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shinButton.setBackgroundResource(R.drawable.kneeyellow);
                statusText.setText("Searching for Shin Sensor");
            }
        });
    }

    @Override
    public void searchingForFoot() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                footButton.setBackgroundResource(R.drawable.ankleyellow);
                statusText.setText("Searching for Foot Sensor");
            }
        });
    }

    @Override
    public void searchingForFF() {
        statusText.setText("Searching for PCM");
    }

    @Override
    public void notSearchingForThigh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                thighButton.setBackgroundResource(R.drawable.hipwhite);
            }
        });
    }

    @Override
    public void notSearchingForShin() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shinButton.setBackgroundResource(R.drawable.kneewhite);
            }
        });
    }

    @Override
    public void notSearchingForFoot() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                footButton.setBackgroundResource(R.drawable.anklewhite);
            }
        });
    }

    @Override
    public void notSearchingForThighText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                thighButton.setBackgroundResource(R.drawable.hipwhite);
                statusText.setText("Search Ended");
            }
        });
    }

    @Override
    public void notSearchingForShinText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shinButton.setBackgroundResource(R.drawable.kneewhite);
                statusText.setText("Search Ended");
            }
        });
    }

    @Override
    public void notSearchingForFootText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                footButton.setBackgroundResource(R.drawable.anklewhite);
                statusText.setText("Search Ended");
            }
        });
    }

    public void thighClicked(View v){mPresenter.clickSensorButton(THIGH);
    }
    public void shinClicked(View v){mPresenter.clickSensorButton(SHIN);}
    public void footClicked(View v){mPresenter.clickSensorButton(FOOT);}
    public void recordClicked(View v){mPresenter.clickRecord();}
    public void detailsClicked(View v){
        Intent intent = new Intent(this, DetailsActivity.class);
        startActivity(intent);
    }
    public void pcmClicked(View v){mPresenter.clickSensorButton(4);}
}
