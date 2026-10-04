package com.example.utilitymod.gui;

import com.example.utilitymod.UtilityMod;
import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.*;

public class ClickGUI extends GuiScreen {
    private static final int W = 110, ROW = 14, SROW = 13;
    private final Map<Category, int[]> pos = new LinkedHashMap<Category, int[]>(); // x,y
    private final Set<Module> expanded = new HashSet<Module>();
    private Module binding;
    private NumberSetting dragging;
    private int dragX, dragW;

    public ClickGUI() {
        int x = 10;
        for (Category c : Category.values()) { pos.put(c, new int[]{x, 10}); x += W + 8; }
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        if (dragging != null) {
            double pct = Math.max(0, Math.min(1, (mx - dragX) / (double) dragW));
            dragging.set(dragging.min + pct * (dragging.max - dragging.min));
        }
        for (Map.Entry<Category, int[]> en : pos.entrySet()) {
            int x = en.getValue()[0], y = en.getValue()[1];
            Gui.drawRect(x, y, x + W, y + ROW, 0xFF1E6FD9);
            fontRendererObj.drawStringWithShadow(en.getKey().label, x + 4, y + 3, 0xFFFFFF);
            y += ROW;
            for (Module m : UtilityMod.modules.byCategory(en.getKey())) {
                Gui.drawRect(x, y, x + W, y + ROW, m.isEnabled() ? 0xFF2A8F4E : 0xDD111111);
                String name = binding == m ? "Press a key..." : m.getName();
                fontRendererObj.drawStringWithShadow(name, x + 4, y + 3, 0xFFFFFF);
                if (m.getKey() != Keyboard.KEY_NONE && binding != m) {
                    String k = Keyboard.getKeyName(m.getKey());
                    fontRendererObj.drawStringWithShadow(k, x + W - fontRendererObj.getStringWidth(k) - 3, y + 3, 0xAAAAAA);
                }
                y += ROW;
                if (expanded.contains(m)) {
                    for (Setting s : m.getSettings()) {
                        Gui.drawRect(x, y, x + W, y + SROW, 0xEE000000);
                        if (s instanceof BooleanSetting) {
                            BooleanSetting b = (BooleanSetting) s;
                            fontRendererObj.drawString(s.name, x + 6, y + 3, b.get() ? 0x55FF55 : 0xAAAAAA);
                        } else if (s instanceof ModeSetting) {
                            fontRendererObj.drawString(s.name + ": " + ((ModeSetting) s).get(), x + 6, y + 3, 0xFFFFFF);
                        } else if (s instanceof NumberSetting) {
                            NumberSetting n = (NumberSetting) s;
                            double pct = (n.get() - n.min) / (n.max - n.min);
                            Gui.drawRect(x, y, x + (int) (W * pct), y + SROW, 0xFF1E6FD9);
                            fontRendererObj.drawString(s.name + ": " + n.get(), x + 6, y + 3, 0xFFFFFF);
                        }
                        y += SROW;
                    }
                }
            }
        }
        super.drawScreen(mx, my, pt);
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        for (Map.Entry<Category, int[]> en : pos.entrySet()) {
            int x = en.getValue()[0], y = en.getValue()[1] + ROW;
            for (Module m : UtilityMod.modules.byCategory(en.getKey())) {
                if (in(mx, my, x, y, W, ROW)) {
                    if (btn == 0) m.toggle();
                    else if (btn == 1) { if (!expanded.remove(m)) expanded.add(m); }
                    else if (btn == 2) binding = m;
                    return;
                }
                y += ROW;
                if (expanded.contains(m)) {
                    for (Setting s : m.getSettings()) {
                        if (in(mx, my, x, y, W, SROW)) {
                            if (s instanceof BooleanSetting) ((BooleanSetting) s).toggle();
                            else if (s instanceof ModeSetting) ((ModeSetting) s).cycle();
                            else if (s instanceof NumberSetting) {
                                dragging = (NumberSetting) s; dragX = x; dragW = W;
                            }
                            return;
                        }
                        y += SROW;
                    }
                }
            }
        }
        super.mouseClicked(mx, my, btn);
    }

    @Override protected void mouseReleased(int mx, int my, int state) { dragging = null; super.mouseReleased(mx, my, state); }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (binding != null) {
            binding.setKey(key == Keyboard.KEY_ESCAPE ? Keyboard.KEY_NONE : key);
            binding = null;
            return;
        }
        if (key == Keyboard.KEY_ESCAPE || key == UtilityMod.GUI_KEY) mc.displayGuiScreen(null);
    }

    private boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
