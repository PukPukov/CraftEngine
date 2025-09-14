package ru.mrbedrockpy.craftengine.gui.screen.widget;

import org.jetbrains.annotations.Nullable;
import ru.mrbedrockpy.craftengine.registry.Registries;
import ru.mrbedrockpy.craftengine.world.item.ItemStack;
import ru.mrbedrockpy.craftengine.world.item.Items;
import ru.mrbedrockpy.renderer.graphics.Texture;
import ru.mrbedrockpy.renderer.gui.DrawContext;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SlotWidget extends AbstractWidget {
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

    public ItemStack stack() {
        return getter.get() != null ? getter.get() : ItemStack.EMPTY;
    }

    public void stack(ItemStack stack) {
        setter.accept(stack == null ? ItemStack.EMPTY : stack);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        ItemStack s = stack();
        if (s != null && !s.isEmpty()) {
            String path = Registries.ITEMS.getName(s.item()) + ".png";
            ctx.drawTexture(x, y, 16, 16, path);
        }

        if (isMouseOver(mouseX, mouseY)) {
            ctx.drawRect(x, y, width, height, new Color(255, 255, 0, 40));
        }
    }
    
    @Override
    public void onMouseClick(int mouseX, int mouseY, int button) {
        if (visible && isMouseOver(mouseX, mouseY)) onClick.accept(this);
    }
}