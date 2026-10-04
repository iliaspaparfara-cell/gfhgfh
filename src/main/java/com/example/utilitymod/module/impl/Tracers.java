package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class Tracers extends Module {
    public Tracers() { super("Tracers", Category.RENDER); }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent e) {
        if (!inGame()) return;
        float pt = e.partialTicks;
        Entity v = mc.getRenderViewEntity();
        double vx = v.lastTickPosX + (v.posX - v.lastTickPosX) * pt;
        double vy = v.lastTickPosY + (v.posY - v.lastTickPosY) * pt;
        double vz = v.lastTickPosZ + (v.posZ - v.lastTickPosZ) * pt;

        Vec3 look = new Vec3(0, 0, 1)
                .rotatePitch(-(float) Math.toRadians(v.rotationPitch))
                .rotateYaw(-(float) Math.toRadians(v.rotationYaw));

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GL11.glLineWidth(1.5F);
        GL11.glBegin(GL11.GL_LINES);
        for (Object o : mc.theWorld.playerEntities) {
            EntityPlayer p = (EntityPlayer) o;
            if (p == mc.thePlayer || p.isDead) continue;
            double x = p.lastTickPosX + (p.posX - p.lastTickPosX) * pt - vx;
            double y = p.lastTickPosY + (p.posY - p.lastTickPosY) * pt - vy + p.height / 2;
            double z = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * pt - vz;
            float dist = Math.min(1f, mc.thePlayer.getDistanceToEntity(p) / 40f);
            GL11.glColor4f(1f - dist, dist, 0.2f, 1f);
            GL11.glVertex3d(look.xCoord, look.yCoord + v.getEyeHeight(), look.zCoord);
            GL11.glVertex3d(x, y, z);
        }
        GL11.glEnd();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}
