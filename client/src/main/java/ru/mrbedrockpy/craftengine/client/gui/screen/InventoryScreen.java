package ru.mrbedrockpy.craftengine.client.gui.screen;

import ru.mrbedrockpy.craftengine.client.CraftEngineClient;
import ru.mrbedrockpy.craftengine.client.gui.screen.layout.GridLayout;
import ru.mrbedrockpy.craftengine.client.gui.screen.widget.SlotWidget;
import ru.mrbedrockpy.craftengine.core.registry.Registries;
import ru.mrbedrockpy.craftengine.core.world.inventory.PlayerInventory;
import ru.mrbedrockpy.craftengine.core.world.item.ItemStack;
import ru.mrbedrockpy.renderer.gui.DrawContext;
import ru.mrbedrockpy.renderer.window.Window;

public class InventoryScreen extends Screen {

    private final PlayerInventory inventory;

    public InventoryScreen(PlayerInventory inventory){
        this.inventory = inventory;
    }

    @Override
    public void init() {
        super.init();

        GridLayout layout = new GridLayout()
                .columns(9)
                .gap(0, 0)
                .padding(0)
                .align(GridLayout.Align.FILL, GridLayout.Align.CENTER)
                .bounds(0, 0, 9 * 18, 3 * 18) // ширина/высота сетки под 9×3 слота по 18px
                .moveToCenter();

        // Привязываем каждый слот к своему индексу i
        for (int i = 9; i < 36; i++) {
            final int slotIndex = i;
            SlotWidget w = new SlotWidget(
                    0, 0,
                    () -> inventory.getStack(slotIndex),
                    (stack) -> inventory.setStack(slotIndex, stack),
                    this::slotClick
            );
            layout.addWidget(String.valueOf(slotIndex), w);
        }

        addLayout(layout);
        // НЕ зовём layout.moveWidgetsToLayout(); GridLayout сам разложит в render()
    }

    public void slotClick(SlotWidget slot) {
        PlayerInventory inv = CraftEngineClient.INSTANCE.getPlayer().getInventory();

        ItemStack cursor = inv.getCursorStack();      // копии/ссылки зависят от твоей реализации
        ItemStack slotStack = slot.stack();

        // Нормализуем null -> EMPTY, чтобы не ловить NPE
        if (cursor == null) cursor = ItemStack.EMPTY;
        if (slotStack == null) slotStack = ItemStack.EMPTY;

        // 1) Если курсор пуст и слот не пуст — забираем всё из слота
        if (cursor.isEmpty() && !slotStack.isEmpty()) {
            inv.setCursorStack(slotStack);
            slot.stack(ItemStack.EMPTY);
            return;
        }

        // 2) Если слот пуст и курсор не пуст — кладём из курсора в слот (по максимуму)
        if (slotStack.isEmpty() && !cursor.isEmpty()) {
            int max = cursor.item().getMaxStackSize();
            int toMove = Math.min(cursor.count(), max);
            ItemStack moved = cursor.copy();
            slot.stack(moved);

            cursor.decrement(toMove);
            inv.setCursorStack(cursor.isEmpty() ? ItemStack.EMPTY : cursor);
            return;
        }

        // 3) Слот не пуст и курсор не пуст
        if (!slotStack.isEmpty() && !cursor.isEmpty()) {
            boolean same = slotStack.item() == cursor.item() && slotStack.merge(cursor);

            if (same) {
                // MERGE: докинуть из курсора в слот до лимита
                int max = slotStack.item().getMaxStackSize();
                int space = Math.max(0, max - slotStack.count());
                if (space > 0) {
                    int toMove = Math.min(cursor.count(), space);
                    slotStack.increment(toMove);
                    cursor.decrement(toMove);
                    slot.stack(slotStack);
                    inv.setCursorStack(cursor.isEmpty() ? ItemStack.EMPTY : cursor);
                    return;
                }
                // если места нет — своп
            }

            // SWAP: меняем местами
            ItemStack tmp = slotStack;
            slot.stack(cursor);
            inv.setCursorStack(tmp);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int width = Window.scaledWidth();
        int height = Window.scaledHeight();
        context.drawTexture(width / 2 - 90, height / 2 - 70, 180, 140, 0, 0, 256, 256, "gui/inventory.png");

        super.render(context, mouseX, mouseY, delta);
        // Потом — курсор поверх
        ItemStack cursorStack = inventory.getCursorStack();
        if (cursorStack != null && !cursorStack.isEmpty()) {
            int sx = mouseX;
            int sy = mouseY;
            context.drawTexture(sx - 9, sy - 9, 17, 17,
                    "gui/" + Registries.ITEMS.getName(cursorStack.item()) + ".png");
        }
    }
}