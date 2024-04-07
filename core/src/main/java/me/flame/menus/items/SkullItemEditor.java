package me.flame.menus.items;

import me.flame.menus.util.VersionHelper;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

/**
 * @since 2.0.0
 */
public class SkullItemEditor extends ItemEditor {
    private final SkullMeta skullMeta;
    public SkullItemEditor(MenuItem item) {
        super(item);
        this.skullMeta = (SkullMeta) meta;
    }

    @SuppressWarnings("deprecation")
    public SkullItemEditor setOwningPlayer(OfflinePlayer player) {
        if (VersionHelper.IS_SKULL_OWNER_LEGACY) {
            skullMeta.setOwner(player.getName());
            return this;
        }
        skullMeta.setOwningPlayer(player);
        return this;
    }

    @Override
    public MenuItem done() {
        this.item.setItemMeta(skullMeta);
        menuItem.itemStack = this.item;
        menuItem.clickAction = clickAction;
        return menuItem;
    }
}
