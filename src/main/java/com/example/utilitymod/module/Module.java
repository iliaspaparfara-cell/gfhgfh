package com.example.utilitymod.module;

import com.example.utilitymod.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final Minecraft mc = Minecraft.getMinecraft();
    private final String name;
    private final Category category;
    private int key = Keyboard.KEY_NONE;
    private boolean enabled;
    private final List<Setting> settings = new ArrayList<Setting>();

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    protected <T extends Setting> T add(T s) { settings.add(s); return s; }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean state) {
        if (state == enabled) return;
        enabled = state;
        if (enabled) { MinecraftForge.EVENT_BUS.register(this); onEnable(); }
        else { MinecraftForge.EVENT_BUS.unregister(this); onDisable(); }
    }

    protected void onEnable() {}
    protected void onDisable() {}

    /** Optional text shown next to name in the ArrayList. */
    public String getSuffix() { return ""; }

    public String getName() { return name; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public int getKey() { return key; }
    public void setKey(int key) { this.key = key; }
    public List<Setting> getSettings() { return settings; }

    protected boolean inGame() { return mc.thePlayer != null && mc.theWorld != null; }
}
