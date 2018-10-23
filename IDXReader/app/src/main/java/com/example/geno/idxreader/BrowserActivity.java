package com.example.geno.idxreader;

import android.app.ListActivity;

import android.os.Environment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class BrowserActivity extends ListActivity{
    private boolean pathFlag;
    private String rootPath;
    private List<String> itemList;
    private List<String> pathList;
    private File clickFile;
    private FileAdapter fileAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        Button backUpper = findViewById(R.id.back_button);
        LinearLayout layout = findViewById(R.id.browser_mask);
        dayNightModelChange(layout);
        rootPath =getRootPath();//根目录的路径
        initInfo();
        //点击返回上一层目录
        backUpper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickFile.getParentFile()==null){
                    Toast.makeText(BrowserActivity.this,"不能再返回了",
                            Toast.LENGTH_SHORT).show();
                }
                else clickFile = clickFile.getParentFile();
                getFileDir(clickFile.getPath());
                fileAdapter.setAdapter(itemList,pathList);
                fileAdapter.notifyDataSetChanged();
            }
        });

    }

    //加载信息
    private void initInfo(){
        if(rootPath == null){
            Toast.makeText(BrowserActivity.this,"没有外置sd卡",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            getFileDir(rootPath);
            fileAdapter = new FileAdapter(this);
            fileAdapter.setAdapter(itemList,pathList);
            setListAdapter(fileAdapter);
            clickFile = new File(rootPath);
        }
    }

    //获取sd卡根目录
    private String getRootPath(){
        String sdCardPath;
        pathFlag = getIntent().getBooleanExtra("area",false);
        try {
            if(pathFlag){
                sdCardPath = Environment.getExternalStorageDirectory().toString();
            }
            else{
                sdCardPath = System.getenv("SECONDARY_STORAGE");
                if(sdCardPath.equals(Environment.getExternalStorageState())){
                    sdCardPath = null;
                }
            }
            return sdCardPath;
        }catch (Exception e){
            return null;
        }
    }

    //获取当前目录中的文件夹和txt文件的文件名及路径
    private void getFileDir(String filePath){
        itemList = new ArrayList<>();
        pathList = new ArrayList<>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        if(files == null){
            Toast.makeText(BrowserActivity.this,"当前文件夹为空",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            for(int i = 0;i<files.length;i++){
                if(files[i].isDirectory()||isTxt(files[i])){
                    itemList.add(files[i].getName());
                    pathList.add(files[i].getPath());
                }
                else {
                    files[i].delete();//清除txt文件和文件夹以外的文件
                }
            }
           if(itemList.size() == 0)Toast.makeText(BrowserActivity.this,
                   "当前文件夹为空", Toast.LENGTH_SHORT).show();
        }
    }

    //判断文件是否为txt
    private boolean isTxt(File file){
        String fileName = file.getName();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1,
                fileName.length());
        if(suffix.equals("txt"))return true;
        else return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        clickFile = new File(pathList.get(position));
        if(clickFile.isDirectory()){
            getFileDir(clickFile.getPath());
            fileAdapter.setAdapter(itemList,pathList);
            fileAdapter.notifyDataSetChanged();
        }
    }

    private void dayNightModelChange(LinearLayout layout){
        Boolean isDay = getIntent().getBooleanExtra("isDay",true);
        if (isDay){
            layout.setVisibility(View.GONE);
        }
        else {
            layout.setVisibility(View.VISIBLE);
        }
    }

}
