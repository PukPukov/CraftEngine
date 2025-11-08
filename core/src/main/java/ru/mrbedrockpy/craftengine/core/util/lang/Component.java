package ru.mrbedrockpy.craftengine.core.util.lang;

import java.util.*;
import java.util.function.Consumer;

public interface Component {
    Style style();
    List<Component> children();
    Component withStyle(Consumer<Style.Builder> mut);
    Component append(Component child);

    Map<String, Object> toJson();

    // factory
    static Component literal(String text) { return new LiteralComponent(text); }
    static Component translatable(String key, Object... args) { return new TranslatableComponent(key, args); }
    static Component keybind(String key) { return new KeybindComponent(key); }

    record Style(TextColor color, Boolean bold, Boolean italic, Boolean underlined, Boolean strikethrough,
                 Boolean obfuscated, ClickEvent clickEvent, HoverEvent hoverEvent, String insertion) {

        public static Style empty() {
            return new Style(null, null, null, null, null, null, null, null, null);
        }

        public boolean isEmpty() {
            return color == null && bold == null && italic == null && underlined == null && strikethrough == null && obfuscated == null
                    && clickEvent == null && hoverEvent == null && insertion == null;
        }

        public Builder toBuilder() {
            return new Builder()
                    .color(color).bold(bold).italic(italic).underlined(underlined)
                    .strikethrough(strikethrough).obfuscated(obfuscated)
                    .clickEvent(clickEvent).hoverEvent(hoverEvent).insertion(insertion);
        }

        public Map<String, Object> toJson() {
            Map<String, Object> m = new LinkedHashMap<>();
            if (color != null) m.put("color", color.toString());
            if (bold != null) m.put("bold", bold);
            if (italic != null) m.put("italic", italic);
            if (underlined != null) m.put("underlined", underlined);
            if (strikethrough != null) m.put("strikethrough", strikethrough);
            if (obfuscated != null) m.put("obfuscated", obfuscated);
            if (insertion != null) m.put("insertion", insertion);
            if (clickEvent != null) m.put("clickEvent", clickEvent.toJson());
            if (hoverEvent != null) m.put("hoverEvent", hoverEvent.toJson());
            return m;
        }

        public static final class Builder {
            private TextColor color;
            private Boolean bold, italic, underlined, strikethrough, obfuscated;
            private ClickEvent clickEvent;
            private HoverEvent hoverEvent;
            private String insertion;

            public Builder color(TextColor c) {
                this.color = c;
                return this;
            }

            public Builder bold(Boolean v) {
                this.bold = v;
                return this;
            }

            public Builder italic(Boolean v) {
                this.italic = v;
                return this;
            }

            public Builder underlined(Boolean v) {
                this.underlined = v;
                return this;
            }

            public Builder strikethrough(Boolean v) {
                this.strikethrough = v;
                return this;
            }

            public Builder obfuscated(Boolean v) {
                this.obfuscated = v;
                return this;
            }

            public Builder clickEvent(ClickEvent e) {
                this.clickEvent = e;
                return this;
            }

            public Builder hoverEvent(HoverEvent e) {
                this.hoverEvent = e;
                return this;
            }

            public Builder insertion(String s) {
                this.insertion = s;
                return this;
            }

            public Style build() {
                return new Style(color, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent, insertion);
            }
        }
    }

}


abstract class BaseComponent implements Component {
    protected Style style = Style.empty();
    protected final List<Component> children = new ArrayList<>();

    @Override public Style style() { return style; }
    @Override public List<Component> children() { return children; }

    @Override public Component withStyle(Consumer<Style.Builder> mut) {
        Style.Builder b = style.toBuilder();
        mut.accept(b);
        this.style = b.build();
        return this;
    }

    @Override public Component append(Component child) {
        this.children.add(child);
        return this;
    }

    protected Map<String, Object> baseJson() {
        Map<String, Object> m = style.isEmpty() ? new LinkedHashMap<>() : style.toJson();
        if (!children.isEmpty()) {
            List<Object> arr = new ArrayList<>(children.size());
            for (Component c : children) arr.add(c.toJson());
            m.put("extra", arr);
        }
        return m;
    }
}
final class LiteralComponent extends BaseComponent {
    private final String text;
    LiteralComponent(String text) { this.text = text; }

    @Override public Map<String, Object> toJson() {
        Map<String, Object> m = baseJson();
        m.put("text", text);
        return m;
    }
}

final class TranslatableComponent extends BaseComponent {
    private final String key;
    private final Object[] args;
    TranslatableComponent(String key, Object... args) {
        this.key = key; this.args = args == null ? new Object[0] : args;
    }

    @Override public Map<String, Object> toJson() {
        Map<String, Object> m = baseJson();
        m.put("translate", key);
        if (args.length > 0) {
            List<Object> with = new ArrayList<>(args.length);
            for (Object a : args) {
                if (a instanceof Component c) with.add(c.toJson());
                else with.add(String.valueOf(a));
            }
            m.put("with", with);
        }
        return m;
    }
}

final class KeybindComponent extends BaseComponent {
    private final String keybind;
    KeybindComponent(String keybind) { this.keybind = keybind; }

    @Override public Map<String, Object> toJson() {
        Map<String, Object> m = baseJson();
        m.put("keybind", keybind);
        return m;
    }
}


enum ClickAction { OPEN_URL, RUN_COMMAND, SUGGEST_COMMAND, COPY_TO_CLIPBOARD, OPEN_FILE }
final class ClickEvent {
    public final ClickAction action;
    public final String value;
    public ClickEvent(ClickAction action, String value){ this.action = action; this.value = value; }
    public Map<String, Object> toJson(){
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("action", action.name().toLowerCase(Locale.ROOT));
        m.put("value", value);
        return m;
    }
}

enum HoverAction { SHOW_TEXT/*(Component)*/, SHOW_ITEM/*string or obj*/, SHOW_ENTITY/*string or obj*/ }
final class HoverEvent {
    public final HoverAction action;
    public final Object contents;
    public HoverEvent(HoverAction action, Object contents){ this.action=action; this.contents=contents; }
    public Map<String, Object> toJson(){
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("action", action.name().toLowerCase(Locale.ROOT));
        if (contents instanceof Component c) m.put("contents", c.toJson());
        else m.put("contents", contents);
        return m;
    }
}
