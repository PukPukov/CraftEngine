package ru.mrbedrockpy.craftengine.client.gui.screen;

import lombok.RequiredArgsConstructor;
import org.lwjgl.glfw.GLFW;
import ru.mrbedrockpy.craftengine.client.CraftEngineClient;
import ru.mrbedrockpy.craftengine.client.event.client.input.KeyPressEvent;
import ru.mrbedrockpy.craftengine.client.gui.screen.layout.Layout;
import ru.mrbedrockpy.craftengine.client.gui.screen.widget.TextField;
import ru.mrbedrockpy.craftengine.client.network.GameClient;
import ru.mrbedrockpy.craftengine.core.util.config.CraftEngineConfig;

@RequiredArgsConstructor
public class MultiplayerScreen extends Screen {
    private final GameClient gameClient;
    private TextField tf;
    @Override
    public void init() {
        super.init();
        Layout layout = new Layout();
        tf = new TextField(width / 2 - 60, height / 2, 120, 20);
        tf.setText(CraftEngineConfig.Network.LAST_IP);
        layout.addWidget("dc", tf);
        addLayout(layout);
    }

    @Override
    public void onKeyPressed(KeyPressEvent event) {
        super.onKeyPressed(event);
        if(event.getKeyCode() == GLFW.GLFW_KEY_ENTER || event.getKeyCode() == GLFW.GLFW_KEY_KP_ENTER) {
            gameClient.connect(tf.getText());
            CraftEngineConfig.Network.LAST_IP = tf.getText();
            CraftEngineClient.INSTANCE.getConfigManager().saveConfigs();
            CraftEngineClient.INSTANCE.play();
        }
    }
}
