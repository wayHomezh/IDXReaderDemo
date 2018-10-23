package com.example.geno.idxreader;

import java.util.List;

/**
 * Created by geno on 24/05/18.
 */

public interface ChildCallBack {
    void call(List<String> resultData);
    void dayAndNightModel();
    void autoPageTurning();
    void showAndHideTurning();
    void showAndHideBookProcess();
}
