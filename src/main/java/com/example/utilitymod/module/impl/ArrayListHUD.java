package com.example.utilitymod.module.impl;

import com.example.utilitymod.UtilityMod;
import com.example.utilitymod.module.*;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.*;

public class ArrayListHUD extends Module {
    public ArrayListHUD() { super("ArrayList", Category.RENDER); }

    private String label(Module m) {
        return m.getSuffix().isEmpty() ? m.getName() : m.getName() + " \u00a77" + m.getSuffix();
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text e) {
        if (!inGame()) return;
        final FontRenderer fr = mc.fontRendererObj;
        ScaledResolution sr = new ScaledResolution(mc);
        List<Module> active = new ArrayList<Module>();
        for (Module m : UtilityMod.modules.all())
            if (m.isEnabled() && m != this) active.add(m);
        Collections.sort(active, new Comparator<Module>() {
            public int compare(Module a, Module b) {
                return fr.getStringWidth(label(b)) - fr.getStringWidth(label(a));
            }
        });
        fr.drawStringWithShadow("Utility \u00a77v1.0", 4, 4, 0x55FFFF);
        int y = 2;
        for (int i = 0; i < active.size(); i++) {
            String s = label(active.get(i));
            int color = java.awt.Color.HSBtoRGB((System.currentTimeMillis() % 4000) / 4000f - i * 0.04f, 0.6f, 1f);
            fr.drawStringWithShadow(s, sr.getScaledWidth() - fr.getStringWidth(s) - 3, y, color);
            y += fr.FONT_HEIGHT + 1;
        }
    }
}
