package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import com.example.utilitymod.util.MoveUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Fly extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 0.5, 0.1, 3.0, 0.05));

    public Fly() { super("Fly", Category.MOVEMENT); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame()) return;
        EntityPlayerSP p = mc.thePlayer;
        p.motionY = 0;
        if (mc.gameSettings.keyBindJump.isKeyDown()) p.motionY = speed.get();
        else if (mc.gameSettings.keyBindSneak.isKeyDown()) p.motionY = -speed.get();
        MoveUtil.setSpeed(p, speed.get());
        p.fallDistance = 0;
    }

    @Override public String getSuffix() { return String.valueOf(speed.get()); }
}
