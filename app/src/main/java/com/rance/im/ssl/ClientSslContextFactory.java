package com.rance.im.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * Created by chenxu
 * On 19-8-27 下午5:29
 */
public class ClientSslContextFactory {

    private static SSLContext CLIENT_CONTEXT;
    private static String keyStorePassword = "nimchen24#";

    public static SSLContext getClientContext(InputStream inputStream1, InputStream inputStream2) {
        if (CLIENT_CONTEXT != null) {
            return CLIENT_CONTEXT;
        }
        try {
            KeyManagerFactory keyManagerFactory;
            //密钥库KeyStore
            KeyStore keyStore = KeyStore.getInstance("JKS");


//            KeyStore keyStore = KeyStore.getInstance("BKS");
            //加载服务端证书
            //加载服务端的KeyStore  ；sNetty是生成仓库时设置的密码，用于检查密钥库完整性的密码
            keyStore.load(inputStream1, keyStorePassword.toCharArray());
            keyManagerFactory = KeyManagerFactory.getInstance("X509");
            //初始化密钥管理器
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            //信任库
            TrustManagerFactory trustManagerFactory;

            KeyStore tks = KeyStore.getInstance("JKS");
            tks.load(inputStream2, keyStorePassword.toCharArray());

            trustManagerFactory = TrustManagerFactory.getInstance("X509");

            trustManagerFactory.init(tks);

            TrustManager[] trustManagers = trustManagerFactory == null ? null
                    : trustManagerFactory.getTrustManagers();

            CLIENT_CONTEXT = SSLContext.getInstance("TLS");
            CLIENT_CONTEXT.init(keyManagers, trustManagers, null);

        } catch (Exception e) {
            throw new Error("Failed to initialize the server-side SSLContext", e);
        } finally {
            if (inputStream1 != null) {
                try {
                    inputStream1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return CLIENT_CONTEXT;
    }


    public static SslContext getInstance(InputStream inputStream,InputStream inputStream2) {
        // Get an SSL context using the TLS protocol
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            // Get a key manager factory using the default algorithm
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

            // Load the PKCS12 key chain
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(inputStream, keyStorePassword.toCharArray());
            kmf.init(ks, keyStorePassword.toCharArray());
            return SslContextBuilder.forClient().keyManager(inputStream,inputStream2).build();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
