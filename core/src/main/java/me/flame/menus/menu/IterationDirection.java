package me.flame.menus.menu;

@SuppressWarnings("unused")
public enum IterationDirection {
    HORIZONTAL {
        @Override
        public int shift(int slot, int size) {
            // Shift to the next slot horizontally
            return slot >= size ? -1 : slot + 1;
        }
    },

    VERTICAL {
        @Override
        public int shift(int slot, int size) {
            int col = (slot % 9) + 1;
            int row = ((slot - 1) / 9) + 1;
            if (row == 6) {
                if (col == 9) {
                    return -1;
                } else {
                    return slot - (size - 9);
                }
            }
            return (slot + 9 > size) ? -1 : slot + 9;
        }
    },

    UPWARDS_ONLY {
        @Override
        public int shift(int slot, int size) {
            return (slot - 9 < 0) ? -1 : slot - 9;
        }
    },

    DOWNWARDS_ONLY {
        @Override
        public int shift(int slot, int size) {
            return (slot + 9 >= size) ? -1 : slot + 9;
        }
    },

    RIGHT_ONLY {
        @Override
        public int shift(int slot, int size) {
            return slot / 9 != ((slot + 1) / 9) ? -1 : slot + 1;
        }
    },

    LEFT_ONLY {
        @Override
        public int shift(int slot, int size) {
            return slot / 9 != ((slot + 1) / 9) ? -1 : slot - 1;
        }
    },

    RIGHT_UPWARDS_ONLY {
        @Override
        public int shift(int slot, int size) {
            return (slot - 8 < 0 || slot - 8 >= size) ? -1 : slot - 8;
        }
    },

    RIGHT_DOWNWARDS_ONLY {
        @Override
        public int shift(int slot, int size) {
            return (slot + 10 >= size) ? -1 : slot + 10;
        }
    },

    LEFT_UPWARDS {
        @Override
        public int shift(int slot, int size) {
            return (slot - 10 < 0 || slot - 10 >= size) ? -1 : slot - 10;
        }
    },

    LEFT_DOWNWARDS {
        @Override
        public int shift(int slot, int size) {
            return (slot + 8 >= size) ? -1 : slot + 8;
        }
    },

    BACKWARDS_HORIZONTAL {
        @Override
        public int shift(int slot, int size) {
            return (slot - 1 < 0) ? -1 : slot - 1;
        }
    },

    BACKWARDS_VERTICAL {
        @Override
        public int shift(int slot, int size) {
            return (slot - size < 0) ? -1 : slot - size;
        }
    };

    public abstract int shift(int slot, int size);
}