package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Zoom extends Module {
    private final NumberSetting fov = add(new NumberSetting("FOV", 30, 5, 90, 1));
    private float old = 70F;

    public Zoom() { super("Zoom", Category.RENDER); }

    @Override protected void onEnable() { old = mc.gameSettings.fovSetting; }
    @Override protected void onDisable() { mc.gameSettings.fovSetting = old; }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        mc.gameSettings.fovSetting = fov.getF();
    }
}
