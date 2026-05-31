package com.magicbili.islandnpc.providers;

import com.magicbili.islandnpc.IslandNpcPlugin;
import com.magicbili.islandnpc.api.IslandProvider;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Skyllia 岛屿提供者
 * 
 * @author magicbili
 */
public class SkylliaProvider implements IslandProvider {
    
    private final IslandNpcPlugin plugin;
    private SkylliaListener listener;
    
    public SkylliaProvider(IslandNpcPlugin plugin) {
        this.plugin = plugin;
        // 不在构造函数中初始化 listener，因为此时 npcProvider 还未初始化
    }
    
    /**
     * 初始化监听器（在 NPC 提供者初始化后调用）
     */
    public void initializeListener() {
        if (this.listener == null) {
            this.listener = new SkylliaListener(plugin, plugin.getNpcProvider());
        }
    }
    
    @Override
    public String getProviderName() {
        return "Skyllia";
    }
    
    @Override
    public boolean hasIsland(Player player) {
        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        return island != null;
    }
    
    @Override
    public UUID getIslandUUID(Player player) {
        Island island = SkylliaAPI.getIslandByPlayerId(player.getUniqueId());
        return island != null ? island.getId() : null;
    }
    
    @Override
    public Location getIslandCenter(UUID islandUUID) {
        Island island = SkylliaAPI.getIslandByIslandId(islandUUID);
        if (island == null) {
            return null;
        }
        
        // 获取主世界
        World world = Bukkit.getWorld("world");
        if (world == null) {
            // 如果主世界不存在，尝试获取第一个世界
            if (!Bukkit.getWorlds().isEmpty()) {
                world = Bukkit.getWorlds().get(0);
            } else {
                return null;
            }
        }
        
        return island.getCenterLocation(world);
    }
    
    @Override
    public UUID getIslandOwner(UUID islandUUID) {
        Island island = SkylliaAPI.getIslandByIslandId(islandUUID);
        if (island == null) {
            return null;
        }
        Players owner = island.getOwner();
        return owner != null ? owner.getMojangId() : null;
    }
    
    @Override
    public String getIslandOwnerName(UUID islandUUID) {
        Island island = SkylliaAPI.getIslandByIslandId(islandUUID);
        if (island == null) {
            return null;
        }
        Players owner = island.getOwner();
        return owner != null ? owner.getLastKnowName() : null;
    }
    
    @Override
    public boolean islandExists(UUID islandUUID) {
        Island island = SkylliaAPI.getIslandByIslandId(islandUUID);
        return island != null && !island.isDisable();
    }
    
    @Override
    public Listener getEventListener() {
        return listener;
    }
}
