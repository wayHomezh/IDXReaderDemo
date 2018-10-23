package com.example.geno.idxreader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by geno on 12/05/18.
 */

public class TextPopupWindow implements MainCall {
    private Context mContent;
    private View mView;
    public PopupWindow popupWindow;
    private View popupView;
    public int x,y;
    private CallBack callBack;
    private ChildCallBack childCallBack;

    public TextPopupWindow(final Context context, View view){
        mContent = context;
        mView = view;
        callBack = (CallBack) view;
        childCallBack = ReaderActivity.readerActivity;
        popupView = LayoutInflater.from(mContent).inflate(R.layout.popup_window,
                null);

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,true);


        //加载view组件
        note = popupView.findViewById(R.id.note);
        dictionarySearch = popupView.findViewById(R.id.dictionary_search);
        dayAndNight = popupView.findViewById(R.id.day_night);
        autoPageTurning = popupView.findViewById(R.id.auto_page_turning);
        selectContent = popupView.findViewById(R.id.selected_content);
        listView = popupView.findViewById(R.id.search_list);

        //设置ListView
        setListView();

        //设置点击事件
        note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack.call();

                Boolean isDay = ReaderActivity.readerActivity.getIsDay();

                Intent intent = new Intent(ReaderActivity.readerActivity,NoteActivity.class);
                intent.putExtra("note",selectTextData.toString());
                intent.putExtra("isDay",isDay);
                ReaderActivity.readerActivity.startActivityForResult(intent,1);
            }
        });

        dictionarySearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchWord();
            }
        });

        dayAndNight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack.call();

                childCallBack.dayAndNightModel();
            }
        });

        autoPageTurning.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                callBack.call();
                childCallBack.autoPageTurning();
            }
        });
    }

    public void setPopupWindow(int X,int Y){
        x = X;
        y = Y;
    }


    public int popupWidth;

    //测量popupWindow的宽高
    public void setPopupWidthAndHeight() {
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWidth = popupView.getMeasuredWidth();
        popupHeight = popupView.getMeasuredHeight();
    }

    public int popupHeight;

    public void showPopupWindow(){
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.showAtLocation(mView, Gravity.NO_GRAVITY,x,y);
    }

    private StringBuilder selectTextData = new StringBuilder();

    public void setResultData(List<String> resultData) {
        this.resultData = resultData;
        adapter.setAdapter(resultData);
        adapter.notifyDataSetChanged();
    }

    private List<String> resultData = new ArrayList<>();


    public void setSelectTextData(StringBuilder selectText){
        selectTextData = selectText;
    }

    //加载view组件
    Button note;
    Button dictionarySearch;
    Button dayAndNight;
    Button autoPageTurning;
    TextView selectContent;

    public void setListView() {
        adapter = new SearchResultAdapter(mContent);
        adapter.setAdapter(resultData);
        listView.setAdapter(adapter);
    }

    private ListView listView;
    private SearchResultAdapter adapter;

    public void setText(){
        selectContent.setText(selectTextData);
    }

    public void setDayModel(){
        popupView.setBackgroundColor(Color.parseColor("#0f395b"));
        note.setBackgroundColor(Color.parseColor("#203984"));
        dictionarySearch.setBackgroundColor(Color.parseColor("#17285C"));
        dayAndNight.setBackgroundColor(Color.parseColor("#203984"));
        autoPageTurning.setBackgroundColor(Color.parseColor("#203984"));
        dayAndNight.setText("夜间模式");
    }

    public void setNightModel(){
        popupView.setBackgroundColor(Color.parseColor("#292929"));
        note.setBackgroundColor(Color.parseColor("#2F4F4F"));
        dictionarySearch.setBackgroundColor(Color.parseColor("#2d2d2d"));
        dayAndNight.setBackgroundColor(Color.parseColor("#2F4F4F"));
        autoPageTurning.setBackgroundColor(Color.parseColor("#2F4F4F"));
        dayAndNight.setText("日间模式");
    }

    //在线查词
    WordsSearch wordsSearch = new WordsSearch();

    private void showSearchWord(){
            String sendText = selectTextData.toString();
            wordsSearch.setSendText(sendText);

            try {
                if (sendText.length() == 1){
                    wordsSearch.sendRequestWordWithHttpURLConnection();
                }
                else {
                    wordsSearch.sendRequestWordsWithHttpURLConnection();
                }
            } catch (Exception e) {
                Toast.makeText(mContent,"请求超时或查询词语无释义",Toast.LENGTH_SHORT).show();
            }
        }

    @Override
    public void call(List<String> resultData) {
        this.resultData = resultData;
        if (resultData != null&&resultData.size()>0){
            adapter.setAdapter(resultData);
            adapter.notifyDataSetChanged();

        }
    }
}
