package ru.mrbedrockpy.craftengine.gui.screen.layout;

import ru.mrbedrockpy.craftengine.gui.screen.widget.AbstractWidget;
import ru.mrbedrockpy.craftengine.renderer.gui.DrawContext;
import ru.mrbedrockpy.craftengine.renderer.window.Window;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class GridLayout extends Layout {

    public enum Align { START, CENTER, END, FILL }

    // Геометрия сетки
    private int originX = 0, originY = 0;
    private int width = 0, height = 0;               // 0 => использовать текущий размер окна
    private int cols = 2;                             // количество столбцов
    private int hgap = 8, vgap = 8;                   // расстояния между ячейками
    private int padLeft = 0, padTop = 0, padRight = 0, padBottom = 0;

    // Поведение и выравнивание
    private Align alignX = Align.CENTER;
    private Align alignY = Align.CENTER;
    private boolean uniformCellSize = false;          // если true — виджетам задаётся размер ячейки

    // Явные позиции/спаны + авто-поток
    private static final class Item {
        final String id;
        final AbstractWidget w;
        int row, col, colspan, rowspan;
        Item(String id, AbstractWidget w, int row, int col, int colspan, int rowspan) {
            this.id = id; this.w = w;
            this.row = max(0, row); this.col = max(0, col);
            this.colspan = max(1, colspan); this.rowspan = max(1, rowspan);
        }
    }
    private final List<Item> items = new ArrayList<>();
    private int autoIndex = 0; // для авто-потока

    // ---------- Конфигурирование ----------
    public GridLayout bounds(int x, int y, int w, int h) { this.originX=x; this.originY=y; this.width=w; this.height=h; return this; }
    public GridLayout columns(int columns) { this.cols = max(1, columns); return this; }
    public GridLayout gap(int hgap, int vgap) { this.hgap=max(0,hgap); this.vgap=max(0,vgap); return this; }
    public GridLayout padding(int all) { return padding(all, all, all, all); }
    public GridLayout padding(int left, int top, int right, int bottom) {
        this.padLeft=left; this.padTop=top; this.padRight=right; this.padBottom=bottom; return this;
    }
    public GridLayout align(Align x, Align y) { this.alignX=x; this.alignY=y; return this; }
    public GridLayout uniformCellSize(boolean v) { this.uniformCellSize = v; return this; }

    // ---------- Добавление виджетов ----------
    /** авто-поток по строкам */
    @Override
    public void addWidget(String id, AbstractWidget widget) {
        super.addWidget(id, widget);
        int row = autoIndex / cols;
        int col = autoIndex % cols;
        items.add(new Item(id, widget, row, col, 1, 1));
        autoIndex++;
    }

    /** явная позиция */
    public void addWidget(String id, AbstractWidget widget, int row, int col) {
        super.addWidget(id, widget);
        items.add(new Item(id, widget, row, col, 1, 1));
    }

    /** явная позиция с объединением ячеек */
    public void addWidget(String id, AbstractWidget widget, int row, int col, int colspan, int rowspan) {
        super.addWidget(id, widget);
        items.add(new Item(id, widget, row, col, colspan, rowspan));
    }

    // ---------- Раскладка ----------
    private void relayout() {
        if (widgets.isEmpty()) return;

        // размеры области
        int sw = (width  > 0 ? width  : Window.scaledWidth());
        int sh = (height > 0 ? height : Window.scaledHeight());

        // вычислим число строк (по максимальному затронутому индексу)
        int maxRow = 0;
        for (Item it : items) {
            maxRow = max(maxRow, it.row + it.rowspan - 1);
        }
        int rows = max(1, maxRow + 1);

        // размеры ячейки без учёта спанов
        int gridW = max(0, sw - padLeft - padRight - hgap * (cols - 1));
        int gridH = max(0, sh - padTop  - padBottom - vgap * (rows - 1));
        int cellW = (cols > 0 ? gridW / cols : gridW);
        int cellH = (rows > 0 ? gridH / rows : gridH);

        // раскладываем
        for (Item it : items) {
            if (it.w == null || !it.w.isVisible()) continue;

            int cw = cellW * it.colspan + hgap * (it.colspan - 1);
            int ch = cellH * it.rowspan + vgap * (it.rowspan - 1);

            int cellX = originX + padLeft + it.col * (cellW + hgap);
            int cellY = originY + padTop  + it.row * (cellH + vgap);

            int w = it.w.getWidth();
            int h = it.w.getHeight();

            int x = switch (alignX) {
                case START  -> cellX;
                case CENTER -> cellX + (cw - w) / 2;
                case END    -> cellX + (cw - w);
                case FILL   -> cellX;
            };
            int y = switch (alignY) {
                case START  -> cellY;
                case CENTER -> cellY + (ch - h) / 2;
                case END    -> cellY + (ch - h);
                case FILL   -> cellY;
            };

            if (uniformCellSize || alignX == Align.FILL) it.w.setWidth( max(1, cw) );
            if (uniformCellSize || alignY == Align.FILL) it.w.setHeight( max(1, ch) );

            it.w.setX(x);
            it.w.setY(y);
        }
    }

    // ---------- Жизненный цикл ----------
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        relayout();
        super.render(context, mouseX, mouseY, delta);
    }

    public GridLayout moveToCenter() {
        int sw = (width  > 0 ? width  : Window.scaledWidth());
        int sh = (height > 0 ? height : Window.scaledHeight());
        this.originX = (Window.scaledWidth()  - sw) / 2;
        this.originY = (Window.scaledHeight() - sh) / 2;
        return this;
    }

    public GridLayout origin(int x, int y) { this.originX+=x; this.originY+=y; return this; }
}