package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ChestStealer extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay ms", 100, 0, 500, 10));
    private final BooleanSetting autoClose = add(new BooleanSetting("Close when empty", true));
    private long next;

    public ChestStealer() { super("ChestStealer", Category.PLAYER); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame()) return;
        if (!(mc.currentScreen instanceof GuiChest)) return;
        if (!(mc.thePlayer.openContainer instanceof ContainerChest)) return;

        long now = System.currentTimeMillis();
        if (now < next) return;

        ContainerChest c = (ContainerChest) mc.thePlayer.openContainer;
        IInventory inv = c.getLowerChestInventory();
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack s = inv.getStackInSlot(i);
            if (s == null) continue;
            mc.playerController.windowClick(c.windowId, i, 0, 1, mc.thePlayer); // shift-click
            next = now + (long) delay.get();
            return;
        }
        if (autoClose.get()) mc.thePlayer.closeScreen();
    }
}
