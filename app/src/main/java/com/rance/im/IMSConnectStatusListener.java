package com.rance.im;


import com.rance.im.base.listener.IMSConnectStatusCallback;

import org.simple.eventbus.EventBus;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       IMSConnectStatusListener.java</p>
 * <p>@PackageName:     com.freddy.chat.im</p>
 * <b>
 * <p>@Description:     类描述</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/04/08 00:31</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class IMSConnectStatusListener implements IMSConnectStatusCallback {

    @Override
    public void onConnecting() {
    }

    @Override
    public void onConnected() {
        EventBus.getDefault().post(1,"connect");
    }

    @Override
    public void onConnectFailed() {
        EventBus.getDefault().post(0,"connect");
    }
}
