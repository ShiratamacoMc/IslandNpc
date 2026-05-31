package com.magicbili.islandnpc.providers;

import com.magicbili.islandnpc.IslandNpcPlugin;
import com.magicbili.islandnpc.api.NpcProvider;
import com.magicbili.islandnpc.utils.SchedulerUtil;
import fr.euphyllia.skyllia.api.event.SkyblockCreateEvent;
import fr.euphyllia.skyllia.api.event.SkyblockDeleteEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Skyllia 事件监听器
 * 
 * @author magicbili
 */
public class SkylliaListener implements Listener {
    
    private final IslandNpcPlugin plugin;
    private final NpcProvider npcProvider;
    
    public SkylliaListener(IslandNpcPlugin plugin, NpcProvider npcProvider) {
        this.plugin = plugin;
        this.npcProvider = npcProvider;
    }
    
    /**
     * 输出debug日志（仅在debug模式启用时）
     */
    private void debug(String message) {
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
    
    /**
     * 监听岛屿创建事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandCreate(SkyblockCreateEvent event) {
        Island island = event.getIsland();
        UUID islandId = island.getId();
        
        debug("检测到 Skyllia 岛屿创建事件: " + islandId);
        debug("岛屿拥有者: " + event.getOwnerId());
        
        // 延迟创建 NPC，确保岛屿完全加载
        SchedulerUtil.runTaskLater(plugin, () -> {
            if (npcProvider != null) {
                debug("延迟任务执行: 开始为岛屿 " + islandId + " 创建 NPC");
                
                // 获取主世界
                World world = Bukkit.getWorld("world");
                if (world == null && !Bukkit.getWorlds().isEmpty()) {
                    world = Bukkit.getWorlds().get(0);
                }
                
                if (world != null) {
                    Location center = island.getCenterLocation(world);
                    if (center != null) {
                        debug("岛屿中心位置: " + center);
                        Location spawnLoc = calculateSpawnLocation(center);
                        debug("NPC 生成位置: " + spawnLoc);
                        boolean success = npcProvider.createNpc(islandId, spawnLoc);
                        debug("NPC 创建结果: " + success);
                    } else {
                        debug("岛屿中心位置为 null，无法创建 NPC");
                    }
                } else {
                    debug("找不到世界，无法创建 NPC");
                }
            } else {
                debug("NPC 提供者未初始化");
            }
        }, 20L);
    }
    
    /**
     * 监听岛屿删除事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandDelete(SkyblockDeleteEvent event) {
        Island island = event.getIsland();
        if (island != null && npcProvider != null) {
            UUID islandId = island.getId();
            debug("监听到 Skyllia 岛屿删除事件: " + islandId);
            
            // 删除 NPC
            SchedulerUtil.runTask(plugin, () -> {
                npcProvider.deleteNpc(islandId);
            });
        } else {
            debug("岛屿删除事件中的岛屿为 null");
        }
    }
    
    /**
     * 计算NPC生成位置
     */
    private Location calculateSpawnLocation(Location islandCenter) {
        if (islandCenter == null) return null;

        double offsetX = plugin.getConfigManager().getSpawnOffsetX();
        double offsetY = plugin.getConfigManager().getSpawnOffsetY();
        double offsetZ = plugin.getConfigManager().getSpawnOffsetZ();
        float yaw = plugin.getConfigManager().getNpcYaw();
        float pitch = plugin.getConfigManager().getNpcPitch();

        Location spawnLoc = islandCenter.clone().add(offsetX, offsetY, offsetZ);
        spawnLoc.setYaw(yaw);
        spawnLoc.setPitch(pitch);
        
        return spawnLoc;
    }
}
