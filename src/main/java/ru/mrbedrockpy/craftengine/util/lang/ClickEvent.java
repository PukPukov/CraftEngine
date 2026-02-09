package ru.mrbedrockpy.craftengine.util.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ClickEvent {

    private final ClickAction action;
    private final String value;

    public Map<String, Object> toJson() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("action", action.name().toLowerCase(Locale.ROOT));
        m.put("value", value);
        return m;
    }

    enum ClickAction {
        OPEN_URL, RUN_COMMAND, SUGGEST_COMMAND, COPY_TO_CLIPBOARD, OPEN_FILE
    }
}
