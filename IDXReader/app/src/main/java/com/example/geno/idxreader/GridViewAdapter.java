package com.example.geno.idxreader;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by geno on 18/04/18.
 */

public class GridViewAdapter extends BaseAdapter{
    private String[] bookNames;
    private LayoutInflater mInflater;
    private Context context;
    private boolean isShow;
    private boolean isCheckAll;
    private static final String TAG = "GridViewAdapter";

    public GridViewAdapter(Context mContext){
        context = mContext;
        mInflater = LayoutInflater.from(context);
    }

    public void setGridAdapter(String[] names,boolean show,boolean check){
        bookNames = names;
        isShow = show;
        isCheckAll = check;
    }

    @Override
    public int getCount() {
        return bookNames.length;
    }

    @Override
    public Object getItem(int position) {
        return bookNames[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if(convertView == null){
            Log.i(TAG, "getView: 空的进来了");
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.gridview_item,null);
            viewHolder.imageView = convertView.findViewById(R.id.img);
            viewHolder.textView = convertView.findViewById(R.id.text);
            viewHolder.checkBox = convertView.findViewById(R.id.check_box);
            convertView.setTag(viewHolder);
        }
        else {
            Log.i(TAG, "getView: 被复用了");
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if(isShow == true){
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            if (isCheckAll == true) viewHolder.checkBox.setChecked(true);
        }
        else viewHolder.checkBox.setVisibility(View.GONE);
        if (isCheckAll == false) viewHolder.checkBox.setChecked(false);
        viewHolder.imageView.setImageResource(R.drawable.cover);
        viewHolder.textView.setText(bookNames[position]);
        return convertView;
    }

    private class ViewHolder{
        ImageView imageView;
        TextView textView;
        CheckBox checkBox;
    }
}
