package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import com.example.utilitymod.util.MoveUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Speed extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 0.3, 0.1, 1.0, 0.01));
    private final BooleanSetting autoJump = add(new BooleanSetting("Auto jump", true));

    public Speed() { super("Speed", Category.MOVEMENT); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame()) return;
        EntityPlayerSP p = mc.thePlayer;
        if (!MoveUtil.isMoving(p) || p.isInWater() || p.isInLava() || p.isSneaking()) return;
        if (autoJump.get() && p.onGround) p.jump();
        MoveUtil.setSpeed(p, speed.get());
    }

    @Override public String getSuffix() { return String.valueOf(speed.get()); }
}
