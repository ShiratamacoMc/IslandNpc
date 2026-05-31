package com.magicbili.islandnpc.typewriter.entries.event

import com.magicbili.islandnpc.typewriter.entries.entity.IslandNpcReference
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry
import com.typewritermc.engine.paper.entry.eventTriggers
import com.typewritermc.engine.paper.entry.triggerFor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

@Entry("island_npc_interact_event", "玩家与岛屿 NPC 交互", Colors.YELLOW, "mdi:island")
/**
 * 岛屿 NPC 交互事件
 * 
 * 当玩家与岛屿 NPC 交互时触发
 * 这个事件会自动检测玩家是否在自己的岛屿，并只响应本岛屿的 NPC
 * 
 * ## 如何使用
 * 1. 在 Manifest 创建"本岛屿 NPC 引用"
 * 2. 创建此事件，选择上面创建的 NPC 引用
 * 3. 配置触发器（对话、任务等）
 * 4. 玩家右键点击自己岛屿的 NPC 时触发
 * 
 * ## 工作流程
 * - IslandNpc 插件创建 NPC（使用 Citizens 或 FancyNpcs）
 * - 玩家交互 NPC
 * - 检测是否有此事件的触发器
 * - 有 → 触发 TypeWriter 对话/任务
 * - 无 → 打开 FancyDialogs 菜单
 */
class IslandNpcInteractEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("本岛屿 NPC 引用 - 选择要触发事件的 NPC")
    val npcReference: Ref<IslandNpcReference> = emptyRef(),
    @Help("是否只响应玩家自己岛屿的 NPC")
    val ownIslandOnly: Boolean = true,
) : EventEntry

// 注意：实际的 NPC 交互监听由 IslandNpc 主插件的 UnifiedNpcInteractListener 处理
// 这里只定义事件条目，供 TypeWriter 编辑器中引用和任务检测

/**
 * 检查玩家是否有活跃的岛屿 NPC 事件
 * 可以从 Java 代码中调用此方法来检查
 */
object IslandNpcInteractChecker {
    
    /**
     * 检查玩家是否有针对本岛屿 NPC 的活跃事件
     * 
     * @param player 玩家
     * @param islandUUID 岛屿 UUID
     * @return 是否有活跃的事件（有任务/对话等待触发）
     */
    @JvmStatic
    fun hasActiveEvent(player: Player, islandUUID: UUID): Boolean {
        val events: Sequence<IslandNpcInteractEventEntry> = Query.find()
        
        return events.any { event ->
            // 1. 必须有触发器
            if (event.triggers.isEmpty()) {
                return@any false
            }
            
            // 2. 检查 NPC 引用配置
            val npcRef = event.npcReference.get()
            
            // 3. 根据配置判断是否匹配
            when {
                // 没有指定 NPC 引用 -> 只在 ownIslandOnly=true 时匹配玩家自己的岛屿
                npcRef == null -> {
                    if (event.ownIslandOnly) {
                        // 检查玩家是否在这个岛屿上
                        try {
                            val sPlayer = com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI.getPlayer(player)
                            val playerIsland = sPlayer.island
                            playerIsland != null && playerIsland.uniqueId == islandUUID
                        } catch (e: Exception) {
                            false
                        }
                    } else {
                        // 如果不限制岛屿，则匹配所有岛屿 NPC（但仍需要是岛屿 NPC）
                        true
                    }
                }
                
                // 有 NPC 引用且要求只匹配玩家自己的岛屿
                event.ownIslandOnly -> {
                    // 检查玩家是否在这个岛屿上
                    npcRef.matchesIsland(player, islandUUID)
                }
                
                // 有 NPC 引用但不限制岛屿 -> 匹配所有岛屿 NPC
                else -> true
            }
        }
    }
    
    /**
     * 获取匹配的事件列表
     * 用于触发对话或任务
     */
    @JvmStatic
    fun getMatchingEvents(player: Player, islandUUID: UUID): List<IslandNpcInteractEventEntry> {
        val events: Sequence<IslandNpcInteractEventEntry> = Query.find()
        
        return events.filter { event ->
            // 1. 必须有触发器
            if (event.triggers.isEmpty()) {
                return@filter false
            }
            
            // 2. 检查 NPC 引用配置
            val npcRef = event.npcReference.get()
            
            // 3. 根据配置判断是否匹配
            when {
                // 没有指定 NPC 引用 -> 只在 ownIslandOnly=true 时匹配玩家自己的岛屿
                npcRef == null -> {
                    if (event.ownIslandOnly) {
                        // 检查玩家是否在这个岛屿上
                        try {
                            val sPlayer = com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI.getPlayer(player)
                            val playerIsland = sPlayer.island
                            playerIsland != null && playerIsland.uniqueId == islandUUID
                        } catch (e: Exception) {
                            false
                        }
                    } else {
                        // 如果不限制岛屿，则匹配所有岛屿 NPC（但仍需要是岛屿 NPC）
                        true
                    }
                }
                
                // 有 NPC 引用且要求只匹配玩家自己的岛屿
                event.ownIslandOnly -> {
                    // 检查玩家是否在这个岛屿上
                    npcRef.matchesIsland(player, islandUUID)
                }
                
                // 有 NPC 引用但不限制岛屿 -> 匹配所有岛屿 NPC
                else -> true
            }
        }.toList()
    }
    
    /**
     * 触发匹配的事件
     * 从 IslandNpc 主插件调用，用于主动触发 TypeWriter 事件
     * 
     * @param player 玩家
     * @param islandUUID 岛屿 UUID
     */
    @JvmStatic
    fun triggerEvents(player: Player, islandUUID: UUID) {
        val matchingEvents = getMatchingEvents(player, islandUUID)
        
        if (matchingEvents.isEmpty()) {
            return
        }
        
        matchingEvents.forEach { eventEntry ->
            try {
                // 转换为 EventTriggers 并触发
                val eventTriggers = eventEntry.triggers.eventTriggers
                eventTriggers.triggerFor(player, context())
            } catch (e: Exception) {
                Bukkit.getLogger().warning("[IslandNpc-TypeWriter] 触发事件 ${eventEntry.name} 失败: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
