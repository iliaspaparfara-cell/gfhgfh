package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AimAssist extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", 4.0, 1, 8, 0.1));
    private final NumberSetting fov = add(new NumberSetting("FOV", 90, 10, 360, 5));
    private final NumberSetting speed = add(new NumberSetting("Speed", 4, 1, 20, 0.5));
    private final BooleanSetting clickOnly = add(new BooleanSetting("Only while clicking", true));

    public AimAssist() { super("AimAssist", Category.COMBAT); }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame() || mc.currentScreen != null) return;
        if (clickOnly.get() && !mc.gameSettings.keyBindAttack.isKeyDown()) return;

        EntityPlayer best = null;
        float bestDiff = Float.MAX_VALUE;
        for (Object o : mc.theWorld.playerEntities) {
            EntityPlayer p = (EntityPlayer) o;
            if (p == mc.thePlayer || p.isDead || p.getHealth() <= 0) continue;
            if (mc.thePlayer.getDistanceToEntity(p) > range.get()) continue;
            float diff = Math.abs(yawDiff(p));
            if (diff > fov.get() / 2.0 || diff >= bestDiff) continue;
            best = p; bestDiff = diff;
        }
        if (best == null) return;
        float d = yawDiff(best);
        float step = Math.min(Math.abs(d), speed.getF() * (0.6F + (float) Math.random() * 0.4F));
        mc.thePlayer.rotationYaw += Math.signum(d) * step;
    }

    private float yawDiff(Entity t) {
        double dx = t.posX - mc.thePlayer.posX, dz = t.posZ - mc.thePlayer.posZ;
        float target = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        return MathHelper.wrapAngleTo180_float(target - mc.thePlayer.rotationYaw);
    }
}
