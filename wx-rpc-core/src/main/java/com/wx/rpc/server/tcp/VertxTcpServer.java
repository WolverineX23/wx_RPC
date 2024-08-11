package com.wx.rpc.server.tcp;

import com.wx.rpc.server.HttpServer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.parsetools.RecordParser;

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
//        server.connectHandler(new TcpServerHandler());

        // test RecordParser 解决半包/粘包问题
        server.connectHandler(socket -> {
            // 构造 RecordParser
            RecordParser parser = RecordParser.newFixed(8); // 读取固定长度值的内容 - 测试中是 请求头的 两个 int 值
            parser.setOutput(new Handler<Buffer>() {
                // 初始化
                int size = -1;

                // 一次完整的读取（头 + 体）
                Buffer resultBuffer = Buffer.buffer();

                @Override
                public void handle(Buffer buffer) {
                    if (size == -1) {
                        // 读取请求体长度
                        size = buffer.getInt(4);
                        parser.fixedSizeMode(size);     // 设置 parse 长度，下一步获取请求体长度

                        // 写入头信息到结果
                        resultBuffer.appendBuffer(buffer);
                    } else {
                        // 写入体信息到结果
                        resultBuffer.appendBuffer(buffer);
                        System.out.println("get TCP package: " + resultBuffer.toString());

                        // 重置一轮
                        parser.fixedSizeMode(8);
                        size = -1;
                        resultBuffer = Buffer.buffer();
                    }
                }
            });

            socket.handler(parser);
        });

        /* test 半包/粘包问题演示
        server.connectHandler(socket -> {
            // 处理连接
            socket.handler(buffer -> {
                String testMessage = "Hello, server!Hello, server!Hello, server!Hello, server!";
                int messageLength = testMessage.getBytes().length;

                // 出现半包问题
                if (buffer.getBytes().length < messageLength) {
                    System.out.println("原包：length = " + messageLength + " 出现半包：length = " + buffer.getBytes().length);
                    return;
                }

                // 出现粘包问题
                if (buffer.getBytes().length > messageLength) {
                    System.out.println("原包：length = " + messageLength + " 出现粘包：length = " + buffer.getBytes().length);
                    return;
                }

                // 比较响应结果
                String str = new String(buffer.getBytes(0, messageLength));
                System.out.println("接收到包：" + str);
                if (testMessage.equals(str)) {
                    System.out.println("good");
                }

                // test 半包/粘包问题时，先注释掉下面这段
                // 处理接收到的字节数组
//                byte[] requestData = buffer.getBytes();

                // 在这里进行自定义的字节数组处理逻辑，比如解析请求、调用服务、构造响应等
//                byte[] responseData = handleRequest(requestData);

                // 发送响应
//                socket.write(Buffer.buffer(responseData));
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
