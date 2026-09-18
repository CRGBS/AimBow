package net.famzangl.minecraft.aimbow;
public final class SmartAimConfig {
    private SmartAimConfig() {}
    public static final double AUTO_AIM_RANGE = 128.0D;
    public static final double MAX_AIM_ANGLE = 25.0D;
    public static final float AIM_SMOOTH = 0.18F;
    public static final float MAX_YAW_PER_TICK = 3.0F;
    public static final float MAX_PITCH_PER_TICK = 2.0F;
    public static final int TARGET_UPDATE_INTERVAL = 3;
    public static final int MAX_CANDIDATES = 12;
    public static final int LOCK_GRACE_TICKS = 10;
    public static final double TARGET_SWITCH_FACTOR = 0.82D;
    public static final float HITBOX_BIAS = 0.20F;
}
