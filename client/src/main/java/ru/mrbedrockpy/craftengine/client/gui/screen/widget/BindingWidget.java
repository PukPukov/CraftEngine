package ru.mrbedrockpy.craftengine.client.gui.screen.widget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mrbedrockpy.craftengine.client.keybind.KeyBind;
import ru.mrbedrockpy.renderer.gui.DrawContext;

import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

public class BindingWidget extends ButtonWidget {
    private final KeyBind bind;
    private final Consumer<KeyBind> onChange;
    private boolean listening = false;

    public BindingWidget(@NotNull KeyBind bind, int x, int y, int width, int height, @Nullable Consumer<KeyBind> onChange) {
        super(labelFor(bind), x, y, width, height, b -> {});
        this.bind = Objects.requireNonNull(bind, "bind");
        this.onChange = onChange;
    }

    // --- Рендер: меняем подпись динамически ---
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (listening) {
            setText(bind.getName() + ": <press key...>");
        } else {
            setText(labelFor(bind));
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void onMouseClick(int x, int y, int button) {
        if (!isVisible() || !isMouseOver(x, y)) return;

        if (button == GLFW_MOUSE_BUTTON_RIGHT) {
            unbind();
            return;
        }

        if (listening) {
            setMouseBinding(button);
            return;
        }

        listening = true;
    }

    @Override
    public void onKeyPressed(int keyCode) {
        if (!listening) return;
        if (keyCode == GLFW_KEY_UNKNOWN) return;

        if (keyCode == GLFW_KEY_ESCAPE) {
            listening = false;
            return;
        }

        setKeyboardBinding(keyCode);
    }

    private void setKeyboardBinding(int keyCode) {
        bind.setKey(new KeyBind.Key(KeyBind.Key.Type.KEYBOARD, keyCode));
        listening = false;
        if (onChange != null) onChange.accept(bind);
    }

    private void setMouseBinding(int button) {
        bind.setKey(new KeyBind.Key(KeyBind.Key.Type.MOUSE, button));
        listening = false;
        if (onChange != null) onChange.accept(bind);
    }

    private void unbind() {
        // «Снять биндинг»: договоримся, что code = -1 означает NONE.
        bind.setKey(new KeyBind.Key(KeyBind.Key.Type.KEYBOARD, -1));
        listening = false;
        if (onChange != null) onChange.accept(bind);
    }

    private static String labelFor(KeyBind bind) {
        var k = bind.getKey();
        String keyName;
        if (k == null || k.code() < 0) {
            keyName = "None";
        } else if (k.type() == KeyBind.Key.Type.KEYBOARD) {
            keyName = glfwKeyName(k.code());
        } else {
            keyName = mouseButtonName(k.code());
        }
        return bind.getName() + ": " + keyName;
    }

    // Человеческие названия (можешь заменить на свой маппер/локализацию)
    private static String mouseButtonName(int btn) {
        return switch (btn) {
            case GLFW_MOUSE_BUTTON_LEFT   -> "Mouse Left";
            case GLFW_MOUSE_BUTTON_RIGHT  -> "Mouse Right";
            case GLFW_MOUSE_BUTTON_MIDDLE -> "Mouse Middle";
            default -> "Mouse " + btn;
        };
    }

    private static String glfwKeyName(int key) {
        return switch (key) {
            case GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT -> "Shift";
            case GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL -> "Ctrl";
            case GLFW_KEY_LEFT_ALT, GLFW_KEY_RIGHT_ALT -> "Alt";
            case GLFW_KEY_ENTER -> "Enter";
            case GLFW_KEY_TAB -> "Tab";
            case GLFW_KEY_BACKSPACE -> "Backspace";
            case GLFW_KEY_SPACE -> "Space";
            case GLFW_KEY_ESCAPE -> "Esc";
            default -> {
                yield "Key " + key;
            }
        };
    }
}