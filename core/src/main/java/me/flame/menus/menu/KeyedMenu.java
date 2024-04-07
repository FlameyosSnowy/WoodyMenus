package me.flame.menus.menu;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.Setter;
import me.flame.menus.adventure.TextHolder;
import me.flame.menus.events.PageChangeEvent;
import me.flame.menus.items.MenuItem;
import me.flame.menus.modifiers.Modifier;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class KeyedMenu extends Menu {
    @NotNull
    final Map<String, ItemData> pages;

    private final Map<Integer, MenuItem> pageItems = new HashMap<>();

    @Getter
    private String pageIdentifier;

    private @Getter int nextItemSlot = -1, previousItemSlot = -1;

    @Setter
    Consumer<PageChangeEvent> onPageChange = event -> {};

    /**
     * Adds a blank page to the menu.
     * @return the index the page was added at
     */
    public int addPage(String page) {
        pages.put(page, new ItemData(this));
        if (data == null) data = pages.get(page);
        return pages.size() - 1;
    }

    public void setPageItems(ItemData items) {
        if (pageItems == null) return;
        for (int i : pageItems.keySet()) items.setItem(i, pageItems.get(i));
    }

    /**
     * Main constructor to provide a way to create KeyedMenu
     *
     * @param pageRows The page size.
     */
    private KeyedMenu(final int pageRows, final int pageCount, TextHolder title, EnumSet<Modifier> modifiers) {
        super(pageRows, title, modifiers);
        this.pages = new HashMap<>(pageCount);
    }

    private KeyedMenu(final int pageRows, final int pageCount, TextHolder title, EnumSet<Modifier> modifiers, int nextItemSlot, int previousItemSlot) {
        super(pageRows, title, modifiers);
        this.pages = new HashMap<>(pageCount);
        this.previousItemSlot = previousItemSlot;
        this.nextItemSlot = nextItemSlot;
    }

    /**
     * Main constructor to provide a way to create KeyedMenu
     */
    private KeyedMenu(MenuType type, final int pageCount, TextHolder title, EnumSet<Modifier> modifiers) {
        super(type, title, modifiers);
        this.pages = new HashMap<>(pageCount);
    }

    private KeyedMenu(MenuType type, final int pageCount, TextHolder title, EnumSet<Modifier> modifiers, int nextItemSlot, int previousItemSlot) {
        super(type, title, modifiers);
        this.pages = new HashMap<>(pageCount);
        this.previousItemSlot = previousItemSlot;
        this.nextItemSlot = nextItemSlot;
    }

    public ImmutableList<ItemData> pages() { return ImmutableList.copyOf(pages.values()); }

    public void recreateInventory() {
        super.recreateInventory();
        pages.values().forEach(ItemData::recreateInventory);
    }

    @Override
    public void setContents(MenuItem... items) {
        data.contents(items);
    }

    /**
     * Opens the GUI to a specific page for the given player
     *
     * @param player   The {@link HumanEntity} to open the GUI to
     * @param openPage The specific page to open at
     */
    public void open(@NotNull final HumanEntity player, final String openPage) {
        if (player.isSleeping()) return;

        this.data = pages.get(openPage);
        player.openInventory(inventory);
    }

    /**
     * Gets the number of pages the GUI has
     *
     * @return The number of pages
     */
    public int getPagesSize() {
        return pages.size();
    }

    /**
     * Goes to the specified page
     *
     * @return False if there is no next page.
     */
    public boolean page(String page) {
        ItemData pageData = pages.get(page);
        if (pageData == null) return false;

        this.pageIdentifier = page;
        this.data = pageData;
        update(true);
        return true;
    }

    public Optional<ItemData> getPage(String index) {
        return Optional.ofNullable(pages.get(index));
    }

    public void addPageItems(MenuItem... items) {
        for (ItemData page : pages.values()) page.addItem(items);
    }

    public void addPageItems(ItemStack... items) {
        for (ItemData page : pages.values()) page.addItem(items);
    }

    public void removePageItem(int slot) {
        for (ItemData page : pages.values()) page.removeItem(slot);
    }

    public void removePageItem(ItemStack slot) {
        for (ItemData page : pages.values()) page.removeItem(slot);
    }

    public void removePageItem(MenuItem slot) {
        for (ItemData page : pages.values()) page.removeItem(slot);
    }

    public void removePageItem(ItemStack... slot) {
        Set<ItemStack> set = ImmutableSet.copyOf(slot);
        for (ItemData page : pages.values()) page.removeItem(slot);
    }

    public void removePageItem(MenuItem... slot) {
        Set<MenuItem> set = ImmutableSet.copyOf(slot);
        for (ItemData page : pages.values()) page.removeItem(slot);
    }

    public void setPageItem(int[] slots, MenuItem[] items) {
        int size = slots.length;
        if (size != items.length) throw new IllegalArgumentException("Number of slots and number of items must be equal.");
        for (ItemData page : pages.values()) {
            for (int i = 0; i < size; i++) {
                page.setItem(slots[i], items[i]);
                this.pageItems.put(slots[i], items[i]);
            }
        }
    }

    public void setPageItem(int slot, ItemStack item) {
        setPageItem(slot, MenuItem.of(item));
    }

    public void setPageItem(int slot, MenuItem item) {
        this.pageItems.put(slot, item);
        for (ItemData page : pages.values()) page.setItem(slot, item);
    }

    public void setPageItem(int[] slots, MenuItem item) {
        int size = slots.length;
        for (ItemData page : pages.values()) {
            for (int slot : slots) {
                page.setItem(slot, item);
                this.pageItems.put(slot, item);
            }
        }
    }

    public @NotNull MenuData getMenuData() { return MenuData.intoData(this); }

    public void setContents(ItemData data) {
        this.data = data;
        this.changed = true;
    }
}