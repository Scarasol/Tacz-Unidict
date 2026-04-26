package com.scarasol.tud.api.serialization;

import com.scarasol.tud.util.io.JsonTypeIds;

import java.nio.file.Path;

/**
 * 用来标志可序列化
 * @author Scarasol
 */
public interface JsonData {

    /**
     * 从IO初始化后进行操作
     */
    void onLoaded();

    /**
     * @return JsonData类型的ID
     */
    default String typeId() {
        return JsonTypeIds.idOf(this.getClass());
    }

    /**
     * @return 序列化的路径地址
     */
    Path getPath();
}
