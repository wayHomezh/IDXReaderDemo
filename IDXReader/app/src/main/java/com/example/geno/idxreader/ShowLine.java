package com.example.geno.idxreader;

import java.util.List;

/**
 * Created by geno on 15/05/18.
 */

public class ShowLine {
    public List<ShowChar> CharsData = null;

    @Override
    public String toString() {
        return "ShowLine [LineData=" + getLineData() + "]";
    }
    //获取该行数据
    public String getLineData(){
        String lineData = "";
        if(CharsData==null||CharsData.size()==0) return lineData;
        for(ShowChar c:CharsData){
            lineData = lineData+c.charData;
        }
        return lineData;
    }
}
