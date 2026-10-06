package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class AutoBlock extends Module {
    private final NumberSetting ticks = add(new NumberSetting("Block ticks", 4, 1, 10, 1));
    private final BooleanSetting swordOnly = add(new BooleanSetting("Swords only", true));
    private int blockTicks;
    private boolean forcing;

    public AutoBlock() { super("AutoBlock", Category.COMBAT); }

    private boolean physicalUse() {
        int code = mc.gameSettings.keyBindUseItem.getKeyCode();
        if (code < 0) return Mouse.isButtonDown(code + 100);
        return code > 0 && Keyboard.isKeyDown(code);
    }

    private boolean holdingSword() {
        ItemStack held = mc.thePlayer.getHeldItem();
        return held != null && held.getItem() instanceof ItemSword;
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        if (!inGame() || e.entityPlayer != mc.thePlayer) return;
        if (swordOnly.get() && !holdingSword()) return;
        blockTicks = (int) ticks.get();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame() || mc.currentScreen != null) return;
        int code = mc.gameSettings.keyBindUseItem.getKeyCode();
        if (blockTicks > 0) {
            if (swordOnly.get() && !holdingSword()) { blockTicks = 0; return; }
            KeyBinding.setKeyBindState(code, true);
            forcing = true;
            blockTicks--;
        } else if (forcing) {
            KeyBinding.setKeyBindState(code, physicalUse());
            forcing = false;
        }
    }

    @Override protected void onDisable() {
        blockTicks = 0;
        if (forcing && mc.gameSettings != null)
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), physicalUse());
        forcing = false;
    }

    @Override public String getSuffix() { return String.valueOf((int) ticks.get()); }
}
