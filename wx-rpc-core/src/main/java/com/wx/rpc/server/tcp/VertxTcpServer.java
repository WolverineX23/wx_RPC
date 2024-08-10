package com.wx.rpc.server.tcp;

import com.wx.rpc.server.HttpServer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;

/**
 * TCP 服务器
 */
public class VertxTcpServer implements HttpServer {

    /**
     * 处理请求
     *
     * @param requestData
     * @return
     */
    private byte[] handleRequest(byte[] requestData) {
        // todo: 在这里编写处理请求的逻辑，根据 requestData 构造响应数据并返回

        // 这里只是一个示例，实际逻辑需要根据具体的业务需求来实现
        return "Hello, client!".getBytes();
    }

    /**
     * 启动 TCP 服务器
     *
     * 数据发送格式为 Buffer： 这是 Vert.x 为我们提供的字节数组缓冲区实现
     *
     * @param port
     */
    @Override
    public void doStart(int port) {

        // 创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();

        // 创建 TCP 服务器
        NetServer server = vertx.createNetServer();

        // 处理请求
        server.connectHandler(new TcpServerHandler());
        /* old demo
        server.connectHandler(socket -> {
            // 处理连接
            socket.handler(buffer -> {
                // 处理接收到的字节数组
                byte[] requestData = buffer.getBytes();

                // 在这里进行自定义的字节数组处理逻辑，比如解析请求、调用服务、构造响应等
                byte[] responseData = handleRequest(requestData);

                // 发送响应
                socket.write(Buffer.buffer(responseData));
            });
        });
         */

        // 启动 TCP 服务器并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("TCP server started on port " + port);
            } else {
                System.err.println("Failed to start TCP server: " + result.cause());
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpServer().doStart(8888);
    }
}
