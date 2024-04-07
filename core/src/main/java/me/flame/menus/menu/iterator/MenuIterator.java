package me.flame.menus.menu.iterator;

import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.IMenu;
import me.flame.menus.menu.IterationDirection;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

@SuppressWarnings("unused")
public final class MenuIterator implements Iterator<Optional<MenuItem>> {
    private int position, next;
    private final int size;

    @NotNull
    private final IterationDirection direction;

    @NotNull
    private final IMenu menu;

    private static final String NOTHING_MORE_NEXT =
            "Used MenuIterator#next() but nothing more" +
                    "\nFix: Use hasNext() beforehand to avoid this error.";

    private static final String NOTHING_MORE_NEXT_OPTIONAL =
            "Used MenuIterator#nextOptional() but nothing more" +
                    "\nFix: Use hasNext() beforehand to avoid this error.";

    private static final String NOTHING_MORE_NEXT_NOT_NULL =
            "Used MenuIterator#nextNotNull() but no non-null value was found";

    private static final String GREATER_THAN_ONE_ONLY =
            "Starting row and column must be 1 or greater only." +
                    "\nFix: If you're using an algorithm for rows/cols, you might wanna check it";

    public MenuIterator(int startingRow, int startingCol,
                        @NotNull IterationDirection direction,
                        @NotNull IMenu menu) {
        int prepos = getSlotFromRowCol(startingRow, startingCol);
        
        this.menu = menu;
        this.size = menu.size();        
        if (prepos < 0 || prepos >= size) throw new IllegalArgumentException(GREATER_THAN_ONE_ONLY);
        this.next = prepos;
        this.position = prepos;
        this.direction = direction;
    }

    public MenuIterator(@NotNull IterationDirection direction, @NotNull IMenu menu) {
        this.menu = menu;
        this.next = 0;
        this.position = 0;
        this.size = menu.size();
        this.direction = direction;
    }

    private static int getSlotFromRowCol(int row, int col) {
        return ((row - 1) * 9) + col - 1;
    }

    @Override
    public boolean hasNext() {
        if (next != -1) return true;
        next = direction.shift(position, size);
        return next != -1;
    }

    @Override
    public Optional<MenuItem> next() {
        if (next == -1) throw new NoSuchElementException(NOTHING_MORE_NEXT);
        position = next;
        next = -1;
        return menu.get(position);
    }

    /**
     * Retrieves the next non-null MenuItem in the menu.
     *
     * @return the next non-null MenuItem in the menu
     */
    public Optional<MenuItem> nextNotNull() {
        while (hasNext()) {
            position = next;
            next = direction.shift(position, menu.size());
            if (position == -1)
                throw new NoSuchElementException(NOTHING_MORE_NEXT_NOT_NULL +
                        "\nFix: Everything after slot " + position + " is empty/null."
                );
            Optional<MenuItem> item = menu.get(position);
            if (item.isPresent()) return item;
        }

        throw new NoSuchElementException(NOTHING_MORE_NEXT_NOT_NULL +
                "\nFix: Everything after slot " + position + " is empty/null."
        );
    }
}