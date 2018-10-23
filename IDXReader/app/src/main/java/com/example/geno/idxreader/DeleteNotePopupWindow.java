package com.example.geno.idxreader;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Created by geno on 02/06/18.
 */

public class DeleteNotePopupWindow {
    private Context mContent;
    private View mView;
    public PopupWindow popupWindow;
    private View popupView;
    public int x,y;
    private TextView delete;
    private CallBack callBack;

    public DeleteNotePopupWindow(Context context,View view){
        mContent = context;
        mView = view;
        callBack = (CallBack) view;
        popupView = LayoutInflater.from(mContent).inflate(R.layout.delete_popupwindow,
                null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,true);

        //加载view组件
        delete = popupView.findViewById(R.id.delete_note);

        //测量popupWindow的宽高
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWidth = popupView.getMeasuredWidth();
        popupHeight = popupView.getMeasuredHeight();

        //设置点击事件
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack.deleNoteCall();

                ReaderActivity.readerActivity.delNote();
            }
        });
    }

    public int popupWidth;
    public int popupHeight;

    public void setDeletePopupWindowPosition(int X,int Y){
        x = X;
        y = Y;
    }


    public void showPopupWindow(){
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.showAtLocation(mView, Gravity.NO_GRAVITY,x,y);
    }

}
