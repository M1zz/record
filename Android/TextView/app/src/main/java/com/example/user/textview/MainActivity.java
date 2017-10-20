package com.example.user.textview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    Button button;
    String text = null;
    ArrayList<String> list = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                try{
                    InputStream is = getResources().openRawResource(R.raw.test);
                    InputStreamReader inputStreamReader= new InputStreamReader(is, "utf-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder stringBuilder = new StringBuilder("");
                    String line;
                    String[] temp = null;
                    while ((line = bufferedReader.readLine()) != null){
                        stringBuilder.append(line);
                        stringBuilder.append("\n");
                        temp = line.split(" ");
                        for(int i = 0 ; i < temp.length ; i++){
                            list.add(temp[i]);
                        }
                    }
                    text =  stringBuilder.toString();
                }catch(Exception e){e.printStackTrace();}
                textView.setText(text);
            }
        });

        textView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int offset = getOffset(textView, motionEvent);
                //makeToast(offset);
                return false;
            }
        });



    }
    public int[] textToSpaceArray(){
        char[] textArray = text.toCharArray();
        int[] spaceArray = new int[textArray.length];
        int a = 0;
        for(int i = 0 ; i < spaceArray.length ; i++) {
            if(textArray[i] == ' ' || textArray[i] == '\n') {
                spaceArray[i] = -1;
                a ++;
            }
            else if(textArray[i] != ' ') {
                spaceArray[i] = a;
            }
        }
        return spaceArray;
    }
    public void makeToast(int offset){
        int index = getIndex(offset);
        String word = getWord(offset);
        Toast.makeText(this, "index : " + index + "\nword : " + word, Toast.LENGTH_SHORT).show();
    }

    public int getOffset(TextView textView, MotionEvent event){
        int posX = (int) event.getX();
        int posY = (int) event.getY();
        posX -= textView.getTotalPaddingLeft();
        posY -= textView.getTotalPaddingTop();
        Layout layout = textView.getLayout();

        int line = layout.getLineForVertical(posY);
        int off = layout.getOffsetForHorizontal(line, posX);
        //클릭된 부분의 offset을 통하여 어떤 단어가 클릭되었는지 구한다.

        //getIndex(off);
        makeToast(off);
        return off;
    }
    public int getIndex(int offset){
        int[] spaceArray = textToSpaceArray();
        return spaceArray[offset];
    }
    public String getWord(int offset) {
        String word = "";
        String temp = "";
        temp = String.valueOf(text.charAt(offset));
        //공백, 엔터에 대한 offset의 경우
        if(temp.equals(" ")) return "space";
        if(temp.equals("\n")) return "enter";

        for(int i = offset ; ; i--){
            if(i == -1) break;
            temp = String.valueOf(text.charAt(i));
            if( !temp.equals(" ")&& !temp.equals("\n")) {
                word = temp + word;
            }
            else break;
        }
        for(int i = offset+1 ; ; i++){
            if(i == text.length() + 1) break;
            temp = String.valueOf(text.charAt(i));
            //System.out.println(temp);

            if(!temp.equals(" ")&& !temp.equals("\n")){
                word = word + temp;
            }
            else break;
        }

        return word;
    }


}
