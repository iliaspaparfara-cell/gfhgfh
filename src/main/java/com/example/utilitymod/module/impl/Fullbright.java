package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Fullbright extends Module {
    private float old = 1.0F;

    public Fullbright() { super("Fullbright", Category.RENDER); }

    @Override protected void onEnable() { old = mc.gameSettings.gammaSetting; }
    @Override protected void onDisable() { mc.gameSettings.gammaSetting = old; }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        mc.gameSettings.gammaSetting = 100.0F;
    }
}
