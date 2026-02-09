package ru.mrbedrockpy.craftengine.gui.screen;

import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.craftengine.gui.screen.layout.GridLayout;
import ru.mrbedrockpy.craftengine.gui.screen.layout.Layout;
import ru.mrbedrockpy.craftengine.gui.screen.widget.SlotWidget;
import ru.mrbedrockpy.craftengine.registry.Registries;
import ru.mrbedrockpy.craftengine.util.id.RL;
import ru.mrbedrockpy.craftengine.world.inventory.PlayerInventory;
import ru.mrbedrockpy.craftengine.world.item.ItemStack;
import ru.mrbedrockpy.craftengine.renderer.gui.DrawContext;
import ru.mrbedrockpy.craftengine.renderer.window.Window;

public class InventoryScreen extends Screen {

    private static final int VERTICAL_OFFSET = -20;

    private final PlayerInventory inventory;

    private float animProgress = 0f;
    private boolean animating = true;

    public InventoryScreen(PlayerInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public void init() {
        super.init();
        GridLayout grid = new GridLayout()
                .columns(4)
                .gap(0, 0)
                .padding(0)
                .align(GridLayout.Align.FILL, GridLayout.Align.CENTER)
                .bounds(0, 0, 4 * 18, 7 * 18)
                .moveToCenter()
                .origin(43, VERTICAL_OFFSET);

        for (int i = 9; i < 37; i++) {
            final int slotIndex = i;
            SlotWidget w = new SlotWidget(
                    Window.scaledWidth() / 2 - 40, 300,
                    () -> inventory.getStack(slotIndex),
                    (stack) -> inventory.setStack(slotIndex, stack),
                    this::slotClick
            );
            grid.addWidget(String.valueOf(slotIndex), w);
        }

        addLayout(grid);
        Layout layout = new Layout();
        for(int i = 0; i < inventory.getArmorSlots().length; i++) {
            final int slotIndex = i;
            SlotWidget w = new SlotWidget(
                    Window.scaledWidth() / 2 + i * 18 - 83, 153 + VERTICAL_OFFSET,
                    () -> inventory.getArmor(slotIndex),
                    st -> inventory.setArmor(slotIndex, st),
                    this::slotClick
            );
            layout.addWidget("armor_" + slotIndex, w);
        }
        addLayout(layout);

        animProgress = 0f;
        animating = true;
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
            int max = cursor.getItem().getMaxStackSize();
            int toMove = Math.min(cursor.getCount(), max);
            ItemStack moved = cursor.copy();
            slot.stack(moved);

            cursor.decrement(toMove);
            inv.setCursorStack(cursor.isEmpty() ? ItemStack.EMPTY : cursor);
            return;
        }

        // 3) Слот не пуст и курсор не пуст
        if (!slotStack.isEmpty() && !cursor.isEmpty()) {
            boolean same = slotStack.getItem() == cursor.getItem() && slotStack.merge(cursor);

            if (same) {
                // MERGE: докинуть из курсора в слот до лимита
                int max = slotStack.getItem().getMaxStackSize();
                int space = Math.max(0, max - slotStack.getCount());
                if (space > 0) {
                    int toMove = Math.min(cursor.getCount(), space);
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
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (animating) {
            float speed = 8f;
            animProgress += (1f - animProgress) * (1f - (float)Math.exp(-speed * delta));
            if (animProgress > 0.999f) {
                animProgress = 1f;
                animating = false;
            }
        }

        int w = Window.scaledWidth();
        int h = Window.scaledHeight();
        float cx = w / 2f;
        float cy = h / 2f;

        var ms = ctx.getMatrices();
        ms.push();

        // масштаб + лёгкий подъём
        float scale = 0.8f + 0.2f * animProgress;
        ms.translate(cx, cy - (1f - animProgress) * 40f, 0);
        ms.scale(scale, scale, 1);
        ms.translate(-cx, -cy, 0);

        // прозрачность (альфа)
        float alpha = animProgress;

        // фон инвентаря с альфой
        ctx.setShaderColor(1f, 1f, 1f, alpha);
        ctx.drawTexture(
                (int)(cx - (float) 180 / 2),
                (int)(cy - (float) 140 / 2) + VERTICAL_OFFSET,
                176, 140, 0, 0, 176, 140,
                RL.of("gui/inventory.png")
        );
        ctx.setShaderColor(1f, 1f, 1f, 1f);

        super.render(ctx, mouseX, mouseY, delta);
        ms.pop();

        // курсор поверх, не анимируется
        ItemStack cursorStack = inventory.getCursorStack();
        if (cursorStack != null && !cursorStack.isEmpty()) {
            int sx = mouseX;
            int sy = mouseY;
            ctx.drawTexture(sx - 9, sy - 9, 17, 17,
                    RL.of("gui/" + Registries.ITEMS.getRL(cursorStack.getItem()).path() + ".png"));
        }
    }
}