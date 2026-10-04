package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FastPlace extends Module {
    public FastPlace() { super("FastPlace", Category.PLAYER); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame()) return;
        ObfuscationReflectionHelper.setPrivateValue(net.minecraft.client.Minecraft.class, mc, 0,
                "rightClickDelayTimer", "field_71467_ac");
    }
}
