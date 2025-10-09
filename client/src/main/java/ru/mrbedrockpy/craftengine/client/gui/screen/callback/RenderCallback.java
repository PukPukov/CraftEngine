package ru.mrbedrockpy.craftengine.client.gui.screen.callback;

import ru.mrbedrockpy.renderer.gui.DrawContext;

@FunctionalInterface
public interface RenderCallback {
    void render(DrawContext context, int mouseX, int mouseY, float delta);
}