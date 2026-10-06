package com.example.utilitymod.util;

import net.minecraft.client.entity.EntityPlayerSP;

public class MoveUtil {
    public static boolean isMoving(EntityPlayerSP p) {
        return p.movementInput.moveForward != 0 || p.movementInput.moveStrafe != 0;
    }

    /** Sets horizontal motion to `speed` in the direction the player is steering. */
    public static void setSpeed(EntityPlayerSP p, double speed) {
        double forward = p.movementInput.moveForward;
        double strafe = p.movementInput.moveStrafe;
        float yaw = p.rotationYaw;
        if (forward == 0 && strafe == 0) { p.motionX = 0; p.motionZ = 0; return; }
        if (forward != 0) {
            if (strafe > 0) yaw += forward > 0 ? -45 : 45;
            else if (strafe < 0) yaw += forward > 0 ? 45 : -45;
            strafe = 0;
            forward = forward > 0 ? 1 : -1;
        }
        double cos = Math.cos(Math.toRadians(yaw + 90));
        double sin = Math.sin(Math.toRadians(yaw + 90));
        p.motionX = forward * speed * cos + strafe * speed * sin;
        p.motionZ = forward * speed * sin - strafe * speed * cos;
    }
}
