package com.rance.chatui.util;

import com.rance.im.BaseMessage;

/**
 * 作者：Rance on 2016/12/20 16:51
 * 邮箱：rance935@163.com
 */
public class Constants {
    public static final String TAG = "rance";
    public static final String AUTHORITY = "com.chatui.fileprovider";
    /** 0x001-接受消息  0x002-发送消息**/
    public static final int CHAT_ITEM_TYPE_LEFT = 0x001;
    public static final int CHAT_ITEM_TYPE_RIGHT = 0x002;
    /** 0x003-发送中  0x004-发送失败  0x005-发送成功**/
    public static final int CHAT_ITEM_SENDING = 0x003;
    public static final int CHAT_ITEM_SEND_ERROR = 0x004;
    public static final int CHAT_ITEM_SEND_SUCCESS = 0x005;

    public static final String CHAT_FILE_TYPE_TEXT = "text";
    public static final String CHAT_FILE_TYPE_FILE = "file";
    public static final String CHAT_FILE_TYPE_IMAGE = "image";
    public static final String CHAT_FILE_TYPE_VOICE = "voice";
    public static final String CHAT_FILE_TYPE_CONTACT = "contact";
    public static final String CHAT_FILE_TYPE_LINK = "LINK";

    public static BaseMessage.User user = null;

    /**
     * 自定义报文类型
     */
    public static class CommandType {
        /**
         * 登录
         */
        public static final int LOGIN = 1;
        /**
         * 业务消息
         */
        public static final int MSG = 2;

        /**
         * ping
         */
        public static final int PING = 3;

        /**
         * push
         */
        public static final int PUSH = 4;

        /**
         * 确认接收消息
         */
        public static final int ACK_MSG = 20;

        /**
         * 图片消息
         */
        public static final int MSG_PIC = 21;

        /**
         * 视频消消息
         */
        public static final int MSG_VIDEO = 22;


        /**
         * 关闭消息
         */
        public static final int MSG_CLOSE = 0;
    }
}
