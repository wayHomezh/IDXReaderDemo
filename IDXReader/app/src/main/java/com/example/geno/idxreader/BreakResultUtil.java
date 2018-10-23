package com.example.geno.idxreader;

import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by geno on 15/05/18.
 */

public class BreakResultUtil {
    /**
     *@param cs
     *@param measurewidth 行测量的最大宽度
     *@param textpadding 字符间距
     *@param paint 测量的笔画
     *@return 如果cs为空或者长度为0，返回null
     *--------------------
     *TODO
     *--------------------
     */
    public static BreakResult BreakText(char[] cs, float measurewidth, float textpadding, Paint paint) {
        if(cs==null||cs.length==0){return null;}
        BreakResult breakResult = new BreakResult();
        breakResult.showChars = new ArrayList<>();
        float width = 0;

        for (int i = 0, size = cs.length; i < size; i++) {
            //Log.i(TAG, "BreakText:字符码 "+(byte) cs[i]);
            String mesasrustr = String.valueOf(cs[i]);
            float charwidth = paint.measureText(mesasrustr);

            if (mesasrustr.getBytes()[0] == 10){
                breakResult.charNums = i+1;
                breakResult.isFullLine = true;
                return breakResult;
            }

            if (width <= measurewidth && (width + textpadding + charwidth) > measurewidth) {
                breakResult.charNums = i;
                breakResult.isFullLine = true;
                return breakResult;
            }

            ShowChar showChar = new ShowChar();
            showChar.charData = cs[i];
            showChar.charWidth = charwidth;
            breakResult.showChars.add(showChar);
            width += charwidth + textpadding;
        }

        breakResult.charNums = cs.length;
        return breakResult;
    }

    /**
     *@param text
     *@param measurewidth
     *@param textpadding
     *@param paint
     *@return 如果text为空，返回null
     *--------------------
     *TODO
     *--------------------
     */
    public static BreakResult BreakText(String text, float measurewidth, float textpadding, Paint paint) {
        if (TextUtils.isEmpty(text)) {
            int[] is = new int[2];
            is[0] = 0;
            is[1] = 0;
            return null;
        }
        return BreakText(text.toCharArray(), measurewidth, textpadding, paint);

    }

    public static float MeasureText(String text, float textpadding, Paint paint) {
        if (TextUtils.isEmpty(text))
            return 0;
        char[] cs = text.toCharArray();
        float width = 0;
        for (int i = 0, size = cs.length; i < size; i++) {
            String mesasrustr = String.valueOf(cs[i]);
            float charwidth = paint.measureText(mesasrustr);
            width += charwidth + textpadding;
        }

        return width;
    }

}
