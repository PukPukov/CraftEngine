package ru.mrbedrockpy.craftengine.core.util.lang;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

abstract class BaseComponent implements Component {
    protected Style style = Style.empty();
    protected final List<Component> children = new ArrayList<>();

    @Override public Style style() { return style; }
    @Override public List<Component> children() { return children; }

    @Override
    public Component withStyle(Consumer<Style.Builder> mut) {
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