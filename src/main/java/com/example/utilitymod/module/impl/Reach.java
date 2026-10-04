package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Reach extends Module {
    private final NumberSetting reach = add(new NumberSetting("Reach", 4.0, 3.0, 6.0, 0.1));
    private final BooleanSetting playersOnly = add(new BooleanSetting("Players only", true));
    private final BooleanSetting throughWalls = add(new BooleanSetting("Through walls", false));

    public Reach() { super("Reach", Category.COMBAT); }

    @SubscribeEvent
    public void onMouse(MouseEvent e) {
        if (e.button != 0 || !e.buttonstate || !inGame() || mc.currentScreen != null) return;

        // vanilla already found an entity in normal range, let it handle the hit
        if (mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) return;

        double r = reach.get();
        Vec3 eye = mc.thePlayer.getPositionEyes(1.0F);
        Vec3 look = mc.thePlayer.getLook(1.0F);
        Vec3 end = eye.addVector(look.xCoord * r, look.yCoord * r, look.zCoord * r);

        Entity target = null;
        double best = r;
        for (Object o : mc.theWorld.loadedEntityList) {
            Entity en = (Entity) o;
            if (en == mc.thePlayer || en.isDead || !(en instanceof EntityLivingBase)) continue;
            if (playersOnly.get() && !(en instanceof EntityPlayer)) continue;

            float b = en.getCollisionBorderSize();
            AxisAlignedBB bb = en.getEntityBoundingBox().expand(b, b, b);
            double dist;
            if (bb.isVecInside(eye)) {
                dist = 0;
            } else {
                MovingObjectPosition hit = bb.calculateIntercept(eye, end);
                if (hit == null) continue;
                dist = eye.distanceTo(hit.hitVec);
            }
            if (dist >= best) continue;

            if (!throughWalls.get()) {
                Vec3 point = eye.addVector(look.xCoord * dist, look.yCoord * dist, look.zCoord * dist);
                if (mc.theWorld.rayTraceBlocks(eye, point) != null) continue;
            }
            target = en;
            best = dist;
        }
        if (target == null) return;

        mc.thePlayer.swingItem();
        mc.playerController.attackEntity(mc.thePlayer, target);
        e.setCanceled(true);
    }

    @Override public String getSuffix() { return String.valueOf(reach.get()); }
}
