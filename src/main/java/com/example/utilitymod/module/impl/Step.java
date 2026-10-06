package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Step extends Module {
    private final NumberSetting height = add(new NumberSetting("Height", 1.0, 0.6, 3.0, 0.1));

    public Step() { super("Step", Category.MOVEMENT); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (!inGame()) return;
        mc.thePlayer.stepHeight = height.getF();
    }

    @Override protected void onDisable() {
        if (mc.thePlayer != null) mc.thePlayer.stepHeight = 0.5F;
    }

    @Override public String getSuffix() { return String.valueOf(height.get()); }
}
