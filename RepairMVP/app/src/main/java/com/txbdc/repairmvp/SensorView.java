package com.txbdc.repairmvp;

import android.app.Activity;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Cole on 11/21/2017.
 */

public class SensorView extends MainActivity{
    ImageButton sensorButton;
    ProgressBar posPB, negPB;
    SeekBar posSB, negSB;
    TextView posTV, negTV;
    RelativeLayout background;

    int white, yellow, green;

    SensorView(int sensorConnectButton, int posProgressBar,
               int negProgressBar, int posSeekBar, int negSeekBar,
               int posTextView, int negTextView, int relativeLayout,
               int whiteButton, int yellowButton, int greenButton, Activity MainActivity){
        sensorButton = (ImageButton) MainActivity.findViewById(sensorConnectButton);
        posPB = (ProgressBar) MainActivity.findViewById(posProgressBar);
        negPB = (ProgressBar) MainActivity.findViewById(negProgressBar);
        posSB = (SeekBar) MainActivity.findViewById(posSeekBar);
        negSB = (SeekBar) MainActivity.findViewById(negSeekBar);
        posTV = (TextView) MainActivity.findViewById(posTextView);
        negTV = (TextView) MainActivity.findViewById(negTextView);
        background = (RelativeLayout) MainActivity.findViewById(relativeLayout);
        green = greenButton;
        yellow = yellowButton;
        white = whiteButton;

    }
}
