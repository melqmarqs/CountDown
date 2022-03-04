package com.melqmarqs.countdown;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.ArgbEvaluator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private ImageView imgPlay, imgPause, imgStop;
    private EditText edtSec, edtMin;
    private View mainView;
    private CountDownTimer cdt;
    private long auxMilli = 0;
    private int auxMin = 0, auxSec = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeElements();
        changeEditTextToDefaultColor(0, 0);

        mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainView.clearFocus();
                try {
                    int auxMin = Integer.parseInt(edtMin.getText().toString());
                    if (auxMin == 0)
                        edtMin.setText("00");
                } catch (Exception e) {
                    edtMin.setText("00");
                }

                try {
                    int auxSec = Integer.parseInt(edtSec.getText().toString());
                    if (auxSec == 0)
                        edtSec.setText("00");
                } catch (Exception e) {
                    edtSec.setText("00");
                }

                //Hidding/Closing soft keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mainView.getWindowToken(), 0);
            }
        });

        //region VALIDATE INPUT CHARACTERS
        edtSec.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return edtSec.getText().length() == 0 && (i >= 13 && i <= 16);
            }
        });

        edtMin.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return edtMin.getText().length() == 0 && (i >= 13 && i <= 16);
            }
        });
        //endregion

        imgPlay.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                try {
                    if (auxMilli < 1_000) {
                        //refreshing starting values
                        auxSec = Integer.parseInt(edtSec.getText().toString());
                        auxMin = Integer.parseInt(edtMin.getText().toString());
                    }

                    long timeInMilli = (auxSec * 1_000L) + (auxMin * 60_000L) + 1_000;

                    cdt = new CountDownTimer(auxMilli < 1_000 ? timeInMilli : auxMilli, 1_000) {
                        @Override
                        public void onTick(long l) {
                            auxMilli = l; //store the time in milliseconds to reuse later
                            int min = Integer.parseInt(String.valueOf(l / 60_000));
                            edtMin.setText(min < 10 ? "0" + min : String.valueOf(min));

                            int sec = (int) ((l - (min * 60_000)) / 1_000);
                            edtSec.setText(sec < 10 ? "0" + sec : String.valueOf(sec));

                            //if (min == 0 && sec <= 10)
                                //edtSec.setTextColor(Color.rgb(232, 39, 42));

                            updateColor(timeInMilli, l);
                        }

                        @Override
                        public void onFinish() {
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                            changeEditTextToDefaultColor(auxMin, auxSec);

                            auxMilli = 0; //setting 0 value to refresh auxSec and auxMin

                            imgPlay.setVisibility(View.VISIBLE);
                            imgPause.setVisibility(View.INVISIBLE);
                            imgStop.setVisibility(View.INVISIBLE);

                            edtMin.setEnabled(true);
                            edtSec.setEnabled(true);

                            //getting type of sound
                            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), alert);
                            ringtone.play(); //playing ringtone
                        }
                    };

                    cdt.start();

                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    imgPlay.setVisibility(View.INVISIBLE);
                    imgPause.setVisibility(View.VISIBLE);
                    imgStop.setVisibility(View.VISIBLE);

                    edtMin.setEnabled(false); //disabling edittext
                    edtSec.setEnabled(false);

                } catch (Exception e) {
                    Snackbar.make(mainView, R.string.check_numbers, Snackbar.LENGTH_LONG)
                            .setTextColor(Color.rgb(255, 255, 255))
                            .setBackgroundTint(Color.rgb(181, 24, 26))
                            .show();
                }
            }
        });

        imgPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cdt.cancel(); //pause count down
                imgPlay.setVisibility(View.VISIBLE);
                imgPause.setVisibility(View.INVISIBLE);
                imgStop.setVisibility(View.VISIBLE);
            }
        });

        imgStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cdt.cancel(); //pause count down
                cdt.onFinish(); //finish count down
            }
        });
    }

    private void initializeElements() {
        mainView = findViewById(R.id.main_layout);
        imgPlay = findViewById(R.id.img_play);
        imgPause = findViewById(R.id.img_pause);
        imgStop = findViewById(R.id.img_stop);
        edtMin = findViewById(R.id.edt_min);
        edtSec = findViewById(R.id.edt_sec);
    }

    private void changeEditTextToDefaultColor(int min, int sec) {
        edtMin.setText(min == 0 ? "00" : min < 10 ? "0" + min : String.valueOf(min));
        edtMin.setTextColor(Color.rgb(235, 151, 16));
        edtSec.setText(sec == 0 ? "00" : sec < 10 ? "0" + sec : String.valueOf(sec));
        edtSec.setTextColor(Color.rgb(107, 16, 235));
    }

    private void updateColor(long time, long parcialTime) {
        float fraction = (float)(time - parcialTime) / (float)time;
        int color = (int) new ArgbEvaluator().evaluate(fraction, ContextCompat.getColor(this, R.color.normal_purple), ContextCompat.getColor(this, R.color.normal_red));
        edtSec.setTextColor(color);
    }
}