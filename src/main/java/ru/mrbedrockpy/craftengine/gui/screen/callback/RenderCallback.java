package ru.mrbedrockpy.craftengine.gui.screen.callback;

import ru.mrbedrockpy.craftengine.render.gui.DrawContext;

@FunctionalInterface
public interface RenderCallback {
    void render(DrawContext context, int mouseX, int mouseY, float delta);
}