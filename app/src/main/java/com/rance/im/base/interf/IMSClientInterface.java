package com.rance.im.base.interf;


import com.rance.im.BaseMessage;
import com.rance.im.MsgTimeoutTimerManager;
import com.rance.im.netty.ServiceInfo;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       IMSClientInterface.java</p>
 * <p>@PackageName:     com.freddy.im.interf</p>
 * <b>
 * <p>@Description:     ims抽象接口，需要切换到其它方式实现im功能，实现此接口即可</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/03/31 20:04</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public interface IMSClientInterface {

    /**
     * 初始化
     ** @param serviceInfo      服务器信息
     */
    void init(ServiceInfo serviceInfo);

    /**
     * 重置连接，也就是重连
     * 首次连接也可认为是重连
     */
    void resetConnect();


    /**
     * 登录用户
     */
    void login(BaseMessage.Message msg);

    /**
     * 登录用户
     */
    void login(String username, String password);

    /**
     * 重置连接，也就是重连
     * 首次连接也可认为是重连
     * 重载
     *
     * @param isFirst 是否首次连接
     */
    void resetConnect(boolean isFirst);

    /**
     * 关闭连接，同时释放资源
     */
    void close();

    /**
     * 标识ims是否已关闭
     *
     */
    boolean isClosed();

    /**
     * 发送消息
     *
     * @param msg
     */
    void sendMsg(BaseMessage.Message msg);

    /**
     * 发送消息
     * 重载
     *
     * @param msg
     * @param isJoinTimeoutManager 是否加入发送超时管理器
     */
    void sendMsg(BaseMessage.Message msg, boolean isJoinTimeoutManager);

    /**
     * 获取重连间隔时长
     *
     * @return
     */
    int getReconnectInterval();

    /**
     * 获取连接超时时长
     *
     * @return
     */
    int getConnectTimeout();

    /**
     * 获取应用在前台时心跳间隔时间
     *
     * @return
     */
    int getForegroundHeartbeatInterval();

    /**
     * 获取应用在后台时心跳间隔时间
     *
     * @return
     */
    int getBackgroundHeartbeatInterval();

    /**
     * 设置app前后台状态
     *
     * @param appStatus
     */
    void setAppStatus(int appStatus);

    /**
     * 获取由应用层构造的握手消息
     *
     * @return
     */
    BaseMessage.Message getHandshakeMsg();

    /**
     * 获取由应用层构造的心跳消息
     *
     * @return
     */
    BaseMessage.Message getHeartbeatMsg();

    /**
     * 获取应用层消息发送状态报告消息类型
     *
     * @return
     */
    int getServerSentReportMsgType();

    /**
     * 获取应用层消息接收状态报告消息类型
     *
     * @return
     */
    int getClientReceivedReportMsgType();

    /**
     * 获取应用层消息发送超时重发次数
     *
     * @return
     */
    int getResendCount();

    /**
     * 获取应用层消息发送超时重发间隔
     *
     * @return
     */
    int getResendInterval();

    /**
     * 获取消息发送超时定时器
     *
     * @return
     */
    MsgTimeoutTimerManager getMsgTimeoutTimerManager();
}
