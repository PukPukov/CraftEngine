package ru.mrbedrockpy.craftengine.core.world.item;

/**
 * «Стайк» предмета — экземпляр Item + количество.
 */
public class ItemStack {
    public static final ItemStack EMPTY = new ItemStack(Items.AIR, 0);
    private final Item item;
    private int count;
    public ItemStack(Item item){
        this(item, 1);
    }

    public ItemStack(Item item, int count) {
        if (count < 0 || count > item.getMaxStackSize()) {
            throw new IllegalArgumentException("Invalid stack size");
        }
        this.item = item;
        this.count = count;
    }

    public Item item() { return item; }
    public int count() { return count; }
    public void setCount(int count) {
        if (count < 0 || count > item.getMaxStackSize()) {
            throw new IllegalArgumentException("Invalid stack size");
        }
        this.count = count;
    }

    public boolean isOf(Item item) {
        return this.item.equals(item);
    }

    public void increment(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be non-negative");
        this.count = Math.min(this.count + amount, item.getMaxStackSize());
    }

    /** Уменьшить количество на amount (и не уйти в минус) */
    public void decrement(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be non-negative");
        this.count = Math.max(this.count - amount, 0);
    }

    public boolean isEmpty() {
        return this == EMPTY || count == 0;
    }

    /**
     * Пытается добавить содержимое другого стека того же Item.
     * @return true, если всё «влезло»; false — если остались лишние.
     */
    public boolean merge(ItemStack other) {
        if (!this.item.equals(other.item)) return false;
        int space = item.getMaxStackSize() - this.count;
        int toTransfer = Math.min(space, other.count);
        this.count += toTransfer;
        other.count -= toTransfer;
        return other.count == 0;
    }

    public ItemStack copy() {
        return new ItemStack(this.item, this.count);
    }

    @Override
    public String toString() {
        return isEmpty()
                ? "ItemStack.EMPTY"
                : "ItemStack[" + item.getDisplayName() + " x" + count + "]";
    }
}