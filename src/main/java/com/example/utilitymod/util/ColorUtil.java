package com.example.utilitymod.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import java.awt.Color;

public class ColorUtil {
    private static final int[] CODES = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF};

    /** RGB color of the player's team. */
    public static int team(EntityPlayer p) {
        // 1. color code in the (team-formatted) display name
        String s = p.getDisplayName().getFormattedText();
        for (int i = 0; i < s.length() - 1; i++) {
            if (s.charAt(i) != '\u00a7') continue;
            int idx = "0123456789abcdef".indexOf(Character.toLowerCase(s.charAt(i + 1)));
            if (idx > 0 && idx != 15) return CODES[idx]; // skip black and white
        }
        // 2. dyed leather armor
        for (int slot = 3; slot >= 0; slot--) {
            ItemStack a = p.getCurrentArmor(slot);
            if (a != null && a.getItem() instanceof ItemArmor) {
                ItemArmor armor = (ItemArmor) a.getItem();
                if (armor.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER) {
                    int c = armor.getColor(a);
                    if (c != 10511680) return c; // 10511680 = undyed leather
                }
            }
        }
        return 0xB36BFF;
    }

    public static int rainbow(float t) {
        return Color.HSBtoRGB(t - (float) Math.floor(t), 0.7f, 1f) & 0xFFFFFF;
    }

    public static int health(float fraction) {
        fraction = Math.max(0f, Math.min(1f, fraction));
        return ((int) (255 * (1 - fraction)) << 16) | ((int) (255 * fraction) << 8) | 60;
    }

    /** Pulses the color toward white to give a "shiny" look. */
    public static int shimmer(int rgb, float phase) {
        float k = (0.5f + 0.5f * (float) Math.sin(phase)) * 0.55f;
        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        r += (int) ((255 - r) * k);
        g += (int) ((255 - g) * k);
        b += (int) ((255 - b) * k);
        return (r << 16) | (g << 8) | b;
    }
}
