package com.magicbili.islandnpc.typewriter

import com.typewritermc.core.extension.Initializable
import com.typewritermc.core.extension.annotations.Singleton
import org.bukkit.Bukkit

/**
 * IslandNpc TypeWriter Extension 初始化器
 *
 * TypeWriter 在加载此 Extension 时会自动调用这个初始化器
 */
@Singleton
class IslandNpcExtensionInitializer : Initializable {

    override suspend fun initialize() {
        Bukkit.getLogger().info("[IslandNpc Extension] 正在初始化...")

        // 初始化服务
        IslandNpcTypeWriterService.initialize()

        Bukkit.getLogger().info("[IslandNpc Extension] 初始化完成")
    }

    override suspend fun shutdown() {
        Bukkit.getLogger().info("[IslandNpc Extension] 正在关闭...")

        // 关闭服务
        IslandNpcTypeWriterService.shutdown()

        Bukkit.getLogger().info("[IslandNpc Extension] 已关闭")
    }
}
