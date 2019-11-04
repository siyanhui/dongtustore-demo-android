package com.siyanhui.mojif.demo;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dongtu.sdk.visible.DTOutcomeListener;
import com.dongtu.sdk.widget.DTImageView;
import com.dongtu.store.DongtuStore;
import com.dongtu.store.visible.callback.CollectionExistsCallback;
import com.dongtu.store.widget.DTStoreMessageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends BaseAdapter {
    private final Context mContext;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private List<Message> mMessages;

    ChatAdapter(Context context, List<Message> messages) {
        this.mContext = context;
        if (messages == null) {
            mMessages = new ArrayList<>(0);
        } else {
            mMessages = messages;
        }
    }

    void refresh(List<Message> messages) {
        if (messages == null) {
            mMessages = new ArrayList<>(0);
        } else {
            mMessages = messages;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).getIsSend() ? 1 : 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final ViewHolder holder;
        final Message message = mMessages.get(position);
        if (v == null) {
            holder = new ViewHolder();

            //示例：给发送的和接收的消息框应用不同style
            if (message.getIsSend()) {
                v = View.inflate(mContext, R.layout.dtstore_chat_item_right, null);
                holder.messageView = new DTStoreMessageView(mContext, R.style.DTStoreMessageViewSent);
            } else {
                v = View.inflate(mContext, R.layout.dtstore_chat_item_left, null);
                holder.messageView = new DTStoreMessageView(mContext, R.style.DTStoreMessageViewReceived);
            }

            //DTStoreMessageView可以设置OnClickListener
            holder.messageView.setOnClickListener(view -> Log.i("DTStore", "Message clicked."));
            holder.messageView.setStickerSize(dp150());
            //让emoji显示得比一般字符大一点
            holder.messageView.setUnicodeEmojiSpanSizeRatio(1.5f);

            holder.progress = v.findViewById(R.id.chat_item_progress);
            holder.tv_date = v.findViewById(R.id.chat_item_date);
            holder.messageContainer = v.findViewById(R.id.chat_item_content_message);
            holder.messageContainer.addView(holder.messageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            holder.dtImageView = v.findViewById(R.id.chat_item_content_dt_image);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.tv_date.setText(simpleDateFormat.format(new Date()));
        holder.tv_date.setVisibility(View.VISIBLE);
        Message.Type dataType = message.getType();
        if (dataType == Message.Type.GIF) {
            holder.messageContainer.setVisibility(View.GONE);
            holder.dtImageView.setVisibility(View.VISIBLE);
            int dp150 = dp150();
            DongtuStore.loadImageInto(holder.dtImageView, message.getContent(), message.getImageId(), dp150, Math.round(message.getHeight() * (float) dp150 / message.getWidth()));
            //简单实现Gif的收藏操作。长按消息列表中的Gif时，如果它没有被收藏，则调用收藏接口，否则调用取消收藏接口。
            holder.dtImageView.setOnLongClickListener(view -> {
                DongtuStore.collectionHasGif(message.getImageId(), new CollectionExistsCallback() {
                    @Override
                    public void onSuccess(boolean isExistent) {
                        if (!isExistent) {
                            DongtuStore.collectGif(message.getImageId(), new DTOutcomeListener() {
                                @Override
                                public void onSuccess() {
                                    Log.i("DongtuStore", "Gif collected");
                                }

                                @Override
                                public void onFailure(int errorCode, String reason) {
                                    Log.i("DongtuStore", "Gif not collected");
                                }
                            });
                        } else {
                            DongtuStore.removeCollectedGif(message.getImageId(), new DTOutcomeListener() {
                                @Override
                                public void onSuccess() {
                                    Log.i("DongtuStore", "Gif removed from collection");
                                }

                                @Override
                                public void onFailure(int errorCode, String reason) {
                                    Log.i("DongtuStore", "Gif not removed from collection");
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String reason) {
                        Log.i("DongtuStore", "Gif in collection: unknown");
                    }
                });
                return true;
            });
        } else {
            holder.dtImageView.setVisibility(View.GONE);
            holder.messageContainer.setVisibility(View.VISIBLE);
            if (dataType == Message.Type.STICKER) {
                holder.messageContainer.getBackground().setAlpha(0);
                holder.messageView.showSticker(message.getContent());
                //与Gif类似，简单实现Sticker的收藏操作。
                holder.messageView.setOnLongClickListener(view -> {
                    DongtuStore.collectionHasSticker(message.getContent(), new CollectionExistsCallback() {
                        @Override
                        public void onSuccess(boolean isExistent) {
                            if (!isExistent) {
                                DongtuStore.collectSticker(message.getContent(), new DTOutcomeListener() {
                                    @Override
                                    public void onSuccess() {
                                        Log.i("DongtuStore", "Sticker collected");
                                    }

                                    @Override
                                    public void onFailure(int errorCode, String reason) {
                                        Log.i("DongtuStore", "Sticker not collected");
                                    }
                                });
                            } else {
                                DongtuStore.removeCollectedSticker(message.getContent(), new DTOutcomeListener() {
                                    @Override
                                    public void onSuccess() {
                                        Log.i("DongtuStore", "Sticker removed from Collection");
                                    }

                                    @Override
                                    public void onFailure(int errorCode, String reason) {
                                        Log.i("DongtuStore", "Sticker not removed from Collection");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(int errorCode, String reason) {
                            Log.e("DongtuStore", "Sticker in collection: unknown");
                        }
                    });
                    return true;
                });
            } else {
                holder.messageContainer.getBackground().setAlpha(255);
                holder.messageView.showText(message.getContent());
            }
        }
        return v;
    }

    private int dp150() {
        Resources r = mContext.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 150, r.getDisplayMetrics());
    }

    static class ViewHolder {
        TextView tv_date;
        ProgressBar progress;
        FrameLayout messageContainer;
        DTStoreMessageView messageView;
        DTImageView dtImageView;
    }
}
