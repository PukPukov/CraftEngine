package ru.mrbedrockpy.renderer.font;

import org.joml.Vector2f;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.system.MemoryStack;
import ru.mrbedrockpy.craftengine.core.util.id.RL;
import ru.mrbedrockpy.craftengine.core.util.lang.Component;
import ru.mrbedrockpy.craftengine.core.util.lang.Style;
import ru.mrbedrockpy.craftengine.core.util.lang.TextColor;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.graphics.tex.TextureRegion;
import ru.mrbedrockpy.renderer.window.Window;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.stb.STBTruetype.stbtt_GetPackedQuad;

public final class ComponentRenderer {

    private final GlyphAtlas glyphs;
    private final StickerRegistry stickers;

    public ComponentRenderer(GlyphAtlas glyphs, StickerRegistry stickers) {
        this.glyphs = glyphs;
        this.stickers = stickers != null ? stickers : new StickerRegistry();
    }

    public float draw(QuadBatch batch, Component root, float x, float y, int baseRGBA, Random rng) {
        if (root == null) return x;
        State st = new State(baseRGBA, rng != null ? rng : new Random(0));
        return drawComponent(batch, root, x, y, st);
    }

    public float drawString(QuadBatch batch, String s, float x, float y, int rgba) {
        return draw(batch, Component.literal(s), x, y, rgba, null);
    }

    public Vector2f measure(Component c) {
        float w = measure0(c);
        float h = lineHeightPx();
        return new Vector2f(w, h);
    }

    public Vector2f getTextSize(Component c) {
        Vector2f measure = measure(c);
        return new Vector2f(measure.x * Window.scale().coofX(), measure.y * Window.scale().coofY());
    }

    public int lineHeightPx() {
        return glyphs.ascentPx();
    }

    private static final class State {
        int rgba;
        boolean bold, italic, underline, strike, obfuscated;
        State(int rgba, Random rng) { this.rgba = rgba; this.rng = rng; }
        final Random rng;
    }

    private float drawComponent(QuadBatch b, Component c, float x, float y, State st) {
        Style s = c.getStyle();
        int savedRGBA = st.rgba;
        boolean sb = st.bold, si = st.italic, su = st.underline, ss = st.strike, so = st.obfuscated;
        if (s != null && !s.isEmpty()) {
            if (s.getColor() != null) st.rgba = (0xFF << 24) | rgbFromTextColor(s.getColor());
            if (Boolean.TRUE.equals(s.getBold())) st.bold = true;
            if (Boolean.TRUE.equals(s.getItalic())) st.italic = true;
            if (Boolean.TRUE.equals(s.getUnderlined())) st.underline = true;
            if (Boolean.TRUE.equals(s.getStrikethrough())) st.strike = true;
            if (Boolean.TRUE.equals(s.getObfuscated())) st.obfuscated = true;
        }

        Object text = c.toJson().get("text");
        Object translate = c.toJson().get("translate");
        Object keybind = c.toJson().get("keybind");

        if (text instanceof String lit) {
            x = drawLiteral(b, lit, x, y, st);
        } else if (translate instanceof String key) {
            x = drawTranslatable(b, key, c.toJson().get("with"), x, y, st);
        } else if (keybind instanceof String kb) {
            x = drawLiteral(b, "[" + kb + "]", x, y, st);
        }

        for (Component child : c.getChildren()) x = drawComponent(b, child, x, y, st);

        st.rgba = savedRGBA; st.bold = sb; st.italic = si; st.underline = su; st.strike = ss; st.obfuscated = so;
        return x;
    }

    private float drawTranslatable(QuadBatch b, String key, Object with, float x, float y, State st) {
        // сюда можно подключить реальную локализацию; пока: "%s ..." заменим по порядку
        List<Object> args = new ArrayList<>();
        if (with instanceof List<?> list) args.addAll(list);
        String pattern = lookupKey(key); // stub
        int ai = 0;
        for (String token : splitFormat(pattern)) {
            if ("%s".equals(token) && ai < args.size()) {
                Object a = args.get(ai++);
                if (a instanceof Map<?,?> json) {
                    x = drawComponent(b, fromJson(json), x, y, st);
                } else {
                    x = drawLiteral(b, String.valueOf(a), x, y, st);
                }
            } else {
                x = drawLiteral(b, token, x, y, st);
            }
        }
        return x;
    }

