package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Scaffold extends Module {
    private final BooleanSetting autoSwitch = add(new BooleanSetting("Auto switch", true));

    public Scaffold() { super("Scaffold", Category.PLAYER); }

    private boolean replaceable(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock().getMaterial().isReplaceable();
    }

    private boolean equipBlock() {
        ItemStack held = mc.thePlayer.getHeldItem();
        if (held != null && held.getItem() instanceof ItemBlock) return true;
        if (!autoSwitch.get()) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.thePlayer.inventory.getStackInSlot(i);
            if (s != null && s.getItem() instanceof ItemBlock && s.stackSize > 0) {
                mc.thePlayer.inventory.currentItem = i;
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame() || mc.currentScreen != null) return;
        BlockPos below = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
        if (!replaceable(below) || !equipBlock()) return;

        for (EnumFacing f : EnumFacing.values()) {
            BlockPos n = below.offset(f);
            if (replaceable(n)) continue;
            EnumFacing side = f.getOpposite();
            Vec3 hit = new Vec3(
                    n.getX() + 0.5 + side.getFrontOffsetX() * 0.5,
                    n.getY() + 0.5 + side.getFrontOffsetY() * 0.5,
                    n.getZ() + 0.5 + side.getFrontOffsetZ() * 0.5);
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), n, side, hit)) {
                mc.thePlayer.swingItem();
            }
            return;
        }
    }
}
