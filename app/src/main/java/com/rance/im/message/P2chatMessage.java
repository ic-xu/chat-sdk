package com.rance.im.message;


import com.rance.im.BaseMessage;

/**
 * Created by ubuntu
 * On 19-7-5 上午10:49
 */
public class P2chatMessage {


    private BaseMessage.Message message;

    public BaseMessage.Message getMessage() {
        return message;
    }

    public P2chatMessage(BaseMessage.Message message) {
        this.message = message;
    }
}
