package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class Eagle extends Module {
    private final BooleanSetting blocksOnly = add(new BooleanSetting("Only holding blocks", true));
    private final BooleanSetting lookDown = add(new BooleanSetting("Only looking down", false));
    private final NumberSetting minPitch = add(new NumberSetting("Min pitch", 60, 0, 90, 5));
    private final BooleanSetting backOnly = add(new BooleanSetting("Only moving backwards", false));

    public Eagle() { super("Eagle", Category.PLAYER); }

    private boolean physicalSneak() {
        int code = mc.gameSettings.keyBindSneak.getKeyCode();
        return code > 0 && Keyboard.isKeyDown(code);
    }

    private void setSneak(boolean state) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), state);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame()) return;
        if (mc.currentScreen != null) { return; }

        boolean edge = false;
        if (mc.thePlayer.onGround && shouldWork()) {
            BlockPos below = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ);
            edge = mc.theWorld.getBlockState(below).getBlock() == Blocks.air;
        }
        // stay sneaking if edge OR you're physically holding the key
        setSneak(edge || physicalSneak());
    }

    private boolean shouldWork() {
        if (blocksOnly.get()) {
            ItemStack held = mc.thePlayer.getHeldItem();
            if (held == null || !(held.getItem() instanceof ItemBlock)) return false;
        }
        if (lookDown.get() && mc.thePlayer.rotationPitch < minPitch.get()) return false;
        if (backOnly.get() && mc.thePlayer.moveForward >= 0) return false;
        return true;
    }

    @Override protected void onDisable() {
        if (mc.gameSettings != null) setSneak(physicalSneak());
    }
}
