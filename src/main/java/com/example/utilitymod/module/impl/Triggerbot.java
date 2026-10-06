package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Triggerbot extends Module {
    private final NumberSetting cps = add(new NumberSetting("CPS", 10, 1, 20, 1));
    private final BooleanSetting playersOnly = add(new BooleanSetting("Players only", true));
    private long next;

    public Triggerbot() { super("Triggerbot", Category.COMBAT); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame() || mc.currentScreen != null) return;
        MovingObjectPosition o = mc.objectMouseOver;
        if (o == null || o.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) return;
        if (!(o.entityHit instanceof EntityLivingBase)) return;
        if (playersOnly.get() && !(o.entityHit instanceof EntityPlayer)) return;

        long now = System.currentTimeMillis();
        if (now < next) return;
        next = now + (long) (1000.0 / cps.get());

        mc.thePlayer.swingItem();
        mc.playerController.attackEntity(mc.thePlayer, o.entityHit);
    }
}
