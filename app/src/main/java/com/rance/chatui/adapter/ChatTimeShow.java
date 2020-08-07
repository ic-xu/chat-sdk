package com.rance.chatui.adapter;

import android.view.View;
import android.widget.TextView;
import com.rance.chatui.enity.MessageInfo;
import java.text.SimpleDateFormat;

public class ChatTimeShow {

    long lastTime = 0;

    long noShowtime = 60000;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");

    private static ChatTimeShow instance;

    public static ChatTimeShow getInstance() {
        if (instance == null) {
            synchronized (ChatTimeShow.class) {
                if (null == instance) {
                    instance = new ChatTimeShow();
                }
            }
        }
        return instance;
    }


    public void showTime(TextView view, MessageInfo messageInfo) {
        String[] format = new SimpleDateFormat("yyyy年-MM月-dd日- HH:mm").format(System.currentTimeMillis()).split("-");
        long messageTime = messageInfo.getTime() == null ? System.currentTimeMillis() : messageInfo.getTime();
        if (Math.abs(messageTime - lastTime) > noShowtime) {
            String messageDataString = simpleDateFormat.format(messageTime);
            for (String prex : format) {
                if (messageDataString.startsWith(prex)) {
                    messageDataString = messageDataString.replaceFirst(prex, "");
                }
            }
            view.setText(messageDataString);
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
        lastTime = messageTime;
    }

}
