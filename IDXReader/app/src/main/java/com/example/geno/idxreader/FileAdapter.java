package com.example.geno.idxreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * Created by geno on 11/04/18.
 */

public class FileAdapter extends BaseAdapter {
    private List<String> items;
    private List<String> paths;
    private LayoutInflater mInflater;
    private Context context;
    private String name;
    private String path;
    private SharedPreferences pre;
    private SharedPreferences.Editor editor;
    //新添加的书名和路径
    private List<String> newNameList = new ArrayList<>();
    private List<String> newPathList = new ArrayList<>();

    private String[] nameList;

    //构造函数
    public FileAdapter(Context mContext){
        context = mContext;
        mInflater = LayoutInflater.from(context);

        getBookPathAndBookName();
        nameList = name.split(",");
    }

    public void setAdapter(List<String> item_list,List<String> path_list){
        items = item_list;
        paths = path_list;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_item,null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = convertView.findViewById(R.id.image_item);
            viewHolder.textView = convertView.findViewById(R.id.text_item);
            viewHolder.button = convertView.findViewById(R.id.button_add);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
            if(convertView.getVisibility()==View.VISIBLE){
                viewHolder.button.setVisibility(View.GONE);
            }
        }
        final File file = new File(paths.get(position));
        final String fileName = file.getName();
        final String filePath = file.getPath();
        Boolean hasNotAdd;
        if(file.isDirectory())//file是文件夹
            viewHolder.imageView.setImageResource(R.drawable.folder_icon);
        else{//file是txt文件

            viewHolder.imageView.setImageResource(R.drawable.txt);
            viewHolder.button.setVisibility(View.VISIBLE);
            oldButtonStyle(viewHolder.button);

            hasNotAdd = true;

            //对比data文件中txt文件名是否已添加
            for(int i = 0;i<nameList.length;i++){
                if(nameList[i].equals(fileName)){//已经添加此小说
                    buttonHasAdd(viewHolder.button);
                    hasNotAdd = false;
                    break;
                }
            }

            if (newNameList!=null){
                //对比新添加的书名中txt文件是否已添加
                for (String s:newNameList){
                    if (s.equals(fileName)){
                        buttonJustAdd(viewHolder.button);
                        break;
                    }
                }
            }

            if(hasNotAdd){//没有添加此小说
                viewHolder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {//点击添加
                        if (viewHolder.button.getText() == "添加"){//未添加
                            //remove后name可能为null
                            if(!"".equals(name)&&name!=null){//不是第一个新添加的小说
                                newNameList.add(fileName);
                                newPathList.add(filePath);
                                name += ","+fileName;
                                path += ","+filePath;

                                editor.putString("name",name);
                                editor.putString("path",path);
                                editor.apply();

                                buttonJustAdd(viewHolder.button);
                            }
                            else {//第一个新添加的小说
                                newNameList.add(fileName);
                                newPathList.add(filePath);
                                name = fileName;
                                path = filePath;

                                editor.putString("name",name);
                                editor.putString("path",path);
                                editor.apply();

                                buttonJustAdd(viewHolder.button);
                            }
                        }
                        else {//刚添加
                            //删除点击的item的新添加的书名和路径
                            newNameList.remove(fileName);
                            newPathList.remove(filePath);

                            //删除点击的item的书名和路径
                            String[] nameArr = name.split(",");
                            String[] pathArr = path.split(",");
                            List<String> List1 = Arrays.asList(nameArr);
                            List<String> List2 = Arrays.asList(pathArr);
                            List<String> nameListTemp = new ArrayList<>(List1);
                            List<String> pathListTemp = new ArrayList<>(List2);

                            //移除书名和路径
                            nameListTemp.remove(fileName);
                            pathListTemp.remove(filePath);

                            nameArr = nameListTemp.toArray(new String[nameListTemp.size()]);
                            pathArr = pathListTemp.toArray(new String[nameListTemp.size()]);

                            name = bookNameToString(nameArr,pathArr)[0];
                            path = bookNameToString(nameArr,pathArr)[1];

                            editor.putString("name",name);
                            editor.putString("path",path);
                            editor.apply();

                            oldButtonStyle(viewHolder.button);
                        }
                    }
                });
            }
        }
        viewHolder.textView.setText(fileName);
        return convertView;
    }

    //获取已添加的书及其路径
    private void getBookPathAndBookName(){
        pre = context.getSharedPreferences("data",
                Context.MODE_PRIVATE);
        editor = pre.edit();
        name = pre.getString("name","");
        path = pre.getString("path","");
    }

    private String[] bookNameToString(String[] arrBookNames,String[] arrBookPaths){
        String[] bookNameString = new String[2];
        if (arrBookNames != null&&arrBookNames.length != 0){
            bookNameString[0] = arrBookNames[0];
            bookNameString[1] = arrBookPaths[0];
            for (int i = 1;i<arrBookNames.length;i++){
                bookNameString[0] += ","+arrBookNames[i];
                bookNameString[1] += ","+arrBookPaths[i];
            }
        }
        return bookNameString;
    }

    private class ViewHolder{
        ImageView imageView;
        TextView textView;
        Button button;
    }
    //已添加书后的button布局
    private void buttonHasAdd(Button button){
        button.setClickable(false);
        button.setBackgroundColor(0xffffffff);
        button.setTextColor(0xffaaaaaa);
        button.setText("已添加");
    }

    //恢复button原来的布局
    private void oldButtonStyle(Button button){
        button.setText("添加");
        button.setTextColor(0xffffffff);
        button.setBackgroundColor(0xff473573);
        button.setClickable(true);
    }

    //刚添加书后的button布局
    private void buttonJustAdd(Button button){
        button.setBackgroundColor(0xff2abcd2);
        button.setTextColor(0xffffffff);
        button.setText("取消");
    }
}
