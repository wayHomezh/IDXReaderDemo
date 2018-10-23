package com.example.geno.idxreader;

import java.util.List;

/**
 * Created by geno on 25/05/18.
 */

public class Store {
    private String note;
    private List<ShowLine> selectLineList;

    public void setNote(String note) {
        this.note = note;
    }

    public void setSelectLineList(List<ShowLine> selectLineList) {
        this.selectLineList = selectLineList;
    }


    public String getNote() {
        return note;
    }

    public List<ShowLine> getSelectLineList() {
        return selectLineList;
    }
}
