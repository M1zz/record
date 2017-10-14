package com.example.user.mymusicplayer;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;

import com.example.user.mymusicplayer.R;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    MediaPlayer mp; // 음악 재생을 위한 객체
    int pos; // 재생 멈춘 시점
    int a; //구간의 시작 A
    int b; //구간의 종료 B
    int time = 0;
    private Button bStart;
    private Button bPause;
    private Button bRestart;
    private Button bStop;
    private Button bPickA;
    private Button bPickB;
    TimerTask task= null;
    static int count = 0;
    SeekBar sb; // 음악 재생위치를 나타내는 시크바
    boolean isPlaying = false; // 재생중인지 확인할 변수

    class MyThread extends Thread {
        @Override
        public void run() { // 쓰레드가 시작되면 콜백되는 메서드
            // 씨크바 막대기 조금씩 움직이기 (노래 끝날 때까지 반복)
            while(isPlaying) {
                sb.setProgress(mp.getCurrentPosition());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sb = (SeekBar)findViewById(R.id.seekBar);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                isPlaying = true;
                int ttt = seekBar.getProgress(); // 사용자가 움직여놓은 위치
                mp.seekTo(ttt);
                mp.start();
                new MyThread().start();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPlaying = false;
                mp.pause();
            }
            public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser) {
                if (seekBar.getMax()==progress) {
                    bStart.setVisibility(View.VISIBLE);
                    bStop.setVisibility(View.INVISIBLE);
                    bPause.setVisibility(View.INVISIBLE);
                    bRestart.setVisibility(View.INVISIBLE);
                    isPlaying = false;
                    mp.stop();
                }
            }
        });

        bStart = (Button)findViewById(R.id.play);
        bPause = (Button)findViewById(R.id.pause);
        bRestart=(Button)findViewById(R.id.restart);
        bStop  = (Button)findViewById(R.id.stop);
        bPickA = (Button)findViewById(R.id.pickA);
        bPickB = (Button)findViewById(R.id.pickB);

        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MediaPlayer 객체 초기화 , 재생
                mp = MediaPlayer.create(
                        getApplicationContext(), // 현재 화면의 제어권자
                        R.raw.b); // 음악파일
                mp.setLooping(false); // true:무한반복
                mp.start(); // 노래 재생 시작

                int a = mp.getDuration(); // 노래의 재생시간(miliSecond)
                sb.setMax(a);// 씨크바의 최대 범위를 노래의 재생시간으로 설정
                new MyThread().start(); // 씨크바 그려줄 쓰레드 시작
                isPlaying = true; // 씨크바 쓰레드 반복 하도록

                bStart.setVisibility(View.GONE);
                bStop.setVisibility(View.VISIBLE);
                bPause.setVisibility(View.VISIBLE);
            }
        });

        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 음악 종료
                isPlaying = false; // 쓰레드 종료

                mp.stop(); // 멈춤
                mp.release(); // 자원 해제
                bStart.setVisibility(View.VISIBLE);
                bPause.setVisibility(View.GONE);
               // bRestart.setVisibility(View.INVISIBLE);
                bStop.setVisibility(View.INVISIBLE);
                if(task != null) task.cancel();
                sb.setProgress(0); // 씨크바 초기화
            }
        });
        bPickA.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                a = mp.getCurrentPosition();

            }
        });
        bPickB.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                b = mp.getCurrentPosition();
                mp.pause();
                isPlaying = false;
                time = b-a;
                Log.e("time : " , String.valueOf(time));
                mp.seekTo(a);
                mp.start();
                isPlaying = true;
                task = new TimerTask(){
                    public void run() {
                        try {
                            sb.setProgress(a);
                            new MyThread().start();
                            mp.seekTo(a);
                            mp.start();
                            Log.e("TIMER 반복 : ", String.valueOf(count));
                            count++;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                Timer mTimer = new Timer();
                mTimer.schedule(task,0, time);
                time = 0;
            }
        });
        bPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 일시중지
                pos = mp.getCurrentPosition();
                mp.pause(); // 일시중지

                bPause.setVisibility(View.INVISIBLE);
                bRestart.setVisibility(View.VISIBLE);
                isPlaying = false; // 쓰레드 정지
            }
        });
        bRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 멈춘 지점부터 재시작
                mp.seekTo(pos); // 일시정지 시점으로 이동
                mp.start(); // 시작
                bRestart.setVisibility(View.INVISIBLE);
                bPause.setVisibility(View.VISIBLE);
                isPlaying = true; // 재생하도록 flag 변경
                new MyThread().start(); // 쓰레드 시작
            }
        });
    } // end of onCreate

    @Override
    protected void onPause() {
        super.onPause();
        isPlaying = false; // 쓰레드 정지
        if (mp!=null) {
            mp.release(); // 자원해제
        }
        bStart.setVisibility(View.VISIBLE);
        bStop.setVisibility(View.INVISIBLE);
        bPause.setVisibility(View.INVISIBLE);
        bRestart.setVisibility(View.INVISIBLE);
    }
} // end of class