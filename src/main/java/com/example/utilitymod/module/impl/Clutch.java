package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockTNT;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Clutch extends Module {
    private final NumberSetting minFall = add(new NumberSetting("Min fall", 3.0, 1.0, 10.0, 0.5));
    private final NumberSetting delay = add(new NumberSetting("Delay ms", 0, 0, 300, 10));
    private final BooleanSetting silent = add(new BooleanSetting("Silent aim", true));
    private final BooleanSetting autoSwitch = add(new BooleanSetting("Auto switch", true));
    private final BooleanSetting voidOnly = add(new BooleanSetting("Void only", false));
    private long next;
    private int prevSlot = -1;

    public Clutch() { super("Clutch", Category.PLAYER); }

    private static class Placement {
        BlockPos neighbor;
        EnumFacing side;
        Vec3 hit;
    }

    private boolean replaceable(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock().getMaterial().isReplaceable();
    }

    private boolean usable(ItemStack s) {
        if (s == null || s.stackSize <= 0 || !(s.getItem() instanceof ItemBlock)) return false;
        net.minecraft.block.Block b = ((ItemBlock) s.getItem()).getBlock();
        return !(b instanceof BlockFalling) && !(b instanceof BlockContainer) && !(b instanceof BlockTNT);
    }

    private int findBlockSlot(EntityPlayerSP p) {
        if (usable(p.getHeldItem())) return p.inventory.currentItem;
        if (!autoSwitch.get()) return -1;
        for (int i = 0; i < 9; i++) if (usable(p.inventory.getStackInSlot(i))) return i;
        return -1;
    }

    private boolean overVoid(EntityPlayerSP p) {
        int x = (int) Math.floor(p.posX), z = (int) Math.floor(p.posZ);
        for (int y = (int) p.posY - 1; y >= 0; y--) {
            if (!mc.theWorld.isAirBlock(new BlockPos(x, y, z))) return false;
        }
        return true;
    }

    private Placement findPlacement(EntityPlayerSP p) {
        BlockPos base = new BlockPos(p.posX, p.posY, p.posZ);
        Vec3 eye = p.getPositionEyes(1.0F);
        Placement best = null;
        double bestScore = Double.MAX_VALUE;

        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -4; dy <= -1; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos pos = base.add(dx, dy, dz);
                    if (!replaceable(pos)) continue;
                    for (EnumFacing f : EnumFacing.values()) {
                        BlockPos n = pos.offset(f);
                        if (replaceable(n)) continue;
                        EnumFacing side = f.getOpposite();
                        Vec3 hit = new Vec3(
                                n.getX() + 0.5 + side.getFrontOffsetX() * 0.5,
                                n.getY() + 0.5 + side.getFrontOffsetY() * 0.5,
                                n.getZ() + 0.5 + side.getFrontOffsetZ() * 0.5);
                        if (eye.distanceTo(hit) > 4.5) continue;

                        double hx = pos.getX() + 0.5 - p.posX, hz = pos.getZ() + 0.5 - p.posZ;
                        double score = hx * hx + hz * hz + Math.abs(pos.getY() - (base.getY() - 1)) * 0.5;
                        if (score < bestScore) {
                            bestScore = score;
                            best = new Placement();
                            best.neighbor = n;
                            best.side = side;
                            best.hit = hit;
                        }
                    }
                }
            }
        }
        return best;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame() || mc.currentScreen != null) return;
        EntityPlayerSP p = mc.thePlayer;

        if (p.onGround || p.capabilities.isFlying || p.isInWater() || p.isInLava()) {
            if (prevSlot != -1) { p.inventory.currentItem = prevSlot; prevSlot = -1; }
            return;
        }
        if (p.motionY >= -0.1 || p.fallDistance < minFall.get()) return;
        if (voidOnly.get() && !overVoid(p)) return;

        long now = System.currentTimeMillis();
        if (now < next) return;

        int slot = findBlockSlot(p);
        if (slot == -1) return;
        Placement pl = findPlacement(p);
        if (pl == null) return;

        if (slot != p.inventory.currentItem) {
            if (prevSlot == -1) prevSlot = p.inventory.currentItem;
            p.inventory.currentItem = slot;
        }

        Vec3 eye = p.getPositionEyes(1.0F);
        double dx = pl.hit.xCoord - eye.xCoord;
        double dy = pl.hit.yCoord - eye.yCoord;
        double dz = pl.hit.zCoord - eye.zCoord;
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(dy, Math.hypot(dx, dz)) * 180.0 / Math.PI);

        if (silent.get()) {
            // server sees the aim, your camera doesn't move
            p.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(yaw, pitch, p.onGround));
        } else {
            p.rotationYaw = yaw;
            p.rotationPitch = pitch;
        }

        if (mc.playerController.onPlayerRightClick(p, mc.theWorld, p.getHeldItem(), pl.neighbor, pl.side, pl.hit)) {
            p.swingItem();
            next = now + (long) delay.get();
        }
    }

    @Override protected void onDisable() {
        if (prevSlot != -1 && mc.thePlayer != null) mc.thePlayer.inventory.currentItem = prevSlot;
        prevSlot = -1;
    }

    @Override public String getSuffix() { return silent.get() ? "Silent" : "Normal"; }
}
