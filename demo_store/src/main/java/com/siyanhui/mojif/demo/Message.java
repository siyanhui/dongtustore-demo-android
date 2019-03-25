package com.siyanhui.mojif.demo;

import com.dongtu.sdk.model.DTImage;

import java.util.Date;

/**
 * 聊天消息javabean
 */
public class Message {
    private Type type; // 1-大表情 | 2-图文混排 ...

    public final static int MSG_STATE_SENDING = 3;
    public final static int MSG_STATE_SUCCESS = 1;
    public final static int MSG_STATE_FAIL = 2;

    private Long id;
    private String content;
    private String fromUserName;
    private String toUserName;

    private Boolean isSend;
    private Date time;

    public Message(Type type, String fromUserName, String toUserName, String content, Boolean isSend, Date time, DTImage image) {
        super();
        this.type = type;
        this.fromUserName = fromUserName;
        this.toUserName = toUserName;
        this.content = content;
        this.isSend = isSend;
        this.time = time;
        if (type == Type.GIF) {
            imageId = image.getId();
            width = image.getWidth();
            height = image.getHeight();
            isAnimated = image.isAnimated();
        }
    }

    private String imageId;
    private int width;
    private int height;
    private boolean isAnimated;

    public Type getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public String getToUserName() {
        return toUserName;
    }

    public Boolean getIsSend() {
        return isSend;
    }

    public Date getTime() {
        return time;
    }

    public enum Type {
        TEXT, STICKER, GIF
    }

    public String getImageId() {
        return imageId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isAnimated() {
        return isAnimated;
    }
}
