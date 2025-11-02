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

    // анимируемые состояния
    private float lift = 0f;      // вверх
    private float scale = 1f;     // масштаб
    private float slideX = 0f;    // лёгкий сдвиг по X

    // радиус "магнита" курсора в пикселях
    private static final float INFLUENCE_RADIUS = 32f;
    // скорость сглаживания
    private static final float SMOOTH = 44f;

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
        ItemStack s = getter.get();
        return s != null ? s : ItemStack.EMPTY;
    }
    public void stack(ItemStack stack) {
        setter.accept(stack == null ? ItemStack.EMPTY : stack);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        // центр слота
        float cx = x + width * 0.5f;
        float cy = y + height * 0.5f;

        // расстояние до курсора и сила влияния 0..1
        float dx = mouseX - cx;
        float dy = mouseY - cy;
        float dist = (float)Math.hypot(dx, dy);
        float influence = Math.max(0f, 1f - dist / INFLUENCE_RADIUS);

        // цели анимации
        float liftTarget  = 6f * influence;              // до 6px вверх
        float scaleTarget = 1f + 0.18f * influence;      // до +18% масштаба
        float slideTarget = -Math.signum(dx) * 4f * influence; // лёгкий отъезд в сторону от курсора

        // сглаживание как у тебя
        float t = 1f - (float)Math.exp(-SMOOTH * delta);
        lift  += (liftTarget  - lift)  * t;
        scale += (scaleTarget - scale) * t;
        slideX += (slideTarget - slideX) * t;

        // отрисовка: матстэк → translate→scale вокруг центра слота → draw → pop
        var ms = ctx.getMatrices(); // или ctx.matrices(), если у тебя так называется
        ms.push();
        ms.translate(cx + slideX, cy - lift, 0);
        ms.scale(scale, scale, 1f);
        ms.translate(-cx, -cy, 0);

        // фон слота (по желанию)
        // ctx.drawRect(x, y, width, height, new Color(0,0,0,60));

        ItemStack s = stack();
        if (!s.isEmpty()) {
            RL item = Registries.ITEMS.getRL(s.item());
            RL path = RL.of(item.namespace(), "gui/" + item.path() + ".png");
            ctx.drawTexture(x + 1, y + 1, 16, 16, path);
        }

        ms.pop();

        if (isMouseOver(mouseX, mouseY)) {
            ctx.drawRect(x, y, width, height, new Color(255, 255, 0, 40));
        }
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int button) {
        if (visible && isMouseOver(mouseX, mouseY)) onClick.accept(this);
    }
}