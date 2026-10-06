package com.example.utilitymod.module.impl;

import com.example.utilitymod.UtilityMod;
import com.example.utilitymod.module.*;
import com.example.utilitymod.setting.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;

public class Backtrack extends Module {
    private final ModeSetting mode = add(new ModeSetting("Mode", "Entity", "Entity", "Lag"));
    private final NumberSetting delay = add(new NumberSetting("Delay ms", 100, 20, 400, 10));
    private final NumberSetting range = add(new NumberSetting("Range", 6.0, 3.0, 10.0, 0.5));

    // updated on the main thread each tick, read from the network thread
    private volatile boolean playerNear;

    public Backtrack() { super("Backtrack", Category.COMBAT); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        boolean near = false;
        if (inGame()) {
            for (Object o : mc.theWorld.playerEntities) {
                EntityPlayer p = (EntityPlayer) o;
                if (p != mc.thePlayer && !p.isDead && mc.thePlayer.getDistanceToEntity(p) <= range.get()) {
                    near = true;
                    break;
                }
            }
        }
        playerNear = near;
    }

    /** Register once in UtilityMod.init; attaches the packet handler on every connection. */
    public static class ConnectHook {
        @SubscribeEvent
        public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent e) {
            try {
                e.manager.channel().pipeline().addBefore("packet_handler", "utilitymod_backtrack", new Handler());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static class Pending {
        final Object msg;
        final long release;
        Pending(Object msg, long release) { this.msg = msg; this.release = release; }
    }

    private static class Handler extends ChannelInboundHandlerAdapter {
        private final ArrayDeque<Pending> queue = new ArrayDeque<Pending>();
        private ChannelHandlerContext ctx;
        private boolean scheduled;

        @Override
        public void channelRead(ChannelHandlerContext context, Object msg) throws Exception {
            this.ctx = context;
            Backtrack bt = UtilityMod.modules == null ? null : UtilityMod.modules.get(Backtrack.class);
            boolean active = bt != null && bt.isEnabled();
            boolean lag = active && bt.mode.is("Lag");
            boolean hold = active && bt.shouldHold(msg);

            // Entity mode lets other packets pass; Lag mode must keep everything in order
            if (!hold && (queue.isEmpty() || !lag)) {
                super.channelRead(context, msg);
                return;
            }

            long now = System.currentTimeMillis();
            long release = hold ? now + (long) bt.delay.get() : now;
            if (!queue.isEmpty()) release = Math.max(release, queue.peekLast().release);
            queue.addLast(new Pending(msg, release));
            schedule(Math.max(0, queue.peekFirst().release - now));
        }

        private void schedule(long ms) {
            if (scheduled) return;
            scheduled = true;
            ctx.executor().schedule(new Runnable() {
                public void run() { flush(); }
            }, ms, TimeUnit.MILLISECONDS);
        }

        private void flush() {
            scheduled = false;
            Backtrack bt = UtilityMod.modules == null ? null : UtilityMod.modules.get(Backtrack.class);
            boolean releaseAll = bt == null || !bt.isEnabled();
            long now = System.currentTimeMillis();
            while (!queue.isEmpty() && (releaseAll || queue.peekFirst().release <= now)) {
                ctx.fireChannelRead(queue.pollFirst().msg);
            }
            if (!queue.isEmpty()) schedule(Math.max(1, queue.peekFirst().release - now));
        }
    }

    private boolean shouldHold(Object msg) {
        if (mode.is("Lag")) return playerNear;
        try {
            if (mc.thePlayer == null || mc.theWorld == null) return false;
            Entity target = null;
            if (msg instanceof S14PacketEntity) target = ((S14PacketEntity) msg).getEntity(mc.theWorld);
            else if (msg instanceof S18PacketEntityTeleport)
                target = mc.theWorld.getEntityByID(((S18PacketEntityTeleport) msg).getEntityId());
            return target instanceof EntityPlayer && target != mc.thePlayer
                    && mc.thePlayer.getDistanceToEntity(target) <= range.get();
        } catch (Exception ex) {
            return false;
        }
    }

    @Override public String getSuffix() { return mode.get() + " " + (int) delay.get() + "ms"; }
}
