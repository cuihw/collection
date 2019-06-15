package com.data.collection.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.data.collection.R;
import com.data.collection.module.CollectType;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class PointTypeAdapter extends BaseAdapter {
    private Context ctx;
    private LayoutInflater li;
    private List<CollectType> dataList;

    public PointTypeAdapter (Context context, List<CollectType> list){
        ctx = context;
        li = LayoutInflater.from(ctx);
        dataList = list;
    }

    @Override
    public int getCount() {
        return dataList == null ? 0:dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList == null ? null:
                (dataList.size() > position)?
                        dataList.get(position): null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(ctx, R.layout.item_type_point, null);
            ViewHolder viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();// get convertView's holder

        ImageLoader.getInstance().displayImage(dataList.get(position).getIcon(),holder.icon);
        holder.type_name.setText(dataList.get(position).getName());
        return convertView;
    }

    public void setSelected(CollectType collectType) {
        // dataList

    }

    class ViewHolder {
        ImageView icon;
        TextView type_name;

        public ViewHolder(View convertView){
            icon =   convertView.findViewById(R.id.icon);
            type_name =  convertView.findViewById(R.id.type_name);
        }
    }
}
