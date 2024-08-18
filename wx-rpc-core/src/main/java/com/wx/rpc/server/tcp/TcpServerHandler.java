package com.wx.rpc.server.tcp;

import com.wx.rpc.model.RpcRequest;
import com.wx.rpc.model.RpcResponse;
import com.wx.rpc.protocol.ProtocolMessage;
import com.wx.rpc.protocol.code.ProtocolMessageDecoder;
import com.wx.rpc.protocol.code.ProtocolMessageEncoder;
import com.wx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import com.wx.rpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Vert.x TCP 请求处理器（服务提供者）
 */
public class TcpServerHandler implements Handler<NetSocket> {

    /**
     * 处理请求
     *
     * @param socket
     */
    @Override
    public void handle(NetSocket socket) {
        // 处理连接

        /* 原 处理方式
        socket.handler(buffer -> {
            // 请求处理代码
        });
         */

        // 装饰者模式 RecordParser 增强处理
        TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            // 接收请求，解码
            ProtocolMessage<RpcRequest> protocolMessage;

            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("TCP 请求处理器：协议消息解码错误！");
            }
            RpcRequest rpcRequest = protocolMessage.getBody();

            // 处理请求
            // 构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            try {
                // 获取要调用的服务实现类，通过反射调用
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.getDeclaredConstructor().newInstance(), rpcRequest.getArgs());

                // 封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // 发送响应，编码
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);

            try {
                Buffer encodeBuffer = ProtocolMessageEncoder.encode(responseProtocolMessage);
                socket.write(encodeBuffer);
            } catch (IOException e) {
                throw new RuntimeException("TCP 请求处理器：协议消息编码错误！");
            }
        });

        socket.handler(bufferHandlerWrapper);
    }
}
