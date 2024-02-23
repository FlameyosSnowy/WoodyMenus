package me.flame.menus.menu;

import com.google.common.collect.ImmutableList;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.Setter;
import me.flame.menus.adventure.TextHolder;
import me.flame.menus.events.PageChangeEvent;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.fillers.*;
import me.flame.menus.modifiers.Modifier;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Consumer;

/**
 * Menu that allows you to have multiple pages
 * <p>1.1.0: PaginatedMenu straight out of Triumph-GUIS</p>
 * <p>1.4.0: PaginatedMenu rewritten as List<Page></p>
 * <p>2.0.0: PaginatedMenu rewritten as List<ItemData> instead to improve DRY and reduce it by about 250+ lines</p>
 * @since 2.0.0
 * @author FlameyosFlow
 */
@SuppressWarnings("unused")
public final class PaginatedMenu extends Menu implements Pagination {
    @NotNull
    final List<ItemData> pages;

    private final Map<Integer, MenuItem> pageItems = new HashMap<>();

    @Getter
    private int pageNumber;

    private @Getter int nextItemSlot = -1, previousItemSlot = -1;

    @Setter Consumer<PageChangeEvent> onPageChange = event -> {};

    private final MenuFiller pageDecorator = PageDecoration.create(this);

    public <T extends MenuFiller> T getPageDecorator(Class<T> pageClass) {
        return pageClass.cast(pageDecorator);
    }

    public PageDecoration getPageDecorator() {
        return getPageDecorator(PageDecoration.class);
    }

    /**
     * Adds a blank page to the menu.
     * @return the index the page was added at
     */
    public int addPage() {
        pages.add(new ItemData(this));
        return pages.size() - 1;
    }

    public void setPageItems(ItemData items) {
        if(pageItems == null)
            return;
        for (int i : pageItems.keySet()) {
            items.setItem(i, pageItems.get(i));
        }
        if(nextItemSlot != -1)
            items.setItem(nextItemSlot, pages.get(0).getItem(nextItemSlot));
        if(previousItemSlot != -1)
            items.setItem(previousItemSlot, pages.get(0).getItem(previousItemSlot));
    }

