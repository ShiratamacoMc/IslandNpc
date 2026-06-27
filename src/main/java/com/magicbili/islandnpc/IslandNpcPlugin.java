package com.magicbili.islandnpc;

import com.magicbili.islandnpc.api.HologramProvider;
import com.magicbili.islandnpc.api.IslandProvider;
import com.magicbili.islandnpc.api.NpcProvider;
import com.magicbili.islandnpc.commands.IslandNpcCommand;
import com.magicbili.islandnpc.config.ConfigManager;
import com.magicbili.islandnpc.hologram.HologramProviderFactory;
import com.magicbili.islandnpc.npc.NpcProviderFactory;
import com.magicbili.islandnpc.providers.BentoBoxProvider;
import com.magicbili.islandnpc.providers.SkylliaProvider;
import com.magicbili.islandnpc.providers.SuperiorSkyblockProvider;
import com.magicbili.islandnpc.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * IslandNpc Plugin - 岛屿NPC插件
 * 为SuperiorSkyblock2岛屿创建和管理NPC
 * 
 * @author magicbili
 */
public class IslandNpcPlugin extends JavaPlugin {

    private static IslandNpcPlugin instance;
    private ConfigManager configManager;
    private NpcProvider npcProvider;
    private IslandProvider islandProvider;
    private HologramProvider hologramProvider;

