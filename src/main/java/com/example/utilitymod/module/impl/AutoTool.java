package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoTool extends Module {
    public AutoTool() { super("AutoTool", Category.PLAYER); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame() || mc.currentScreen != null) return;
        if (!mc.gameSettings.keyBindAttack.isKeyDown()) return;
        MovingObjectPosition o = mc.objectMouseOver;
        if (o == null || o.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        Block block = mc.theWorld.getBlockState(o.getBlockPos()).getBlock();
        float best = 1.0F;
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.thePlayer.inventory.getStackInSlot(i);
            if (s == null) continue;
            float str = s.getStrVsBlock(block);
            if (str > best) { best = str; slot = i; }
        }
        if (slot != -1) mc.thePlayer.inventory.currentItem = slot;
    }
}
