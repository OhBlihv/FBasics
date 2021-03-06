package org.originmc.fbasics.listener;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.settings.AntiGlitchSettings;

import java.util.ArrayList;
import java.util.List;

public final class McMMOMiningListener implements Listener {

    private final AntiGlitchSettings settings;

    private final boolean useNewApi;

    public McMMOMiningListener(FBasics plugin) {
        settings = plugin.getSettings().getAntiGlitchSettings();
        useNewApi = plugin.getServer().getClass().getPackage().getName().split("\\.")[3].compareTo("v1_8") >= 0;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void denyPistonGlitch(BlockPistonExtendEvent event) {
        denyPistonGlitch(event, event.getBlocks());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void denyPistonGlitch(BlockPistonRetractEvent event) {
        denyPistonGlitch(event, getBlocks(event));
    }

    private List<Block> getBlocks(BlockPistonRetractEvent event) {
        // No need to do anything special when using the new API.
        if (useNewApi) return event.getBlocks();

        // Since the old API does not have "event.getBlocks()" we'll return a new list of the one affected block.
        List<Block> blocks = new ArrayList<>();
        blocks.add(event.getRetractLocation().getBlock());
        return blocks;
    }

    private void denyPistonGlitch(BlockPistonEvent event, List<Block> blocks) {
        // Do nothing if this module is not enabled.
        if (!settings.isMcmmoMining()) return;

        // Iterate through each effected block and cancel event if block is denied.
        for (Block block : blocks) {
            if (settings.getMcmmoMiningBlocks().contains(block.getType())) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
