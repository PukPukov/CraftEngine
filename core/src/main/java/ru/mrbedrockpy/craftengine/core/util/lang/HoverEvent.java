package ru.mrbedrockpy.craftengine.core.util.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Getter
@AllArgsConstructor
public class HoverEvent {

    private final HoverAction action;
    private final Object contents;

    public Map<String, Object> toJson() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("action", action.name().toLowerCase(Locale.ROOT));
        if (contents instanceof Component c) {
            m.put("contents", c.toJson());
        } else {
            m.put("contents", contents);
        }
        return m;
    }

    enum HoverAction {
        SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY
    }
}
