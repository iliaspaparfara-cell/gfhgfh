package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.Random;

public class Velocity extends Module {
    // percent of knockback you KEEP (100 = vanilla, 0 = none)
    private final NumberSetting horizontal = add(new NumberSetting("Horizontal %", 60, 0, 100, 1));
    private final NumberSetting vertical = add(new NumberSetting("Vertical %", 100, 0, 100, 1));
    private final NumberSetting chance = add(new NumberSetting("Chance %", 100, 0, 100, 5));
    private final BooleanSetting debug = add(new BooleanSetting("Debug chat", false));
    private final Random rand = new Random();
    private double lastX, lastY, lastZ;

    public Velocity() { super("Velocity", Category.COMBAT); }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent e) {
        if (!inGame() || e.entityLiving != mc.thePlayer) return;
        EntityPlayerSP p = mc.thePlayer;

        // only look for knockback in the first few ticks after being hurt
        boolean recentlyHurt = p.hurtTime > 0 && p.hurtTime >= p.maxHurtTime - 3;

        if (recentlyHurt && rand.nextInt(100) < chance.get()) {
            double hNow = Math.hypot(p.motionX, p.motionZ);
            double hLast = Math.hypot(lastX, lastZ);

            if (hNow - hLast > 0.1) {
                double h = horizontal.get() / 100.0;
                p.motionX *= h;
                p.motionZ *= h;
                if (debug.get()) p.addChatMessage(new ChatComponentText("[Velocity] horizontal scaled"));
            }
            if (p.motionY - lastY > 0.1) {
                p.motionY *= vertical.get() / 100.0;
                if (debug.get()) p.addChatMessage(new ChatComponentText("[Velocity] vertical scaled"));
            }
        }

        lastX = p.motionX;
        lastY = p.motionY;
        lastZ = p.motionZ;
    }

    @Override public String getSuffix() { return (int) horizontal.get() + "% " + (int) vertical.get() + "%"; }
}
