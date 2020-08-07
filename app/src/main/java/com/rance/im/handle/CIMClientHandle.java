package com.rance.im.handle;

import android.util.Log;

import com.rance.chatui.util.Constants;
import com.rance.im.BaseMessage;
import com.rance.im.message.P2chatMessage;
import com.rance.im.message.ShutDownMsg;
import com.rance.im.netty.NettyTcpClient;
import com.rance.im.utils.NettyAttrUtil;

import org.simple.eventbus.EventBus;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 16/02/2018 18:09
 * @since JDK 1.8
 */
public class CIMClientHandle extends SimpleChannelInboundHandler<BaseMessage.Message> {

    private String TAG = CIMClientHandle.class.getName();


    private NettyTcpClient imsClient;

    public CIMClientHandle(NettyTcpClient imsClient) {
        this.imsClient = imsClient;
    }

    private ShutDownMsg shutDownMsg;

    public static Channel channel;


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            switch (state) {
                case READER_IDLE:
                    // 规定时间内没收到服务端心跳包响应，进行重连操作
                    imsClient.resetConnect(false);
                    break;
                case WRITER_IDLE:
                    // 规定时间内没向服务端发送心跳包，即发送一个心跳包
//                    BaseMessage.User user = BaseMessage.User.newBuilder()
//                            .setAvatar(Constants.user.getAvatar())
//                            .setDisplayName(Constants.user.getDisplayName())
//                            .setId(Constants.user.getId())
//                            .build();
                    BaseMessage.Message message = BaseMessage.Message.newBuilder()
                            .setType(Constants.CommandType.PING)
                            .setText("PING")
//                            .setUser(user)
                            .build();
                    ctx.writeAndFlush(message);
                    break;
            }
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        channel = ctx.channel();
        //客户端和服务端建立连接时调用
        Log.i(TAG + "------------->", "客户链接成功，!");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        if (shutDownMsg == null) {
            shutDownMsg = new ShutDownMsg();
        }
        //用户主动退出，不执行重连逻辑
        if (shutDownMsg.checkStatus()) {
            return;
        }
        channel = null;
        Log.i(TAG + "------------->", "客户端断开了，重新连接！");
        //重连
        imsClient.resetConnect();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseMessage.Message msg) {
        Log.i(TAG + "接收到服务器消息体为：", msg.toString());
        channel = ctx.channel();
        switch (msg.getType()) {
            case Constants.CommandType.PING:
                Log.i(TAG, "收到服务端心跳！！！");
                //修改channel 的读取超时时间时间
                NettyAttrUtil.updateReaderTime(ctx.channel(), System.currentTimeMillis());
                break;

            case Constants.CommandType.MSG:
                try {
                    P2chatMessage p2chatMessage = new P2chatMessage(msg);
                    EventBus.getDefault().post(p2chatMessage);
                } catch (Exception e) {
                    EventBus.getDefault().post(msg.getText());
                }
                break;

            case Constants.CommandType.MSG_PIC:
                try {
                    P2chatMessage p2chatMessage = new P2chatMessage(msg);
                    EventBus.getDefault().post(p2chatMessage);
                } catch (Exception e) {
                    EventBus.getDefault().post(msg.getText());
                }
                break;

            case Constants.CommandType.PUSH:
            case Constants.CommandType.MSG_VIDEO:
                P2chatMessage p2chatMessage = new P2chatMessage(msg);
                EventBus.getDefault().post(p2chatMessage);
                break;
            case Constants.CommandType.ACK_MSG:
                EventBus.getDefault().post(new P2chatMessage(msg));
                break;

            case Constants.CommandType.MSG_CLOSE:
                //TODO 通知用户的账号在其他地方登录
                EventBus.getDefault().post("MSG_CLOSE", "MSG_CLOSE");
                break;

            case Constants.CommandType.LOGIN:
                EventBus.getDefault().post(1, "loginIM");
                break;
            default:
                break;

        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //异常时断开连接
        channel.close();
//        Client.INSTANCE.reconnect();
        imsClient.resetConnect();
        ctx.close();
        Log.e("出错信息，", cause.getMessage());
        //重连
        imsClient.resetConnect();
    }

}
