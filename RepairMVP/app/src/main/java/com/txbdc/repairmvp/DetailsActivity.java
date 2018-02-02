package com.txbdc.repairmvp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Cole on 12/13/2017.
 */

public class DetailsActivity extends AppCompatActivity implements DetailsInterface.ViewToPresenter{
    private DetailsInterface.PresenterToView mPresenter;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @BindView(R.id.scanButton) Button scanButton;
    ArrayList<TextView> approvedDevices;
    ArrayList<TextView> approvedPCMs;
    @BindView(R.id.device1) TextView approvedDevice1;
    @BindView(R.id.device2) TextView approvedDevice2;
    @BindView(R.id.device3) TextView approvedDevice3;
    @BindView(R.id.device4) TextView approvedDevice4;
    @BindView(R.id.device5) TextView approvedDevice5;
    @BindView(R.id.pcm1) TextView approvedPCM1;
    @BindView(R.id.pcm2) TextView approvedPCM2;
    @BindView(R.id.pcm3) TextView approvedPCM3;

    @Override
    public Context getAppContext() {
        return getApplicationContext();
    }

    @Override
    public Context getActivityContext() {
        return this;
    }

    @Override
    public void setTextApprovedDevices() {
        approvedDevices.set(0, approvedDevice1);
        approvedDevices.set(1, approvedDevice2);
        approvedDevices.set(2, approvedDevice3);
        approvedDevices.set(3, approvedDevice4);
        approvedDevices.set(4, approvedDevice5);
        approvedPCMs.set(0, approvedPCM1);
        approvedPCMs.set(1, approvedPCM2);
        approvedPCMs.set(2, approvedPCM3);
        int i = 0;
        for (TextView approvedDevice : approvedDevices){
            approvedDevice.setText(mPresenter.getApprovedDevice(i));
            i++;
        }
        for(TextView approvedPCM : approvedPCMs){
            approvedPCM.setText(mPresenter.getApprovedDevice(i));
            i++;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        mPresenter = new DetailsPresenter(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        LocalBroadcastManager.getInstance(this).registerReceiver(recyclerViewReceiver, new IntentFilter("newDevice"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.activityStopped();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mPresenter.activityStopped();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.v("recycler click", String.valueOf(item));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void scanClicked(View v) {
        mPresenter.startDetailsScan();
    }

    @Override
    public void stoppedButtonText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanButton.setText("search for new devices");
            }
        });
    }

    @Override
    public void startedButtonText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanButton.setText("searching...");
            }
        });
    }

    @Override
    public void updateRecyclerView(List<String> input) {
        mAdapter = new RecyclerAdapter(input, this);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public String getValueOfApprovedDevice(int element) {
        if (element == 0) return approvedDevice1.getText().toString();
        else if (element == 1) return approvedDevice2.getText().toString();
        else if (element == 2) return approvedDevice3.getText().toString();
        else if (element == 3) return approvedDevice4.getText().toString();
        else return approvedDevice5.getText().toString();

    }

    @Override
    public void setApprovedDeviceText(final int element, final String newDevice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if( element == 0 ) approvedDevice1.setText(newDevice);
                else if (element == 1) approvedDevice2.setText(newDevice);
                else if (element == 2) approvedDevice3.setText(newDevice);
                else if (element == 3) approvedDevice4.setText(newDevice);
                else if (element == 4) approvedDevice5.setText(newDevice);
            }
        });
    }

    @Override
    public void setApprovedPCMText(final int element, final String newDevice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(element == 0)approvedPCM1.setText(newDevice);
                else if (element == 1) approvedPCM2.setText(newDevice);
                else if (element == 2) approvedPCM3.setText(newDevice);
            }
        });
    }

    @Override
    public String getValueOfApprovedPCM(int element) {
        if (element == 0) return approvedPCM1.getText().toString();
        else if (element == 1) return approvedPCM2.getText().toString();
        else return approvedPCM3.getText().toString();

    }

    @Override
    public void addPCMsToApproved() {

    }


    @Override
    public void addDevicesToApproved() {
        approvedDevice1.setText(mPresenter.getApprovedDevice(0));
        approvedDevice2.setText(mPresenter.getApprovedDevice(1));
        approvedDevice3.setText(mPresenter.getApprovedDevice(2));
        approvedDevice4.setText(mPresenter.getApprovedDevice(3));
        approvedDevice5.setText(mPresenter.getApprovedDevice(4));
        approvedPCM1.setText(mPresenter.getApprovedDevice(5));
        approvedPCM2.setText(mPresenter.getApprovedDevice(6));
        approvedPCM3.setText(mPresenter.getApprovedDevice(7));
    }


    public void pcmClick(View v) {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater PCMInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View PCMLayout = PCMInflater.inflate(R.layout.pcmlayout, null);
        builder.setView(PCMLayout);
        //
        DiscreteSeekBar ds = (DiscreteSeekBar) PCMLayout.findViewById(R.id.PCMDurationSeekbar);
        ds.setMin(0);
        ds.setMax(30);
        ds.setProgress(12);
        final TextView CurrentPCMValue = (TextView) PCMLayout.findViewById(R.id.PCMDurationDisplay);
        CurrentPCMValue.setTypeface(CurrentPCMValue.getTypeface(), Typeface.BOLD);
        CurrentPCMValue.setText(ds.getProgress() + " Seconds");
        ds.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 1;
            }
        });
        ds.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                CurrentPCMValue.setTypeface(CurrentPCMValue.getTypeface(), Typeface.BOLD);
                CurrentPCMValue.setText(value + " Seconds");
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
        //
        DiscreteSeekBar pulseWidthSeekbar = (DiscreteSeekBar) PCMLayout.findViewById(R.id.PCMPulseWidthSeekbar);
        pulseWidthSeekbar.setMin(1);
        pulseWidthSeekbar.setMax(10);
        pulseWidthSeekbar.setProgress(4);
        final TextView currentPCMPulseWidth = (TextView) PCMLayout.findViewById(R.id.PCMPulseWidthDisplay);
        currentPCMPulseWidth.setTypeface(currentPCMPulseWidth.getTypeface(), Typeface.BOLD);
        currentPCMPulseWidth.setText((pulseWidthSeekbar.getProgress() * 25) + " μs");
        pulseWidthSeekbar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 25;
            }
        });
        pulseWidthSeekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                currentPCMPulseWidth.setTypeface(currentPCMPulseWidth.getTypeface(), Typeface.BOLD);
                currentPCMPulseWidth.setText((value * 25) + " μs");
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
        //
        DiscreteSeekBar amplitudeSeekbar = (DiscreteSeekBar) PCMLayout.findViewById(R.id.PCMAmplitudeSeekbar);
        amplitudeSeekbar.setMin(0);
        amplitudeSeekbar.setMax(16);
        amplitudeSeekbar.setProgress(5);
        final TextView amplitudeDisplay = (TextView) PCMLayout.findViewById(R.id.PCMAmplitudeDisplay);
        amplitudeDisplay.setTypeface(amplitudeDisplay.getTypeface(), Typeface.BOLD);
        amplitudeDisplay.setText((amplitudeSeekbar.getProgress()/10.0) + " mA");
        amplitudeSeekbar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 10;
            }
        });

        amplitudeSeekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                String format = String.valueOf((double) seekBar.getProgress()/10);
                seekBar.setIndicatorFormatter(format);
                amplitudeDisplay.setText(format + " mA");
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                String format = String.valueOf((double) seekBar.getProgress()/10);
                seekBar.setIndicatorFormatter(format);
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                String format = String.valueOf((double) seekBar.getProgress()/10);
                seekBar.setIndicatorFormatter(format);
            }
        });
        //
        DiscreteSeekBar PCMFrequencySeekbar = (DiscreteSeekBar) PCMLayout.findViewById(R.id.PCMFrequencySeekbar);
        PCMFrequencySeekbar.setMin(0);
        PCMFrequencySeekbar.setMax(20);
        PCMFrequencySeekbar.setProgress(11);
        final TextView PCMFreqDisplay = (TextView) PCMLayout.findViewById(R.id.PCMFrequencyDisplay);
        PCMFreqDisplay.setTypeface(PCMFreqDisplay.getTypeface(), Typeface.BOLD);
        PCMFreqDisplay.setText((PCMFrequencySeekbar.getProgress() * 5) + " Hz");
        PCMFrequencySeekbar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value * 5;
            }
        });
        PCMFrequencySeekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                PCMFreqDisplay.setText((value * 5) + " Hz");
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });


        builder.setTitle("PCM Settings: ");

        // add OK and Cancel buttons
        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        android.app.AlertDialog dialog = builder.create();
        dialog.show();

    }
    private BroadcastReceiver recyclerViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String newApprovedDevice = extras.getString("deviceAddress");
            Log.v("new device", "Address: " + newApprovedDevice);
            mPresenter.setNewApprovedDevice(newApprovedDevice);
        }
    };

}
