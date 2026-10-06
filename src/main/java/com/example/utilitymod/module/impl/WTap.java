package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.util.Random;

public class WTap extends Module {
    private final NumberSetting chance = add(new NumberSetting("Chance %", 100, 0, 100, 5));
    private final BooleanSetting onlySprinting = add(new BooleanSetting("Only when sprinting", true));
    private final Random rand = new Random();
    private int stage; // 2 = release W, 1 = re-sprint

    public WTap() { super("WTap", Category.COMBAT); }

    private boolean physical(KeyBinding kb) {
        int code = kb.getKeyCode();
        if (code < 0) return Mouse.isButtonDown(code + 100);
        return code > 0 && Keyboard.isKeyDown(code);
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        if (!inGame() || e.entityPlayer != mc.thePlayer) return;
        if (onlySprinting.get() && !mc.thePlayer.isSprinting()) return;
        if (rand.nextInt(100) >= chance.get()) return;
        if (stage == 0) stage = 2;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame()) return;
        if (stage == 2) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
            mc.thePlayer.setSprinting(false);
            stage = 1;
        } else if (stage == 1) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), physical(mc.gameSettings.keyBindForward));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
            stage = -1;
        } else if (stage == -1) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), physical(mc.gameSettings.keyBindSprint));
            stage = 0;
        }
    }

    @Override protected void onDisable() {
        stage = 0;
        if (mc.gameSettings == null) return;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), physical(mc.gameSettings.keyBindForward));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), physical(mc.gameSettings.keyBindSprint));
    }
}
