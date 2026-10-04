package com.example.utilitymod;

import com.example.utilitymod.module.Module;
import com.example.utilitymod.module.ModuleManager;
import com.example.utilitymod.gui.ClickGUI;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = UtilityMod.MODID, name = "Utility Mod", version = "1.0", acceptedMinecraftVersions = "[1.8.9]")
public class UtilityMod {
    public static final String MODID = "utilitymod";
    public static ModuleManager modules;
    public static ClickGUI clickGUI;
    public static final int GUI_KEY = Keyboard.KEY_RSHIFT;

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        modules = new ModuleManager();
        clickGUI = new ClickGUI();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent e) {
        if (!Keyboard.getEventKeyState()) return;
        int key = Keyboard.getEventKey();
        if (Minecraft.getMinecraft().currentScreen != null) return;
        if (key == GUI_KEY) {
            Minecraft.getMinecraft().displayGuiScreen(clickGUI);
            return;
        }
        for (Module m : modules.all()) {
            if (m.getKey() != Keyboard.KEY_NONE && m.getKey() == key) m.toggle();
        }
    }
}
