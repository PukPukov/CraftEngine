package ru.mrbedrockpy.craftengine.core.util.lang;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public final class TranslatableComponent extends BaseComponent {

    private String key;
    private Object[] args;

    public TranslatableComponent(String key, Object... args) {
        this.key = key;
        this.args = args == null ? new Object[0] : args;
    }

    @Override
    public Map<String, Object> toJson() {
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
