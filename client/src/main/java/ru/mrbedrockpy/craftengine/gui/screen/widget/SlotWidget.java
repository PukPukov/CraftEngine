package ru.mrbedrockpy.craftengine.gui.screen.widget;

import org.jetbrains.annotations.Nullable;
import ru.mrbedrockpy.craftengine.registry.Registries;
import ru.mrbedrockpy.craftengine.world.item.ItemStack;
import ru.mrbedrockpy.craftengine.world.item.Items;
import ru.mrbedrockpy.renderer.graphics.Texture;
import ru.mrbedrockpy.renderer.gui.DrawContext;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SlotWidget extends AbstractWidget {
    /** Откуда брать и куда писать ItemStack */
    private final Supplier<ItemStack> getter;
    private final Consumer<ItemStack> setter;

    private final Consumer<SlotWidget> onClick;

    public SlotWidget(int x, int y,
                      Supplier<ItemStack> getter,
                      Consumer<ItemStack> setter,
                      Consumer<SlotWidget> onClick) {
        super(x, y, 16, 16);
        this.getter = getter;
        this.setter = setter;
        this.onClick = onClick;
    }

    /* ---------- API для экрана ---------- */

    /** Текущий стек в этом игровом слоте */
    public ItemStack stack() {
        return getter.get() != null ? getter.get() : ItemStack.EMPTY;
    }

    /** Записать новый стек в слот */
    public void stack(ItemStack stack) {
        setter.accept(stack == null ? ItemStack.EMPTY : stack);
    }

    /* ---------- UI ---------- */

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        ItemStack s = stack();
        if (s != null &&!s.isEmpty()) {
            ctx.drawTexture(x + 1, y + 1, 16, 16, Texture.load(Registries.ITEMS.name(s.item()) + ".png"));
        }
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int button) {
        if (visible && isMouseOver(mouseX, mouseY)) onClick.accept(this);
    }
}
