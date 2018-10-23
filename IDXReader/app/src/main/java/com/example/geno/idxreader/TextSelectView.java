package com.example.geno.idxreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by geno on 15/05/18.
 */

public class TextSelectView extends View implements CallBack {
    private String textData = "";

    public void setTextData(String text) {
        textData = text;
    }


    public TextSelectView(Context context) {
        super(context);
        init();
    }

    public TextSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private Paint mPaint = null;
    private Paint mTextSelectPaint = null;
    private Paint mBorderPointPaint = null;
    private Paint mUnderlinePaint = null;
    private int textSelectColor = Color.parseColor("#77fa8908");
    private int borderPointColor = Color.RED;

    private void init() {

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(25);

        mTextSelectPaint = new Paint();
        mTextSelectPaint.setAntiAlias(true);
        mTextSelectPaint.setTextSize(19);
        mTextSelectPaint.setColor(textSelectColor);

        mBorderPointPaint = new Paint();
        mBorderPointPaint.setAntiAlias(true);
        mBorderPointPaint.setTextSize(19);
        mBorderPointPaint.setColor(borderPointColor);

        mUnderlinePaint = new Paint();
        mUnderlinePaint.setAntiAlias(true);
        mUnderlinePaint.setStyle(Paint.Style.STROKE);
        mUnderlinePaint.setColor(Color.parseColor("#EE7600"));

        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        TextHeight = Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent);
        setOnLongClickListener(mLongClickListener);

    }

    public void setTextColor(String color){
        mPaint.setColor(Color.parseColor(color));
        invalidate();
    }

    private Context context = getContext();

    public TextPopupWindow gettPW() {
        return tPW;
    }

    private TextPopupWindow tPW = new TextPopupWindow(context,this);

    private DeleteNotePopupWindow delPWindow= new DeleteNotePopupWindow(context,this);



    private OnLongClickListener mLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if (!isTurning){
                if (ReaderActivity.readerActivity.getHideSeekBar()){
                    if (mCurrentMode == Mode.Normal) {
                        if (isClickNote){
                            showDelPWindow(Down_X,Down_Y);
                        }

                        else {
                            if (detectPressChar(Down_X,Down_Y) != null){
                                if (Down_X > 0 && Down_Y > 0) {//长按事件未释放
                                    canNotScroll = true;
                                    mCurrentMode = Mode.PressSelectText;
                                    postInvalidate();
                                }
                            }
                        }

                    }
                }

            }

            return true;
        }
    };


    //设置popupWindow的坐标点
     private void setTextPopupWindowPosition(ShowChar firstShowChar, ShowChar lastShowChar,
                                                 int popupWidth, int popupHeight) {

        popupPosition.x = lastShowChar.BottomRightPosition.x - Math.abs(lastShowChar.BottomRightPosition.x
                - firstShowChar.BottomLeftPosition.x) / 2 - popupWidth / 2;
        popupPosition.y = lastShowChar.BottomLeftPosition.y + 90;

        if (popupPosition.y + popupHeight > vHeight) {
            popupPosition.y = firstShowChar.TopLeftPosition.y - popupHeight;
        }

        if (popupPosition.x + popupWidth > vWidth) {
            popupPosition.x = vWidth - popupWidth -5;
        } else if (popupPosition.x < 10) {
            popupPosition.x = 10;
        }
    }

    //设置notePopupWindow的坐标点
    private void setNotePopupWindowPosition(int downX,int downY,int popupWidth,
                                            int popupHeight){
        popupPosition.x = downX - popupWidth / 2;
        popupPosition.y = downY - popupHeight - 10;

        if (popupPosition.y < 0) {
            popupPosition.y = downY +85;
        }

        if (popupPosition.x + popupWidth > vWidth) {
            popupPosition.x = vWidth - popupWidth -5;
        } else if (popupPosition.x < 10) {
            popupPosition.x = 10;
        }
    }

    @Override
    public void call() {
        tPW.popupWindow.dismiss();
        mCurrentMode = Mode.Normal;
        canNotScroll = false;
        invalidate();
    }

    @Override
    public void noteCall() {
        notePopupWindow.popupWindow.dismiss();
    }

    @Override
    public void deleNoteCall() {
        delPWindow.popupWindow.dismiss();
    }


    public void setTurning(Boolean turning) {
        isTurning = turning;
    }

    private Boolean isTurning = false;

    private class PopupPosition {
        private int x = 0;
        private int y = 0;
    }




    private Mode mCurrentMode = Mode.Normal;

    public float getDown_X() {
        return Down_X;
    }

    public float getDown_Y() {
        return Down_Y;
    }

    private float Down_X = -1, Down_Y = -1;
    private Boolean isClickNote = false;
    private Boolean canShowBookProcess = true;
    private int clickNoteIndex;


    private ChildCallBack childCallBack = ReaderActivity.readerActivity;

    public int getClickNoteIndex(){
        return clickNoteIndex;
    }

    public boolean onTouchEvent(MotionEvent event) {
        float Touch_X = event.getX();
        float Touch_Y = event.getY();

        if (isTurning){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    childCallBack.showAndHideTurning();
                    break;
                default:
            }
        }
        else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Down_X = Touch_X;
                    Down_Y = Touch_Y;

                    if (mCurrentMode != Mode.Normal){
                        Boolean isTrySelectMove = CheckedIfTrySelectMove(Down_X, Down_Y);

                        if (!isTrySelectMove) {//如果不是滑动选择文字，转变为正常模式，隐藏选择框
                            mCurrentMode = Mode.Normal;
                            invalidate();

                            canNotScroll = false;
                            isClickNote = false;
                            tPW.popupWindow.dismiss();
                            canShowBookProcess = false;
                        }
                    }

                    notePopupWindow.popupWindow.dismiss();
                    delPWindow.popupWindow.dismiss();

                    //判断是否点击到笔记
                    if (storeList.size() != 0&&storeList != null){
                        int i = 0;
                        for (Store s:storeList){
                            //
                            noteClick(s.getSelectLineList(),s.getNote(),Down_X,Down_Y);
                            if (isClickNote){
                                clickNoteIndex = i;
                                break;
                            }

                            i++;
                        }

                    }

                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mCurrentMode == Mode.SelectMoveForward) {
                        if (canMoveForward(event.getX(), event.getY())) {//判断是否向上移动
                            ShowChar firstSelectChar = detectPressChar(event.getX(), event.getY());
                            if (firstSelectChar != null) {
                                firstSelectShowChar = firstSelectChar;
                                invalidate();
                            }
                        }
                    } else if (mCurrentMode == Mode.SelectMoveBack) {
                        if (canMoveBack(event.getX(), event.getY())) {//判断是否向下移动
                            ShowChar lastSelectChar = detectPressChar(event.getX(), event.getY());
                            if (lastSelectChar != null) {
                                lastSelectShowChar = lastSelectChar;
                                invalidate();
                            }
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (mCurrentMode == Mode.Normal&&canShowBookProcess&&!isClickNote){
                        childCallBack.showAndHideBookProcess();
                    }
                    canShowBookProcess = true;
                    isClickNote = false;
                    release();
                    break;

                default:
                    break;
            }
        }


        return super.onTouchEvent(event);
    }

    //判断是否向下滑动
    private Boolean canMoveBack(float touchX, float touchY) {
        Path path = new Path();
        path.moveTo(firstSelectShowChar.TopLeftPosition.x,
                firstSelectShowChar.TopLeftPosition.y);
        path.lineTo(getWidth(), firstSelectShowChar.TopLeftPosition.y);
        path.lineTo(getWidth(), getHeight());
        path.lineTo(0, getHeight());
        path.lineTo(0, firstSelectShowChar.BottomLeftPosition.y);
        path.lineTo(firstSelectShowChar.BottomLeftPosition.x,
                firstSelectShowChar.BottomLeftPosition.y);
        path.lineTo(firstSelectShowChar.TopLeftPosition.x,
                firstSelectShowChar.TopLeftPosition.y);

        return computeRegion(path).contains((int) touchX, (int) touchY);
    }


    //判断是否向上滑动
    private Boolean canMoveForward(float touchX, float touchY) {
        Path path = new Path();
        path.moveTo(lastSelectShowChar.TopRightPosition.x,
                lastSelectShowChar.TopRightPosition.y);
        path.lineTo(getWidth(), lastSelectShowChar.TopRightPosition.y);
        path.lineTo(getWidth(), 0);
        path.lineTo(0, 0);
        path.lineTo(0, lastSelectShowChar.BottomRightPosition.y);
        path.lineTo(lastSelectShowChar.BottomRightPosition.x,
                lastSelectShowChar.BottomRightPosition.y);
        path.lineTo(lastSelectShowChar.TopRightPosition.x,
                lastSelectShowChar.TopRightPosition.y);

        return computeRegion(path).contains((int) touchX, (int) touchY);
    }

    //按压释放
    private void release() {
        Down_X = -1;
        Down_Y = -1;
    }


    //计算path所在的区域
    private Region computeRegion(Path path) {
        Region region = new Region();
        RectF f = new RectF();
        path.computeBounds(f, true);

        region.setPath(path, new Region((int) f.left, (int) f.top, (int) f.right, (int) f.bottom));
        return region;
    }

    private List<ShowLine> BreakText(int viewWidth, int viewHeight) {
        List<ShowLine> showLines = new ArrayList<>();
        String data = textData;
        while (data.length() > 0) {
            BreakResult breakResult = BreakResultUtil.BreakText(data,
                    viewWidth, 0, mPaint);
            if (breakResult != null && breakResult.hasData()) {
                ShowLine showLine = new ShowLine();
                showLine.CharsData = breakResult.showChars;//一行的数据字符集合
                showLines.add(showLine);
            } else break;
            data = data.substring(breakResult.charNums);
        }

        int index = 0;
        for (ShowLine l : showLines) {
            for (ShowChar c : l.CharsData) {
                c.Index = index++;
            }
        }
        return showLines;
    }

    public List<ShowLine> mLineData = null;

    public void initData(int viewWidth, int viewHeight) {
        mLineData = BreakText(viewWidth, viewHeight);
    }

    //检测当前按压位置是否有字符，没有返回null
    private ShowChar detectPressChar(float down_X1, float down_Y1) {
        for (ShowLine l : mLineData) {
            int i = 0;
            for (ShowChar c : l.CharsData) {
                i++;
                if (down_Y1 > c.BottomLeftPosition.y+LinePadding) break;//说明在下一行
                if (down_X1 >= c.BottomLeftPosition.x && down_X1 <= c.BottomRightPosition.x){
                    return c;
                }
                else if (i == l.CharsData.size()){
                    return null;
                }
            }
        }
        return null;
    }


    public enum Mode {
        Normal,//正常模式
        PressSelectText,//长按选中文字
        SelectMoveForward,//向前滑动选中文字
        SelectMoveBack,//向后滑动选中文字
    }

    //检测是否滑动选择文字
    private Boolean CheckedIfTrySelectMove(float xPosition, float yPosition) {
        if (firstSelectShowChar == null && lastSelectShowChar == null) return false;
        float flx, fty, frx, fby;
        float hPadding = firstSelectShowChar.charWidth;
        hPadding = hPadding < 10 ? 10 : hPadding;

        flx = firstSelectShowChar.TopLeftPosition.x - hPadding * 2;
        frx = firstSelectShowChar.TopLeftPosition.x + hPadding / 2;
        fty = firstSelectShowChar.TopLeftPosition.y - TextHeight;
        fby = firstSelectShowChar.BottomLeftPosition.y + TextHeight;

        float llx, lty, lrx, lby;
        llx = lastSelectShowChar.BottomRightPosition.x - hPadding / 2;
        lrx = lastSelectShowChar.BottomRightPosition.x + hPadding * 2;
        lty = lastSelectShowChar.TopRightPosition.y - TextHeight;
        lby = lastSelectShowChar.BottomRightPosition.y + TextHeight;

        if ((xPosition >= flx && xPosition <= frx) && (yPosition >= fty && yPosition <= fby)) {
            mCurrentMode = Mode.SelectMoveForward;
            return true;
        }

        if ((xPosition >= llx && xPosition <= lrx) && (yPosition >= lty && yPosition <= lby)) {
            mCurrentMode = Mode.SelectMoveBack;
            return true;
        }
        return false;
    }

    private Path mSelectTextPath = new Path();

    public void setFirstSelectShowChar(ShowChar firstSelectShowChar) {
        this.firstSelectShowChar = firstSelectShowChar;
    }

    private ShowChar firstSelectShowChar = null;

    public void setLastSelectShowChar(ShowChar lastSelectShowChar) {
        this.lastSelectShowChar = lastSelectShowChar;
    }

    private ShowChar lastSelectShowChar = null;

    private float LineYPosition = 0;
    public float TextHeight = 0;


    //绘制文字
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        LineYPosition = TextHeight + LinePadding;

        if (!"".equals(textData)) {
            for (ShowLine l : mLineData) {
                DrawLineText(l, canvas);
            }

            if (hasNote == true){
                drawNote(canvas);
            }

            if (mCurrentMode != Mode.Normal) {
                drawSelectText(canvas);
            }
        }
    }


    private void drawSelectText(Canvas canvas) {
        if (mCurrentMode == Mode.PressSelectText) {
            drawPressSelectText(canvas);
        } else if (mCurrentMode == Mode.SelectMoveForward) {
            drawMoveSelectText(canvas);

        } else if (mCurrentMode == Mode.SelectMoveBack) {
            drawMoveSelectText(canvas);
        }
    }

    public List<ShowLine> mSelectLines = new ArrayList<>();
    private PopupPosition popupPosition = new PopupPosition();

    private void drawMoveSelectText(Canvas canvas) {
        if (firstSelectShowChar == null || lastSelectShowChar == null) {
            return;
        }
        getSelectData();
        drawSelectLines(canvas);
        drawBorderPoint(canvas);

        tPW.setSelectTextData(selectText);
        tPW.setText();

        //拖动选字光标更新popupWindow的位置
        tPW.setPopupWidthAndHeight();
        setTextPopupWindowPosition(firstSelectShowChar,
                lastSelectShowChar, tPW.popupWidth, tPW.popupHeight);
        tPW.popupWindow.update(popupPosition.x, popupPosition.y, -1, -1, true);
    }

    private void drawSelectLines(Canvas canvas) {
        drawSelectLinesBg(canvas);
    }

    private void drawSelectLinesBg(Canvas canvas) {
        for (ShowLine line : mSelectLines) {
            if (line.CharsData != null && line.CharsData.size() > 0) {
                ShowChar firstChar = line.CharsData.get(0);
                ShowChar lastChar = line.CharsData.get(line.CharsData.size() - 1);

                RectF rectF = new RectF(firstChar.TopLeftPosition.x, firstChar.TopLeftPosition.y,
                        lastChar.BottomRightPosition.x, lastChar.BottomRightPosition.y);

                canvas.drawRect(rectF, mTextSelectPaint);
            }
        }
    }

    //选中的内容
    private StringBuilder selectText = new StringBuilder();

    public void getSelectData() {
        Boolean started = false;
        Boolean ended = false;

        mSelectLines.clear();

        if (selectText != null) selectText.delete(0,selectText.length());

        //找到选择的字符数据，转化为选择的行，然后将选择的行画出来
        for (ShowLine line : mLineData) {
            ShowLine selectLine = new ShowLine();
            selectLine.CharsData = new ArrayList<>();

            for (ShowChar c : line.CharsData) {
                if (!started) {
                    if (c.Index == firstSelectShowChar.Index) {
                        started = true;
                        selectLine.CharsData.add(c);
                        selectText.append(c.charData);
                        if (c.Index == lastSelectShowChar.Index) {
                            ended = true;
                            break;
                        }
                    }
                }
                else {
                    if (c.Index == lastSelectShowChar.Index) {
                        ended = true;
                        if (!selectLine.CharsData.contains(c)) {
                            selectLine.CharsData.add(c);
                            selectText.append(c.charData);
                        }
                        break;
                    }
                    else {
                        selectLine.CharsData.add(c);
                        selectText.append(c.charData);
                    }
                }
            }
            mSelectLines.add(selectLine);

            if (started && ended) break;
        }
    }

    //画按压后选择的第一个字
    private void drawPressSelectText(Canvas canvas) {
        ShowChar p = detectPressChar(Down_X, Down_Y);
        ShowLine selectLine = new ShowLine();
        selectLine.CharsData = new ArrayList<>();
        selectLine.CharsData.add(p);
        mSelectLines.clear();
        mSelectLines.add(selectLine);

        if (selectText != null){
            selectText.delete(0,selectText.length());
        }

        if (p != null) {
            firstSelectShowChar = lastSelectShowChar = p;
            mSelectTextPath.reset();
            mSelectTextPath.moveTo(p.TopLeftPosition.x, p.TopLeftPosition.y);
            mSelectTextPath.moveTo(p.TopRightPosition.x, p.TopRightPosition.y);
            mSelectTextPath.moveTo(p.BottomRightPosition.x, p.BottomRightPosition.y);
            mSelectTextPath.moveTo(p.BottomLeftPosition.x, p.BottomLeftPosition.y);

            canvas.drawPath(mSelectTextPath, mTextSelectPaint);
            drawBorderPoint(canvas);

            //显示popupWindow弹窗
            tPW.setPopupWidthAndHeight();
            setTextPopupWindowPosition(firstSelectShowChar,
                    lastSelectShowChar, tPW.popupWidth, tPW.popupHeight);
            selectText.append(p.charData);
            tPW.setPopupWindow(popupPosition.x, popupPosition.y);
            tPW.setSelectTextData(selectText);
            tPW.setText();
            tPW.setResultData(new ArrayList<String>());
            tPW.showPopupWindow();
        }
    }

    //画选文字的两个游标
    private void drawBorderPoint(Canvas canvas) {
        if (firstSelectShowChar != null && lastSelectShowChar != null) {
            drawPoint(canvas);
        }
    }

    private float borderPointRadius = 10;

    //画选文字的两个游标
    private void drawPoint(Canvas canvas) {

        canvas.drawCircle(firstSelectShowChar.TopLeftPosition.x,
                firstSelectShowChar.TopLeftPosition.y, borderPointRadius, mBorderPointPaint);
        canvas.drawCircle(lastSelectShowChar.BottomRightPosition.x ,
                lastSelectShowChar.BottomRightPosition.y, borderPointRadius, mBorderPointPaint);

        canvas.drawLine(firstSelectShowChar.TopLeftPosition.x, firstSelectShowChar.
                TopLeftPosition.y, firstSelectShowChar.
                BottomLeftPosition.x, firstSelectShowChar.BottomLeftPosition.y,
                mBorderPointPaint);
        canvas.drawLine(lastSelectShowChar.TopRightPosition.x, lastSelectShowChar.
                TopRightPosition.y, lastSelectShowChar.
                BottomRightPosition.x, lastSelectShowChar.BottomRightPosition.y,
                mBorderPointPaint);
    }

    //绘制一行文本
    private void DrawLineText(ShowLine line, Canvas canvas) {
        canvas.drawText(line.getLineData(), 0, LineYPosition, mPaint);

        float leftposition = 0;
        float rightposition ;
        float bottomposition = LineYPosition + mPaint.getFontMetrics().descent;

        for (ShowChar c : line.CharsData) {
            rightposition = leftposition + c.charWidth;
            Point tlp = new Point();
            c.TopLeftPosition = tlp;
            tlp.x = (int) leftposition;
            tlp.y = (int) (bottomposition - TextHeight);

            Point blp = new Point();
            c.BottomLeftPosition = blp;
            blp.x = (int) leftposition;
            blp.y = (int) bottomposition;

            Point trp = new Point();
            c.TopRightPosition = trp;
            trp.x = (int) rightposition;
            trp.y = (int) (bottomposition - TextHeight);

            Point brp = new Point();
            c.BottomRightPosition = brp;
            brp.x = (int) rightposition;
            brp.y = (int) bottomposition;

            leftposition = rightposition;

        }
        LineYPosition = LineYPosition + TextHeight + LinePadding;
    }

    public int vWidth = 0, vHeight = 0;
    public float textWidth = 25;
    public int LinePadding = 30;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        vWidth = getMeasuredWidth();
        vHeight = getMeasuredHeight();
    }

    public void setCanNotScroll(Boolean canNotScroll) {
        this.canNotScroll = canNotScroll;
    }

    private Boolean canNotScroll = false;

    //禁止viewPage滑动
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(canNotScroll);
        return super.dispatchTouchEvent(event);
    }

    public void setHasNote(Boolean hasNote) {
        this.hasNote = hasNote;
    }

    private Boolean hasNote = false;


    private void drawNote(Canvas canvas){
        //画以前添加的笔记
        if (storeList.size() != 0&&storeList != null){
            for (Store s:storeList){
                //给添加笔记的文本画下划线
                drawNoteUnderline(s.getSelectLineList(),canvas);
            }

        }
    }

    @SuppressLint("UseSparseArrays")
    private List<Store> storeList = new ArrayList<>();


    public void setStoreList(List<Store> storeList){
        this.storeList = storeList;
    }

    //给添加笔记的文本画下划线
    private void drawNoteUnderline(List<ShowLine> mSelectNoteLines,Canvas canvas){
        ShowChar firstChar;
        ShowChar lastChar;
        Path path;
        for (ShowLine line : mSelectNoteLines) {
            if (line.CharsData != null && line.CharsData.size() > 0) {
                firstChar = line.CharsData.get(0);
                lastChar = line.CharsData.get(line.CharsData.size() - 1);

                path = new Path();
                path.moveTo(firstChar.BottomLeftPosition.x,firstChar.BottomLeftPosition.y);
                path.lineTo(lastChar.BottomRightPosition.x,lastChar.BottomRightPosition.y);

                canvas.drawPath(path,mUnderlinePaint);
            }
        }
    }

    //给添加笔记的文本画点击区域添加点击事件
    private void noteClick(List<ShowLine> mSelectNoteLines,String note,float downX,float downY){
        Path path = new Path();
        ShowChar firstChar;
        ShowChar lastChar;
        RectF rectF;
        String selectData = "";
        for (ShowLine line : mSelectNoteLines) {
            if (line.CharsData != null && line.CharsData.size() > 0) {
                firstChar = line.CharsData.get(0);
                lastChar = line.CharsData.get(line.CharsData.size() - 1);

                rectF = new RectF(firstChar.TopLeftPosition.x-5, firstChar.TopLeftPosition.y,
                        lastChar.BottomRightPosition.x+5, lastChar.BottomRightPosition.y+8);

                path.addRect(rectF,Path.Direction.CW);
            }
            selectData += line.getLineData();
        }
        if (computeRegion(path).contains((int) downX,(int) downY)){
            notePopupWindow.setData(note,selectData);
            notePopupWindow.setText();
            notePopupWindow.refreshWidthAndHeight();
            setNotePopupWindowPosition((int) downX,(int) downY,notePopupWindow.popupWidth,
                    notePopupWindow.popupHeight);
            notePopupWindow.setNotePopupWindowPosition(popupPosition.x,popupPosition.y);
            notePopupWindow.showPopupWindow();

            isClickNote = true;
        }
    }

    private NotePopupWindow notePopupWindow = new NotePopupWindow(context,this);

    //弹出删除笔记窗口
    private void showDelPWindow(float downX,float downY){
        notePopupWindow.popupWindow.dismiss();

        setNotePopupWindowPosition((int) downX,(int) downY,delPWindow.popupWidth,
                delPWindow.popupHeight);
        delPWindow.setDeletePopupWindowPosition(popupPosition.x,popupPosition.y);
        delPWindow.showPopupWindow();
    }

}
