package com.siyanhui.mojif.demo;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dongtu.sdk.widget.DTImageView;
import com.dongtu.store.DongtuStore;
import com.dongtu.store.widget.DTStoreMessageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends BaseAdapter {
    private Context context;
    private List<Message> datas;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    ChatAdapter(Context context, List<Message> datas) {
        this.context = context;
        if (datas == null) {
            datas = new ArrayList<>(0);
        }
        this.datas = datas;
    }

    void refresh(List<Message> datas) {
        if (datas == null) {
            datas = new ArrayList<>(0);
        }
        this.datas = datas;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return datas.get(position).getIsSend() ? 1 : 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final ViewHolder holder;
        final Message data = datas.get(position);
        if (v == null) {
            holder = new ViewHolder();
            if (data.getIsSend()) {
                v = View.inflate(context, R.layout.dtstore_chat_item_right, null);
            } else {
                v = View.inflate(context, R.layout.dtstore_chat_item_left, null);
            }
            holder.img_sendfail = v
                    .findViewById(R.id.chat_item_fail);
            holder.progress = v
                    .findViewById(R.id.chat_item_progress);
            holder.tv_date = v.findViewById(R.id.chat_item_date);
            holder.message = v.findViewById(R.id.chat_item_content_message);
            holder.message.setStickerSize(dip2px(150));
            holder.message.setEmojiSize(dip2px(20));
            holder.message.setUnicodeEmojiSpanSizeRatio(1.5f);//让emoji显示得比一般字符大一点
            holder.dtImageView = v.findViewById(R.id.chat_item_content_dt_image);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.tv_date.setText(simpleDateFormat.format(new Date()));
        holder.tv_date.setVisibility(View.VISIBLE);

        Message.Type dataType = data.getType();
        if (dataType == Message.Type.GIF) {
            holder.message.setVisibility(View.GONE);
            holder.dtImageView.setVisibility(View.VISIBLE);
            DongtuStore.loadImageInto(holder.dtImageView, data.getContent(), data.getImageId(), data.getWidth(), data.getHeight());
        } else {
            holder.dtImageView.setVisibility(View.GONE);
            holder.message.setVisibility(View.VISIBLE);
            if (dataType == Message.Type.STICKER) {
                holder.message.getBackground().setAlpha(0);
                holder.message.showSticker(data.getContent());
            } else {
                holder.message.getBackground().setAlpha(255);
                holder.message.showText(data.getContent());
            }
        }
        return v;
    }

    private int dip2px(float dp) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    static class ViewHolder {
        TextView tv_date;
        ImageView img_sendfail;
        ProgressBar progress;
        DTStoreMessageView message;
        DTImageView dtImageView;
    }
}
