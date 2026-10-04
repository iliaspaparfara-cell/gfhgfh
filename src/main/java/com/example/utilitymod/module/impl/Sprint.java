package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.BooleanSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Sprint extends Module {
    private final BooleanSetting omni = add(new BooleanSetting("Omni (all directions)", false));

    public Sprint() { super("Sprint", Category.MOVEMENT); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !inGame()) return;
        boolean moving = omni.get()
                ? (mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0)
                : mc.thePlayer.moveForward > 0;
        if (moving && !mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isSneaking()
                && mc.thePlayer.getFoodStats().getFoodLevel() > 6) {
            mc.thePlayer.setSprinting(true);
        }
    }
}
