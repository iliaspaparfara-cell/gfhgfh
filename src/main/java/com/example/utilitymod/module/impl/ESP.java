package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import com.example.utilitymod.util.ColorUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class ESP extends Module {
    private final ModeSetting style = add(new ModeSetting("Style", "Both", "Outline", "Filled", "Both"));
    private final ModeSetting colorMode = add(new ModeSetting("Color", "Team", "Team", "Health", "Rainbow"));
    private final BooleanSetting shine = add(new BooleanSetting("Shine", true));
    private final BooleanSetting glow = add(new BooleanSetting("Glow", true));
    private final BooleanSetting walls = add(new BooleanSetting("Through walls", true));
    private final NumberSetting width = add(new NumberSetting("Line width", 2, 1, 5, 0.5));
    private final NumberSetting fill = add(new NumberSetting("Fill alpha", 40, 5, 120, 5));

    public ESP() { super("ESP", Category.RENDER); }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent e) {
        if (!inGame()) return;
        float pt = e.partialTicks;
        Entity v = mc.getRenderViewEntity();
        double vx = v.lastTickPosX + (v.posX - v.lastTickPosX) * pt;
        double vy = v.lastTickPosY + (v.posY - v.lastTickPosY) * pt;
        double vz = v.lastTickPosZ + (v.posZ - v.lastTickPosZ) * pt;
        float time = (System.currentTimeMillis() % 100000L) / 1000f;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        if (walls.get()) GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        boolean filled = !style.is("Outline");
        boolean outline = !style.is("Filled");

        for (Object o : mc.theWorld.playerEntities) {
            EntityPlayer p = (EntityPlayer) o;
            if (p == mc.thePlayer || p.isDead) continue;

            double ix = p.lastTickPosX + (p.posX - p.lastTickPosX) * pt;
            double iy = p.lastTickPosY + (p.posY - p.lastTickPosY) * pt;
            double iz = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * pt;
            AxisAlignedBB bb = p.getEntityBoundingBox()
                    .offset(ix - p.posX - vx, iy - p.posY - vy, iz - p.posZ - vz);

            int rgb = pickColor(p, time);
            if (shine.get()) rgb = ColorUtil.shimmer(rgb, time * 3f + p.getEntityId() * 0.7f);
            int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;

            if (filled) {
                GlStateManager.color(r / 255f, g / 255f, b / 255f, fill.getF() / 255f);
                drawFilled(bb);
            }
            if (outline) {
                if (glow.get()) {
                    for (int layer = 3; layer >= 1; layer--) {
                        GL11.glLineWidth(width.getF() + layer * 2.5f);
                        RenderGlobal.drawOutlinedBoundingBox(bb, r, g, b, layer == 3 ? 25 : layer == 2 ? 40 : 60);
                    }
                }
                GL11.glLineWidth(width.getF());
                RenderGlobal.drawOutlinedBoundingBox(bb, r, g, b, 255);
            }
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
    }

    private int pickColor(EntityPlayer p, float time) {
        if (colorMode.is("Rainbow")) return ColorUtil.rainbow(time * 0.4f + p.getEntityId() * 0.1f);
        if (colorMode.is("Health")) return ColorUtil.health(p.getHealth() / p.getMaxHealth());
        return ColorUtil.team(p);
    }

    private void drawFilled(AxisAlignedBB b) {
        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();
        w.begin(7, DefaultVertexFormats.POSITION);
        // bottom
        w.pos(b.minX, b.minY, b.minZ).endVertex(); w.pos(b.maxX, b.minY, b.minZ).endVertex();
        w.pos(b.maxX, b.minY, b.maxZ).endVertex(); w.pos(b.minX, b.minY, b.maxZ).endVertex();
        // top
        w.pos(b.minX, b.maxY, b.minZ).endVertex(); w.pos(b.minX, b.maxY, b.maxZ).endVertex();
        w.pos(b.maxX, b.maxY, b.maxZ).endVertex(); w.pos(b.maxX, b.maxY, b.minZ).endVertex();
        // north
        w.pos(b.minX, b.minY, b.minZ).endVertex(); w.pos(b.minX, b.maxY, b.minZ).endVertex();
        w.pos(b.maxX, b.maxY, b.minZ).endVertex(); w.pos(b.maxX, b.minY, b.minZ).endVertex();
        // south
        w.pos(b.minX, b.minY, b.maxZ).endVertex(); w.pos(b.maxX, b.minY, b.maxZ).endVertex();
        w.pos(b.maxX, b.maxY, b.maxZ).endVertex(); w.pos(b.minX, b.maxY, b.maxZ).endVertex();
        // west
        w.pos(b.minX, b.minY, b.minZ).endVertex(); w.pos(b.minX, b.minY, b.maxZ).endVertex();
        w.pos(b.minX, b.maxY, b.maxZ).endVertex(); w.pos(b.minX, b.maxY, b.minZ).endVertex();
        // east
        w.pos(b.maxX, b.minY, b.minZ).endVertex(); w.pos(b.maxX, b.maxY, b.minZ).endVertex();
        w.pos(b.maxX, b.maxY, b.maxZ).endVertex(); w.pos(b.maxX, b.minY, b.maxZ).endVertex();
        t.draw();
    }
}
