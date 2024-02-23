package me.flame.menus.util;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface ItemResponse {
    void execute(Player player, InventoryClickEvent event);
}
