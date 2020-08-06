package com.rance.im.message;

/**
 * 关闭连接消息
 */
public class ShutDownMsg {
    private boolean isCommand;

    /**
     * 置为用户主动退出状态
     */
    public void shutdown() {
        isCommand = true;
    }

    public boolean checkStatus() {
        return isCommand;
    }
}