    /**
     * Main constructor to provide a way to create PaginatedMenu
     *
     * @param pageRows The page size.
     */
    private PaginatedMenu(final int pageRows, final int pageCount, TextHolder title, EnumSet<Modifier> modifiers) {
        super(pageRows, title, modifiers, true);
        this.pages = new ArrayList<>(pageCount);

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++)
            pages.add(new ItemData(this));
        this.data = pages.get(pageNumber);
    }

    private PaginatedMenu(final int pageRows, final int pageCount, TextHolder title, EnumSet<Modifier> modifiers, int nextItemSlot, int previousItemSlot) {
        super(pageRows, title, modifiers, true);
        this.pages = new ArrayList<>(pageCount);

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++)
            pages.add(new ItemData(this));
        this.data = pages.get(pageNumber);
        this.previousItemSlot = previousItemSlot;
        this.nextItemSlot = nextItemSlot;
    }

    /**
     * Main constructor to provide a way to create PaginatedMenu
     */
    private PaginatedMenu(MenuType type, final int pageCount, TextHolder title, EnumSet<Modifier> modifiers) {
        super(type, title, modifiers, true);
        this.pages = new ArrayList<>(pageCount);

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++)
            pages.add(new ItemData(this));
        this.data = pages.get(pageNumber);
    }

    private PaginatedMenu(MenuType type, final int pageCount, TextHolder title, EnumSet<Modifier> modifiers, int nextItemSlot, int previousItemSlot) {
        super(type, title, modifiers, true);
        this.pages = new ArrayList<>(pageCount);

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++)
            pages.add(new ItemData(this));
        this.data = pages.get(pageNumber);
        this.previousItemSlot = previousItemSlot;
        this.nextItemSlot = nextItemSlot;
    }

    public ImmutableList<ItemData> pages() { return ImmutableList.copyOf(pages); }

    @NotNull
    public static PaginatedMenu create(String title, int rows, int pages) {
        return new PaginatedMenu(rows, pages, TextHolder.of(title), EnumSet.noneOf(Modifier.class));
    }

    @NotNull
    public static PaginatedMenu create(String title, MenuType type, int pages) {
        return new PaginatedMenu(type, pages, TextHolder.of(title), EnumSet.noneOf(Modifier.class));
    }

    @NotNull
    public static PaginatedMenu create(String title, int rows, int pages, EnumSet<Modifier> modifiers) {
        return new PaginatedMenu(rows, pages, TextHolder.of(title), modifiers);
    }

    @NotNull
    public static PaginatedMenu create(TextHolder title, int rows, int pages, EnumSet<Modifier> modifiers, int previousItemSlot, int nextItemSlot) {
        return new PaginatedMenu(rows, pages, title, modifiers, nextItemSlot, previousItemSlot);
    }

    @NotNull
    public static PaginatedMenu create(String title, MenuType type, int pages, EnumSet<Modifier> modifiers) {
        return new PaginatedMenu(type, pages, TextHolder.of(title), modifiers);
    }

    @NotNull
    public static PaginatedMenu create(TextHolder title, MenuType type, int pages, EnumSet<Modifier> modifiers, int previousItemSlot, int nextItemSlot) {
        return new PaginatedMenu(type, pages, title, modifiers, nextItemSlot, previousItemSlot);
    }

    @NotNull
    public static PaginatedMenu create(TextHolder title, int rows, int pages) {
        return new PaginatedMenu(rows, pages, title, EnumSet.noneOf(Modifier.class));
    }

    @NotNull
    public static PaginatedMenu create(TextHolder title, MenuType type, int pages) {
        return new PaginatedMenu(type, pages, title, EnumSet.noneOf(Modifier.class));
    }

    @NotNull
    public static PaginatedMenu create(TextHolder title, int rows, int pages, EnumSet<Modifier> modifiers) {
        return new PaginatedMenu(rows, pages, title, modifiers);
    }

    @NotNull
    public static PaginatedMenu create(TextHolder title, MenuType type, int pages, EnumSet<Modifier> modifiers) {
        return new PaginatedMenu(type, pages, title, modifiers);
    }

    public static @NotNull PaginatedMenu create(MenuData data) {
        Menu menu = data.intoMenu();
        try { return (PaginatedMenu) menu; } catch (ClassCastException error) {
            throw new IllegalArgumentException(
                "Attempted to create a PaginatedMenu from an incompatible MenuData object." +
                "\nExpected PaginatedMenu, but got " + data.getClass().getSimpleName() +
                "\nFix: MenuData must include the size of the pages, or it'll default to 1."
            );
        }
    }

    public void recreateInventory() {
        super.recreateInventory();
        pages.forEach((data) -> {
            if (data != this.data) data.recreateInventory();
        });
    }

    @Override
    public void setContents(MenuItem... items) {
        ItemData itemData = new ItemData(this);
        itemData.contents(items);
        pages.set(pageNumber, itemData);
        data = itemData;
    }

    /**
     * Opens the GUI to a specific page for the given player
     *
     * @param player   The {@link HumanEntity} to open the GUI to
     * @param openPage The specific page to open at
     */
    public void open(@NotNull final HumanEntity player, final int openPage) {
        if (player.isSleeping()) return;

        int pagesSize = pages.size();
        if (openPage < 0 || openPage >= pagesSize) {
            throw new IllegalArgumentException(
                    "\"openPage\" out of bounds; must be 0-" + (pagesSize - 1) +
                    "\nopenPage: " + openPage +
                    "\nFix: Make sure \"openPage\" is 0-" + (pagesSize - 1)
            );
        }

        this.pageNumber = openPage;
        this.data = pages.get(openPage);
        player.openInventory(inventory);
    }

    /**
     * Opens the GUI to a specific page for the given player
     *
     * @param player   The {@link HumanEntity} to open the GUI to
     */
    public void open(@NotNull final HumanEntity player) {
        this.open(player, 0);
    }

    /**
     * Gets the current page number (Inflated by 1)
     *
     * @return The current page number
     */
    @Override
    public int getCurrentPageNumber() {
        return pageNumber + 1;
    }

    /**
     * Gets the number of pages the GUI has
     *
     * @return The number of pages
     */
    @Override
    public int getPagesSize() {
        return pages.size();
    }

    /**
     * Goes to the next page
     *
     * @return False if there is no next page.
     */
    @Override
    public boolean next() {
        int size = pages.size();
        if (pageNumber + 1 >= size) return false;
        int oldPageNum = pageNumber;

        pageNumber++;
        this.data = pages.get(pageNumber);
        super.changed = true;
        update();
        return true;
    }

    /**
     * Goes to the previous page if possible
     *
     * @return False if there is no previous page.
     */
    @Override
    public boolean previous() {
        if (pageNumber - 1 < 0) return false;

        pageNumber--;
        this.data = pages.get(pageNumber);

        super.changed = true;
        update();
        return true;
    }

    /**
     * Goes to the specified page
     *
     * @return False if there is no next page.
     */
    @Override
    public boolean page(int pageNum) {
        int size = pages.size();
        if (pageNum < 0 || pageNum > size) return false;

        this.pageNumber = pageNum;
        this.data = pages.get(pageNum);
        update();
        return true;
    }

    @Override
    public @Nullable ItemData getPage(int index) {
        return (index < 0 || index > pages.size()) ? null : pages.get(index);
    }

    @Override
    public Optional<ItemData> getOptionalPage(int index) {
        return (index < 0 || index > pages.size()) ? Optional.empty() : Optional.ofNullable(getPage(index));
    }

    @Override
    public void addPageItems(MenuItem... items) {
        for (ItemData page : pages) page.addItem(items);
    }

    @Override
    public void addPageItems(ItemStack... items) {
        for (ItemData page : pages) page.addItem(items);
    }

    @Override
    public void removePageItem(int slot) {
        for (ItemData page : pages) page.removeItem(slot);
    }

    @Override
    public void removePageItem(ItemStack slot) {
        for (ItemData page : pages) page.removeItem((item) -> item.getItemStack().equals(slot));
    }

    @Override
    public void removePageItem(MenuItem slot) {
        for (ItemData page : pages) page.removeItem((item) -> item.equals(slot));
    }

    @Override
    public void removePageItem(ItemStack... slot) {
        Set<ItemStack> set = ImmutableSet.copyOf(slot);
        for (ItemData page : pages) page.indexed((item, index) -> { if (set.contains(item.getItemStack())) page.removeItem(index); });
    }

    @Override
    public void removePageItem(MenuItem... slot) {
        Set<MenuItem> set = ImmutableSet.copyOf(slot);
        for (ItemData page : pages) page.indexed((item, index) -> { if (set.contains(item)) page.removeItem(index); });
    }

    @Override
    public void setPageItem(int[] slots, MenuItem[] items) {
        int size = slots.length;
        for (ItemData page : pages) setPageItem0(page, size, slots, items);
    }

    public void addItems(@NotNull MenuItem... items) {
        // make a mutable list of items to add
        List<MenuItem> toAdd = new ArrayList<>();
        ItemData oldPage = data;
        int size = pages.size();
        for (ItemData page : pages) {
            page.addItem(toAdd, items);
            items = toAdd.toArray(new MenuItem[0]);
            toAdd.clear();
        }
        toAdd = new ArrayList<>(List.of(items));
        while (!toAdd.isEmpty()){
            MenuItem[] leftToAdd = toAdd.toArray(new MenuItem[0]);
            toAdd.clear();
            page(addPage());
            getPage(pageNumber).addItem(toAdd, leftToAdd);
        }
        page(0);
        super.changed = true;
        update();
    }

    public void setPageItem(int[] slots, MenuItem item) {
        int size = slots.length;
        for (ItemData page : pages) setPageItem0(page, size, slots, item);
    }

    private void setPageItem0(ItemData page, int size, int[] slots, MenuItem[] items) {
        for (int i = 0; i < size; i++) {
            page.setItem(slots[i], items[i]);
            this.pageItems.put(slots[i], items[i]);
        }
    }

    private void setPageItem0(ItemData page, int size, int[] slots, MenuItem item) {
        for (int i = 0; i < size; i++) {
            page.setItem(slots[i], item);
            this.pageItems.put(slots[i], item);
        }
    }

    @Override
    public void setPageItem(int slot, ItemStack item) {
        setPageItem(slot, MenuItem.of(item));
    }

    public void setPageItem(int slot, MenuItem item) {
        this.pageItems.put(slot, item);
        for (ItemData page : pages) page.setItem(slot, item);
    }

    @Override
    public @NotNull MenuData getMenuData() { return MenuData.intoData(this); }

    public PaginatedMenu copy() {
        return create(getMenuData());
    }

    public void setContents(ItemData data) {
        this.data = data;
    }
}
