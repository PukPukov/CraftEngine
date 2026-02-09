package ru.mrbedrockpy.craftengine.gui.screen.widget;

import lombok.Setter;
import ru.mrbedrockpy.craftengine.util.id.RL;
import ru.mrbedrockpy.craftengine.render.gui.DrawContext;

import java.util.function.Consumer;

public class ButtonWidget extends AbstractWidget{
    @Setter
    private String text;
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
        context.drawTexture(x, y, width, height, RL.of("gui/button.png"));
        context.drawCentredText(text, x + width / 2, y + height / 2);
    }
}
