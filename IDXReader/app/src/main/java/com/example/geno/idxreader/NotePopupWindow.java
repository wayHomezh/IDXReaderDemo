package com.example.geno.idxreader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Created by geno on 01/06/18.
 */

public class NotePopupWindow {
    private Context mContent;
    private View mView;
    public PopupWindow popupWindow;
    private View popupView;
    public int x,y;
    private CallBack callBack;
    TextView note;
    public NotePopupWindow(final Context context, View view){
        mContent = context;
        mView = view;
        callBack = (CallBack) view;
        popupView = LayoutInflater.from(mContent).inflate(R.layout.note_popup_window,
                null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,true);

        //加载view组件
        note = popupView.findViewById(R.id.show_note);

        //设置点击事件
        note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack.noteCall();

                Boolean isDay = ReaderActivity.readerActivity.getIsDay();

                Intent intent = new Intent(ReaderActivity.readerActivity,NoteActivity.class);
                intent.putExtra("note",selectData);
                intent.putExtra("editNote",noteData);
                intent.putExtra("isDay",isDay);
                ReaderActivity.readerActivity.startActivityForResult(intent,2);
            }
        });
    }
    public int popupWidth;
    public int popupHeight;

    private String noteData;
    private String selectData;

    public void setData(String noteData,String selectData){
        this.noteData = noteData;
        this.selectData = selectData;
    }

    public void setNotePopupWindowPosition(int X,int Y){
        x = X;
        y = Y;
    }

    public void refreshWidthAndHeight(){
        //测量popupWindow的宽高
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWidth = popupView.getMeasuredWidth();
        popupHeight = popupView.getMeasuredHeight();
    }

    public void setText(){
        note.setText(noteData);
    }

    public void showPopupWindow(){
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.showAtLocation(mView, Gravity.NO_GRAVITY,x,y);
    }
}
