package ru.mrbedrockpy.craftengine.util.lang;

import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Style {

    private TextColor color;
    private Boolean bold;
    private Boolean italic;
    private Boolean underlined;
    private Boolean strikethrough;
    private Boolean obfuscated;
    private ClickEvent clickEvent;
    private HoverEvent hoverEvent;
    private String insertion;

    public static Style empty() {
        return new Style();
    }

    public boolean isEmpty() {
        return color == null && bold == null && italic == null &&
               underlined == null && strikethrough == null && obfuscated == null &&
               clickEvent == null && hoverEvent == null && insertion == null;
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

    public Builder toBuilder() {
        return new Builder();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Builder {
        private TextColor color;
        private Boolean bold;
        private Boolean italic;
        private Boolean underlined;
        private Boolean strikethrough;
        private Boolean obfuscated;
        private ClickEvent clickEvent;
        private HoverEvent hoverEvent;
        private String insertion;

        public Builder color(TextColor color) {
            this.color = color;
            return this;
        }

        public Builder bold(Boolean bold) {
            this.bold = bold;
            return this;
        }

        public Builder italic(Boolean italic) {
            this.italic = italic;
            return this;
        }

        public Builder underlined(Boolean underlined) {
            this.underlined = underlined;
            return this;
        }

        public Builder strikethrough(Boolean strikethrough) {
            this.strikethrough = strikethrough;
            return this;
        }

        public Builder obfuscated(Boolean obfuscated) {
            this.obfuscated = obfuscated;
            return this;
        }

        public Builder clickEvent(ClickEvent clickEvent) {
            this.clickEvent = clickEvent;
            return this;
        }

        public Builder hoverEvent(HoverEvent hoverEvent) {
            this.hoverEvent = hoverEvent;
            return this;
        }

        public Builder insertion(String insertion) {
            this.insertion = insertion;
            return this;
        }

        public Style build() {
            return new Style(color, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent, insertion);
        }
    }
}
