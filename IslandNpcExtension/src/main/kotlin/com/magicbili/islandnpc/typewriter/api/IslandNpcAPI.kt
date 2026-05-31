package com.magicbili.islandnpc.typewriter.api

import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*

/**
 * IslandNpc TypeWriter API - 简化版
 * 
 * 提供给 IslandNpc 插件调用的基本 API
 * 实际的 NPC 创建由主插件处理
 */
object IslandNpcAPI {

    @Volatile
    private var initialized = false

    /**
     * 初始化 API
     */
    @JvmStatic
    fun initialize() {
        if (initialized) return
        initialized = true
        Bukkit.getLogger().info("[IslandNpcAPI] API 已初始化（简化版）")
    }

    /**
     * 检查 API 是否已初始化
     */
    @JvmStatic
    fun isInitialized(): Boolean {
        return initialized
    }
}