    @Override
    public void onEnable() {
        instance = this;

        // 初始化配置
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // 初始化岛屿提供者
        islandProvider = initializeIslandProvider();
        if (islandProvider == null) {
            getLogger().severe("未找到支持的岛屿插件！请安装 SuperiorSkyblock2、BentoBox 或 Skyllia");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 初始化NPC提供者
        npcProvider = NpcProviderFactory.createProvider(this);
        if (npcProvider == null) {
            getLogger().severe("未找到支持的NPC插件！请安装 Citizens 或 FancyNpcs");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // 初始化全息图提供者(可选)
        hologramProvider = HologramProviderFactory.createProvider(this);
        if (hologramProvider != null && npcProvider instanceof com.magicbili.islandnpc.api.AbstractNpcProvider) {
            ((com.magicbili.islandnpc.api.AbstractNpcProvider) npcProvider).setHologramProvider(hologramProvider);
        }
        
        // 初始化岛屿提供者的监听器（在 NPC 提供者初始化后）
        if (islandProvider instanceof SkylliaProvider) {
            ((SkylliaProvider) islandProvider).initializeListener();
        }

        // 注册监听器
        registerListeners();

        // 注册指令
        registerCommands();
        
        // 延迟加载所有已存在的 NPC
        SchedulerUtil.runTaskLater(this, () -> {
            if (configManager.isDebugEnabled()) {
                getLogger().info("[DEBUG] 开始加载已存在世界中的 NPC...");
            }
            loadAllExistingNpcs();
            
            // 启动后自动清理孤立的NPC
            if (configManager.isDebugEnabled()) {
                getLogger().info("[DEBUG] 开始清理孤立的NPC...");
            }
            int[] result = cleanupOrphanedNpcs();
            if (result[1] > 0) {
                getLogger().info("启动清理：检查了 " + result[0] + " 个NPC，清理了 " + result[1] + " 个孤立NPC");
            }
        }, 100L);

        // 统一输出启用信息
        getLogger().info("插件已启用 (v" + getDescription().getVersion() + ")");
        getLogger().info("  岛屿提供者: " + islandProvider.getProviderName());
        getLogger().info("  NPC提供者: " + npcProvider.getProviderName());
        if (hologramProvider != null) {
            getLogger().info("  全息图提供者: " + hologramProvider.getProviderName());
        } else {
            getLogger().info("  全息图提供者: 未启用");
        }
    }

    @Override
    public void onDisable() {
        // 清理全息图提供者
        if (hologramProvider != null) {
            hologramProvider.cleanup();
            debug("已清理全息图");
        }
        
        // 清理NPC提供者
        if (npcProvider != null) {
            npcProvider.cleanup();
            debug("已保存 " + npcProvider.getProviderName() + " NPC 数据");
        }

        getLogger().info("IslandNpc 插件已禁用！");
    }

    /**
     * 初始化岛屿提供者
     * @return 岛屿提供者实例，如果没有可用的返回null
     */
    private IslandProvider initializeIslandProvider() {
        boolean hasSuperiorSkyblock = Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2") != null;
        boolean hasBentoBox = Bukkit.getPluginManager().getPlugin("BentoBox") != null;
        boolean hasSkyllia = Bukkit.getPluginManager().getPlugin("Skyllia") != null;
        
        if (hasSuperiorSkyblock) {
            debug("检测到 SuperiorSkyblock2，使用其作为岛屿提供者");
            return new SuperiorSkyblockProvider(this);
        } else if (hasBentoBox) {
            debug("检测到 BentoBox，使用其作为岛屿提供者");
            return new BentoBoxProvider(this);
        } else if (hasSkyllia) {
            debug("检测到 Skyllia，使用其作为岛屿提供者");
            return new SkylliaProvider(this);
        }
        
        return null;
    }

    /**
     * 注册事件监听器
     */
    private void registerListeners() {
        // 注册岛屿提供者的事件监听器
        if (islandProvider != null && islandProvider.getEventListener() != null) {
            getServer().getPluginManager().registerEvents(islandProvider.getEventListener(), this);
        }
        
        // 注意：NPC 交互由 FancyNpcs 的 open_dialog 动作处理
        // TypeWriter 交互由 TypeWriter Extension 自己处理
        // 不再需要自定义交互监听器
        
        // 世界加载监听器
        try {
            Class.forName("com.infernalsuite.asp.api.events.LoadSlimeWorldEvent");
            com.magicbili.islandnpc.listeners.WorldLoadListener worldListener = 
                new com.magicbili.islandnpc.listeners.WorldLoadListener(this);
            getServer().getPluginManager().registerEvents(worldListener, this);
        } catch (ClassNotFoundException e) {
            // API 不可用，跳过
        } catch (Exception e) {
            getLogger().severe("注册世界加载监听器失败: " + e.getMessage());
        }
    }

    /**
     * 注册指令
     */
    private void registerCommands() {
        IslandNpcCommand commandHandler = new IslandNpcCommand(this);
        getCommand("islandnpc").setExecutor(commandHandler);
        getCommand("islandnpc").setTabCompleter(commandHandler);
    }

    /**
     * 重载插件配置
     */
    public void reloadPlugin() {
        configManager.loadConfig();
        if (npcProvider != null) {
            npcProvider.reloadAllNpcs();
        }
        debug("插件配置已重载");
    }

    public static IslandNpcPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public NpcProvider getNpcProvider() {
        return npcProvider;
    }
    
    public IslandProvider getIslandProvider() {
        return islandProvider;
    }
    
    /**
     * 输出debug日志（仅在debug模式启用时）
     */
    private void debug(String message) {
        if (configManager != null && configManager.isDebugEnabled()) {
            getLogger().info("[DEBUG] " + message);
        }
    }
    
    /**
     * 加载所有当前已加载世界中的 NPC
     * 在插件启动时调用，延迟 5 秒确保所有插件和世界都已加载
     * 
     * 优化策略（性能优化版本）：
     * 1. 直接跳过所有标记为 SlimeWorld 的岛屿（无需判断世界是否加载）
     * 2. 只处理非 SlimeWorld 且世界已加载的岛屿
     * 3. SlimeWorld 岛屿完全由世界加载事件处理
     * 4. 性能提升：减少世界加载状态检查，启动更快
     */
    private void loadAllExistingNpcs() {
        org.bukkit.configuration.ConfigurationSection section = configManager.getNpcDataConfig().getConfigurationSection("npcs");
        if (section == null) {
            if (configManager.isDebugEnabled()) {
                getLogger().info("[DEBUG] 配置中没有 npcs 节点，无需加载");
            }
            return;
        }
        
        // 获取当前所有已加载的世界名称（性能优化：使用 Set 快速查找）
        java.util.Set<String> loadedWorldNames = new java.util.HashSet<>();
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            loadedWorldNames.add(world.getName());
        }
        
        if (configManager.isDebugEnabled()) {
            getLogger().info("[DEBUG] 当前已加载 " + loadedWorldNames.size() + " 个世界");
        }
        
        int loadedCount = 0;
        int skippedSlimeWorld = 0;
        int skippedUnloaded = 0;
        
        for (String key : section.getKeys(false)) {
            try {
                java.util.UUID islandUUID = java.util.UUID.fromString(key);
                String worldName = section.getString(key + ".location.world");
                
                if (worldName == null) {
                    getLogger().warning("岛屿 " + key + " 的世界名称为空，跳过");
                    continue;
                }
                
                // 优化：直接跳过 SlimeWorld，无需任何判断
                // 注意：如果配置中没有 is_slimeworld 字段（旧数据），需要动态检测
                boolean isSlimeWorld;
                if (section.contains(key + ".is_slimeworld")) {
                    isSlimeWorld = section.getBoolean(key + ".is_slimeworld");
                } else {
                    // 旧数据没有标记，检查世界是否已加载来判断
                    org.bukkit.World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        isSlimeWorld = com.magicbili.islandnpc.utils.WorldUtils.isSlimeWorld(world);
                        if (configManager.isDebugEnabled()) {
                            getLogger().info("[DEBUG] 岛屿 " + key + " 缺少 is_slimeworld 标记，动态检测结果: " + isSlimeWorld);
                        }
                    } else {
                        // 世界未加载，无法判断，假定为普通世界（由世界加载事件处理）
                        isSlimeWorld = false;
                    }
                }
                
                if (isSlimeWorld) {
                    if (configManager.isDebugEnabled()) {
                        getLogger().info("[DEBUG] 跳过 SlimeWorld 岛屿: " + worldName + " (将由世界加载事件处理)");
                    }
                    skippedSlimeWorld++;
                    continue;
                }
                
                // 只处理已加载的普通世界
                if (!loadedWorldNames.contains(worldName)) {
                    if (configManager.isDebugEnabled()) {
                        getLogger().info("[DEBUG] 世界未加载，跳过: " + worldName + " (将由世界加载事件处理)");
                    }
                    skippedUnloaded++;
                    continue;
                }
                
                // 使用 NPC 提供者重新创建 NPC
                if (npcProvider != null && npcProvider.recreateNpc(islandUUID)) {
                    loadedCount++;
                }
            } catch (IllegalArgumentException e) {
                getLogger().warning("无效的岛屿UUID: " + key);
            } catch (Exception e) {
                getLogger().severe("加载岛屿 " + key + " 的 NPC 时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        debug("成功加载了 " + loadedCount + " 个已加载世界的 NPC");
        if (configManager.isDebugEnabled()) {
            if (skippedSlimeWorld > 0) {
                getLogger().info("[DEBUG] 跳过了 " + skippedSlimeWorld + " 个 SlimeWorld 岛屿（性能优化：直接跳过）");
            }
            if (skippedUnloaded > 0) {
                getLogger().info("[DEBUG] 跳过了 " + skippedUnloaded + " 个未加载的普通世界");
            }
            if (loadedCount == 0 && skippedSlimeWorld == 0 && skippedUnloaded == 0) {
                getLogger().info("[DEBUG] 没有需要加载的 NPC");
            }
        }
    }
    
    /**
     * 清理孤立的NPC（岛屿已删除但NPC数据仍存在）
     * 
     * @return int数组：[0]=检查的总数, [1]=删除的数量
     */
    public int[] cleanupOrphanedNpcs() {
        if (npcProvider == null || islandProvider == null) {
            debug("提供者未初始化，无法执行清理");
            return new int[]{0, 0};
        }
        
        int deleted = 0;
        int total = 0;
        
        // 获取所有已记录的岛屿NPC
        java.util.Set<java.util.UUID> allNpcIslands = new java.util.HashSet<>(npcProvider.getAllIslandUUIDs());
        total = allNpcIslands.size();
        
        if (total == 0) {
            debug("没有找到任何已记录的岛屿NPC");
            return new int[]{0, 0};
        }
        
        debug("开始检查 " + total + " 个岛屿NPC...");
        
        // 检查每个岛屿是否仍然存在
        for (java.util.UUID islandUUID : allNpcIslands) {
            // 使用 IslandProvider 的 islandExists 方法验证岛屿是否存在
            if (!islandProvider.islandExists(islandUUID)) {
                // 岛屿不存在，删除NPC
                debug("岛屿 " + islandUUID + " 不存在，删除其NPC");
                if (npcProvider.deleteNpc(islandUUID)) {
                    deleted++;
                    getLogger().info("已清理不存在岛屿的NPC: " + islandUUID);
                } else {
                    getLogger().warning("删除孤立NPC失败: " + islandUUID);
                }
            }
        }
        
        debug("清理完成：检查了 " + total + " 个NPC，删除了 " + deleted + " 个孤立NPC");
        return new int[]{total, deleted};
    }

}
