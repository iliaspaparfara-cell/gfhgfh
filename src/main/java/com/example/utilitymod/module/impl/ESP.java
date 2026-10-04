package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class ESP extends Module {
    private final NumberSetting width = add(new NumberSetting("Line width", 2, 1, 5, 0.5));

    public ESP() { super("ESP", Category.RENDER); }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent e) {
        if (!inGame()) return;
        float pt = e.partialTicks;
        Entity v = mc.getRenderViewEntity();
        double vx = v.lastTickPosX + (v.posX - v.lastTickPosX) * pt;
        double vy = v.lastTickPosY + (v.posY - v.lastTickPosY) * pt;
        double vz = v.lastTickPosZ + (v.posZ - v.lastTickPosZ) * pt;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GL11.glLineWidth(width.getF());

        for (Object o : mc.theWorld.playerEntities) {
            EntityPlayer p = (EntityPlayer) o;
            if (p == mc.thePlayer || p.isDead) continue;
            double ix = p.lastTickPosX + (p.posX - p.lastTickPosX) * pt;
            double iy = p.lastTickPosY + (p.posY - p.lastTickPosY) * pt;
            double iz = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * pt;
            AxisAlignedBB bb = p.getEntityBoundingBox()
                    .offset(ix - p.posX - vx, iy - p.posY - vy, iz - p.posZ - vz);
            float hp = Math.max(0f, Math.min(1f, p.getHealth() / p.getMaxHealth()));
            RenderGlobal.drawOutlinedBoundingBox(bb, (int) (255 * (1 - hp)), (int) (255 * hp), 60, 255);
        }

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
