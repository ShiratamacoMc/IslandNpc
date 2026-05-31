package com.magicbili.islandnpc.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * 调度器工具类 - 兼容 Folia 和 Spigot/Paper
 * 
 * Folia 使用区域化调度器，需要指定实体或位置
 * Spigot/Paper 使用全局调度器
 * 
 * 注意：使用 Folia API 编译，它包含了 Paper 和 Spigot API
 * 
 * @author magicbili
 */
public class SchedulerUtil {
    
    private static final boolean IS_FOLIA;
    
    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        IS_FOLIA = folia;
    }
    
    /**
     * 检查是否运行在 Folia 上
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }
    
    /**
     * 在主线程上运行任务
     * 
     * @param plugin 插件实例
     * @param task 要执行的任务
     */
    public static void runTask(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * 延迟在主线程上运行任务
     * 
     * @param plugin 插件实例
     * @param task 要执行的任务
     * @param delay 延迟时间（ticks）
     */
    public static void runTaskLater(Plugin plugin, Runnable task, long delay) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
    
    /**
     * 异步运行任务
     * 
     * @param plugin 插件实例
     * @param task 要执行的任务
     */
    public static void runTaskAsynchronously(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
    
    /**
     * 延迟异步运行任务
     * 
     * @param plugin 插件实例
     * @param task 要执行的任务
     * @param delay 延迟时间（ticks）
     */
    public static void runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay) {
        if (IS_FOLIA) {
            long delayMillis = delay * 50; // 转换为毫秒
            Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
        }
    }
    
    /**
     * 在实体所在区域运行任务（Folia 专用）
     * 
     * @param plugin 插件实例
     * @param entity 实体
     * @param task 要执行的任务
     */
    public static void runAtEntity(Plugin plugin, Entity entity, Runnable task) {
        if (IS_FOLIA) {
            entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * 延迟在实体所在区域运行任务（Folia 专用）
     * 
     * @param plugin 插件实例
     * @param entity 实体
     * @param task 要执行的任务
     * @param delay 延迟时间（ticks）
     */
    public static void runAtEntityLater(Plugin plugin, Entity entity, Runnable task, long delay) {
        if (IS_FOLIA) {
            entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), null, delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
    
    /**
     * 在指定位置所在区域运行任务（Folia 专用）
     * 
     * @param plugin 插件实例
     * @param location 位置
     * @param task 要执行的任务
     */
    public static void runAtLocation(Plugin plugin, Location location, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * 延迟在指定位置所在区域运行任务（Folia 专用）
     * 
     * @param plugin 插件实例
     * @param location 位置
     * @param task 要执行的任务
     * @param delay 延迟时间（ticks）
     */
    public static void runAtLocationLater(Plugin plugin, Location location, Runnable task, long delay) {
        if (IS_FOLIA) {
            Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> task.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
}
