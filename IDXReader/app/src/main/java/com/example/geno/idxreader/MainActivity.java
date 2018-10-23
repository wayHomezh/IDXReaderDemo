package com.example.geno.idxreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    //声明GridView相关变量
    private GridView gridView;
    private CheckBox checkBox;
    private GridViewAdapter adapter;
    private String[] arrBookName;
    private String[] arrBookPath;
    private Boolean isDay;
    private SharedPreferences pre;
    private SharedPreferences.Editor editor;
    private static final String TAG = "MainActivity";
    private boolean isShow;
    private ArrayList<Boolean> isCheckList;
    private boolean isCheckAll;
    private RelativeLayout footerMenu;
    private LinearLayout mask;
    private LinearLayout bookShelf;
    private Context context;

    public static MainActivity mainActivity;
    private MenuItem item;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;

        isShow = false;
        isCheckAll = false;
        context = getApplicationContext();
        footerMenu = findViewById(R.id.select_footer);
        Button checkAll = findViewById(R.id.select_all);
        Button delete = findViewById(R.id.delete);
        mask = findViewById(R.id.mask);
        bookShelf = findViewById(R.id.book_shelf);
        //设置toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //显示GridView
        gridView = findViewById(R.id.grid_view);
        getBookInfo();
        sortBookInfo();
        createGridView();
        //设置isCheck List数组
        setIsCheckList(arrBookName.length,false);

        //显示日夜模式
        isDay = pre.getBoolean("isDay",true);
        dayAndNightModel(isDay);

        //长按GridView显示CheckBox
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView,
                                           View view, int position, long id) {
                isShow = true;
                adapter.setGridAdapter(arrBookName,isShow,isCheckAll);
                adapter.notifyDataSetChanged();
                footerMenu.setVisibility(View.VISIBLE);
                return false;
            }
        });

        //点击GridView
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                checkBox = view.findViewById(R.id.check_box);
                if(isShow){
                    if (!isCheckList.get(position)) {
                        checkBox.setChecked(true);
                        isCheckList.set(position,true);
                    }
                    else {
                        checkBox.setChecked(false);
                        isCheckList.set(position,false);
                    }
                }
                else {
                    Intent intent = new Intent(MainActivity.this,
                            ReaderActivity.class);
                    Log.d(TAG, "onItemClick:arrBookName "+arrBookPath[position]);
                    intent.putExtra("bookPath",arrBookPath[position]);
                    intent.putExtra("bookName",arrBookName[position]);
                    intent.putExtra("isDay",isDay);
                    startActivityForResult(intent,1);
                }
            }
        });

        //点击全选按钮
        checkAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCheckAll = true;
                adapter.setGridAdapter(arrBookName,isShow,isCheckAll);
                adapter.notifyDataSetChanged();
                setIsCheckList(arrBookName.length,true);
            }
        });

        //点击删除按钮
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> listN = Arrays.asList(arrBookName);
                List<String> listP = Arrays.asList(arrBookPath);
                List<String> arrBookList = new ArrayList<>(listN);
                List<String> arrPathList = new ArrayList<>(listP);
                for (int i = arrBookName.length-1;i>=0;i--){
                    if (isCheckList.get(i)){
                        arrBookList.remove(i);
                        arrPathList.remove(i);
                        isCheckList.remove(i);
                    }
                }
                Log.d(TAG, "onClick:arrBookList "+arrBookList);
                arrBookName = arrBookList.toArray(new String[arrBookList.size()]);
                arrBookPath = arrPathList.toArray(new String[arrPathList.size()]);
                editor.putString("name",bookNameToString(arrBookName,
                        arrBookPath)[0]);
                editor.putString("path",bookNameToString(arrBookName,
                        arrBookPath)[1]);
                editor.apply();

                isShow = false;
                adapter.setGridAdapter(arrBookName,isShow,isCheckAll);
                adapter.notifyDataSetChanged();
                footerMenu.setVisibility(View.GONE);
            }
        });
    }

    //创建菜单并显示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        item = menu.findItem(R.id.day_night);
        if (isDay){
            item.setTitle("夜间模式");
        }
        else item.setTitle("日间模式");
        return true;
    }

    //给菜单添加点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_txt:
                openBrowser();
                break;
            case R.id.day_night:
                dayAndNightModelChange(item);

            default:
                break;
        }
        return true;
    }

    //创建GridView
    public void createGridView() {
        if(!arrBookName[0].equals("")){
            adapter = new GridViewAdapter(this);
            adapter.setGridAdapter(arrBookName,isShow,isCheckAll);
            gridView.setAdapter(adapter);
        }
    }

    //对话框选取sd卡后打开文件目录
    private void openBrowser() {
        new AlertDialog.Builder(this,R.style.AlertDialogCustom).setTitle("选择储存卡").
                setSingleChoiceItems(new String[]{"内置sd卡", "外部sd卡"}, 0,
                new DialogInterface.OnClickListener() {
                    Intent intent = new Intent(MainActivity.this,
                            BrowserActivity.class);

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0)
                            intent.putExtra("area", true);
                        else
                            intent.putExtra("area", false);
                        intent.putExtra("isDay",isDay);
                        startActivity(intent);
                        dialogInterface.dismiss();

                    }
                }
        ).setNegativeButton("取消", null).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        editor.putBoolean("isDay",isDay);
        editor.apply();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getBookInfo();
        if(!arrBookName[0].equals("")){
            isShow = false;
            isCheckAll =false;
            sortBookInfo();
            createGridView();
            setIsCheckList(arrBookName.length,false);
            footerMenu.setVisibility(View.GONE);
        }
    }

    private void sortBookInfo(){
        for(int start = 0,end = arrBookName.length-1;start <= end;start++,end--){
            arrBookName[start] = cleanSuffix(arrBookName[start]);
            if(start == end){
                break;
            }
            else {
                arrBookName[end] = cleanSuffix(arrBookName[end]);
                String tempN = arrBookName[end];
                arrBookName[end] = arrBookName[start];
                arrBookName[start] = tempN;
                String tempP = arrBookPath[end];
                arrBookPath[end] = arrBookPath[start];
                arrBookPath[start] = tempP;
            }
        }
    }

    //去除txt后缀名
    private String cleanSuffix(String fileName){
        String bookName = "";
        if (fileName.endsWith(".txt")){
            bookName = fileName.substring(0,fileName.indexOf("."));
        }
        return bookName;
    }

    //获得书名和路径
    @SuppressLint("CommitPrefEdits")
    private void getBookInfo(){
        pre = getSharedPreferences("data",MODE_PRIVATE);
        editor = context.getSharedPreferences(
                "data",Context.MODE_PRIVATE).edit();
        String bookName = pre.getString("name","");
        String bookPath = pre.getString("path","");
        arrBookName = bookName.split(",");
        arrBookPath = bookPath.split(",");
    }

    @Override
    public void onBackPressed() {
        if(isShow){
            isShow = false;
            isCheckAll = false;
            adapter.setGridAdapter(arrBookName,isShow,isCheckAll);
            adapter.notifyDataSetChanged();
        }
        else super.onBackPressed();
        setIsCheckList(arrBookName.length,false);
        footerMenu.setVisibility(View.GONE);
    }

    private void setIsCheckList(int num,boolean isCheck){
        isCheckList = new ArrayList<>();
        for (int i = 0;i<num;i++){
            isCheckList.add(isCheck);
        }
    }

    //将书名数组变成字符串
    private String[] bookNameToString(String[] arrBookNames,String[] arrBookPaths){
        String[] bookNameString = new String[2];
        if (arrBookNames != null&&arrBookNames.length != 0){
            bookNameString[0] = arrBookNames[0]+".txt";
            bookNameString[1] = arrBookPaths[0];
            for (int i = 1;i<arrBookNames.length;i++){
                bookNameString[0] += ","+arrBookNames[i]+".txt";
                bookNameString[1] += ","+arrBookPaths[i];
            }
        }
        return bookNameString;
    }


    private void dayAndNightModel(Boolean isDay){
        if (isDay){
            mask.setVisibility(View.GONE);
            bookShelf.setBackgroundColor(Color.parseColor("#ffffff"));

        }
        else {
            mask.setVisibility(View.VISIBLE);
            bookShelf.setBackgroundColor(Color.parseColor("#999999"));
        }
    }

    private void dayAndNightModelChange(MenuItem item){
        if (isDay){
            mask.setVisibility(View.VISIBLE);
            bookShelf.setBackgroundColor(Color.parseColor("#999999"));
            item.setTitle("日间模式");
            isDay = false;
             }
        else {
            mask.setVisibility(View.GONE);
            bookShelf.setBackgroundColor(Color.parseColor("#ffffff"));
            item.setTitle("夜间模式");
            isDay = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1){
            if (resultCode == RESULT_OK){
                isDay = data.getBooleanExtra("return_data",true);
                dayAndNightModel(isDay);
                if (isDay){
                    item.setTitle("夜间模式");
                }
                else item.setTitle("日间模式");
            }
        }
    }

}
