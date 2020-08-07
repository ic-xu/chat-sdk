package com.rance.im.netty;


import android.annotation.SuppressLint;

import com.rance.IMApplication;
import com.rance.chatui.util.Constants;
import com.rance.im.BaseMessage;
import com.rance.im.ExecutorServiceFactory;
import com.rance.im.IMSConnectStatusListener;
import com.rance.im.MsgTimeoutTimerManager;
import com.rance.im.base.interf.IMSClientInterface;
import com.rance.im.base.listener.IMSConnectStatusCallback;
import com.rance.im.config.IMSConfig;
import com.rance.im.handle.CIMClientHandle;
import com.rance.im.utils.NetWorkUtils;

import org.simple.eventbus.Subscriber;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.StringUtil;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       NettyTcpClient.java</p>
 * <p>@PackageName:     com.freddy.im.netty</p>
 * <b>
 * <p>@Description:     基于netty实现的tcp ims</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/03/31 20:41</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class NettyTcpClient implements IMSClientInterface {

    private static volatile NettyTcpClient instance;
    private Bootstrap bootstrap;
    private Channel channel;

    private boolean isClosed = false;// 标识ims是否已关闭


    private ServiceInfo serviceInfo;//用户信息

    private IMSConnectStatusCallback mIMSConnectStatusCallback;// ims连接状态回调监听器

    private ExecutorServiceFactory loopGroup;// 线程池工厂

    private boolean isReconnecting = false;// 是否正在进行重连
    private int connectStatus = IMSConfig.CONNECT_STATE_FAILURE;// ims连接状态，初始化为连接失败
    // 心跳间隔时间
    private int heartbeatInterval = IMSConfig.DEFAULT_HEARTBEAT_INTERVAL_FOREGROUND;
    // 应用在后台时心跳间隔时间
    private int foregroundHeartbeatInterval = IMSConfig.DEFAULT_HEARTBEAT_INTERVAL_FOREGROUND;
    // 应用在前台时心跳间隔时间
    private int backgroundHeartbeatInterval = IMSConfig.DEFAULT_HEARTBEAT_INTERVAL_BACKGROUND;

    private String currentHost = null;// 当前连接host
    private int currentPort = -1;// 当前连接port


    private MsgTimeoutTimerManager msgTimeoutTimerManager;// 消息发送超时定时器管理

    private BaseMessage.Message handleMessage;

    private NettyTcpClient() {
    }

    public static NettyTcpClient getInstance() {
        if (null == instance) {
            synchronized (NettyTcpClient.class) {
                if (null == instance) {
                    instance = new NettyTcpClient();
                }
            }
        }

        return instance;
    }

    /**
     * 初始化
     *
     * @param serviceInfo 服务器地址信息
     */
    @Override
    public void init(ServiceInfo serviceInfo) {
        close();
        isClosed = false;
        this.serviceInfo = serviceInfo;
        this.mIMSConnectStatusCallback = new IMSConnectStatusListener();
        loopGroup = new ExecutorServiceFactory();
        loopGroup.initBossLoopGroup();// 初始化重连线程组
        msgTimeoutTimerManager = new MsgTimeoutTimerManager(this);
        resetConnect(true);// 进行第一次连接
    }


    @Override
    public void login(BaseMessage.Message msg) {
        handleMessage = msg;
        if (null != channel && channel.isActive()) {
            // 发送登录消息
            sendMsg(handleMessage, true);
        }
    }


    @Override
    public void login(String username, String password) {
        handleMessage = getLoginUserInfo(username,password);
        login(handleMessage);
    }


    private BaseMessage.Message getLoginUserInfo(String username, String password) {
        BaseMessage.User user = BaseMessage.User
                .newBuilder()
                .setAvatar(username)
                .setDisplayName(username)
                .setId(username)
                .setProject("usercare")
                .build();
        Constants.user = user;
        BaseMessage.Message loginMessage = BaseMessage.Message.newBuilder()
                .setId(2L)
                .setUser(user)
                .setType(Constants.CommandType.LOGIN)
                .build();
        return loginMessage;
    }



    /**
     * 重置连接，也就是重连
     * 首次连接也可认为是重连
     */
    @Override
    public void resetConnect() {
        this.resetConnect(false);
    }


    /**
     * 重置连接，也就是重连
     * 首次连接也可认为是重连
     * 重载
     *
     * @param isFirst 是否首次连接
     */
    @Override
    public void resetConnect(boolean isFirst) {
        if (!isFirst) {
            try {
                Thread.sleep(IMSConfig.DEFAULT_RECONNECT_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 只有第一个调用者才能赋值并调用重连
        if (!isClosed && !isReconnecting) {
            synchronized (this) {
                if (!isClosed && !isReconnecting) {
                    // 标识正在进行重连
                    isReconnecting = true;
                    // 回调ims连接状态
                    onConnectStatusCallback(IMSConfig.CONNECT_STATE_CONNECTING);
                    // 先关闭channel
                    closeChannel();
                    // 执行重连任务
                    loopGroup.execBossTask(new ResetConnectRunnable(isFirst));
                }
            }
        }
    }

    /**
     * 关闭连接，同时释放资源
     */
    @Override
    public void close() {
        if (isClosed) {
            return;
        }

        isClosed = true;

        // 关闭channel
        try {
            closeChannel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 关闭bootstrap
        try {
            if (bootstrap != null) {
                bootstrap.group().shutdownGracefully();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            // 释放线程池
            if (loopGroup != null) {
                loopGroup.destroy();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            isReconnecting = false;
            channel = null;
            bootstrap = null;
        }
    }

    /**
     * 标识ims是否已关闭
     */
    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Subscriber(tag = "MSG_CLOSE")
    private void closeNettyTclClient(String message) {
        if ("MSG_CLOSE".equals(message))
            isClosed = true;
    }

    /**
     * 发送消息
     */
    @Override
    public void sendMsg(BaseMessage.Message msg) {
        this.sendMsg(msg, true);
    }

    /**
     * 发送消息
     * 重载
     *
     * @param msg                  dfs
     * @param isJoinTimeoutManager 是否加入发送超时管理器
     */
    @Override
    public void sendMsg(BaseMessage.Message msg, boolean isJoinTimeoutManager) {
        if (msg == null || msg.getId() == 0) {
            System.out.println("发送消息失败，消息为空\tmessage=" + msg);
            return;
        }

        if (!StringUtil.isNullOrEmpty(msg.getText())) {
            if (isJoinTimeoutManager) {
                msgTimeoutTimerManager.add(msg);
            }
        }

        if (channel == null) {
            System.out.println("发送消息失败，channel为空\tmessage=" + msg);
        }

        try {
            channel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    msgTimeoutTimerManager.remove(msg.getId() + "");
                }
            });
        } catch (Exception ex) {
            System.out.println("发送消息失败，reason:" + ex.getMessage() + "\tmessage=" + msg);
        }
    }

    /**
     * 获取重连间隔时长
     */
    @Override
    public int getReconnectInterval() {
        // 重连间隔时长
        return IMSConfig.DEFAULT_RECONNECT_BASE_DELAY_TIME;
    }

    /**
     * 获取连接超时时长
     */
    @Override
    public int getConnectTimeout() {
        // 连接超时时长
        return IMSConfig.DEFAULT_CONNECT_TIMEOUT;
    }

    /**
     * 获取应用在前台时心跳间隔时间
     */
    @Override
    public int getForegroundHeartbeatInterval() {
        return foregroundHeartbeatInterval;
    }

    /**
     * 获取应用在前台时心跳间隔时间
     */
    @Override
    public int getBackgroundHeartbeatInterval() {
        return backgroundHeartbeatInterval;
    }

    /**
     * 设置app前后台状态
     *
     * @param appStatus app前后台状态
     */
    @Override
    public void setAppStatus(int appStatus) {
        // app前后台状态
        if (appStatus == IMSConfig.APP_STATUS_FOREGROUND) {
            heartbeatInterval = foregroundHeartbeatInterval;
        } else if (appStatus == IMSConfig.APP_STATUS_BACKGROUND) {
            heartbeatInterval = backgroundHeartbeatInterval;
        }

//        addHeartbeatHandler();
    }

    @Override
    public BaseMessage.Message getHandshakeMsg() {
        return null;
    }


    /**
     * 获取由应用层构造的心跳消息
     */
    @Override
    public BaseMessage.Message getHeartbeatMsg() {
        return BaseMessage.Message.newBuilder()
                .setType(Constants.CommandType.PING)
                .setText("PING")
                .setId(1L)
                .build();
    }

    /**
     * 获取应用层消息发送状态报告消息类型
     */
    @Override
    public int getServerSentReportMsgType() {
        return 0;
    }

    /**
     * 获取应用层消息接收状态报告消息类型
     */
    @Override
    public int getClientReceivedReportMsgType() {
        return 0;
    }


    /**
     * 获取应用层消息发送超时重发次数
     */
    @Override
    public int getResendCount() {
        // 消息发送超时重发次数
        return IMSConfig.DEFAULT_RESEND_COUNT;
    }

    /**
     * 获取应用层消息发送超时重发间隔
     */
    @Override
    public int getResendInterval() {
        // 消息发送失败重发间隔时长
        return IMSConfig.DEFAULT_RESEND_INTERVAL;
    }


    /**
     * 获取消息发送超时定时器
     */
    @Override
    public MsgTimeoutTimerManager getMsgTimeoutTimerManager() {
        return msgTimeoutTimerManager;
    }

    /**
     * 初始化bootstrap
     */
    private void initBootstrap() {
        try {

            EventLoopGroup loopGroup = new NioEventLoopGroup(4);
            bootstrap = new Bootstrap();
            bootstrap.group(loopGroup).channel(NioSocketChannel.class);
            // 设置该选项以后，如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            // 设置禁用nagle算法
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            // 设置连接超时时长
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnectTimeout());
            // 设置初始化Channel
            bootstrap.handler(new TCPChannelInitializerHandler(this));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 回调ims连接状态
     *
     * @param connectStatus 链接的回调状态
     */
    private void onConnectStatusCallback(int connectStatus) {
        this.connectStatus = connectStatus;
        switch (connectStatus) {
            case IMSConfig.CONNECT_STATE_CONNECTING: {
                System.out.println("ims连接中...");
                if (mIMSConnectStatusCallback != null) {
                    mIMSConnectStatusCallback.onConnecting();
                }
                break;
            }

            case IMSConfig.CONNECT_STATE_SUCCESSFUL: {
                System.out.println(String.format("ims连接成功，host『%s』, port『%s』", currentHost, currentPort));
                if (mIMSConnectStatusCallback != null) {
                    mIMSConnectStatusCallback.onConnected();
                }

                break;
            }

            case IMSConfig.CONNECT_STATE_FAILURE:
            default: {
                System.out.println("ims连接失败");
                if (mIMSConnectStatusCallback != null) {
                    mIMSConnectStatusCallback.onConnectFailed();
                }
                break;
            }
        }
    }


    /**
     * 移除指定handler
     *
     * @param handlerName handlerName
     */
    private void removeHandler(String handlerName) {
        try {
            if (channel.pipeline().get(handlerName) != null) {
                channel.pipeline().remove(handlerName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("移除handler失败，handlerName=" + handlerName);
        }
    }

    /**
     * 关闭channel
     */
    private void closeChannel() {
        try {
            if (channel != null) {
                try {
//                    removeHandler(HeartbeatHandler.class.getSimpleName());
                    removeHandler(CIMClientHandle.class.getSimpleName());
                    removeHandler(IdleStateHandler.class.getSimpleName());
                } finally {
                    try {
                        channel.close();
                        channel.eventLoop().shutdownGracefully();
                    } catch (Exception ignored) {
                    }
                    channel = null;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("关闭channel出错，reason:" + ex.getMessage());
        }
    }

    /**
     * 从应用层获取网络是否可用
     */
    private boolean isNetworkAvailable() {
        return NetWorkUtils.isNetworkAvailable(IMApplication.getInstance());
    }

    /**
     * 真正连接服务器的地方
     */
    private void toServer() {
        try {
            channel = bootstrap.connect(currentHost, currentPort).sync().channel();
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            System.err.println(String.format("连接Server(ip[%s], port[%s])失败", currentHost, currentPort));
            channel = null;
        }
    }

    /**
     * 重连任务
     */
    private class ResetConnectRunnable implements Runnable {

        private boolean isFirst;

        ResetConnectRunnable(boolean isFirst) {
            this.isFirst = isFirst;
        }

        @Override
        public void run() {
            // 非首次进行重连，执行到这里即代表已经连接失败，回调连接状态到应用层
            if (!isFirst) {
                onConnectStatusCallback(IMSConfig.CONNECT_STATE_FAILURE);
            }

            try {
                // 重连时，释放工作线程组，也就是停止心跳
                loopGroup.destroyWorkLoopGroup();

                while (!isClosed) {
                    if (!isNetworkAvailable()) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    // 网络可用才进行连接
                    int status;
                    if ((status = reConnect()) == IMSConfig.CONNECT_STATE_SUCCESSFUL) {
                        onConnectStatusCallback(status);
                        // 连接成功，跳出循环
                        if(null!= handleMessage){
                            login(handleMessage);
                        }
                        break;
                    }

                    if (status == IMSConfig.CONNECT_STATE_FAILURE) {
                        onConnectStatusCallback(status);
                        try {
                            Thread.sleep(IMSConfig.DEFAULT_RECONNECT_INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } finally {
                // 标识重连任务停止
                isReconnecting = false;
            }
        }

        /**
         * 重连，首次连接也认为是第一次重连
         */
        private int reConnect() {
            // 未关闭才去连接
            if (!isClosed) {
                try {
                    // 先释放EventLoop线程组
                    if (bootstrap != null) {
                        bootstrap.group().shutdownGracefully();
                    }
                } finally {
                    bootstrap = null;
                }

                // 初始化bootstrap
                initBootstrap();
                return connectServer();
            }
            return IMSConfig.CONNECT_STATE_FAILURE;
        }


        /**
         * 连接服务器
         */
        @SuppressLint("DefaultLocale")
        private int connectServer() {
            // 如果服务器地址无效，直接回调连接状态，不再进行连接
            // 有效的服务器地址示例：127.0.0.1 8860
            if (serviceInfo == null || serviceInfo.getUrl() == null || serviceInfo.getServerPort() == 0) {
                return IMSConfig.CONNECT_STATE_FAILURE;
            }

            for (int j = 1; j <= IMSConfig.DEFAULT_RECONNECT_COUNT; j++) {
                // 如果ims已关闭，或网络不可用，直接回调连接状态，不再进行连接
                if (isClosed || !isNetworkAvailable()) {
                    return IMSConfig.CONNECT_STATE_FAILURE;
                }

                // 回调连接状态
                if (connectStatus != IMSConfig.CONNECT_STATE_CONNECTING) {
                    onConnectStatusCallback(IMSConfig.CONNECT_STATE_CONNECTING);
                }
                System.out.println(String.format("正在进行『%s』的第『%d』次连接，当前重连延时时长为『%dms』", serviceInfo.getUrl(), j, j * getReconnectInterval()));

                try {
                    currentHost = serviceInfo.getUrl();// 获取host
                    currentPort = serviceInfo.getServerPort();// 获取port
                    toServer();// 连接服务器

                    // channel不为空，即认为连接已成功
                    if (channel != null) {
                        return IMSConfig.CONNECT_STATE_SUCCESSFUL;
                    } else {
                        // 连接失败，则线程休眠n * 重连间隔时长
                        Thread.sleep(j * getReconnectInterval());
                    }
                } catch (InterruptedException e) {
                    close();
                    break;// 线程被中断，则强制关闭
                }
            }

            // 执行到这里，代表连接失败
            return IMSConfig.CONNECT_STATE_FAILURE;
        }
    }
}
