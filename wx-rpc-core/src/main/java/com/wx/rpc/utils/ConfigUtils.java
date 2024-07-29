package com.wx.rpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * 配置工具类
 *
 * 作用：读取配置文件，并返回配置对象，以简化调用
 * hutool - Props
 */
public class ConfigUtils {

    /**
     * 加载配置对象
     *
     * @param tClass    用于指定要将配置文件内容映射到的类类型
     * @param prefix    配置属性的前缀，用于从配置文件中选择特定的配置项
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "");
    }

    /**
     * 加载配置对象，支持区分环境
     *
     * @param tClass        用于指定要将配置文件内容映射到的类类型
     * @param prefix        配置属性的前缀，用于从配置文件中选择特定的配置项
     * @param environment   指定环境，决定要加载哪个环境的配置文件
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        // 构建 配置文件名
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".properties");

        // 创建 Props 实例，读取并解析对应配置文件中的内容
        Props props = new Props(configFileBuilder.toString());

        // 根据前缀过滤配置项，然后将这些配置项的值赋给对应对象的属性
        return props.toBean(tClass, prefix);
    }
}
