package com.siyanhui.mojif.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ListView;

import com.dongtu.sdk.constant.DTGender;
import com.dongtu.sdk.model.DTImage;
import com.dongtu.store.DongtuStore;
import com.dongtu.store.visible.messaging.DTStoreSendMessageListener;
import com.dongtu.store.visible.messaging.DTStoreSticker;
import com.dongtu.store.widget.DTStoreEditView;
import com.dongtu.store.widget.DTStoreKeyboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressLint("ClickableViewAccessibility")
public class MyActivity extends FragmentActivity {
    List<Message> mMessages = new ArrayList<>();
    private DTStoreKeyboard mKeyboard;
    private View mMainContainer;
    private ListView mMessageListView;
    private ChatAdapter mMessageAdapter;
    private View mTextInput;
    private CheckBox mKeyboardSwitch;
    private DTStoreEditView mEditText;
    private int mScreenHeight;
    private boolean mPendingShowPlaceHolder;
    private Rect mTmpRect = new Rect();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DongtuStore.setUserInfo("00001", "Tester", DTGender.MALE, "301, No.99, Yandang Road, Shanghai", "xxx@dongtu.com", "12312312345", null);
        setContentView(R.layout.dtstore_myactivity_chat);
        initView();
    }

    private void initView() {
        findViewById(R.id.button_back).setOnClickListener(v -> finish());
        mTextInput = findViewById(R.id.messageToolBox);
        mMainContainer = findViewById(R.id.main_container);
        mKeyboard = findViewById(R.id.chat_msg_input_box);
        mMessageListView = findViewById(R.id.chat_listview);
        mMessageListView.setSelector(android.R.color.transparent);
        mKeyboardSwitch = findViewById(R.id.chatbox_open);
        mEditText = findViewById(R.id.chatbox_message);
        mEditText.setUnicodeEmojiSpanSizeRatio(1.5f);//让emoji显示得比一般字符大一点
        mEditText.requestFocus();

        UnicodeToEmoji.initPhotos(this);

        /*
         * 加载SDK
         */
        DongtuStore.setKeyboard(findViewById(R.id.chat_msg_input_box));
        DongtuStore.setEditText(mEditText);
        DongtuStore.setupSearchPopupAboveView(findViewById(R.id.messageToolBox), mEditText);
        /*
         * 默认方式打开软键盘时切换表情符号的状态
         */
        mEditText.setOnTouchListener((v, event) -> {
            mKeyboardSwitch.setChecked(false);
            return false;
        });
        findViewById(R.id.chatbox_send).setOnClickListener(v -> {
            final String content = mEditText.getText().toString();
            if (!TextUtils.isEmpty(content)) {
                Message message = new Message(Message.Type.TEXT, "Tom", "Jerry", content, true, new Date(), null);
                mMessages.add(message);
                mMessageAdapter.refresh(mMessages);
                mEditText.setText(null);

                /*
                 * 1秒后增加一条和发出的这条相同的消息，模拟对话
                 */
                new Handler().postDelayed(() -> {
                    Message reply = new Message(Message.Type.TEXT, "Jerry", "Tom", content, false, new Date(), null);
                    mMessages.add(reply);
                    mMessageAdapter.refresh(mMessages);
                }, 1000);
            }
        });
        DongtuStore.setSendMessageListener(new DTStoreSendMessageListener() {

            @Override
            public void onSendSticker(final DTStoreSticker sticker) {
                Message message = new Message(Message.Type.STICKER, "Tom", "Jerry", sticker.code, true, new Date(), null);
                mMessages.add(message);
                mMessageAdapter.refresh(mMessages);

                /*
                 * 1秒后增加一条和发出的这条相同的消息，模拟对话
                 */
                new Handler().postDelayed(() -> {
                    Message reply = new Message(Message.Type.STICKER, "Jerry", "Tom", sticker.code, false, new Date(), null);
                    mMessages.add(reply);
                    mMessageAdapter.refresh(mMessages);
                }, 1000);
            }

            @Override
            public void onSendDTImage(final DTImage image) {
                Message message = new Message(Message.Type.GIF, "Tom", "Jerry", image.getImage(), true, new Date(), image);
                mMessages.add(message);
                mMessageAdapter.refresh(mMessages);

                /*
                 * 1秒后增加一条和发出的这条相同的消息，模拟对话
                 */
                new Handler().postDelayed(() -> {
                    Message reply = new Message(Message.Type.GIF, "Jerry", "Tom", image.getImage(), false, new Date(), image);
                    mMessages.add(reply);
                    mMessageAdapter.refresh(mMessages);
                }, 1000);
                mEditText.setText(null);
            }
        });

        mMessageListView.setOnTouchListener((v, event) -> {
            // 关闭键盘
            mKeyboardSwitch.setChecked(false);
            closeKeyboard();
            return false;
        });

        byte[] emoji = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x81};
        Message message = new Message(Message.Type.TEXT, "Jerry", "Tom", new String(emoji), false, new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 8), null);
        mMessages.add(message);
        mMessageAdapter = new ChatAdapter(this, mMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        /*
         * 表情键盘切换监听
         */
        mEditText.getViewTreeObserver().addOnPreDrawListener(() -> {
            if (!isKeyboardVisible()) {
                // 在设置mPendingShowPlaceHolder时已经调用了隐藏Keyboard的方法，取消重绘
                if (mPendingShowPlaceHolder) {
                    mMessageListView.setSelection(mMessageListView.getAdapter().getCount() - 1);
                    mKeyboard.showKeyboard();
                    mPendingShowPlaceHolder = false;
                    return false;
                }
            } else if (mKeyboard.isKeyboardVisible()) {
                mMessageListView.setSelection(mMessageListView.getAdapter().getCount() - 1);
                mKeyboard.hideKeyboard();
                if (!mEditText.isFocused()) {
                    mPendingShowPlaceHolder = true;
                }
                return false;
            }
            return true;
        });

        //切换开关
        mKeyboardSwitch.setOnClickListener(v -> {
            // 除非软键盘和PlaceHolder全隐藏时直接显示PlaceHolder，其他情况此处处理软键盘，onPreDrawListener处理PlaceHolder
            if (mKeyboard.isKeyboardVisible()) { // PlaceHolder -> Keyboard
                showSoftInput(mEditText);
            } else if (isKeyboardVisible()) { // Keyboard -> PlaceHolder
                mPendingShowPlaceHolder = true;
                hideSoftInput(mEditText);
            } else { // Just show PlaceHolder
                mMessageListView.setSelection(mMessageListView.getAdapter().getCount() - 1);
                mKeyboard.showKeyboard();
            }
        });

        DongtuStore.setUnicodeEmojiDrawableProvider(UnicodeToEmoji.EmojiImageSpan::getEmojiDrawable);
    }

    private void closeKeyboard() {
        if (mKeyboard.isKeyboardVisible()) {
            mKeyboard.hideKeyboard();
        }
        if (isKeyboardVisible()) {
            hideSoftInput(mEditText);
        }
    }

    private boolean isKeyboardVisible() {
        int DISTANCE_SLOP = 180;
        return (mScreenHeight - getInputBottom() > DISTANCE_SLOP && !mKeyboard.isKeyboardVisible()) || (mScreenHeight - getInputBottom() > (mKeyboard.getHeight() + DISTANCE_SLOP) && mKeyboard.isKeyboardVisible());
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
     * 输入框下边的位置
     */
    private int getInputBottom() {
        mTextInput.getGlobalVisibleRect(mTmpRect);
        return mTmpRect.bottom;
    }

    /**
     * Activity在此方法中测量根布局的高度
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && mScreenHeight <= 0) {
            mMainContainer.getGlobalVisibleRect(mTmpRect);
            mScreenHeight = mTmpRect.bottom;
        }
    }

    @Override
    protected void onDestroy() {
        // 关闭SDK
        DongtuStore.destroy();
        super.onDestroy();
    }

    /**
     * 软键盘或者表情键盘打开时，按返回则关闭
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((mKeyboard.isKeyboardVisible() || isKeyboardVisible())) {
                closeKeyboard();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
