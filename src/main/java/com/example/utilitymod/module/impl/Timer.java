package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.NumberSetting;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Timer extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 1.2, 0.1, 3.0, 0.05));

    public Timer() { super("Timer", Category.WORLD); }

    private net.minecraft.util.Timer timer() {
        return ObfuscationReflectionHelper.getPrivateValue(net.minecraft.client.Minecraft.class, mc, "timer", "field_71428_T");
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (!inGame()) return;
        timer().timerSpeed = speed.getF();
    }

    @Override protected void onDisable() {
        try { timer().timerSpeed = 1.0F; } catch (Exception ignored) {}
    }

    @Override public String getSuffix() { return String.valueOf(speed.get()); }
}
