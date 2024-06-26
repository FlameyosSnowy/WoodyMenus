package me.flame.menus.menu;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import lombok.Getter;
import lombok.Setter;

import me.flame.menus.adventure.TextHolder;
import me.flame.menus.components.nbt.*;
import me.flame.menus.events.BeforeAnimatingEvent;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.animation.Animation;
import me.flame.menus.menu.fillers.*;
import me.flame.menus.modifiers.Modifier;
import me.flame.menus.util.ItemResponse;
import me.flame.menus.util.VersionHelper;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import org.jetbrains.annotations.*;

import java.time.Duration;
import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * Most commonly used normal Menu
 * <p>
 *
 */
@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class Menu implements IMenu, RandomAccess, Serializable {
    @Getter @NotNull
    protected Inventory inventory;

    @Getter @NotNull
    protected final MenuType type;

    @Getter @NotNull
    protected final EnumSet<Modifier> modifiers;

    protected @NotNull TextHolder title;

    static final @NotNull BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    protected @Getter @Setter MenuFiller defaultFiller = Filler.from(this);

    protected @Getter @Setter boolean dynamicSizing = false, updating = false;

    boolean hasAnimationsStarted = false;

    @Getter
    protected boolean changed = false;

    protected int rows = 1, size;

    protected ItemData data;

    protected ItemResponse[] slotActions;

    @Getter
    final List<Animation> animations = new ArrayList<>(5);

    protected static final Plugin plugin;

    static {
        plugin = JavaPlugin.getProvidingPlugin(Menu.class);
        ItemNbt.wrapper(VersionHelper.IS_PDC_VERSION ? new Pdc(plugin) : new LegacyNbt());
        Bukkit.getPluginManager().registerEvents(new MenuListeners(plugin), plugin);
    }

    @Setter Consumer<InventoryClickEvent> outsideClickAction = event -> {}, bottomClickAction = event -> {}, topClickAction = event -> {}, clickAction = event -> {};
    @Setter BiConsumer<InventoryCloseEvent, Result> closeAction = (event, result) -> {};
    @Setter Consumer<InventoryOpenEvent> openAction = event -> {};
    @Setter Consumer<InventoryDragEvent> dragAction = event -> {};
    @Setter Consumer<BeforeAnimatingEvent> onAnimate = event -> {};

    @Override
    public int rows() { return rows; }

    @Override
    public int size() { return size; }

    Menu(int rows, @NotNull TextHolder title, @NotNull EnumSet<Modifier> modifiers) {
        this.modifiers = modifiers;
        this.rows = rows;
        this.type = MenuType.CHEST;
        this.title = title;
        this.size = rows * 9;
        this.data = new ItemData(this);
        this.inventory = this.title.toInventory(this, size);
    }

    Menu(@NotNull MenuType type, @NotNull TextHolder title, @NotNull EnumSet<Modifier> modifiers) {
        this.type = type;
        this.modifiers = modifiers;
        this.title = title;
        this.size = type.getLimit();
        this.data = new ItemData(this);
        this.inventory = this.title.toInventory(this, type.getType());
    }

    public ItemResponse[] getSlotActions() {
        return (slotActions == null) ? (slotActions = new ItemResponse[size]) : slotActions;
    }

    public boolean hasSlotActions() { return slotActions != null; }

    public MenuFiller getFiller() { return defaultFiller; }

    public <T extends MenuFiller> T getFiller(@NotNull Class<T> value) { return value.cast(defaultFiller); }

    public Stream<MenuItem> stream() { return Arrays.stream(data.getItems()); }

    public Stream<MenuItem> parallelStream() { return stream().parallel(); }

    public void forEach(Consumer<? super MenuItem> action) { data.forEach(action); }

    public List<HumanEntity> getViewers() { return inventory.getViewers(); }

    public boolean addItem(@NotNull final ItemStack... items) {
        return (changed = data.addItem(items));
    }

    public boolean addItem(@NotNull final MenuItem... items) {
        return (changed = data.addItem(items));
    }

    public boolean addItem(@NotNull final List<MenuItem> items) {
        return (changed = addItem(items.toArray(new MenuItem[0])));
    }

    public void setItem(int slot, ItemStack item) {
        this.data.setItem(slot, MenuItem.of(item));
    }

    public void setItem(int slot, MenuItem item) {
        this.data.setItem(slot, item);
        changed = true;
    }

    public Optional<MenuItem> get(int i) {
        return Optional.ofNullable(data.getItem(i));
    }

    public boolean hasItem(int slot) {
        return this.data.hasItem(slot);
    }

    public boolean hasItem(ItemStack item) {
        return data.findFirst(itemOne -> itemOne.getItemStack().equals(item)).isPresent();
    }

    public boolean hasItem(MenuItem item) {
        return data.findFirst(itemOne -> itemOne.equals(item)).isPresent();
    }

    public Optional<MenuItem> get(Predicate<MenuItem> itemDescription) {
        return data.findFirst(itemDescription);
    }

    public void setSlotAction(int slot, ItemResponse response) {
        ItemResponse[] slotActions = getSlotActions();
        slotActions[slot] = response;
    }

    public void removeItem(@NotNull final ItemStack... itemStacks) {
        changed = data.removeItem(itemStacks);
    }

    public void removeItemStacks(@NotNull final List<ItemStack> itemStacks) {
        removeItem(itemStacks.toArray(new ItemStack[0]));
    }

    public void removeItem(@NotNull final MenuItem... items) {
        changed = data.removeItem(items);
    }

    @Override
    public void removeItem(@NotNull final List<MenuItem> itemStacks) {
        removeItem(itemStacks.toArray(new MenuItem[0]));
    }

    @Contract("_ -> new")
    @CanIgnoreReturnValue
    public MenuItem removeItem(int index) {
        MenuItem item = this.data.removeItem(index);
        if (item != null) changed = true;
        return item;
    }

    @Override
    public void update() {
        update(false);
    }

    public void update(boolean force) {
        if (force) {
            updatePlayerInventories(inventory, player -> ((Player) player).updateInventory());
            return;
        }
        if (!changed) return;
        updatePlayerInventories(inventory, player -> ((Player) player).updateInventory());
        this.changed = false;
    }

    public void updatePer(long repeatTime) {
        SCHEDULER.runTaskTimer(plugin, () -> this.update(), 0, repeatTime);
    }

    public void updatePer(@NotNull Duration repeatTime) {
        SCHEDULER.runTaskTimer(plugin, () -> this.update(), 0, repeatTime.toMillis() / 50);
    }

    public void updatePer(long delay, long repeatTime) {
        SCHEDULER.runTaskTimer(plugin, () -> this.update(), delay, repeatTime);
    }

    public void updatePer(@NotNull Duration delay, @NotNull Duration repeatTime) {
        SCHEDULER.runTaskTimer(plugin, () -> this.update(), delay.toMillis() / 50, repeatTime.toMillis() / 50);
    }

    public void updateTitle(String title) { updateTitle(TextHolder.of(title)); }

    public void updateTitle(TextHolder title) {
        Inventory oldInventory = inventory, updatedInventory = copyInventory(type, title, this, rows);
        this.inventory = updatedInventory;
        updatePlayerInventories(oldInventory, player -> player.openInventory(updatedInventory));
    }

    private void updatePlayerInventories(@NotNull Inventory oldInventory, Consumer<HumanEntity> entityPredicate) {
        this.updating = true;
        data.recreateItems(inventory);
        oldInventory.getViewers().forEach(entityPredicate);
        this.updating = false;
    }

    public void open(@NotNull HumanEntity entity) {
        if (!entity.isSleeping()) entity.openInventory(inventory);
    }

    public void close(@NotNull final HumanEntity player) {
        SCHEDULER.runTaskLater(plugin, player::closeInventory, 1L);
    }

    public boolean addModifier(Modifier modifier) { return modifiers.add(modifier); }

    public boolean removeModifier(Modifier modifier) { return modifiers.remove(modifier); }

    public boolean addAllModifiers() { return modifiers.addAll(Modifier.ALL); }

    public void removeAllModifiers() { Modifier.ALL.forEach(modifiers::remove); }

    public boolean areItemsPlaceable() {
        return !modifiers.contains(Modifier.DISABLE_ITEM_ADD);
    }

    public boolean areItemsRemovable() {
        return !modifiers.contains(Modifier.DISABLE_ITEM_REMOVAL);
    }

    public boolean areItemsSwappable() {
        return !modifiers.contains(Modifier.DISABLE_ITEM_SWAP);
    }

    public boolean areItemsCloneable() {
        return !modifiers.contains(Modifier.DISABLE_ITEM_CLONE);
    }

    public void updateItem(final int slot, @NotNull final ItemStack itemStack) {
        data.updateItem(slot, itemStack, this.data.getItem(slot));
    }

    public void setContents(MenuItem... items) {
        changed = true;
        this.data.contents(items);
    }

    @NotNull
    public MenuItem[] getItems() { return this.data.getItems(); }

    public @NotNull ItemData getData() { return new ItemData(this.data); }

    public @NotNull @Unmodifiable List<MenuItem> getItemList() { return ImmutableList.copyOf(getItems()); }

    @Override
    public boolean hasAnimations() { return !animations.isEmpty(); }

    @Override
    public void addAnimation(Animation animation) { animations.add(animation); }

    @Override
    public void removeAnimation(Animation animation) { animations.remove(animation); }

    @Override
    public void removeAnimation(int animationIndex) { animations.remove(animationIndex); }

    @Override
    public boolean hasAnimationsStarted() { return this.hasAnimationsStarted; }

    public void clear() { data = new ItemData(this); }
    @Override
    public boolean allModifiersAdded() { return modifiers.size() == 4; }

    public void recreateInventory() {
        rows++;
        size = rows * 9;
        inventory = copyInventory(type, title, this, size);
        data.recreateInventory();
    }

    private static @NotNull Inventory copyInventory(@NotNull MenuType type, @NotNull TextHolder title, Menu menu, int size) {
        return type == MenuType.CHEST ? title.toInventory(menu, size) : title.toInventory(menu, type.getType());
    }

    public Menu copy() { return MenuData.intoData(this).asMenu(); }

    public @NotNull @Contract(" -> new") static MenuBuilder builder() { return new MenuBuilder(); }

    @Override
    public String getTitle() { return title.toString(); }

    @Override
    public TextHolder title() { return title; }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull MenuLayoutBuilder layout(Map<Character, MenuItem> itemMap) {
        return new MenuLayoutBuilder(itemMap);
    }
}
