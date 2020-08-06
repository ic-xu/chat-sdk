package com.rance.im.netty;

import com.forgetsky.wanandroid.im.BaseMessage;
import com.forgetsky.wanandroid.im.config.IMSConfig;
import com.forgetsky.wanandroid.im.handle.CIMClientHandle;
import com.forgetsky.wanandroid.im.ssl.SslTwoWayContextFactory;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       TCPChannelInitializerHandler.java</p>
 * <p>@PackageName:     com.freddy.im.netty</p>
 * <b>
 * <p>@Description:     Channel初始化配置</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/04/05 07:11</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class TCPChannelInitializerHandler extends ChannelInitializer<Channel> {

    private NettyTcpClient imsClient;
    private int heartbeatInterval = IMSConfig.DEFAULT_HEARTBEAT_INTERVAL_FOREGROUND;


    public TCPChannelInitializerHandler(NettyTcpClient imsClient) {
        this.imsClient = imsClient;
    }


    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        SSLEngine sslEngine = SslTwoWayContextFactory.getClientContext().createSSLEngine();
        sslEngine.setUseClientMode(true);

//        if (null != sslCtx)
//        pipeline.addLast(sslCtx.newHandler(channel.alloc()));
        pipeline
                .addLast(new SslHandler(sslEngine))

                .addLast(new ProtobufVarint32FrameDecoder())
                .addLast(new ProtobufDecoder(BaseMessage.Message.getDefaultInstance()))
                //拆包编码
                .addLast(new ProtobufVarint32LengthFieldPrepender())
                .addLast(new ProtobufEncoder())
                .addFirst(IdleStateHandler.class.getSimpleName(), new IdleStateHandler(
                        heartbeatInterval * 3, heartbeatInterval, heartbeatInterval * 4, TimeUnit.MILLISECONDS))

//        // 握手认证消息响应处理handler
//        pipeline.addLast(LoginAuthRespHandler.class.getSimpleName(), new LoginAuthRespHandler(imsClient));
//        // 心跳消息响应处理handler
//        pipeline.addLast(HeartbeatRespHandler.class.getSimpleName(), new HeartbeatRespHandler(imsClient));
                // 接收消息处理handler
//                .addLast("logging", new LoggingHandler(LogLevel.INFO))
                .addLast(CIMClientHandle.class.getSimpleName(), new CIMClientHandle(imsClient));
    }
}
