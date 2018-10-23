package com.example.geno.idxreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.ibm.icu.text.DecimalFormat;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class ReaderActivity extends AppCompatActivity implements ChildCallBack,
        View.OnClickListener,SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "ReaderActivity";
    private String bookPath;
    private String bookName;
    private RandomAccessFile randomAccessFile;
    public int lineCount;
    public int wordCount;
    private byte[] bytes;
    private int sign,oldSign;
    private SharedPreferences pre;
    private SharedPreferences.Editor editor;

    private SharedPreferences preData;
    private SharedPreferences.Editor editorData;

    public  Context rContext;
    private MyViewPager viewPager;
    private View view0,view1,view2,view3,view4;
    private List<View> viewList;
    private List<TextView> bookProcessList;
    private ReaderPageAdapter readerPageAdapter;
    private Gson gson;
    //每页读取的字节数，用于读上一页时指针的回退
    private int hasReadByte;
    //储存已读页面每页的字节数
    private List<Integer> hasReadByteList;
    //前页面在hasReadByteList中的位置pointer
    private int pointer;
    private int lastPagerIndex;
    private int pagerIndex;
    private Boolean isDay;
    //储存五个显示书内容的TextView
    private TextSelectView bookContent0,bookContent1,bookContent2,bookContent3,bookContent4;
    //储存五个显示标题的TextView
    private TextView bookInfo0,bookInfo1,bookInfo2,bookInfo3,bookInfo4;
    //储存五个显示阅读百分比的TextView
    private TextView bookProcess0,bookProcess1,bookProcess2,bookProcess3,bookProcess4;
    //用于储存viewPage后两个TextView的内容
    private String pageContentRight0, pageContentRight1;
    //用于储存viewPage后两个TextView的阅读进度
    private String bookProcessRight0,bookProcessRight1;
    //用于储存viewPage后两个TextView的笔记
    private List<Store> storeListRight0,storeListRight1;
    //储存每个向上翻页开始读取时的sign
    private List<Integer> oldSignList;
    private int endIndex;
    //判断是否是小说结尾
    private boolean isNotEnd;
    private String txtCode;

    private int height = 0,width = 0;

    public static ReaderActivity readerActivity;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            MainCall mainCall;
            super.handleMessage(msg);
            switch(msg.what){
                case 1:
                    TextSelectView textSelectView = textSelectViewList.get(pagerIndex);
                    mainCall = textSelectView.gettPW();
                    mainCall.call(resultData);
                    break;
                case 2:
                    viewPager.setCurrentItem(pagerIndex+1);
                    break;
                case 3:
                    pageTurning.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };

    //储存页面读取后的返回值
    private String[] pageData = new String[2];
    private List<Store> pageStore = new ArrayList<>();
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        readerActivity = this;
        gson = new Gson();
        rContext = getApplicationContext();
        hasReadByteList = new ArrayList<>();
        oldSignList = new ArrayList<>();
        bookProcessList = new ArrayList<>();

        storeListRight0 = new ArrayList<>();
        storeListRight1 = new ArrayList<>();

        bookPath = getIntent().getStringExtra("bookPath");
        bookName = getIntent().getStringExtra("bookName");
        isDay = getIntent().getBooleanExtra("isDay",true);
        viewPager = findViewById(R.id.view_page);
        initViewPage();
        initContent();
        initAutoPageTurningMenu();
        initSeekBar();
        setBookInfo();
        txtCode = getCharset(bookPath);
        

        //隐藏状态栏和导航栏
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        //获取data文件数据
        preData = rContext.getSharedPreferences("data",Context.MODE_PRIVATE);
        editorData = preData.edit();
        period = preData.getInt("period",10000);

        bookContent0.postDelayed(new Runnable() {
            @Override
            public void run() {
                width = bookContent0.vWidth;
                height = bookContent0.vHeight;
                //获取行数和每行的字数
                lineCount = (int) (height/(bookContent0.TextHeight+bookContent0.LinePadding));//每页行数
                wordCount = (int) (width/bookContent0.textWidth);//每行字数（两个或三个字节）

                openBook();
            }
        },200);

        if (!isDay){
            dayAndNightModelChange();
        }

        reduce.setOnClickListener(this);
        plus.setOnClickListener(this);
        cancelAutoPage.setOnClickListener(this);
        bookContent0.setOnClickListener(this);
        bookContent1.setOnClickListener(this);
        bookContent2.setOnClickListener(this);
        bookContent3.setOnClickListener(this);
        bookContent4.setOnClickListener(this);

    }

    //首次打开书籍或打开上次阅读页面
    public void openBook() {
        try {

        randomAccessFile = new RandomAccessFile(bookPath,"r");
        pre = rContext.getSharedPreferences("data"+bookName,
                Context.MODE_PRIVATE);
        editor = pre.edit();

        pointer = pre.getInt("pointer",-1);
        String intList0 = pre.getString("hasReadByteList","");
        sign = pre.getInt("sign",0);
        String intList1 = pre.getString("oldSignList","");
        pagerIndex = pre.getInt("pagerIndex",0);
        isNotEnd = pre.getBoolean("isNotEnd",true);
        endIndex = pre.getInt("endIndex",2);

        speed.setText(((period/1000)+"秒"));


        map = getSpData();

        if (!"".equals(intList0)) {//非首次打开书籍
            hasReadByteList = gson.fromJson(intList0,new TypeToken<List<Integer>>(){
            }.getType());
            oldSignList = gson.fromJson(intList1,new TypeToken<List<Integer>>(){
            }.getType());

            if (isNotEnd){
                sign = sign-hasReadByteList.get(pointer)-hasReadByteList
                        .get(pointer-1)- hasReadByteList.get(pointer-2)-hasReadByteList
                        .get(pointer-3)-hasReadByteList.get(pointer-4);
            }
            else {//是最后几页
                openEndPageSign();
            }
            pointer -= 5;

            randomAccessFile.seek(sign);
            initOldNextPage(bookContent0,0);
            bookContent0.initData(width,height);
            bookContent0.invalidate();

            initOldNextPage(bookContent1,1);
            bookContent1.initData(width,height);
            bookContent1.invalidate();

            initOldNextPage(bookContent2,2);
            bookContent2.initData(width,height);
            bookContent2.invalidate();

            initOldNextPage(bookContent3,3);
            pageContentRight0 = pageData[0];
            bookProcessRight0 = pageData[1];
            storeListRight0 = pageStore;
            bookContent3.initData(width,height);
            bookContent3.invalidate();

            initOldNextPage(bookContent4,4);
            pageContentRight1 = pageData[0];
            bookProcessRight1 = pageData[1];
            storeListRight1 = pageStore;
            bookContent4.initData(width,height);
            bookContent4.invalidate();

            viewPager.setCurrentItem(pagerIndex,false);
            lastPagerIndex = pagerIndex;//加载第一页是初始化lastPagerIndex
        }
        else {//首次打开书籍
            oldSign = 0;
            oldSignList.add(oldSign);
            initNextPage(bookContent0,0);
            bookContent0.initData(width,height);
            bookContent0.invalidate();

            initNextPage(bookContent1,1);
            bookContent1.initData(width,height);
            bookContent1.invalidate();

            initNextPage(bookContent2,2);
            bookContent2.initData(width,height);
            bookContent2.invalidate();

            initNextPage(bookContent3,3);
            pageContentRight0 = pageData[0];
            bookProcessRight0 = pageData[1];
            storeListRight0 = pageStore;
            bookContent3.initData(width,height);
            bookContent3.invalidate();

            initNextPage(bookContent4,4);
            pageContentRight1 = pageData[0];
            bookProcessRight1 = pageData[1];
            storeListRight1 = pageStore;
            bookContent4.initData(width,height);
            bookContent4.invalidate();
        }
        }catch(IOException e){
            lastView.initData(width,height);
            lastView.invalidate();
        }
    }

    //非首次打开书籍是书的结尾的sign
    private void openEndPageSign(){
        switch (endIndex){
            case 0:
                sign = sign - hasReadByteList.get(pointer-4);
                break;
            case 1:
                sign = sign - hasReadByteList.get(pointer-4) - hasReadByteList.get(pointer-3);
                break;
            case 2:
                sign = sign - hasReadByteList.get(pointer-4) - hasReadByteList.get(pointer-3) -
                        hasReadByteList.get(pointer-2);
                break;
            case 3:
                sign = sign - hasReadByteList.get(pointer-4) - hasReadByteList.get(pointer-1) -
                        hasReadByteList.get(pointer-2) - hasReadByteList.get(pointer-3);
                break;
            case 4:
                sign = sign - hasReadByteList.get(pointer) - hasReadByteList.get(pointer-1) -
                        hasReadByteList.get(pointer-2) - hasReadByteList.get(pointer-3) -
                        hasReadByteList.get(pointer-4);
                break;
            default:
        }
    }

    private TextSelectView lastView;

    //加载新的下一页
    private void initNextPage(TextSelectView mTextView,int bookContentIndex) throws IOException {
        StringBuilder content = new StringBuilder();
        String contents;
        String readProcess;
        hasReadByte = 0;
        try {
            //读取一页字数
            pointer++;
            if ("UTF-8/ISO-8859-1".contains(txtCode)){//加载新的下一页UTF-8
                for (int i = 0;i<lineCount;i++){
                    bytes = new byte[3*wordCount];
                    int l = 0;//标记在bytes中的位置
                    randomAccessFile.seek(sign);
                    for (int j = 0;j<wordCount*2;j++){
                        bytes[l] = randomAccessFile.readByte();
                        if(bytes[l] < 0){//读取到中文
                            if(wordCount*3-l>3){
                                bytes[++l] = randomAccessFile.readByte();
                                bytes[++l] = randomAccessFile.readByte();
                                sign++;
                                sign++;
                                hasReadByte++;
                                hasReadByte++;
                                j++;
                            }
                            else {//sign需要回退一个字节
                                bytes[l] = 0;
                                break;
                            }
                            l++;
                        }
                        else if(bytes[l] == 10){//读取到换行符
                            sign++;
                            hasReadByte++;
                            break;
                        }
                        else {
                            l++;
                        }
                        sign++;
                        hasReadByte++;
                    }
                    content.append(new String(bytes,"UTF-8"));
                }
            }
            else if ("GBK/Big5/GB2312".contains(txtCode)){//加载新的下一页GBK
                for (int i = 0;i<lineCount;i++){
                    bytes = new byte[2*wordCount];
                    int l = 0;//标记在bytes中的位置
                    randomAccessFile.seek(sign);
                    for (int j = 0;j<wordCount*2;j++){
                        bytes[l] =  randomAccessFile.readByte();
                        if(bytes[l] < 0){//读取到中文
                            if(wordCount*2-l>2){
                                bytes[++l] = randomAccessFile.readByte();
                                sign++;
                                hasReadByte++;
                                j++;
                            }
                            else {//sign需要回退一个字节
                                bytes[l] = 0;
                                break;
                            }
                            l++;
                        }
                        else if(bytes[l] == 10){//读取到换行符
                            sign++;
                            hasReadByte++;
                            break;
                        }
                        else {
                            l++;
                        }
                        sign++;
                        hasReadByte++;
                    }
                    content.append(new String(bytes,"GBK"));
                }
            }
            //加载阅读百分比
            readProcess = readProcess(sign);
            bookProcessList.get(bookContentIndex).setText(readProcess);

            contents = content.toString();

            hasReadByteList.add(hasReadByte);
            mTextView.setTextData(contents);

            List<Store> storeList1 = new ArrayList<>();
            mTextView.setStoreList(storeList1);
        } catch (IOException e) {//读到文章结尾了
            hasReadByte++;
            sign++;

            //加载阅读百分比
            readProcess = readProcess(sign);
            bookProcessList.get(bookContentIndex).setText(readProcess);

            if ("GBK/Big5/GB2312".contains(txtCode))content.append(new String(bytes,"GBK"));
            else if("UTF-8/ISO-8859-1".contains(txtCode))content.append(new String(bytes,"UTF-8"));

            contents = content.toString();
            mTextView.setTextData(contents);

            List<Store> storeList1 = new ArrayList<>();
            mTextView.setStoreList(storeList1);

            hasReadByteList.add(hasReadByte);
            endIndex = bookContentIndex;

            if (hasReadByteList.size()>5){
                endHandle();
                viewPager.setCurrentItem(1);
            }
            else startHandle();
            isNotEnd = false;

            pageData[0] = contents;
            pageData[1] = readProcess;
            lastView = mTextView;

            randomAccessFile.readByte();
        }
        pageData[0] = contents;
        pageData[1] = readProcess;

    }

    //加载旧的下一页
    private void initOldNextPage(TextSelectView mTextView,int bookContentIndex) throws IOException {
        StringBuilder content = new StringBuilder();
        String contents;
        String readProcess;
        List<Store> storeList1;
        try {
            //读取一页字数
            pointer++;//先加后判断
            int byteNum = hasReadByteList.get(pointer);
            bytes = new byte[byteNum];
            for (int j = 0;j<byteNum;j++){
                bytes[j] = randomAccessFile.readByte();
                }
            if ("GBK/Big5/GB2312".contains(txtCode))content.append(new String(bytes,"GBK"));
            else if("UTF-8/ISO-8859-1".contains(txtCode))content.append(new String(bytes,"UTF-8"));

            contents = content.toString();

            sign += hasReadByteList.get(pointer);

            mTextView.setTextData(contents);

            //加载阅读百分比
            readProcess = readProcess(sign);
            bookProcessList.get(bookContentIndex).setText(readProcess);

            storeList1 = map.get(pointer);
            if (storeList1== null){
                storeList1 = new ArrayList<>();
            }
            mTextView.setHasNote(true);
            mTextView.setStoreList(storeList1);
        } catch (IOException e) {

            storeList1 = map.get(pointer);
            if (storeList1 == null){
                storeList1 = new ArrayList<>();
            }
            mTextView.setHasNote(true);
            mTextView.setStoreList(storeList1);
            //加载阅读百分比
            sign += hasReadByteList.get(pointer);
            readProcess = readProcess(sign);
            bookProcessList.get(bookContentIndex).setText(readProcess);

            if ("GBK/Big5/GB2312".contains(txtCode))content.append(new String(bytes,"GBK"));
            else if("UTF-8/ISO-8859-1".contains(txtCode))content.append(new String(bytes,"UTF-8"));

            contents = content.toString();
            mTextView.setTextData(contents);
            endIndex = bookContentIndex;

            if (hasReadByteList.size()>5) {
                endHandle();
                viewPager.setCurrentItem(pagerIndex);
            }
            else startHandle();
            isNotEnd = false;

            pageData[0] = contents;
            pageData[1] = readProcess;
            pageStore = storeList1;
            lastView = mTextView;

            randomAccessFile.readByte();
        }
        pageData[0] = contents;
        pageData[1] = readProcess;
        pageStore = storeList1;

    }

    //小说阅读进度
    private String readProcess(float s) throws IOException {
        float len = (int) randomAccessFile.length()+1;
        DecimalFormat decimalFormat = new DecimalFormat("0.00%");
        return decimalFormat.format((s/len));
    }


    Boolean canChangeNext = true;
    Boolean canChangePre = true;

    //加载ViewPage
    private void initViewPage(){
        final LayoutInflater inflater = getLayoutInflater();
        view0 = inflater.inflate(R.layout.page_left_extra,null);
        view1 = inflater.inflate(R.layout.page_left,null);
        view2 = inflater.inflate(R.layout.page_center,null);
        view3 = inflater.inflate(R.layout.page_right,null);
        view4 = inflater.inflate(R.layout.page_right_extra,null);

        viewList = new ArrayList<>();
        viewList.add(view0);
        viewList.add(view1);
        viewList.add(view2);
        viewList.add(view3);
        viewList.add(view4);

        readerPageAdapter = new ReaderPageAdapter(viewList);
        viewPager.setAdapter(readerPageAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "onPageScrolled: "+positionOffset);
                if (isNotEnd){
                    if (position == 3){
                        if (positionOffset>=0.9&&canChangeNext){
                            Log.d(TAG, "onPageScrolled: 加载下");
                            turnNext();
                        }
                    }
                    if (position == 0&&!canChangePre){
                        Log.d(TAG, "onPageScrolled: 下翻");
                        viewPager.setCurrentItem(1,false);
                        canChangeNext = true;
                    }

                    //上翻
                    if (position == 0){
                        if (positionOffset<=0.1&&canChangePre){
                            Log.d(TAG, "onPageScrolled:加载上 ");
                            turnPre();
                        }
                    }
                    if (positionOffset == 0&&!canChangePre){
                        Log.d(TAG, "onPageScrolled:上翻 ");
                        viewPager.setCurrentItem(3,false);
                        canChangePre = true;
                    }
                }

            }

            @Override
            public void onPageSelected(int position) {
                pagerIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
//                if (state == 0){
//                    if (pagerIndex>lastPagerIndex){//下一页
//                        if (isNotEnd){
//                            if (pagerIndex == 4){//翻下一列
//
//                            }
//                        }
//
//                    }
//                    else if(pagerIndex<lastPagerIndex){//上一页
//
//                    }
//                    lastPagerIndex = pagerIndex;
//                }
//                if(state == 1){
//                    Log.d(TAG, "onPageScrollStateChanged:state=1 ");
//                }
//                if (state==2){
//                    Log.d(TAG, "onPageScrollStateChanged:state=2 ");
//                }
            }
        });
    }

    //下翻一列
    private void turnNext(){
        if (pointer+1 == hasReadByteList.size()){//当前页是最新的
            try {
                oldSign = sign-hasReadByteList.get(pointer)-
                        hasReadByteList.get(pointer-1);
                oldSignList.add(oldSign);

                bookContent0.setTextData(pageContentRight0);
                bookContent0.initData(width,height);
                //清除storeList内容
                bookContent0.setStoreList(storeListRight0);
                bookContent0.invalidate();
                bookProcess0.setText(bookProcessRight0);

                bookContent1.setTextData(pageContentRight1);
                bookContent1.initData(width,height);
                bookContent1.setStoreList(storeListRight1);
                bookContent1.invalidate();
                bookProcess1.setText(bookProcessRight1);

//                viewPager.setCurrentItem(1,false);

                initNextPage(bookContent2,2);
                bookContent2.initData(width,height);
                bookContent2.invalidate();

                initNextPage(bookContent3,3);
                pageContentRight0 = pageData[0];
                bookProcessRight0 = pageData[1];
                storeListRight0 = pageStore;
                bookContent3.initData(width,height);
                bookContent3.invalidate();

                initNextPage(bookContent4,4);
                pageContentRight1 = pageData[0];
                bookProcessRight1 = pageData[1];
                storeListRight1 = pageStore;
                bookContent4.initData(width,height);
                bookContent4.invalidate();
            }catch (IOException e){//读到最后一页
                lastView.initData(width,height);
                lastView.invalidate();
            }

        }
        //当前列不是最新的
        else {
            try {
                bookContent0.setTextData(pageContentRight0);
                bookContent0.initData(width,height);
                bookContent0.setStoreList(storeListRight0);
                bookContent0.invalidate();
                bookProcess0.setText(bookProcessRight0);

                bookContent1.setTextData(pageContentRight1);
                bookContent1.initData(width,height);
                bookContent1.setStoreList(storeListRight1);
                bookContent1.invalidate();
                bookProcess1.setText(bookProcessRight1);

//                viewPager.setCurrentItem(1,false);

                initOldNextPage(bookContent2,2);
                bookContent2.initData(width,height);
                bookContent2.invalidate();

                initOldNextPage(bookContent3,3);
                pageContentRight0 = pageData[0];
                bookProcessRight0 = pageData[1];
                storeListRight0 = pageStore;
                bookContent3.initData(width,height);
                bookContent3.invalidate();

                initOldNextPage(bookContent4,4);
                pageContentRight1 = pageData[0];
                bookProcessRight1 = pageData[1];
                storeListRight1 = pageStore;
                bookContent4.initData(width,height);
                bookContent4.invalidate();


            }catch (IOException e){//读到最后一页
                lastView.initData(width,height);
                lastView.invalidate();
            }

        }
        canChangeNext = false;
    }

    //上翻一列
    private void turnPre(){
        if (pointer != 4){//第一列不上翻
            if (pagerIndex == 0){
                if (!isNotEnd){//最后几页
                    viewPager.setAdapter(readerPageAdapter);
                    isNotEnd = true;
                }
                pointer -= 8;//pointer为7,10,13,16.... 即3n+1

                int oldSignPointer =  (pointer+1)/3;
                try {
                    sign = oldSignList.get(oldSignPointer);
                    randomAccessFile.seek(sign);

                    initOldNextPage(bookContent0,0);
                    bookContent0.initData(width,height);
                    bookContent0.invalidate();

                    initOldNextPage(bookContent1,1);
                    bookContent1.initData(width,height);
                    bookContent1.invalidate();

                    initOldNextPage(bookContent2,2);
                    bookContent2.initData(width,height);
                    bookContent2.invalidate();

                    initOldNextPage(bookContent3,3);
                    pageContentRight0 = pageData[0];
                    bookProcessRight0 = pageData[1];
                    storeListRight0 = pageStore;
                    bookContent3.initData(width,height);
                    bookContent3.invalidate();

                    initOldNextPage(bookContent4,4);
                    pageContentRight1 = pageData[0];
                    bookProcessRight1 = pageData[1];
                    storeListRight1 = pageStore;
                    bookContent4.initData(width,height);
                    bookContent4.invalidate();
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                viewPager.setCurrentItem(3,false);
            }
        }
        canChangePre = false;
    }


    @Override
    protected void onStop() {
        String intList1 = gson.toJson(hasReadByteList);
        String intList2 = gson.toJson(oldSignList);
        editor.putString("hasReadByteList",intList1);
        editor.putInt("sign",sign);
        editor.putString("oldSignList",intList2);
        editor.putInt("pointer",pointer);
        editor.putInt("pagerIndex",pagerIndex);
        editor.putBoolean("isNotEnd",isNotEnd);
        editor.putInt("endIndex",endIndex);
        editor.apply();

        editorData.putInt("period",period);
        editorData.putBoolean("isDay",isDay);
        editorData.apply();
        super.onStop();
    }


    private List<TextSelectView> textSelectViewList = new ArrayList<>();

    private void initContent(){
        bookContent0 = viewList.get(0).findViewById(R.id.book_content);
        bookContent1 = viewList.get(1).findViewById(R.id.book_content);
        bookContent2 = viewList.get(2).findViewById(R.id.book_content);
        bookContent3 = viewList.get(3).findViewById(R.id.book_content);
        bookContent4 = viewList.get(4).findViewById(R.id.book_content);

        textSelectViewList.add(bookContent0);
        textSelectViewList.add(bookContent1);
        textSelectViewList.add(bookContent2);
        textSelectViewList.add(bookContent3);
        textSelectViewList.add(bookContent4);

        bookInfo0 = viewList.get(0).findViewById(R.id.info_bar);
        bookInfo1 = viewList.get(1).findViewById(R.id.info_bar);
        bookInfo2 = viewList.get(2).findViewById(R.id.info_bar);
        bookInfo3 = viewList.get(3).findViewById(R.id.info_bar);
        bookInfo4 = viewList.get(4).findViewById(R.id.info_bar);

        bookProcess0 = viewList.get(0).findViewById(R.id.info_process);
        bookProcess1 = viewList.get(1).findViewById(R.id.info_process);
        bookProcess2 = viewList.get(2).findViewById(R.id.info_process);
        bookProcess3 = viewList.get(3).findViewById(R.id.info_process);
        bookProcess4 = viewList.get(4).findViewById(R.id.info_process);

        bookProcessList.add(bookProcess0);
        bookProcessList.add(bookProcess1);
        bookProcessList.add(bookProcess2);
        bookProcessList.add(bookProcess3);
        bookProcessList.add(bookProcess4);
    }

    private Button reduce;
    private Button plus;
    private TextView speed;
    private TextView cancelAutoPage;
    private LinearLayout pageTurning;

    //加载自动翻页调速菜单
    private void initAutoPageTurningMenu(){
        reduce = findViewById(R.id.reduce);
        plus = findViewById(R.id.plus);
        speed = findViewById(R.id.speed);
        cancelAutoPage = findViewById(R.id.cancel_auto_page_turning);
        pageTurning = findViewById(R.id.page_turning);
    }

    private LinearLayout seekBarMenu;
    private TextView bookProcess;
    private SeekBar seekBar;

    //加载seekBar
    private void initSeekBar(){
        seekBarMenu = findViewById(R.id.seekBarMenu);
        bookProcess = findViewById(R.id.book_process);
        seekBar = findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(this);
    }

    private void setBookInfo(){
        bookInfo0.setText(bookName);
        bookInfo1.setText(bookName);
        bookInfo2.setText(bookName);
        bookInfo3.setText(bookName);
        bookInfo4.setText(bookName);
    }

    private void endHandle(){
        List<View> endViewList = new ArrayList<>();
        ReaderPageAdapter endReaderPageAdapter;
        switch (endIndex){
            case 2:{
                endViewList.add(view0);
                endViewList.add(view1);
                endViewList.add(view2);
                endReaderPageAdapter = new ReaderPageAdapter(endViewList);
                viewPager.setAdapter(endReaderPageAdapter);
                pointer += 2;
            }
            break;
            case 3:{
                endViewList.add(view0);
                endViewList.add(view1);
                endViewList.add(view2);
                endViewList.add(view3);
                endReaderPageAdapter = new ReaderPageAdapter(endViewList);
                viewPager.setAdapter(endReaderPageAdapter);
                pointer++;
            }
            break;
        }
    }

    //处理页面数小于等于5页的情况
    private void startHandle(){
        List<View> endViewList = new ArrayList<>();
        ReaderPageAdapter endReaderPageAdapter;
        switch (endIndex){
            case 0:
                endViewList.add(view0);
                endReaderPageAdapter = new ReaderPageAdapter(endViewList);
                viewPager.setAdapter(endReaderPageAdapter);
                pointer += 4;
                break;
            case 1:
                endViewList.add(view0);
                endViewList.add(view1);
                endReaderPageAdapter = new ReaderPageAdapter(endViewList);
                viewPager.setAdapter(endReaderPageAdapter);
                viewPager.setCurrentItem(pagerIndex);
                pointer += 3;
                break;
            case 2:
                endViewList.add(view0);
                endViewList.add(view1);
                endViewList.add(view2);
                endReaderPageAdapter = new ReaderPageAdapter(endViewList);
                viewPager.setAdapter(endReaderPageAdapter);
                viewPager.setCurrentItem(pagerIndex);
                pointer += 2;
                break;
            case 3:
                endViewList.add(view0);
                endViewList.add(view1);
                endViewList.add(view2);
                endViewList.add(view3);
                endReaderPageAdapter = new ReaderPageAdapter(endViewList);
                viewPager.setAdapter(endReaderPageAdapter);
                viewPager.setCurrentItem(pagerIndex);
                pointer++;
                break;
            case 4:
                viewPager.setCurrentItem(pagerIndex);
                break;
        }
    }

    //获取小说编码
    private String getCharset(String filePath){
        String encoding = null;
        try {
            InputStream is = new FileInputStream(filePath);
            byte[] bytes = new byte[10];
            is.read(bytes);
            is.close();
            CharsetDetector detector = new CharsetDetector();
            detector.setText(bytes);
            CharsetMatch match = detector.detect();
            encoding = match.getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encoding;
    }

    private List<String> resultData;

    @Override
    public void call(List<String> resultData) {
        this.resultData = resultData;

        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);
    }

    public Boolean getIsDay() {
        return isDay;
    }

    @Override
    public void dayAndNightModel() {
        if (isDay){
            dayAndNightModelChange();

            isDay = false;
        }
        else {
            view0.setBackgroundColor(Color.parseColor("#ffffff"));
            view1.setBackgroundColor(Color.parseColor("#ffffff"));
            view2.setBackgroundColor(Color.parseColor("#ffffff"));
            view3.setBackgroundColor(Color.parseColor("#ffffff"));
            view4.setBackgroundColor(Color.parseColor("#ffffff"));

            bookContent0.setTextColor("#000000");
            bookContent1.setTextColor("#000000");
            bookContent2.setTextColor("#000000");
            bookContent3.setTextColor("#000000");
            bookContent4.setTextColor("#000000");

            bookInfo0.setTextColor(Color.parseColor("#000000"));
            bookInfo1.setTextColor(Color.parseColor("#000000"));
            bookInfo2.setTextColor(Color.parseColor("#000000"));
            bookInfo3.setTextColor(Color.parseColor("#000000"));
            bookInfo4.setTextColor(Color.parseColor("#000000"));

            bookProcess0.setTextColor(Color.parseColor("#000000"));
            bookProcess1.setTextColor(Color.parseColor("#000000"));
            bookProcess2.setTextColor(Color.parseColor("#000000"));
            bookProcess3.setTextColor(Color.parseColor("#000000"));
            bookProcess4.setTextColor(Color.parseColor("#000000"));

            bookContent0.gettPW().setDayModel();
            bookContent1.gettPW().setDayModel();
            bookContent2.gettPW().setDayModel();
            bookContent3.gettPW().setDayModel();
            bookContent4.gettPW().setDayModel();

            isDay = true;
        }
    }

    private int period;
    private Timer timer;
    private TimerTask timerTask;
    private Boolean isHideTurningMenu;

    public Boolean getHideSeekBar() {
        return isHideSeekBar;
    }

    //判断seekBar的显示与隐藏
    private Boolean isHideSeekBar = true;


    @Override
    public void autoPageTurning() {
        setTurningAndScroll(true);
        isHideTurningMenu = true;

        timer = new Timer();
        timer.schedule(timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isNotEnd){
                    Message message = new Message();
                    message.what = 2;
                    handler.sendMessage(message);
                }
                else {//最后几页
                    if (pagerIndex == endIndex){//最后一页
                        timerCancel();
                        Message message = Message.obtain();
                        message.what = 3;
                        handler.sendMessage(message);
                    }
                    else {
                        Message message = Message.obtain();
                        message.what = 2;
                        handler.sendMessage(message);
                    }
                }
            }
        },period,period);
    }

    @Override
    public void showAndHideTurning() {
        if (isHideTurningMenu) {
            pageTurning.setVisibility(View.VISIBLE);
            isHideTurningMenu = false;
        }
        else {
            pageTurning.setVisibility(View.GONE);
            isHideTurningMenu = true;
        }

    }

    @Override
    public void showAndHideBookProcess() {
        if (isHideSeekBar){
            if (isClickCenter()){
                seekBarMenu.setVisibility(View.VISIBLE);
                isHideSeekBar = false;
            }
        }
        else {
            seekBarMenu.setVisibility(View.GONE);
            isHideSeekBar = true;
        }
    }


    //判断是否点击到屏幕中央
    public Boolean isClickCenter(){
        TextSelectView currentView = textSelectViewList.get(pagerIndex);
        float Down_x = currentView.getDown_X();
        float Down_y = currentView.getDown_Y();
        if (Down_x>width/3&&Down_x< width*2/3&&Down_y>height/3&&Down_y<height*2/3){
            return true;
        }
        else return false;

    }


    //重置定时器
    private void resetTimer(){
        if (timer != null){
            timer.cancel();
            timer = null;

            timerTask.cancel();
            timerTask = null;
        }

        timer = new Timer();
        timer.schedule(timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isNotEnd){
                    Message message = new Message();
                    message.what = 2;
                    handler.sendMessage(message);
                }
                else {
                    if (pagerIndex == endIndex){
                        timerCancel();
                        Message message = new Message();
                        message.what = 3;
                        handler.sendMessage(message);
                    }
                    else {
                        Message message = new Message();
                        message.what = 2;
                        handler.sendMessage(message);
                    }
                }
            }
        },period,period);
    }

    private void setTurningAndScroll(Boolean turningAndScroll){
        bookContent0.setTurning(turningAndScroll);
        bookContent1.setTurning(turningAndScroll);
        bookContent2.setTurning(turningAndScroll);
        bookContent3.setTurning(turningAndScroll);
        bookContent4.setTurning(turningAndScroll);

        bookContent0.setCanNotScroll(turningAndScroll);
        bookContent1.setCanNotScroll(turningAndScroll);
        bookContent2.setCanNotScroll(turningAndScroll);
        bookContent3.setCanNotScroll(turningAndScroll);
        bookContent4.setCanNotScroll(turningAndScroll);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        //隐藏状态栏和导航栏
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1){
            if (resultCode == RESULT_OK){
                String returnData = data.getStringExtra("data_return");
                TextSelectView textSelectView = textSelectViewList.get(pagerIndex);

                currentPointer = pointer - 4 + pagerIndex;
                List<Store> storeList;
                storeList = map.get(currentPointer);
                if (storeList == null){
                    storeList = new ArrayList<>();
                }

                String note;
                note = combineNote(storeList,returnData,textSelectView);

                List<ShowLine> showLineList = getDrawLineData(textSelectView);
                noteStore(note,storeList,showLineList);
                textSelectView.setStoreList(storeList);
                textSelectView.setHasNote(true);
                textSelectView.invalidate();

            }
        }

        if (requestCode == 2){
            if (resultCode == RESULT_OK){
                String returnData = data.getStringExtra("data_return");
                TextSelectView textSelectView = textSelectViewList.get(pagerIndex);

                currentPointer = pointer - 4 + pagerIndex;
                List<Store> storeList;
                storeList = map.get(currentPointer);

                int clickNoteIndex = textSelectView.getClickNoteIndex();
                storeList.get(clickNoteIndex).setNote(returnData);

            }
        }
    }

    private void dayAndNightModelChange(){
            view0.setBackgroundColor(Color.parseColor("#222222"));
            view1.setBackgroundColor(Color.parseColor("#222222"));
            view2.setBackgroundColor(Color.parseColor("#222222"));
            view3.setBackgroundColor(Color.parseColor("#222222"));
            view4.setBackgroundColor(Color.parseColor("#222222"));

            bookContent0.setTextColor("#777777");
            bookContent1.setTextColor("#777777");
            bookContent2.setTextColor("#777777");
            bookContent3.setTextColor("#777777");
            bookContent4.setTextColor("#777777");

            bookInfo0.setTextColor(Color.parseColor("#777777"));
            bookInfo1.setTextColor(Color.parseColor("#777777"));
            bookInfo2.setTextColor(Color.parseColor("#777777"));
            bookInfo3.setTextColor(Color.parseColor("#777777"));
            bookInfo4.setTextColor(Color.parseColor("#777777"));

            bookProcess0.setTextColor(Color.parseColor("#777777"));
            bookProcess1.setTextColor(Color.parseColor("#777777"));
            bookProcess2.setTextColor(Color.parseColor("#777777"));
            bookProcess3.setTextColor(Color.parseColor("#777777"));
            bookProcess4.setTextColor(Color.parseColor("#777777"));

            bookContent0.gettPW().setNightModel();
            bookContent1.gettPW().setNightModel();
            bookContent2.gettPW().setNightModel();
            bookContent3.gettPW().setNightModel();
            bookContent4.gettPW().setNightModel();
    }

    private int currentPointer;
    private Map<Integer,List<Store>> map;

    private void noteStore(String note,List<Store> mStoreList, List<ShowLine> showLineList){
        Store store = new Store();
        store.setNote(note);
        store.setSelectLineList(showLineList);



        mStoreList.add(store);

        map.put(currentPointer,mStoreList);

        //复用最后两页的笔记
        if (pagerIndex == 3){
            storeListRight0 = mStoreList;
        }
        else if (pagerIndex == 4){
            storeListRight1 = mStoreList;
        }

        Gson gson = new Gson();
        String storeNoteMap = gson.toJson(map);

        editor.putString("pageNote",storeNoteMap);
        editor.apply();
    }

    private Map<Integer,List<Store>> getSpData(){
        String jsonData = pre.getString("pageNote",defaultMap());

        Gson gson = new Gson();
        Map<Integer,List<Store>> map = gson.fromJson(jsonData,
                new TypeToken<Map<Integer,List<Store>>>(){}.getType());

        return map;
    }

    private String defaultMap(){
        String defaultData;
        @SuppressLint("UseSparseArrays") Map<Integer,List<Store>> map = new HashMap<>();

        Gson gson = new Gson();
        defaultData = gson.toJson(map);
        return defaultData;
    }

    //将选中的内容重新打包避免使用相同的地址引用
    private List<ShowLine> getDrawLineData(TextSelectView textSelectView){
        List<ShowLine> drawLineData = new ArrayList<>();
        for (ShowLine line:textSelectView.mSelectLines){
            ShowLine showLine = new ShowLine();
            showLine.CharsData = new ArrayList<>();
            for (ShowChar c:line.CharsData){
                ShowChar showChar = new ShowChar();
                showChar.charData = c.charData;
                showChar.BottomLeftPosition = c.BottomLeftPosition;
                showChar.BottomRightPosition = c.BottomRightPosition;
                showChar.TopLeftPosition = c.TopLeftPosition;
                showChar.TopRightPosition = c.TopRightPosition;
                showChar.Index = c.Index;

                showLine.CharsData.add(showChar);
            }
            drawLineData.add(showLine);
        }
        return drawLineData;
    }

    public void delNote(){
        TextSelectView textSelectView = textSelectViewList.get(pagerIndex);

        currentPointer = pointer - 4 + pagerIndex;
        List<Store> storeList;
        storeList = map.get(currentPointer);

        int clickNoteIndex = textSelectView.getClickNoteIndex();
        storeList.remove(clickNoteIndex);

        textSelectView.setStoreList(storeList);
        textSelectView.invalidate();

        //储存删除笔记的storeList
        map.put(currentPointer,storeList);

        //复用最后两页的笔记
        if (pagerIndex == 3){
            storeListRight0 = storeList;
        }
        else if (pagerIndex == 4){
            storeListRight1 = storeList;
        }

        Gson gson = new Gson();
        String storeNoteMap = gson.toJson(map);

        editor.putString("pageNote",storeNoteMap);
        editor.apply();
    }

    //合并笔记
    private String combineNote(List<Store> storeList, String returnNote,
                               TextSelectView textSelectView){
        List<ShowLine> showLines = textSelectView.mSelectLines;
        ShowChar firstSelectChar = new ShowChar();
        for (ShowLine l:showLines){
            if (l.CharsData != null&&l.CharsData.size()>0){
                firstSelectChar = l.CharsData.get(0);
                break;
            }
        }
        ShowLine lastSelectLine = showLines.get(showLines.size()-1);
        ShowChar lastSelectChar = lastSelectLine.CharsData.get(lastSelectLine.CharsData.size()-1);

        String selectNote = returnNote;
        String note;

        for (int i=storeList.size()-1;i>=0;i--){
            Store s = storeList.get(i);
            ShowChar firstNoteChar = new ShowChar();
            for (ShowLine l:s.getSelectLineList()){
                if (l.CharsData != null&&l.CharsData.size()>0){
                    firstNoteChar = l.CharsData.get(0);
                    break;
                }
            }
            ShowLine lastNoteLine = s.getSelectLineList().get(s.getSelectLineList().size()-1);
            ShowChar lastNoteChar = lastNoteLine.CharsData.get(lastNoteLine.CharsData.size()-1);

            //选字第一行小于笔记第一行
            if (firstSelectChar.BottomLeftPosition.y<firstNoteChar.BottomLeftPosition.y){
                //与旧笔记有交集
                if (lastSelectChar.BottomLeftPosition.y>firstNoteChar.BottomLeftPosition.y){
                    if (lastSelectChar.BottomLeftPosition.y == lastNoteChar.BottomLeftPosition.y){
                        if (lastSelectChar.BottomRightPosition.x<lastNoteChar.BottomRightPosition.x){
                            lastSelectChar = lastNoteChar;
                        }
                    }
                    else if (lastSelectChar.BottomLeftPosition.y < lastNoteChar.BottomLeftPosition.y){
                        lastSelectChar = lastNoteChar;
                    }
                    note = s.getNote();
                    selectNote += "，"+note;
                    storeList.remove(i);
                }
                else if (lastSelectChar.BottomLeftPosition.y == firstNoteChar.BottomLeftPosition.y){
                    //与旧笔记有交集
                    if (lastSelectChar.BottomRightPosition.x>firstNoteChar.BottomLeftPosition.x){
                        if (lastSelectChar.BottomRightPosition.x<lastNoteChar.BottomRightPosition.x){
                            lastSelectChar = lastNoteChar;
                        }
                        note = s.getNote();
                        selectNote += "，"+note;
                        storeList.remove(i);
                    }
                }
            }
            //选字第一行等于笔记第一行
            if (firstSelectChar.BottomLeftPosition.y==firstNoteChar.BottomLeftPosition.y){
                if (firstSelectChar.BottomLeftPosition.x<firstNoteChar.BottomLeftPosition.x){
                    if (lastSelectChar.BottomLeftPosition.y>firstNoteChar.BottomLeftPosition.y){
                        if (lastSelectChar.BottomLeftPosition.y == lastNoteChar.BottomLeftPosition.y){
                            if (lastSelectChar.BottomRightPosition.x<lastNoteChar.BottomRightPosition.x){
                                lastSelectChar = lastNoteChar;
                            }
                        }
                        else if (lastSelectChar.BottomLeftPosition.y < lastNoteChar.BottomLeftPosition.y){
                            lastSelectChar = lastNoteChar;
                        }
                        note = s.getNote();
                        selectNote += "，"+note;
                        storeList.remove(i);
                    }
                    else if (lastSelectChar.BottomLeftPosition.y == firstNoteChar.BottomLeftPosition.y){
                        if (lastSelectChar.BottomRightPosition.x>firstNoteChar.BottomLeftPosition.x){
                            if (lastSelectChar.BottomRightPosition.x<lastNoteChar.BottomRightPosition.x){
                                lastSelectChar = lastNoteChar;
                            }
                            note = s.getNote();
                            selectNote += "，"+note;
                            storeList.remove(i);
                        }
                    }
                }
                else if (firstSelectChar.BottomLeftPosition.x>firstNoteChar.BottomLeftPosition.x){
                    //笔记大于一行，有交集
                    if (lastNoteChar.BottomRightPosition.y>firstNoteChar.BottomLeftPosition.y){
                        //最后一行y相等
                        if (lastSelectChar.BottomRightPosition.y==lastNoteChar.BottomRightPosition.y){
                            if (lastSelectChar.BottomRightPosition.x<lastNoteChar.BottomRightPosition.x){
                                firstSelectChar = firstNoteChar;
                                lastSelectChar = lastNoteChar;
                            }
                            if (lastSelectChar.BottomRightPosition.x>lastNoteChar.BottomRightPosition.x){
                                firstSelectChar = firstNoteChar;
                            }
                        }
                        //最后一行小于笔记的最后一行
                        if (lastSelectChar.BottomRightPosition.y<lastNoteChar.BottomRightPosition.y){
                            firstSelectChar = firstNoteChar;
                            lastSelectChar = lastNoteChar;
                        }
                        //最后一行大于笔记的最后一行
                        if (lastSelectChar.BottomRightPosition.y>lastNoteChar.BottomRightPosition.y){
                            firstSelectChar = firstNoteChar;
                        }
                        note = s.getNote();
                        selectNote += "，"+note;
                        storeList.remove(i);
                    }
                    //笔记等于一行
                    else{
                        //首字在笔记中间，有交集
                        if (firstSelectChar.BottomLeftPosition.x<lastNoteChar.BottomRightPosition.x){
                            //选字大于一行
                            if (lastSelectChar.BottomRightPosition.y>lastNoteChar.BottomRightPosition.y){
                                firstSelectChar = firstNoteChar;
                            }
                            //选字等于一行
                            else {
                                if (lastSelectChar.BottomRightPosition.x<lastNoteChar.BottomRightPosition.x){
                                    firstSelectChar = firstNoteChar;
                                    lastSelectChar = lastNoteChar;
                                }
                                if (lastSelectChar.BottomRightPosition.x>lastNoteChar.BottomRightPosition.x){
                                    firstSelectChar = firstNoteChar;
                                }
                            }
                            note = s.getNote();
                            selectNote += "，"+note;
                            storeList.remove(i);
                        }
                    }
                }
            }

            //选字第一行大于笔记第一行
            if (firstSelectChar.BottomLeftPosition.y>firstNoteChar.BottomLeftPosition.y){
                //选字首行小于笔记尾行
                if (firstSelectChar.BottomRightPosition.y<lastNoteChar.BottomRightPosition.y){
                    //选字尾行y小于笔记尾行y
                    if (lastSelectChar.BottomRightPosition.y<lastNoteChar.BottomRightPosition.y){
                        lastSelectChar = lastNoteChar;
                        firstSelectChar = firstNoteChar;
                    }
                    //选字尾行等于笔记尾行
                    if (lastSelectChar.BottomRightPosition.y<lastNoteChar.BottomRightPosition.y){
                        //选字尾行尾字小于笔记尾行尾字
                        if (lastSelectChar.BottomRightPosition.x<lastNoteChar.BottomRightPosition.x){
                            lastSelectChar = lastNoteChar;
                            firstSelectChar = firstNoteChar;
                        }
                        //选字尾行尾字大于等于笔记尾行尾字
                        if (lastSelectChar.BottomRightPosition.x>=lastNoteChar.BottomRightPosition.x){
                            lastSelectChar = lastNoteChar;
                        }
                    }
                    //选字尾行y大于笔记尾行y
                    if (lastSelectChar.BottomRightPosition.y>lastNoteChar.BottomRightPosition.y){
                        firstSelectChar = firstNoteChar;
                    }
                    note = s.getNote();
                    selectNote += "，"+note;
                    storeList.remove(i);
                }
                //选字首行等于笔记尾行
                else if (firstSelectChar.BottomRightPosition.y==lastNoteChar.BottomRightPosition.y){
                    //选字首行首字小于笔记尾行尾字
                    if (firstSelectChar.BottomLeftPosition.x<lastNoteChar.BottomRightPosition.x){
                        //选字尾行y等于笔记尾行y
                        if(lastSelectChar.BottomRightPosition.y==lastNoteChar.BottomRightPosition.y){
                            //选字尾行尾字小于笔记尾行尾字
                            if (lastSelectChar.BottomRightPosition.x<lastNoteChar.BottomRightPosition.x){
                                lastSelectChar = lastNoteChar;
                                firstSelectChar = firstNoteChar;
                            }
                            //选字尾行尾字大于等于笔记尾行尾字
                            if (lastSelectChar.BottomRightPosition.x>=lastNoteChar.BottomRightPosition.x){
                                firstSelectChar = firstNoteChar;
                            }
                        }
                        //选字尾行y大于笔记尾行y
                        else {
                            firstSelectChar = firstNoteChar;
                        }

                        note = s.getNote();
                        selectNote += "，"+note;
                        storeList.remove(i);
                    }
                }

            }
        }
        textSelectView.setFirstSelectShowChar(firstSelectChar);
        textSelectView.setLastSelectShowChar(lastSelectChar);
        textSelectView.getSelectData();

        return selectNote;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("return_data",isDay);
        setResult(RESULT_OK,intent);

        super.onBackPressed();
    }

    //自动翻页菜单的点击事件
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.reduce:
                if (period<=2000)break;
                else {
                    period -= 2000;
                    speed.setText(((period/1000)+"秒"));
                    resetTimer();
                }

                break;
            case R.id.plus:
                if (period>=20000)break;
                else {
                    period += 2000;
                    speed.setText(((period/1000)+"秒"));
                    resetTimer();
                }
                break;
            case R.id.cancel_auto_page_turning:
                pageTurning.setVisibility(View.GONE);
                setTurningAndScroll(false);
                if (timer != null){
                    timer.cancel();
                    timer = null;

                    timerTask.cancel();
                    timerTask = null;
                }
                break;

            default:
        }
    }

    //取消定时器
    private void timerCancel(){
        setTurningAndScroll(false);
        if (timer != null){
            timer.cancel();
            timer = null;

            timerTask.cancel();
            timerTask = null;
        }
    }


    //SeekBar内部方法
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        bookProcess.setText((i+"%"));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
