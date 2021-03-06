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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private ImageButton imgbtnPlay, imgbtnPause, imgbtnStop;
    private EditText edtSec, edtMin, edtRest, edtRound;
    private View mainView;
    private CountDownTimer cdt;
    private long auxMilli = 0;
    private int auxMin = 0, auxSec = 0, auxRest = 0, roundsLeft = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeElements();
        changeEditTextToDefaultColor(0, 0);
        edtSec.setOnFocusChangeListener(this);
        edtMin.setOnFocusChangeListener(this);
        edtRest.setOnFocusChangeListener(this);
        edtRound.setOnFocusChangeListener(this);

        mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.clearFocus();
                hideSoftKeyboard();
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

        edtRest.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return edtRest.getText().length() == 0 && (i >= 13 && i <= 16);
            }
        });
        //endregion

        imgbtnPlay.setOnClickListener(new View.OnClickListener() {
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
                            int min = (int) (l / 1_000) / 60;
                            int sec = (int) (l / 1_000) % 60;
                            auxMilli = l; //store the time in milliseconds to reuse later

                            edtMin.setText(String.format("%02d", min));
                            edtSec.setText(String.format("%02d", sec));

                            updateColor(timeInMilli, l);
                        }

                        @Override
                        public void onFinish() {
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                            changeEditTextToDefaultColor(auxMin, auxSec);
                            decreaseRoundsLeft();
                            restCount();

                            auxMilli = 0; //setting 0 value to refresh auxSec and auxMin

                            imgbtnPlay.setVisibility(View.VISIBLE);
                            imgbtnPause.setVisibility(View.INVISIBLE);
                            imgbtnStop.setVisibility(View.INVISIBLE);

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

                    imgbtnPlay.setVisibility(View.INVISIBLE);
                    imgbtnPause.setVisibility(View.VISIBLE);
                    imgbtnStop.setVisibility(View.VISIBLE);

                    edtMin.setEnabled(false); //disabling edittext
                    edtSec.setEnabled(false);

                } catch (Exception e) {
                    Snackbar.make(mainView, R.string.sorry_iamnotokay, Snackbar.LENGTH_LONG)
                            .setTextColor(getResources().getColor(R.color.white))
                            .setBackgroundTint(getResources().getColor(R.color.dark_red))
                            .show();
                }
            }
        });

        imgbtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cdt.cancel(); //pause count down
                imgbtnPlay.setVisibility(View.VISIBLE);
                imgbtnPause.setVisibility(View.INVISIBLE);
                imgbtnStop.setVisibility(View.VISIBLE);
            }
        });

        imgbtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cdt.cancel(); //pause count down
                cdt.onFinish(); //finish count down
            }
        });
    }

    private void initializeElements() {
        mainView = findViewById(R.id.main_layout);
        imgbtnPlay = findViewById(R.id.imgbtn_play);
        imgbtnPause = findViewById(R.id.imgbtn_pause);
        imgbtnStop = findViewById(R.id.imgbtn_stop);
        edtMin = findViewById(R.id.edt_min);
        edtSec = findViewById(R.id.edt_sec);
        edtRest = findViewById(R.id.edt_rest);
        edtRound = findViewById(R.id.edt_round);
    }

    private void changeEditTextToDefaultColor(int min, int sec) {
        edtMin.setText(String.format("%02d", min));
        edtMin.setTextColor(getResources().getColor(R.color.normal_orange));
        edtSec.setText(String.format("%02d", sec));
        edtSec.setTextColor(getResources().getColor(R.color.normal_purple));
    }

    private void updateColor(long time, long parcialTime) {
        float fraction = (float)(time - parcialTime) / (float)time;
        int color = (int) new ArgbEvaluator().evaluate(fraction,
                ContextCompat.getColor(this, R.color.normal_purple),
                ContextCompat.getColor(this, R.color.normal_red));
        edtSec.setTextColor(color);
    }

    private void decreaseRoundsLeft() {
        roundsLeft = Integer.parseInt(edtRound.getText().toString());
        if (roundsLeft > 0) {
            edtRound.setText(String.format("%02d", --roundsLeft));
        }
    }

    private void restCount() {
        int rest = Integer.parseInt(edtRest.getText().toString());
        if (rest > 0) {
            auxRest = rest > auxRest ? rest : auxRest;
            CountDownTimer restCountDown = new CountDownTimer((auxRest * 1_000) + 1_000, 1_000) {
                @Override
                public void onTick(long l) {
                    int restLeft = (int) (l / 1_000) % 60;
                    edtRest.setText(String.format("%02d", restLeft));
                }

                @Override
                public void onFinish() {
                    edtRest.setText(String.format("%02d", auxRest));

                    if (roundsLeft > 0)
                        imgbtnPlay.performClick();
                }
            }.start();
        }
    }

    @Override
    public void onFocusChange(View view, boolean isFocused) {
        EditText auxEditText = (EditText) view;
        try {
            if (!isFocused) {
                int number = Integer.parseInt(auxEditText.getText().toString());
                auxEditText.setText(String.format("%02d", number));
            }
        } catch (Exception e) {
            auxEditText.setText(String.format("%02d", 0));
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mainView.getWindowToken(), 0);
    }
}