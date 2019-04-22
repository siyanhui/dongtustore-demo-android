package com.siyanhui.mojif.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.dongtu.sdk.constant.DTGender;
import com.dongtu.sdk.model.DTImage;
import com.dongtu.store.DongtuStore;
import com.dongtu.store.visible.messaging.DTStoreSendMessageListener;
import com.dongtu.store.visible.messaging.DTStoreSticker;
import com.dongtu.store.visible.ui.DTStoreUnicodeEmojiDrawableProvider;
import com.dongtu.store.widget.DTStoreEditView;
import com.dongtu.store.widget.DTStoreKeyboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyActivity extends FragmentActivity {
    private final int DISTANCE_SLOP = 180;
    private final String LAST_KEYBOARD_HEIGHT = "last_keyboard_height";
    List<Message> datas = new ArrayList<>();
    private DTStoreKeyboard mKeyboard;
    private ListView mRealListView;
    private ChatAdapter adapter;
    private View inputbox;
    private CheckBox mKeyboardSwich;
    private DTStoreEditView mEditView;
    /**
     * 键盘切换相关
     */
    private Rect tmp = new Rect();
    private int mScreenHeight;
    private View mMainContainer;
    private boolean mPendingShowPlaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DongtuStore.setUserInfo("0001", "Tester", DTGender.MALE, "301, No.99, Yandang Road, Shanghai", "xxx@dongtu.com", "12312312345", null);
        DongtuStore.load();
        setContentView(R.layout.dtstore_myactivity_chat);
        initView();
    }

    private void initView() {
        inputbox = findViewById(R.id.messageToolBox);
        mMainContainer = findViewById(R.id.main_container);
        mKeyboard = findViewById(R.id.chat_msg_input_box);
        mRealListView = findViewById(R.id.chat_listview);
        mRealListView.setSelector(android.R.color.transparent);
        final Button sendButton = findViewById(R.id.chatbox_send);
        mKeyboardSwich = findViewById(R.id.chatbox_open);
        mEditView = findViewById(R.id.chatbox_message);
        mEditView.setUnicodeEmojiSpanSizeRatio(1.5f);//让emoji显示得比一般字符大一点
        mEditView.requestFocus();

        /*
         * 加载SDK
         */
        DongtuStore.setKeyboard(mKeyboard);
        UnicodeToEmoji.initPhotos(this);
        DongtuStore.setEditText(mEditView);
        DongtuStore.setupSearchPopupAboveView(findViewById(R.id.messageToolBox), mEditView);
        /**
         * 默认方式打开软键盘时切换表情符号的状态
         */
        mEditView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mKeyboardSwich.setChecked(false);
                return false;
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String content = mEditView.getText().toString();
                Message message = new Message(Message.Type.TEXT, "Tom", "Jerry", content, true, new Date(), null);
                datas.add(message);
                adapter.refresh(datas);
                mEditView.setText(null);

                /*
                 * 1秒后增加一条和发出的这条相同的消息，模拟对话
                 */
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Message getmessage = new Message(Message.Type.TEXT, "Jerry", "Tom", content, false, new Date(), null);
                        datas.add(getmessage);
                        adapter.refresh(datas);
                    }
                }, 1000);
            }
        });
        DongtuStore.setSendMessageListener(new DTStoreSendMessageListener() {

            @Override
            public void onSendSticker(final DTStoreSticker sticker) {
                Message message = new Message(Message.Type.STICKER, "Tom", "Jerry", sticker.code, true, new Date(), null);
                datas.add(message);
                adapter.refresh(datas);

                /*
                 * 1秒后增加一条和发出的这条相同的消息，模拟对话
                 */
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Message getmessage = new Message(Message.Type.STICKER, "Jerry", "Tom", sticker.code, false, new Date(), null);
                        datas.add(getmessage);
                        adapter.refresh(datas);
                    }
                }, 1000);
            }

            @Override
            public void onSendDTImage(final DTImage image) {
                Message message = new Message(Message.Type.GIF, "Tom", "Jerry", image.getImage(), true, new Date(), image);
                datas.add(message);
                adapter.refresh(datas);

                /**
                 * 1秒后增加一条和发出的这条相同的消息，模拟对话
                 */
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Message message = new Message(Message.Type.GIF, "Jerry", "Tom", image.getImage(), false, new Date(), image);
                        datas.add(message);
                        adapter.refresh(datas);
                    }
                }, 1000);

                closeKeyboard();
            }
        });
        initMessageInputToolBox();
        initListView();

        /**
         * 表情键盘切换监听
         */
        mEditView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (!isKeyboardVisible()) {
                    // 在设置mPendingShowPlaceHolder时已经调用了隐藏Keyboard的方法，取消重绘
                    if (mPendingShowPlaceHolder) {
                        mRealListView.setSelection(mRealListView.getAdapter().getCount() - 1);
                        showDTStoreKeyboard();
                        mPendingShowPlaceHolder = false;
                        return false;
                    }
                } else if (isDTStoreKeyboardVisible()) {
                    mRealListView.setSelection(mRealListView.getAdapter().getCount() - 1);
                    hideDTStoreKeyboard();
                    if (!mEditView.isFocused()) {
                        mPendingShowPlaceHolder = true;
                    }
                    return false;
                }
                return true;
            }
        });

        //切换开关
        mKeyboardSwich.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 除非软键盘和PlaceHolder全隐藏时直接显示PlaceHolder，其他情况此处处理软键盘，onPreDrawListener处理PlaceHolder
                if (isDTStoreKeyboardVisible()) { // PlaceHolder -> Keyboard
                    showSoftInput(mEditView);
                } else if (isKeyboardVisible()) { // Keyboard -> PlaceHolder
                    mPendingShowPlaceHolder = true;
                    hideSoftInput(mEditView);
                } else { // Just show PlaceHolder
                    mRealListView.setSelection(mRealListView.getAdapter().getCount() - 1);
                    showDTStoreKeyboard();
                }
            }
        });

        DongtuStore.setUnicodeEmojiDrawableProvider(new DTStoreUnicodeEmojiDrawableProvider() {
            @Override
            public Drawable getDrawableFromCodePoint(int codePoint) {
                return UnicodeToEmoji.EmojiImageSpan.getEmojiDrawable(codePoint);
            }
        });
    }


    /**************************
     * 表情键盘软键盘切换相关 start
     **************************************/
    private void closeKeyboard() {
        if (isDTStoreKeyboardVisible()) {
            hideDTStoreKeyboard();
        }
        if (isKeyboardVisible()) {
            hideSoftInput(mEditView);
        }
    }

    private boolean isKeyboardVisible() {
        return (getDistanceFromInputToBottom() > DISTANCE_SLOP && !isDTStoreKeyboardVisible()) || (getDistanceFromInputToBottom() > (mKeyboard.getHeight() + DISTANCE_SLOP) && isDTStoreKeyboardVisible());
    }

    private void showSoftInput(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }

    private void hideSoftInput(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Activity在此方法中测量根布局的高度
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && mScreenHeight <= 0) {
            mMainContainer.getGlobalVisibleRect(tmp);
            mScreenHeight = tmp.bottom;
        }
    }

    private void showDTStoreKeyboard() {
        mKeyboard.showKeyboard();
    }

    private void hideDTStoreKeyboard() {
        mKeyboard.hideKeyboard();
    }

    private boolean isDTStoreKeyboardVisible() {
        return mKeyboard.isKeyboardVisible();
    }

    /**
     * 输入框的下边距离屏幕的距离
     */
    private int getDistanceFromInputToBottom() {
        return mScreenHeight - getInputBottom();
    }

    /**
     * 输入框下边的位置
     */
    private int getInputBottom() {
        inputbox.getGlobalVisibleRect(tmp);
        return tmp.bottom;
    }

    /**************************
     * 表情键盘软键盘切换相关 end
     **************************************/

    @Override
    protected void onDestroy() {
        // 关闭SDK
        DongtuStore.destroy();
        super.onDestroy();
    }

    /**
     * 初始化列表信息以及表情键盘的监听
     */
    private void initMessageInputToolBox() {
        mRealListView.setOnTouchListener(getOnTouchListener());
    }

    private void initListView() {
        byte[] emoji = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x81};
        Message message = new Message(Message.Type.TEXT, "Jerry", "Tom", new String(emoji), false, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 8), null);

        datas.add(message);
        adapter = new ChatAdapter(this, datas);
        mRealListView.setAdapter(adapter);
    }

    /**
     * 软键盘或者表情键盘打开时，按返回则关闭
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((isDTStoreKeyboardVisible() || isKeyboardVisible())) {
                closeKeyboard();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


    /**
     * 若软键盘或表情键盘弹起，点击上端空白处应该隐藏输入法键盘
     */
    private View.OnTouchListener getOnTouchListener() {
        return new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 关闭键盘
                mKeyboardSwich.setChecked(false);
                closeKeyboard();
                return false;
            }
        };
    }
}
