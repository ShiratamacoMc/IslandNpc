package com.magicbili.islandnpc.typewriter.entries.entity

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.entry.StaticEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.engine.paper.utils.Sound
import org.bukkit.entity.Player
import java.util.*

@Entry("island_npc_reference", "本岛屿 NPC 引用", Colors.ORANGE, "mdi:island")
@Tags("island_npc_reference")
/**
 * 本岛屿 NPC 引用
 * 
 * 这个 Entry 不会创建新的 NPC，而是引用由 IslandNpc 插件创建的岛屿 NPC。
 * 
 * ## 工作原理
 * - IslandNpc 插件使用 Citizens2 或 FancyNpcs 为每个岛屿创建 NPC
 * - 这个 Entry 让你在 TypeWriter 中引用"玩家自己岛屿的 NPC"
 * - 可以在对话、任务、事件中使用这个引用
 * 
 * ## 如何使用
 * 1. 确保 IslandNpc 插件已配置为使用 Citizens 或 FancyNpcs
 * 2. 在 Manifest 页面创建此引用
 * 3. 在任务目标、事件触发器中使用这个引用
 * 4. 玩家交互自己岛屿的 NPC 时会触发相应的任务/对话
 * 
 * ## 注意
 * - 这个引用会自动匹配玩家所在岛屿的 NPC
 * - 玩家只能与自己岛屿的 NPC 交互
 * - 不需要为每个岛屿创建单独的引用
 */
class IslandNpcReference(
    override val id: String = "",
    override val name: String = "",
    @Help("显示名称")
    override val displayName: Var<String> = ConstVar("本岛屿 NPC"),
    @Help("声音设置")
    override val sound: Var<Sound> = ConstVar(Sound.EMPTY),
    @Help("描述信息，帮助你识别这个 NPC 引用的用途")
    val description: String = "自动匹配玩家岛屿的 NPC",
) : SoundSourceEntry, SpeakerEntry, StaticEntry {
    
    override fun getEmitter(player: Player): SoundEmitter {
        // 返回玩家的实体 ID 作为声音发射源
        // 实际的 NPC 由 IslandNpc 插件管理
        return SoundEmitter(player.entityId)
    }
    
    /**
     * 检查某个 NPC 是否是岛屿 NPC
     * 
     * @param player 玩家
     * @param npcId NPC ID（Citizens 的 NPC ID 或实体 ID）
     * @return 是否匹配
     */
    fun matches(player: Player, npcId: Int): Boolean {
        // 这个方法会被 IslandNpc 插件调用来检查匹配
        // 主插件会判断这个 NPC 是否属于玩家的岛屿
        return true // 由主插件的检查器来判断
    }
    
    /**
     * 检查某个岛屿 UUID 是否匹配
     * 
     * @param player 玩家
     * @param islandUUID 要检查的岛屿 UUID
     * @return 玩家是否在这个岛屿上
     */
    fun matchesIsland(player: Player, islandUUID: UUID): Boolean {
        try {
            val sPlayer = com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI.getPlayer(player)
            val playerIsland = sPlayer.island
            
            if (playerIsland == null) {
                return false
            }
            
            return playerIsland.uniqueId == islandUUID
        } catch (e: Exception) {
            org.bukkit.Bukkit.getLogger().warning(
                "[IslandNpc-TypeWriter] 检查岛屿匹配时出错: ${e.message}"
            )
            e.printStackTrace()
            return false
        }
    }
}
