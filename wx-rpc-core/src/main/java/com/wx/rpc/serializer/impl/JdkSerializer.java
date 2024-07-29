package com.wx.rpc.serializer.impl;

import com.wx.rpc.serializer.Serializer;

import java.io.*;

/**
 * JDK 序列化器
 */
public class JdkSerializer implements Serializer {

    /**
     * 序列化
     *
     * @param object
     * @param <T>
     * @return
     * @throws IOException
     */
    @Override
    public <T> byte[] serializer(T object) throws IOException {
        // 创建字节输出流：是内存中一个可写入和存储字节数组的缓冲区
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // 创建 将对象转换为字节流的包装器，实际并不存储数据，而将对象的字节表示写入到 输出流 outputStream 中
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();

        // 返回 outputStream 中的所有数据的字节数组
        return outputStream.toByteArray();
    }

    /**
     * 反序列化
     *
     * @param bytes
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     */
    @Override
    public <T> T deserializer(byte[] bytes, Class<T> type) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        try {
            return (T) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            objectInputStream.close();
        }
    }
}
