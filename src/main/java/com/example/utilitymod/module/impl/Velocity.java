package com.example.utilitymod.module.impl;

import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.NumberSetting;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import java.util.Random;

public class Velocity extends Module {
    // percent of knockback you KEEP (100 = vanilla, 0 = none)
    private final NumberSetting horizontal = add(new NumberSetting("Horizontal %", 60, 0, 100, 1));
    private final NumberSetting vertical = add(new NumberSetting("Vertical %", 100, 0, 100, 1));
    private final NumberSetting chance = add(new NumberSetting("Chance %", 100, 0, 100, 5));
    private final Random rand = new Random();

    public Velocity() {
        super("Velocity", Category.COMBAT);
        // must always be listening so the packet handler is attached on every connection
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent e) {
                try {
                    e.manager.channel().pipeline().addBefore("packet_handler", "utilitymod_velocity", new Handler());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private class Handler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (isEnabled() && msg instanceof S12PacketEntityVelocity && mc.thePlayer != null) {
                S12PacketEntityVelocity p = (S12PacketEntityVelocity) msg;
                if (p.getEntityID() == mc.thePlayer.getEntityId() && rand.nextInt(100) < chance.get()) {
                    double h = horizontal.get() / 100.0;
                    double v = vertical.get() / 100.0;
                    msg = new S12PacketEntityVelocity(p.getEntityID(),
                            p.getMotionX() / 8000.0 * h,
                            p.getMotionY() / 8000.0 * v,
                            p.getMotionZ() / 8000.0 * h);
                }
            }
            super.channelRead(ctx, msg);
        }
    }

    @Override public String getSuffix() { return (int) horizontal.get() + "% " + (int) vertical.get() + "%"; }
}