    private String lookupKey(String key) {
        return key;
    }

    private static List<String> splitFormat(String s) {
        List<String> out = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            int j = s.indexOf("%s", i);
            if (j < 0) { out.add(s.substring(i)); break; }
            if (j > i) out.add(s.substring(i, j));
            out.add("%s");
            i = j + 2;
        }
        if (out.isEmpty()) out.add("");
        return out;
    }

    private static Component fromJson(Map<?,?> json) {
        if (json.containsKey("text")) return Component.literal(String.valueOf(json.get("text")));
        if (json.containsKey("translate")) return Component.translatable(String.valueOf(json.get("translate")));
        if (json.containsKey("keybind")) return Component.keybind(String.valueOf(json.get("keybind")));
        return Component.literal(String.valueOf(json));
    }

    private float drawLiteral(QuadBatch b, String lit, float x, float y, State st) {
        final float sx = Window.scale().coofX();
        final float sy = Window.scale().coofY();
        for (Token tk : tokenize(lit)) {
            if (tk.kind == TokenKind.TEXT) {
                x = drawTextRun(b, tk.text, x, y, st);
            } else if (tk.kind == TokenKind.STICKER) {
                StickerRegistry.Sticker stc = stickers.get(tk.text);
                if (stc != null) {
                    float h = lineHeightPx() * sx;
                    float w = lineHeightPx() * stc.aspect * sy;
                    drawSticker(b, x, y, w, h, stc);
                    x += w;
                } else {
                    x = drawTextRun(b, ":" + tk.text + ":", x, y, st);
                }
            }
        }
        return x;
    }

    private float drawTextRun(QuadBatch b, String s, float baseX, float yTop, State st) {
        if (s.isEmpty()) return baseX;

        final float sx = Window.scale().coofX();
        final float sy = Window.scale().coofY();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);
            FloatBuffer xb = stack.floats(0f), yb = stack.floats(0f);

            final float epsU = 0.5f / GlyphAtlas.PACK_W;
            final float epsV = 0.5f / GlyphAtlas.PACK_H;

            final float baselineY = yTop + glyphs.ascentPx() * sy;

            for (int cp : s.codePoints().toArray()) {
                int idx = GlyphAtlas.toIndex(st.obfuscated ? obfuscate(cp, st.rng) : cp);

                stbtt_GetPackedQuad(glyphs.chars(), GlyphAtlas.PACK_W, GlyphAtlas.PACK_H, idx, xb, yb, q, false);

                float u0 = q.s0() + epsU, v0 = q.t0() + epsV;
                float u1 = q.s1() - epsU, v1 = q.t1() - epsV;

                float U0 = glyphs.remapU(u0), V0 = glyphs.remapV(v0);
                float U1 = glyphs.remapU(u1), V1 = glyphs.remapV(v1);

                float x0 = baseX + q.x0() * sx;
                float y0 = baselineY + q.y0() * sy;
                float x1 = baseX + q.x1() * sx;
                float y1 = baselineY + q.y1() * sy;

                final float italicSkew = st.italic ? 0.22f * (y1 - y0) : 0f;
                if (st.bold) b.maskedQuad(x0 + 1, y0, x1 + 1, y1, U0, V0, U1, V1, st.rgba, italicSkew);
                b.maskedQuad(x0, y0, x1, y1, U0, V0, U1, V1, st.rgba, italicSkew);
            }

            float newX = baseX + xb.get(0) * sx;

            if (st.underline || st.strike) {
                float lh = lineHeightPx() * sy;
                float thickness = Math.max(1f, 0.08f * lh);
                if (st.underline) {
                    float uy = baselineY + 0.9f * lh;
                    b.solidQuad(baseX, uy, newX, uy + thickness, st.rgba);
                }
                if (st.strike) {
                    float syLine = baselineY + 0.45f * lh;
                    b.solidQuad(baseX, syLine, newX, syLine + thickness, st.rgba);
                }
            }

            return newX;
        }
    }

    private void drawSticker(QuadBatch b, float x, float y, float w, float h,
                             StickerRegistry.Sticker stc) {
        float W = (w / h) * h;
        TextureRegion r = RenderInit.ATLAS_MANAGER.findRegion(RL.of("sticker/" + stc.name));
        if(r == null) return;

        b.texturedQuad(
                x,     y,
                x + W, y + h,
                r.u0, r.v0, r.u1, r.v1
        );
    }

    private static int obfuscate(int cp, Random r) {
        int base = 33; // '!'
        return base + r.nextInt(94);
    }

    private static int rgbFromTextColor(TextColor c) {
        String s = c.toString();
        if (s.startsWith("#") && s.length() == 7) return Integer.parseInt(s.substring(1), 16);
        return switch (s) {
            case "black" -> 0x000000;
            case "dark_blue" -> 0x0000AA;
            case "dark_green" -> 0x00AA00;
            case "dark_aqua" -> 0x00AAAA;
            case "dark_red" -> 0xAA0000;
            case "dark_purple" -> 0xAA00AA;
            case "gold" -> 0xFFAA00;
            case "gray" -> 0xAAAAAA;
            case "dark_gray" -> 0x555555;
            case "blue" -> 0x5555FF;
            case "green" -> 0x55FF55;
            case "aqua" -> 0x55FFFF;
            case "red" -> 0xFF5555;
            case "light_purple" -> 0xFF55FF;
            case "yellow" -> 0xFFFF55;
            case "white" -> 0xFFFFFF;
            default -> 0xFFFFFF;
        };
    }

    private enum TokenKind { TEXT, STICKER }

    private static final class Token {
        final TokenKind kind; final String text;
        Token(TokenKind k, String t){ kind=k; text=t; }
    }

    private static final Pattern STICKER = Pattern.compile(":([a-z0-9_\\-]{1,32}):");

    private List<Token> tokenize(String s) {
        List<Token> out = new ArrayList<>();
        Matcher m = STICKER.matcher(s);
        int last = 0;
        while (m.find()) {
            if (m.start() > last) out.add(new Token(TokenKind.TEXT, s.substring(last, m.start())));
            out.add(new Token(TokenKind.STICKER, m.group(1)));
            last = m.end();
        }
        if (last < s.length()) out.add(new Token(TokenKind.TEXT, s.substring(last)));
        if (out.isEmpty()) out.add(new Token(TokenKind.TEXT, ""));
        return out;
    }

    /* ====== грубая «оценка» ширины (ASCII/стикеры) ====== */
    private float measure0(Component c) {
        float w = 0;
        Object text = c.toJson().get("text");
        if (text instanceof String lit) {
            w += measureRun(lit);
        } else if (c.toJson().get("translate") instanceof String key) {
            String pat = lookupKey(key);
            for (String token : splitFormat(pat)) {
                w += measureRun("%s".equals(token) ? "" : token);
            }
        } else if (c.toJson().get("keybind") instanceof String kb) {
            w += measureRun("[" + kb + "]");
        }
        for (Component ch : c.getChildren()) w += measure0(ch);
        return w;
    }

    private float measureRun(String s) {
        float x = 0;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);
            FloatBuffer xb = stack.floats(0), yb = stack.floats(0);
            for (Token tk : tokenize(s)) {
                if (tk.kind == TokenKind.STICKER) {
                    x += lineHeightPx(); // квадратный стикер по высоте строки
                    xb.put(0, x);
                } else {
                    for (int cp : tk.text.codePoints().toArray()) {
                        int idx = GlyphAtlas.toIndex(cp);
                        stbtt_GetPackedQuad(glyphs.chars(), GlyphAtlas.PACK_W, GlyphAtlas.PACK_H, idx, xb, yb, q, false);
                        x = xb.get(0);
                    }
                }
            }
        }
        return x;
    }


}
