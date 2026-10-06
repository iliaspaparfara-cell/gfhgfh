package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class KillAura extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", 3.0, 2.0, 6.0, 0.1));
    private final NumberSetting cps = add(new NumberSetting("CPS", 10, 1, 20, 1));
    private final BooleanSetting players = add(new BooleanSetting("Players", true));
    private final BooleanSetting mobs = add(new BooleanSetting("Mobs", false));
    private final BooleanSetting rotate = add(new BooleanSetting("Rotate", true));
    private long next;

    public KillAura() { super("KillAura", Category.COMBAT); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame() || mc.currentScreen != null) return;

        EntityLivingBase target = null;
        double best = range.get();
        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityLivingBase) || o == mc.thePlayer) continue;
            EntityLivingBase en = (EntityLivingBase) o;
            if (en.isDead || en.getHealth() <= 0) continue;
            boolean valid = (players.get() && en instanceof EntityPlayer) || (mobs.get() && en instanceof IMob);
            if (!valid) continue;
            double d = mc.thePlayer.getDistanceToEntity(en);
            if (d <= best) { best = d; target = en; }
        }
        if (target == null) return;

        if (rotate.get()) {
            double dx = target.posX - mc.thePlayer.posX;
            double dz = target.posZ - mc.thePlayer.posZ;
            double dy = (target.posY + target.getEyeHeight()) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
            mc.thePlayer.rotationYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
            mc.thePlayer.rotationPitch = (float) -(Math.atan2(dy, Math.hypot(dx, dz)) * 180.0 / Math.PI);
        }

        long now = System.currentTimeMillis();
        if (now < next) return;
        next = now + (long) (1000.0 / cps.get());
        mc.thePlayer.swingItem();
        mc.playerController.attackEntity(mc.thePlayer, target);
    }

    @Override public String getSuffix() { return String.valueOf(range.get()); }
}
