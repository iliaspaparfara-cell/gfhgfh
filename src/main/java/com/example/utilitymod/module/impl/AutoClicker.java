package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.NumberSetting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Method;
import java.util.Random;

public class AutoClicker extends Module {
    private final NumberSetting minCps = add(new NumberSetting("Min CPS", 8, 1, 20, 1));
    private final NumberSetting maxCps = add(new NumberSetting("Max CPS", 12, 1, 20, 1));
    private final Random rand = new Random();
    private Method clickMouse;
    private long next;

    public AutoClicker() { super("AutoClicker", Category.COMBAT); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !inGame() || mc.currentScreen != null) return;
        if (!mc.gameSettings.keyBindAttack.isKeyDown()) return;

        // don't spam-click while mining a block, vanilla hold-to-mine handles that
        if (mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) return;

        long now = System.currentTimeMillis();
        if (now < next) return;

        double lo = Math.min(minCps.get(), maxCps.get());
        double hi = Math.max(minCps.get(), maxCps.get());
        double cps = lo + rand.nextDouble() * (hi - lo);
        next = now + (long) (1000.0 / Math.max(1.0, cps));

        try {
            if (clickMouse == null) {
                clickMouse = ReflectionHelper.findMethod(net.minecraft.client.Minecraft.class, mc,
                        new String[]{"clickMouse", "func_147116_af"});
            }
            // clear vanilla's post-miss click cooldown
            ObfuscationReflectionHelper.setPrivateValue(net.minecraft.client.Minecraft.class, mc, 0,
                    "leftClickCounter", "field_71429_W");
            clickMouse.invoke(mc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override public String getSuffix() { return (int) minCps.get() + "-" + (int) maxCps.get(); }
}
