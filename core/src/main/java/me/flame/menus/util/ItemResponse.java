package me.flame.menus.util;

import me.flame.menus.menu.ActionResponse;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface ItemResponse {
    ActionResponse apply(Player player, InventoryClickEvent event);
}
