package ru.mrbedrockpy.craftengine.client.gui.screen;

import org.lwjgl.glfw.GLFW;
import ru.mrbedrockpy.craftengine.client.CraftEngineClient;
import ru.mrbedrockpy.craftengine.client.event.client.KeyPressEvent;
import ru.mrbedrockpy.craftengine.client.gui.screen.layout.Layout;
import ru.mrbedrockpy.craftengine.client.gui.screen.widget.TextField;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.ChatMessagePacketC2S;
import ru.mrbedrockpy.renderer.gui.DrawContext;

import java.awt.*;

public class ChatScreen extends Screen {
    @Override
    public void init() {
        super.init();
        Layout layout = new Layout();
        layout.setOffset(0, height - 11);
        layout.addWidget("input", new TextField(0, -2, width, 11));
        addLayout(layout);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawRect(0, height - 11, width, 11, new Color(0xB3000000, true));
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void onKeyPressed(KeyPressEvent event) {
        super.onKeyPressed(event);
        TextField tf = findWidget("input", TextField.class);
        if(event.getKeyCode() == GLFW.GLFW_KEY_ENTER || event.getKeyCode() == GLFW.GLFW_KEY_KP_ENTER) {
            CraftEngineClient.INSTANCE.gameClient.send(new ChatMessagePacketC2S(tf.getText()));
            tf.clear();
        }
    }
}
