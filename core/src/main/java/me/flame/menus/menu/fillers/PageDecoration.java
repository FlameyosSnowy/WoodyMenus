package me.flame.menus.menu.fillers;


import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.ItemData;

import me.flame.menus.menu.Pagination;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({ "unused", "DataFlowIssue" })
public final class PageDecoration implements MenuFiller {
    @NotNull
    private final Pagination menu;
    
    private PageDecoration(@NotNull Pagination menu) {
        this.menu = menu;
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static PageDecoration create(Pagination menu) {
        return new PageDecoration(menu);
    }

    public void fillBorders(ItemStack itemStack) {
        fillBorders(MenuItem.of(itemStack));
    }

    public void fillBorders(Material borderMaterial) {
        fillBorders(MenuItem.of(new ItemStack(borderMaterial)));
    }

    public void fillBorders(MenuItem item) {
        final int size = menu.size();
        final int pages = menu.pages().size();
        for (int pageIndex = 0; pageIndex < pages; pageIndex++) {
            final var page = menu.getPage(pageIndex);
            for (int i = 0; i < size; i++) {
                if (MenuFiller.isBorderSlot(i, size)) page.setItem(i, item);
            }
        }
    }

    public void fill(Material borderMaterial) {
        fill(MenuItem.of(new ItemStack(borderMaterial)));
    }

    public void fill(@NotNull MenuItem menuItem) {
        final int size = menu.size();
        final int pages = menu.pages().size();
        for (int pageIndex = 0; pageIndex < pages; pageIndex++) {
            final var page = menu.getPage(pageIndex);
            for (int i = 0; i < size; i++) {
                if (!page.hasItem(i)) page.setItem(i, menuItem);
            }
        }
    }

    public void fill(ItemStack itemStack) {
        fill(MenuItem.of(itemStack));
    }

    public void fillRow(final int row, Material material) {
        fillRow(row, MenuItem.of(new ItemStack(material)));
    }

    public void fillRow(final int row, ItemStack itemStack) {
        fillRow(row, MenuItem.of(itemStack));
    }

    public void fillRow(final int row, MenuItem itemStack) {
        if (row < 1 || row > 6) return;
        final int sizedRow = row * 9, rowSize = sizedRow + 9;
        final int size = menu.size(), pages = menu.pages().size();
        for (int pageIndex = 0; pageIndex < pages; pageIndex++) {
            final var page = menu.getPage(pageIndex);
            for (int i = sizedRow; i < rowSize; i++) page.setItem(i, itemStack);
        }
    }

    public void fillArea(final int length, final int width, Material borderMaterial) {
        fillArea(length, width, MenuItem.of(new ItemStack(borderMaterial)));
    }

    public void fillArea(final int length, final int width, ItemStack itemStack) {
        fillArea(length, width, MenuItem.of(itemStack));
    }

    public void fillArea(final int length, final int width, MenuItem itemStack) {
        final int size = menu.size();
        final int pages = menu.pages().size();
        for (int pageIndex = 0; pageIndex < pages; pageIndex++) {
            final var page = menu.getPage(pageIndex);
            for (int i = 0; i < size; i++) if (MenuFiller.isInArea(i, length, width)) page.setItem(i, itemStack);
        }
    }

    @Override
    public void fillSide(Filler.Side side, Material borderMaterial) {
        MenuItem item = MenuItem.of(new ItemStack(borderMaterial));
        final int size = menu.size(), pages = menu.pages().size();
        for (int pageIndex = 0; pageIndex < pages; pageIndex++) {
            final var page = menu.getPage(pageIndex);
            fillAskedSide(side, item, page);
        }
    }

    void fillAskedSide(Filler.Side side, MenuItem item, ItemData menu) {
        final int rows = menu.size() / 9;
        switch (side) {
            case TOP:
                fillRow(1, item);
                break;
            case BOTTOM:
                fillRow(rows, item);
                break;
            // implement LEFT and RIGHT from scratch; like filling vertical rows
            case LEFT:
                for (int i = 0; i < rows; i++) menu.setItem(i, item);
                break;
            case RIGHT:
                int size = menu.size();
                for (int i = 8; i < size; i += 9) menu.setItem(i, item);
                break;
            case LEFT_RIGHT:
                for (int i = 0; i < rows; i++) {
                    menu.setItem(i, item);
                    menu.setItem(i + 8, item);
                }
                break;
        }
    }

    @Override
    public void fillSide(Filler.Side side, ItemStack borderMaterial) {
        MenuItem item = MenuItem.of(borderMaterial);

        final int size = menu.size(), pages = menu.pages().size();
        for (int pageIndex = 0; pageIndex < pages; pageIndex++) {
            final var page = menu.getPage(pageIndex);
            fillAskedSide(side, item, page);
        }
    }

    @Override
    public void fillSide(Filler.Side side, MenuItem borderMaterial) {
        final int size = menu.size(), pages = menu.pages().size();
        for (int pageIndex = 0; pageIndex < pages; pageIndex++) {
            final var page = menu.getPage(pageIndex);
            fillAskedSide(side, borderMaterial, page);
        }
    }

    // simple geometry to check if a slot is in an area of L*W

    private static boolean isInArea(final int slot, final int length, final int width) {
        final int rows = length / 9;
        final int startRow = slot / 9;
        return startRow < rows && slot % 9 < width;
    }
}
