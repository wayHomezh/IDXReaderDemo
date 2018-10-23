package com.example.geno.idxreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by geno on 20/06/18.
 */

public class SearchResultAdapter extends BaseAdapter {
    private List<String> items;
    private LayoutInflater inflater;

    public SearchResultAdapter(Context context){
        inflater = LayoutInflater.from(context);
    }

    public void setAdapter(List<String> items){
        this.items = items;
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
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = new ViewHolder();

        if (convertView==null){
            convertView = inflater.inflate(R.layout.search_list_item,null);

            viewHolder.textView = convertView.findViewById(R.id.search_text);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.textView.setText(items.get(position));

        return convertView;
    }

    private class ViewHolder{
        TextView textView;
    }
}
