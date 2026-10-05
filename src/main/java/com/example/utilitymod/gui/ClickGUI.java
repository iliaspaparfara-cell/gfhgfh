package com.example.utilitymod.gui;

import com.example.utilitymod.Config;
import com.example.utilitymod.UtilityMod;
import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import java.awt.Color;
import java.io.IOException;
import java.util.*;

public class ClickGUI extends GuiScreen {
    private static final int W = 108, HEAD = 20, ROW = 16, SROW = 16;
    private final Map<Category, int[]> pos = new LinkedHashMap<Category, int[]>();
    private final Set<Module> expanded = new HashSet<Module>();
    private Module binding;
    private NumberSetting dragging;
    private int dragX, dragW;
    private Category dragPanel;
    private int offX, offY;

    public ClickGUI() {
        int x = 14;
        for (Category c : Category.values()) { pos.put(c, new int[]{x, 14}); x += W + 10; }
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    private int accent() {
        float t = (System.currentTimeMillis() % 3000) / 3000f;
        return Color.HSBtoRGB(0.74f + (float) Math.sin(t * 2 * Math.PI) * 0.04f, 0.65f, 1f);
    }

    private void rrect(int x1, int y1, int x2, int y2, int c) {
        Gui.drawRect(x1 + 1, y1, x2 - 1, y2, c);
        Gui.drawRect(x1, y1 + 1, x2, y2 - 1, c);
    }

    private int panelHeight(Category cat) {
        int h = HEAD + 3;
        for (Module m : UtilityMod.modules.byCategory(cat)) {
            h += ROW;
            if (expanded.contains(m)) h += SROW * (m.getSettings().size() + 1); // +1 = bind row
        }
        return h + 2;
    }

    private String fmt(double v) {
        return v == (long) v ? String.valueOf((long) v) : String.valueOf(Math.round(v * 100) / 100.0);
    }

    private String keyName(Module m) {
        return m.getKey() == Keyboard.KEY_NONE ? "None" : Keyboard.getKeyName(m.getKey());
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawGradientRect(0, 0, width, height, 0xB0090514, 0xC01B0C3A);
        int accent = accent();

        if (dragging != null) {
            double pct = Math.max(0, Math.min(1, (mx - dragX) / (double) dragW));
            dragging.set(dragging.min + pct * (dragging.max - dragging.min));
        }
        if (dragPanel != null) {
            int[] p = pos.get(dragPanel);
            p[0] = mx - offX;
            p[1] = my - offY;
        }

        for (Map.Entry<Category, int[]> en : pos.entrySet()) {
            int x = en.getValue()[0], y = en.getValue()[1];
            int h = panelHeight(en.getKey());

            rrect(x - 1, y - 1, x + W + 1, y + h + 1, 0x40000000);
            rrect(x, y, x + W, y + h, 0xD80E0818);
            rrect(x, y, x + W, y + HEAD, 0xEE170D2B);
            Gui.drawRect(x + 4, y + HEAD - 1, x + W - 4, y + HEAD, accent);
            String title = en.getKey().label;
            fontRendererObj.drawString(title, x + (W - fontRendererObj.getStringWidth(title)) / 2, y + 6, 0xFFEDE7FF);

            int ry = y + HEAD + 3;
            for (Module m : UtilityMod.modules.byCategory(en.getKey())) {
                boolean hover = in(mx, my, x, ry, W, ROW);
                int bg = m.isEnabled() ? 0xC8281B4D : (hover ? 0xC81D1433 : 0x00000000);
                if (bg != 0) Gui.drawRect(x + 3, ry, x + W - 3, ry + ROW, bg);
                if (m.isEnabled()) Gui.drawRect(x + 3, ry + 2, x + 5, ry + ROW - 2, accent);

                fontRendererObj.drawString(m.getName(), x + 9, ry + 4, m.isEnabled() ? 0xFFD9C8FF : 0xFFB3AEC4);

                int right = x + W - 8;
                fontRendererObj.drawString(expanded.contains(m) ? "v" : ">", right - 4, ry + 4, 0xFF7C7494);
                right -= 12;
                if (m.getKey() != Keyboard.KEY_NONE) {
                    String k = "[" + Keyboard.getKeyName(m.getKey()) + "]";
                    fontRendererObj.drawString(k, right - fontRendererObj.getStringWidth(k), ry + 4, 0xFF8F86B0);
                }
                ry += ROW;

                if (expanded.contains(m)) {
                    // bind row
                    Gui.drawRect(x + 3, ry, x + W - 3, ry + SROW, 0xD00A0612);
                    fontRendererObj.drawString("Bind", x + 9, ry + 4, 0xFFB3AEC4);
                    String bt = binding == m ? "press a key..." : keyName(m);
                    fontRendererObj.drawString(bt, x + W - 8 - fontRendererObj.getStringWidth(bt), ry + 4,
                            binding == m ? 0xFFFFD27F : accent);
                    ry += SROW;

                    for (Setting s : m.getSettings()) {
                        Gui.drawRect(x + 3, ry, x + W - 3, ry + SROW, 0xD00A0612);
                        if (s instanceof BooleanSetting) {
                            boolean on = ((BooleanSetting) s).get();
                            fontRendererObj.drawString(s.name, x + 9, ry + 4, 0xFFB3AEC4);
                            rrect(x + W - 22, ry + 4, x + W - 8, ry + 12, on ? accent : 0xFF2A2340);
                            Gui.drawRect(on ? x + W - 15 : x + W - 21, ry + 5, on ? x + W - 9 : x + W - 15, ry + 11, 0xFFFFFFFF);
                        } else if (s instanceof ModeSetting) {
                            fontRendererObj.drawString(s.name, x + 9, ry + 4, 0xFFB3AEC4);
                            String v = ((ModeSetting) s).get();
                            fontRendererObj.drawString(v, x + W - 8 - fontRendererObj.getStringWidth(v), ry + 4, accent);
                        } else if (s instanceof NumberSetting) {
                            NumberSetting n = (NumberSetting) s;
                            fontRendererObj.drawString(s.name, x + 9, ry + 2, 0xFFB3AEC4);
                            String v = fmt(n.get());
                            fontRendererObj.drawString(v, x + W - 8 - fontRendererObj.getStringWidth(v), ry + 2, 0xFFEDE7FF);
                            int bx1 = x + 8, bx2 = x + W - 8;
                            Gui.drawRect(bx1, ry + 12, bx2, ry + 14, 0xFF2A2340);
                            double pct = (n.get() - n.min) / (n.max - n.min);
                            int fill = bx1 + (int) ((bx2 - bx1) * pct);
                            Gui.drawRect(bx1, ry + 12, fill, ry + 14, accent);
                            Gui.drawRect(fill - 1, ry + 11, fill + 1, ry + 15, 0xFFFFFFFF);
                        }
                        ry += SROW;
                    }
                }
            }
        }

        String hint = "Left: toggle   Right: settings + bind   Backspace while binding: clear";
        fontRendererObj.drawString(hint, (width - fontRendererObj.getStringWidth(hint)) / 2, height - 12, 0xFF6F6888);
        super.drawScreen(mx, my, pt);
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        // clicking anywhere cancels an active bind
        Module wasBinding = binding;
        binding = null;

        for (Map.Entry<Category, int[]> en : pos.entrySet()) {
            int x = en.getValue()[0], y = en.getValue()[1];
            if (btn == 0 && in(mx, my, x, y, W, HEAD)) {
                dragPanel = en.getKey();
                offX = mx - x;
                offY = my - y;
                return;
            }
            int ry = y + HEAD + 3;
            for (Module m : UtilityMod.modules.byCategory(en.getKey())) {
                if (in(mx, my, x, ry, W, ROW)) {
                    if (btn == 0) m.toggle();
                    else if (btn == 1) { if (!expanded.remove(m)) expanded.add(m); }
                    else if (btn == 2) binding = m;
                    return;
                }
                ry += ROW;
                if (expanded.contains(m)) {
                    if (in(mx, my, x, ry, W, SROW)) {
                        // bind row: click to start listening (click again to cancel)
                        if (wasBinding != m) binding = m;
                        return;
                    }
                    ry += SROW;
                    for (Setting s : m.getSettings()) {
                        if (in(mx, my, x, ry, W, SROW)) {
                            if (s instanceof BooleanSetting) ((BooleanSetting) s).toggle();
                            else if (s instanceof ModeSetting) ((ModeSetting) s).cycle();
                            else if (s instanceof NumberSetting) {
                                dragging = (NumberSetting) s;
                                dragX = x + 8;
                                dragW = W - 16;
                            }
                            return;
                        }
                        ry += SROW;
                    }
                }
            }
        }
        super.mouseClicked(mx, my, btn);
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        dragging = null;
        dragPanel = null;
        super.mouseReleased(mx, my, state);
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (binding != null) {
            if (key == Keyboard.KEY_ESCAPE || key == Keyboard.KEY_BACK || key == Keyboard.KEY_DELETE) {
                binding.setKey(Keyboard.KEY_NONE);
            } else if (key != UtilityMod.GUI_KEY) { // right shift is reserved for the GUI
                binding.setKey(key);
            }
            binding = null;
            Config.save();
            return;
        }
        if (key == Keyboard.KEY_ESCAPE || key == UtilityMod.GUI_KEY) mc.displayGuiScreen(null);
    }

    @Override
    public void onGuiClosed() {
        Config.save();
    }

    private boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
