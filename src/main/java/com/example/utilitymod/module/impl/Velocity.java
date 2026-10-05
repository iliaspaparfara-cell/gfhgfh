package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.NumberSetting;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.Random;

public class Velocity extends Module {
    // percent of knockback you KEEP (100 = vanilla, 0 = none)
    private final NumberSetting horizontal = add(new NumberSetting("Horizontal %", 60, 0, 100, 1));
    private final NumberSetting vertical = add(new NumberSetting("Vertical %", 100, 0, 100, 1));
    private final NumberSetting chance = add(new NumberSetting("Chance %", 100, 0, 100, 5));
    private final Random rand = new Random();

    public Velocity() { super("Velocity", Category.COMBAT); }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent e) {
        if (!inGame() || e.entityLiving != mc.thePlayer) return;
        // hurtTime is at its max on the first tick after taking a hit
        if (mc.thePlayer.hurtTime != mc.thePlayer.maxHurtTime || mc.thePlayer.maxHurtTime <= 0) return;
        if (rand.nextInt(100) >= chance.get()) return;

        double h = horizontal.get() / 100.0;
        double v = vertical.get() / 100.0;
        mc.thePlayer.motionX *= h;
        mc.thePlayer.motionZ *= h;
        if (mc.thePlayer.motionY > 0) mc.thePlayer.motionY *= v;
    }

    @Override public String getSuffix() { return (int) horizontal.get() + "% " + (int) vertical.get() + "%"; }
}
