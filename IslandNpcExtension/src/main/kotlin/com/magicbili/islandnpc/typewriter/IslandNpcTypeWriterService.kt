package com.magicbili.islandnpc.typewriter

import com.magicbili.islandnpc.typewriter.api.IslandNpcAPI
import com.magicbili.islandnpc.typewriter.entries.event.IslandNpcInteractChecker
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

/**
 * IslandNpc TypeWriter 服务
 * 
 * 注意：NPC 交互监听通过 @EntryListener 自动注册，无需手动处理
 */
object IslandNpcTypeWriterService {

    private var initialized = false

    /**
     * 初始化服务
     */
    @JvmStatic
    fun initialize() {
        if (initialized) {
            Bukkit.getLogger().warning("[IslandNpc-TypeWriter] 服务已经初始化，跳过")
            return
        }

        // 初始化 API
        IslandNpcAPI.initialize()

        // 注册桥接服务到主插件
        try {
            val registryClass = Class.forName("com.magicbili.islandnpc.api.TypeWriterServiceRegistry")
            val bridgeClass = Class.forName("com.magicbili.islandnpc.api.TypeWriterBridge")
            
            // 创建桥接实现
            val bridge = java.lang.reflect.Proxy.newProxyInstance(
                bridgeClass.classLoader,
                arrayOf(bridgeClass)
            ) { _, method, args ->
                when (method.name) {
                    "hasActiveEvent" -> {
                        val player = args[0] as Player
                        val islandUUID = args[1] as UUID
                        IslandNpcInteractChecker.hasActiveEvent(player, islandUUID)
                    }
                    "triggerEvents" -> {
                        val player = args[0] as Player
                        val islandUUID = args[1] as UUID
                        IslandNpcInteractChecker.triggerEvents(player, islandUUID)
                        null
                    }
                    else -> null
                }
            }
            
            // 注册服务
            val registerMethod = registryClass.getMethod("register", bridgeClass)
            registerMethod.invoke(null, bridge)
            
        } catch (e: ClassNotFoundException) {
            Bukkit.getLogger().warning("[IslandNpc-TypeWriter] 无法注册服务：主插件版本过旧")
        } catch (e: Exception) {
            Bukkit.getLogger().warning("[IslandNpc-TypeWriter] 注册服务失败: ${e.message}")
            e.printStackTrace()
        }

        initialized = true
    }

    /**
     * 卸载服务
     */
    @JvmStatic
    fun shutdown() {
        if (!initialized) {
            return
        }

        // 取消注册服务
        try {
            val registryClass = Class.forName("com.magicbili.islandnpc.api.TypeWriterServiceRegistry")
            val unregisterMethod = registryClass.getMethod("unregister")
            unregisterMethod.invoke(null)
        } catch (e: Exception) {
            // 忽略错误
        }

        initialized = false
    }
    
    /**
     * 检查服务是否已初始化
     */
    @JvmStatic
    fun isInitialized(): Boolean {
        return initialized && IslandNpcAPI.isInitialized()
    }
}
