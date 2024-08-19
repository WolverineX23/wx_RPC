package com.wx.rpc.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.wx.rpc.RpcApplication;
import com.wx.rpc.model.RpcRequest;
import com.wx.rpc.model.RpcResponse;
import com.wx.rpc.model.ServiceMetaInfo;
import com.wx.rpc.protocol.ProtocolConstant;
import com.wx.rpc.protocol.ProtocolMessage;
import com.wx.rpc.protocol.code.ProtocolMessageDecoder;
import com.wx.rpc.protocol.code.ProtocolMessageEncoder;
import com.wx.rpc.protocol.enums.ProtocolMessageSerializerEnum;
import com.wx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Vertx TPC 请求客户端
 *
 */
public class VertxTcpClient {

    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo)
            throws InterruptedException, ExecutionException {
        // 发送 TCP 请求
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();

        // fixed BUG：这里请求 localhost:8081 一直无响应 done：VertxTcpServer，请求处理端采用了测试代码的问题
        netClient.connect(serviceMetaInfo.getServicePort(), serviceMetaInfo.getServiceHost(), result -> {
            if (!result.succeeded()) {
                System.err.println("Failed to connect to TCP server");
                return;
            }

            System.out.println("Connected to TCP server");
            NetSocket socket = result.result();

            // 发送数据
            // 构造信息
            ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
            ProtocolMessage.Header header = new ProtocolMessage.Header();
            header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
            header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
            header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
            header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
            header.setRequestId(IdUtil.getSnowflake().nextId());    // hutool 雪花算法生成全局请求 ID
            protocolMessage.setHeader(header);
            protocolMessage.setBody(rpcRequest);

            // 编码请求
            try {
                Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                socket.write(encodeBuffer);
            } catch (IOException e) {
                throw new RuntimeException("协议消息编码错误");
            }

            // 接收响应 - RecordParser 解决 半包/粘包问题
            TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
                try {
                    ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                    responseFuture.complete(rpcResponseProtocolMessage.getBody());
                } catch (IOException e) {
                    throw new RuntimeException("协议消息解码错误");
                }
            });
            socket.handler(bufferHandlerWrapper);
        });

        // 阻塞，直到响应完成，才会继续向下执行
        RpcResponse rpcResponse = responseFuture.get();

        // 关闭连接
        netClient.close();

        return rpcResponse;
    }

    /**
     * 简单测试方法：模拟请求发送和接收响应
     *
     * @param port
     */
    public void start(int port) {
        // 创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();

        vertx.createNetClient().connect(port, "localhost", result -> {
            if (result.succeeded()) {
                System.out.println("Connected to TCP server!");
                NetSocket socket = result.result();

                for (int i = 0; i < 1000; i++) {
                    // 发送数据
                    Buffer buffer = Buffer.buffer();
                    String str = "Hello, server!Hello, server!Hello, server!Hello, server!";
                    buffer.appendInt(7);
                    buffer.appendInt(str.getBytes().length);
                    buffer.appendBytes(str.getBytes());
                    socket.write(buffer);
                }

                // 接收响应
                socket.handler(buffer -> {
                    System.out.println("Received response from server: " + buffer.toString());
                });
            } else {
                System.err.println("Failed to connect to TCP server!");
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpClient().start(8888);
    }
}
