package ru.mrbedrockpy.craftengine.gui.screen.widget;

import lombok.Getter;
import lombok.Setter;
import ru.mrbedrockpy.renderer.gui.DrawContext;

@Getter
public abstract class AbstractWidget {
    @Setter
    protected int x, y, width, height, zIndex;
    @Setter
    protected boolean visible = true;

    public AbstractWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public abstract void onMouseClick(int mouseX, int mouseY, int button);

    public abstract void render(DrawContext context, int mouseX, int mouseY, float delta);

    public void setPosition(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void setCentredPosition(int x, int y){
        this.x = x - width / 2;
        this.y = y - height / 2;
    }

    public void tick() {
    }
}
