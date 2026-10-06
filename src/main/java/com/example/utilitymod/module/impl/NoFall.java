package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NoFall extends Module {
    public NoFall() { super("NoFall", Category.PLAYER); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame()) return;
        if (mc.thePlayer.fallDistance > 2.0F && !mc.thePlayer.onGround) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
        }
    }
}
