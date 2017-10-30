package com.zdv.reckoning.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zdv.reckoning.R;
import com.zdv.reckoning.bean.DishBean;
import com.zdv.reckoning.utils.Utils;

import java.util.ArrayList;



/**
 * Info: 消息
 * Created by xiaoyl
 * 创建时间:2017/8/07 10:15
 */
public class DishAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    ArrayList<DishBean> items;
    Utils util;

    public DishAdapter(ArrayList<DishBean> items, Context context) {
        this.items = items;
        this.context = context;
        util = Utils.getInstance();

    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int VIEW_TYPE) {

        return new MyViewHolder(LayoutInflater.from(
                context).inflate(R.layout.item_dish, viewGroup,
                false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder_, int i) {
        MyViewHolder holder = (MyViewHolder) holder_;
        DishBean item = items.get(i);



        holder.item_dish_name.setText(item.getSpfl());

        holder.item_dish_price.setText(item.getDj());
        holder.item_dish_count.setText(item.getSl());
        holder.item_dish_total.setText(Double.parseDouble(item.getSl())*Double.parseDouble(item.getDj())+"");
        if(Double.parseDouble(item.getZk()) ==1) {
            holder.item_dish_discount_tv.setText("");
        }else{
            holder.item_dish_discount_tv.setText(item.getZk());
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView item_dish_name, item_dish_price, item_dish_count, item_dish_discount_tv,item_dish_total;

        LinearLayout item_dish_discount_lay;
        public MyViewHolder(View view) {
            super(view);
            item_dish_name = (TextView) view.findViewById(R.id.item_dish_name);
            item_dish_price = (TextView) view.findViewById(R.id.item_dish_price);
            item_dish_count = (TextView) view.findViewById(R.id.item_dish_count);
            item_dish_total = (TextView) view.findViewById(R.id.item_dish_total);
            item_dish_discount_tv = (TextView) view.findViewById(R.id.item_dish_discount_tv);

            item_dish_discount_lay = (LinearLayout) view.findViewById(R.id.item_dish_discount_lay);
        }
    }


}
