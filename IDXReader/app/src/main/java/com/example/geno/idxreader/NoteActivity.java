package com.example.geno.idxreader;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class NoteActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText editText;
    private TextView showText;
    private Button cancel;
    private Button confirm;
    private TextView title;

    private String TAG = "NoteActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        //隐藏状态栏和导航栏
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        showText = findViewById(R.id.select_text);
        cancel = findViewById(R.id.cancel);
        confirm = findViewById(R.id.confirm);
        title = findViewById(R.id.title);
        editText = findViewById(R.id.edit_text);

        getText();
        showText.setText(mText);
        if (!"".equals(mEditText)&&mEditText != null){
            editText.setText(mEditText);
            editText.setSelection(mEditText.length());
        }

        setBackGround();

        cancel.setOnClickListener(this);
        confirm.setOnClickListener(this);
    }

    private String mText;
    private String mEditText = "";
    private Boolean isDay;

    private void getText(){
        this.mText = getIntent().getStringExtra("note");
        this.mEditText = getIntent().getStringExtra("editNote");
        this.isDay = getIntent().getBooleanExtra("isDay",true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.cancel:
                finish();
                break;
            case R.id.confirm:
                Intent intent = new Intent();
                intent.putExtra("data_return",editText.getText().toString());
                setResult(RESULT_OK,intent);
                finish();
                break;
        }
    }

    private void setBackGround(){
        Log.d(TAG, "setBackGround:isDay "+isDay);
        if (!isDay){
            cancel.setBackgroundColor(Color.parseColor("#595959"));
            cancel.setTextColor(Color.parseColor("#aaaaaa"));

            title.setBackgroundColor(Color.parseColor("#595959"));
            title.setTextColor(Color.parseColor("#999999"));

            confirm.setBackgroundColor(Color.parseColor("#595959"));
            confirm.setTextColor(Color.parseColor("#aaaaaa"));

            showText.setTextColor(Color.parseColor("#999999"));
            showText.setBackgroundColor(Color.parseColor("#2F4F4F"));

            editText.setBackgroundColor(Color.parseColor("#595959"));
            editText.setTextColor(Color.parseColor("#999999"));
        }
    }
}


