package com.example.geno.idxreader;

import java.util.List;

/**
 * Created by geno on 15/05/18.
 */

public class BreakResult {
    public int charNums = 0;//测量了的字符数
    public boolean isFullLine = false;//是否满一行
    public List<ShowChar> showChars = null;//测量了的字符数据

    public boolean hasData(){
        return showChars != null&&showChars.size()>0;
    }
}
