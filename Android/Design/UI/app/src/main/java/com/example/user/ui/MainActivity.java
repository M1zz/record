package com.example.user.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TabHost tabHost1 = (TabHost) findViewById(R.id.tabHost1) ;
        tabHost1.setup() ;

        // 첫 번째 Tab. (탭 표시 텍스트:"TAB 1"), (페이지 뷰:"content1")
        TabHost.TabSpec ts1 = tabHost1.newTabSpec("Tab Spec 1") ;
        ts1.setContent(R.id.content1) ;
        ts1.setIndicator("STT") ;
        tabHost1.addTab(ts1)  ;

        // 두 번째 Tab. (탭 표시 텍스트:"TAB 2"), (페이지 뷰:"content2")
        TabHost.TabSpec ts2 = tabHost1.newTabSpec("Tab Spec 2") ;
        ts2.setContent(R.id.content2) ;
        ts2.setIndicator("DICTO") ;
        tabHost1.addTab(ts2) ;

        // 세 번째 Tab. (탭 표시 텍스트:"TAB 3"), (페이지 뷰:"content3")
        TabHost.TabSpec ts3 = tabHost1.newTabSpec("Tab Spec 3") ;
        ts3.setContent(R.id.content3) ;
        ts3.setIndicator("Setting") ;
        tabHost1.addTab(ts3) ;


        // 버튼 클릭 시 공지 변경
        BtnOnClickListener onClickListener = new BtnOnClickListener() ; // 각 Button의 이벤트 리스너로 onClickListener 지정.
        Button button = (Button) findViewById(R.id.pushbutton) ;
        button.setOnClickListener(onClickListener) ;

    }

    class BtnOnClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            TextView textView1 = (TextView) findViewById(R.id.footer);
            switch (view.getId()) {
                case R.id.pushbutton:
                    textView1.setText("Upload completed");
                    textView1.setBackgroundColor(Color.rgb(255, 144, 0));
                    break;
                /*
                case R.id.buttonGreen :
                    textView1.setText("Green") ;
                    textView1.setBackgroundColor(Color.rgb(0, 255, 0));
                    break ;
                case R.id.buttonBlue :
                    textView1.setText("Blue") ;
                    textView1.setBackgroundColor(Color.rgb(0, 0, 255));
                    break ;
                    */
            }
        }

    }

}

