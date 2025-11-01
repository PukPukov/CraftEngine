package ru.mrbedrockpy.craftengine.client.gui.screen.widget;

import ru.mrbedrockpy.craftengine.core.registry.Registries;
import ru.mrbedrockpy.craftengine.core.util.id.RL;
import ru.mrbedrockpy.craftengine.core.world.item.ItemStack;
import ru.mrbedrockpy.renderer.gui.DrawContext;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SlotWidget extends AbstractWidget {
    private final Supplier<ItemStack> getter;
    private final Consumer<ItemStack> setter;
    
    private final Consumer<SlotWidget> onClick;
    private float lift = 0f;
    private float slideX = 0f;
    
    public SlotWidget(int x, int y,
                      Supplier<ItemStack> getter,
                      Consumer<ItemStack> setter,
                      Consumer<SlotWidget> onClick) {
        super(x, y, 18, 18);
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

        // целевой подъём при ховере
        final float target = isMouseOver(mouseX, mouseY) ? 3f : 0f;

        // скорость «подтягивания» к цели: чем больше, тем быстрее (в 1/сек)
        final float speed = 24f;
        // экспоненциальный шаг с учётом delta (кадрового времени)
        float t = 1f - (float)Math.exp(-speed * delta);
        lift += (target - lift) * t;

        ItemStack s = stack();
        if (s != null && !s.isEmpty()) {
            RL item = Registries.ITEMS.getRL(s.item());
            RL path = RL.of(item.namespace(), "gui/" + item.path() + ".png");
            int yoff = Math.round(lift);  // плавный подъём
            ctx.drawTexture(x + 1, y + 1 - yoff, 16, 16, path);
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