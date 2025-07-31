package ru.mrbedrockpy.craftengine.gui.screen.widget;

import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.renderer.gui.DrawContext;
import ru.mrbedrockpy.craftengine.world.ClientWorld;
import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;

import java.awt.*;
import java.util.function.Consumer;

public class ButtonWidget extends AbstractWidget{
    private final String text;
    private final Consumer<ButtonWidget> onClick;

    public ButtonWidget(String text, int x, int y, int width, int height, Consumer<ButtonWidget> onClick) {
        super(x, y, width, height);
        this.onClick = onClick;
        this.text = text;
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int button) {
        onClick.accept(this);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawRect(x, y, width, height, Color.GREEN);
        context.drawCentredText(text, x + width / 2, y + height / 2);
    }
}
