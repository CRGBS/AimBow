package net.famzangl.minecraft.aimbow;

import net.famzangl.minecraft.aimbow.aiming.Bow.ReverseBowSolver;
import net.famzangl.minecraft.aimbow.aiming.ColissionData;
import net.famzangl.minecraft.aimbow.aiming.ColissionSolver;
import net.famzangl.minecraft.aimbow.aiming.RayData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static net.famzangl.minecraft.aimbow.AimBowMod.*;
import static net.famzangl.minecraft.aimbow.aiming.Bow.BowColissionSolver.force;

public class AimbowGui {

    private final FloatBuffer modelBuffer =
            BufferUtils.createFloatBuffer(16);

    private final FloatBuffer projectionBuffer =
            BufferUtils.createFloatBuffer(16);

    private final IntBuffer viewPort =
            BufferUtils.createIntBuffer(4);

    private final FloatBuffer win_pos =
            BufferUtils.createFloatBuffer(3);

    private final Minecraft mc = Minecraft.getMinecraft();

    private float partialTicks;

    /*
     * 如果你的其他設定程式會修改 autoAim，
     * 可以保留 false。
     *
     * 如果沒有設定系統，改成 true 測試即可。
     */
    public boolean autoAim = false;

    /*
     * 自動瞄準最大距離
     */
    private static final double AUTO_AIM_RANGE = 200.0D;

    /*
     * 每 tick 最大轉動比例。
     *
     * 1.0 = 立即瞄準
     * 0.5 = 一半
     * 0.25 = 比較平滑
     */
    private static final float AIM_SMOOTH = 1.0F;

    /*
     * 防止每個 Render Event 都重複計算。
     */
    private int lastAimTick = -1;


    // =========================================================
    // Tick - AUTO AIM
    // =========================================================

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (mc.thePlayer == null ||
                mc.theWorld == null) {
            return;
        }

        if (!autoAim) {
            return;
        }

        /*
         * 防止同一個 tick 重複執行。
         */
        if (mc.thePlayer.ticksExisted == lastAimTick) {
            return;
        }

        lastAimTick = mc.thePlayer.ticksExisted;

        ItemStack heldItem = mc.thePlayer.getHeldItem();

        if (!shouldAutoAim(heldItem)) {
            return;
        }

        ColissionSolver solver =
                ColissionSolver.forItem(heldItem, mc);

        if (solver == null) {
            return;
        }

        handleAutoAim(solver);
    }


    // =========================================================
    // HUD
    // =========================================================

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {

        if (event.type !=
                RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        partialTicks = event.partialTicks;

        if (mc.thePlayer == null) {
            return;
        }

        ScaledResolution resolution =
                new ScaledResolution(mc);

        if (TrajectoryState) {

            if (blockDistanceState) {
                renderDistanceOverlay(resolution);
            }
        }

        renderCustomCrosshair(resolution);
    }


    // =========================================================
    // WORLD RENDER
    // =========================================================

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {

        if (mc.thePlayer == null) {
            return;
        }

        if (TrajectoryState) {
            renderTrajectory(event.partialTicks);
        }

        drawCollisionBox(event.partialTicks);
    }


    // =========================================================
    // TRAJECTORY
    // =========================================================

    public static void renderTrajectory(float partialTicks) {

        RayData.trajectory.clear();

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.thePlayer == null) {
            return;
        }

        EntityPlayerSP player = mc.thePlayer;

        ItemStack heldItem =
                player.getHeldItem();

        ColissionSolver solver =
                ColissionSolver.forItem(heldItem, mc);

        if (solver == null) {
            return;
        }

        solver.computeCurrentColissionPoints();

        if (force <= 0.2 ||
                RayData.trajectory.isEmpty()) {
            return;
        }

        GlStateManager.pushMatrix();

        try {

            GlStateManager.translate(
                    -mc.getRenderManager().viewerPosX,
                    -mc.getRenderManager().viewerPosY,
                    -mc.getRenderManager().viewerPosZ
            );

            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();

            GlStateManager.enableBlend();

            GlStateManager.blendFunc(
                    GL11.GL_SRC_ALPHA,
                    GL11.GL_ONE_MINUS_SRC_ALPHA
            );

            GL11.glLineWidth(width);

            Tessellator tessellator =
                    Tessellator.getInstance();

            WorldRenderer worldRenderer =
                    tessellator.getWorldRenderer();

            worldRenderer.begin(
                    GL11.GL_LINE_STRIP,
                    DefaultVertexFormats.POSITION_COLOR
            );

            float r = red / 255.0f;
            float g = green / 255.0f;
            float b = blue / 255.0f;
            float a = alpha / 255.0f;

            for (Vec3 point :
                    RayData.trajectory) {

                worldRenderer.pos(
                                point.xCoord,
                                point.yCoord,
                                point.zCoord
                        )
                        .color(r, g, b, a)
                        .endVertex();
            }

            tessellator.draw();

        } finally {

            GL11.glLineWidth(1.0F);

            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();

            GlStateManager.popMatrix();
        }
    }


    // =========================================================
    // DISTANCE
    // =========================================================

    private void renderDistanceOverlay(
            ScaledResolution resolution) {

        EntityPlayerSP player =
                mc.thePlayer;

        if (player == null) {
            return;
        }

        ItemStack heldItem =
                player.getHeldItem();

        ColissionSolver solver =
                ColissionSolver.forItem(
                        heldItem,
                        mc
                );

        if (solver == null) {
            return;
        }

        List<ColissionData> collisions =
                solver.computeCurrentColissionPoints();

        if (collisions == null ||
                collisions.isEmpty()) {
            return;
        }

        ColissionData firstHit =
                collisions.get(0);

        Vec3 playerPos =
                player.getPositionVector();

        Vec3 hitPos =
                new Vec3(
                        firstHit.x,
                        firstHit.y,
                        firstHit.z
                );

        double distance =
                playerPos.distanceTo(hitPos);

        String text;

        if (firstHit.hitEntity != null) {
            text = String.format(
                    "Entity Distance: %.1f",
                    distance
            );
        } else {
            text = String.format(
                    "Block Distance: %.1f",
                    distance
            );
        }

        GlStateManager.pushMatrix();

        mc.fontRendererObj.drawString(
                text,
                resolution.getScaledWidth() / 2 + 10,
                resolution.getScaledHeight() / 2 - 4,
                0xFFFFFF
        );

        GlStateManager.popMatrix();
    }


    // =========================================================
    // CROSSHAIR
    // =========================================================

    private void renderCustomCrosshair(
            ScaledResolution resolution) {

        if (!TrajectoryState) {
            return;
        }

        EntityPlayerSP player =
                mc.thePlayer;

        if (player == null) {
            return;
        }

        ItemStack heldItem =
                player.getHeldItem();

        ColissionSolver solver =
                ColissionSolver.forItem(
                        heldItem,
                        mc
                );

        if (solver == null) {
            return;
        }

        List<ColissionData> collisions =
                solver.computeCurrentColissionPoints();

        boolean drawn = false;

        for (ColissionData p :
                collisions) {

            Pos2 pos = getScreenPosition(
                    p.x,
                    p.y + player.getEyeHeight(),
                    p.z,
                    resolution
            );

            boolean hit =
                    p.hitEntity != null;

            drawAimIndicator(
                    pos.x,
                    pos.y,
                    hit
            );

            /*
             * 注意：
             *
             * 這裡不再執行 handleAutoAim()。
             *
             * AutoAim 已經移到 ClientTick。
             */

            drawn = true;
        }

        if (!drawn) {

            drawAimIndicator(
                    resolution.getScaledWidth() / 2,
                    resolution.getScaledHeight() / 2,
                    false
            );
        }
    }


    // =========================================================
    // COLLISION BOX
    // =========================================================

    private void drawCollisionBox(
            float partialTicks) {

        EntityPlayerSP player =
                mc.thePlayer;

        if (player == null) {
            return;
        }

        ItemStack heldItem =
                player.getHeldItem();

        ColissionSolver solver =
                ColissionSolver.forItem(
                        heldItem,
                        mc
                );

        if (solver == null ||
                RayData.trajectory.isEmpty()) {
            return;
        }

        List<ColissionData> collisions =
                solver.computeCurrentColissionPoints();

        for (ColissionData collision :
                collisions) {

            if (collision.hitEntity
                    instanceof EntityLivingBase) {

                drawEntityTint(
                        (EntityLivingBase)
                                collision.hitEntity,
                        partialTicks
                );
            }
        }

        Vec3 lastPos =
                RayData.trajectory.get(
                        RayData.trajectory.size() - 1
                );

        BlockPos endBlock =
                new BlockPos(
                        lastPos.xCoord,
                        lastPos.yCoord,
                        lastPos.zCoord
                );

        Vec3 direction = null;

        if (RayData.trajectory.size() >= 2) {

            Vec3 prevPos =
                    RayData.trajectory.get(
                            RayData.trajectory.size() - 2
                    );

            direction = new Vec3(
                    lastPos.xCoord -
                            prevPos.xCoord,

                    lastPos.yCoord -
                            prevPos.yCoord,

                    lastPos.zCoord -
                            prevPos.zCoord
            );
        }

        drawBlockHighlight(
                endBlock,
                direction,
                partialTicks
        );
    }


    // =========================================================
    // ENTITY TINT
    // =========================================================

    private void drawEntityTint(
            EntityLivingBase targetEntity,
            float partialTicks) {

        Entity viewer =
                Minecraft.getMinecraft()
                        .getRenderViewEntity();

        if (viewer == null) {
            return;
        }

        GlStateManager.pushMatrix();

        try {

            double viewerX =
                    viewer.lastTickPosX +
                            (viewer.posX -
                                    viewer.lastTickPosX)
                                    * partialTicks;

            double viewerY =
                    viewer.lastTickPosY +
                            (viewer.posY -
                                    viewer.lastTickPosY)
                                    * partialTicks;

            double viewerZ =
                    viewer.lastTickPosZ +
                            (viewer.posZ -
                                    viewer.lastTickPosZ)
                                    * partialTicks;

            GlStateManager.enableBlend();

            GlStateManager.tryBlendFuncSeparate(
                    770,
                    771,
                    1,
                    0
            );

            GlStateManager.disableTexture2D();

            GlStateManager.depthMask(false);

            Tessellator tessellator =
                    Tessellator.getInstance();

            WorldRenderer worldrenderer =
                    tessellator.getWorldRenderer();

            double x =
                    targetEntity.lastTickPosX +
                            (targetEntity.posX -
                                    targetEntity.lastTickPosX)
                                    * partialTicks
                            - viewerX;

            double y =
                    targetEntity.lastTickPosY +
                            (targetEntity.posY -
                                    targetEntity.lastTickPosY)
                                    * partialTicks
                            - viewerY;

            double z =
                    targetEntity.lastTickPosZ +
                            (targetEntity.posZ -
                                    targetEntity.lastTickPosZ)
                                    * partialTicks
                            - viewerZ;

            AxisAlignedBB entityBox =
                    targetEntity.getEntityBoundingBox();

            double width =
                    (entityBox.maxX -
                            entityBox.minX) / 2;

            double height =
                    entityBox.maxY -
                            entityBox.minY;

            AxisAlignedBB box =
                    new AxisAlignedBB(
                            x - width,
                            y,
                            z - width,
                            x + width,
                            y + height,
                            z + width
                    );

            worldrenderer.begin(
                    GL11.GL_LINE_STRIP,
                    DefaultVertexFormats.POSITION_COLOR
            );

            float r = 1.0f;
            float g = 0.0f;
            float b = 0.0f;
            float a = 0.8f;

            worldrenderer.pos(
                    box.minX,
                    box.minY,
                    box.minZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.maxX,
                    box.minY,
                    box.minZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.maxX,
                    box.minY,
                    box.maxZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.minX,
                    box.minY,
                    box.maxZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.minX,
                    box.minY,
                    box.minZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.minX,
                    box.maxY,
                    box.minZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.maxX,
                    box.maxY,
                    box.minZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.maxX,
                    box.maxY,
                    box.maxZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.minX,
                    box.maxY,
                    box.maxZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.minX,
                    box.maxY,
                    box.minZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.minX,
                    box.minY,
                    box.minZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.minX,
                    box.maxY,
                    box.minZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.maxX,
                    box.minY,
                    box.minZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.maxX,
                    box.maxY,
                    box.minZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.maxX,
                    box.minY,
                    box.maxZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.maxX,
                    box.maxY,
                    box.maxZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.minX,
                    box.minY,
                    box.maxZ
            ).color(r, g, b, a).endVertex();

            worldrenderer.pos(
                    box.minX,
                    box.maxY,
                    box.maxZ
            ).color(r, g, b, a).endVertex();

            tessellator.draw();

        } finally {

            GL11.glLineWidth(1.0F);

            GlStateManager.color(
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F
            );

            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();

            GlStateManager.popMatrix();
        }
    }


    // =========================================================
    // AUTO AIM
    // =========================================================

    private boolean shouldAutoAim(
            ItemStack item) {

        if (mc.thePlayer == null) {
            return false;
        }

        if (item == null) {
            return false;
        }

        if (item.getItem() != Items.bow) {
            return false;
        }

        /*
         * Minecraft 1.8：
         *
         * 玩家正在使用物品
         * 且物品必須是弓。
         */
        if (!mc.thePlayer.isUsingItem()) {
            return false;
        }

        ItemStack usingItem =
                mc.thePlayer.getItemInUse();

        return usingItem != null &&
                usingItem.getItem() == Items.bow;
    }


    private void handleAutoAim(
            ColissionSolver solver) {

        if (mc.thePlayer == null ||
                mc.theWorld == null) {
            return;
        }

        EntityPlayerSP player =
                mc.thePlayer;

        AxisAlignedBB searchBox =
                player.getEntityBoundingBox()
                        .expand(
                                AUTO_AIM_RANGE,
                                100.0D,
                                AUTO_AIM_RANGE
                        );

        List<Entity> entities =
                mc.theWorld.getEntitiesWithinAABB(
                        Entity.class,
                        searchBox
                );

        if (entities == null ||
                entities.isEmpty()) {
            return;
        }

        ReverseBowSolver aimHelper =
                new ReverseBowSolver(
                        solver.getGravity(),
                        solver.getVelocity()
                );

        CloseEntity bestCandidate = null;

        for (Entity entity : entities) {

            if (entity == null) {
                continue;
            }

            if (entity == player) {
                continue;
            }

            if (!entity.canBeCollidedWith()) {
                continue;
            }

            if (entity.isDead) {
                continue;
            }

            /*
             * Living Entity 才檢查血量。
             */
            if (entity instanceof EntityLivingBase) {

                EntityLivingBase living =
                        (EntityLivingBase) entity;

                if (living.getHealth() <= 0.0F) {
                    continue;
                }
            }

            double distanceSq =
                    entity.getDistanceSqToEntity(
                            player
                    );

            if (distanceSq >
                    AUTO_AIM_RANGE *
                            AUTO_AIM_RANGE) {
                continue;
            }

            Vec3 aimVector;

            try {

                aimVector =
                        aimHelper.getLookForTarget(
                                entity
                        );

            } catch (Exception e) {
                continue;
            }

            if (aimVector == null) {
                continue;
            }

            /*
             * 檢查向量是否有效。
             */
            double length =
                    Math.sqrt(
                            aimVector.xCoord *
                                    aimVector.xCoord +

                            aimVector.yCoord *
                                    aimVector.yCoord +

                            aimVector.zCoord *
                                    aimVector.zCoord
                    );

            if (length <
                    0.000001D ||
                    Double.isNaN(length) ||
                    Double.isInfinite(length)) {
                continue;
            }

            /*
             * Normalize。
             */
            aimVector =
                    new Vec3(
                            aimVector.xCoord / length,
                            aimVector.yCoord / length,
                            aimVector.zCoord / length
                    );

            List<ColissionData> results;

            try {

                results =
                        solver.computeColissionWithLook(
                                aimVector
                        );

            } catch (Exception e) {
                continue;
            }

            if (results == null ||
                    results.isEmpty()) {
                continue;
            }

            /*
             * 第一個碰撞必須是目標。
             *
             * 如果中間有牆，
             * results.get(0) 會是方塊碰撞，
             * 因此不會瞄準穿牆目標。
             */
            ColissionData first =
                    results.get(0);

            if (first == null) {
                continue;
            }

            if (first.hitEntity != entity) {
                continue;
            }

            double distance =
                    Math.sqrt(distanceSq);

            /*
             * 選擇最近的可命中目標。
             */
            if (bestCandidate == null ||
                    distance <
                            bestCandidate.distance) {

                bestCandidate =
                        new CloseEntity(
                                entity,
                                distance,
                                aimVector
                        );
            }
        }

        /*
         * 找到目標才轉頭。
         */
        if (bestCandidate != null) {

            adjustPlayerLook(
                    bestCandidate.lookDirection
            );
        }
    }


    // =========================================================
    // ROTATION
    // =========================================================

    private void adjustPlayerLook(
            Vec3 lookDir) {

        if (mc.thePlayer == null ||
                lookDir == null) {
            return;
        }

        double dx = lookDir.xCoord;
        double dy = lookDir.yCoord;
        double dz = lookDir.zCoord;

        double horizontal =
                Math.sqrt(
                        dx * dx +
                        dz * dz
                );

        if (horizontal <
                0.000001D) {
            return;
        }

        /*
         * Minecraft 1.8 yaw。
         */
        float targetYaw =
                (float) Math.toDegrees(
                        Math.atan2(dz, dx)
                ) - 90.0F;

        /*
         * Minecraft pitch：
         *
         * 上 = 負
         * 下 = 正
         */
        float targetPitch =
                (float) -Math.toDegrees(
                        Math.atan2(
                                dy,
                                horizontal
                        )
                );

        targetYaw =
                wrapDegrees(targetYaw);

        targetPitch =
                clamp(
                        targetPitch,
                        -90.0F,
                        90.0F
                );

        float currentYaw =
                mc.thePlayer.rotationYaw;

        float currentPitch =
                mc.thePlayer.rotationPitch;

        /*
         * 解決 180/-180 跨界。
         */
        float yawDiff =
                wrapDegrees(
                        targetYaw -
                                currentYaw
                );

        float pitchDiff =
                targetPitch -
                        currentPitch;

        /*
         * 平滑轉向。
         */
        float newYaw =
                currentYaw +
                        yawDiff *
                                AIM_SMOOTH;

        float newPitch =
                currentPitch +
                        pitchDiff *
                                AIM_SMOOTH;

        newPitch =
                clamp(
                        newPitch,
                        -90.0F,
                        90.0F
                );

        mc.thePlayer.rotationYaw =
                newYaw;

        mc.thePlayer.rotationPitch =
                newPitch;

        /*
         * 同步 prevRotation。
         */
        mc.thePlayer.prevRotationYaw =
                newYaw;

        mc.thePlayer.prevRotationPitch =
                newPitch;
    }


    private float wrapDegrees(
            float angle) {

        angle %= 360.0F;

        if (angle >= 180.0F) {
            angle -= 360.0F;
        }

        if (angle < -180.0F) {
            angle += 360.0F;
        }

        return angle;
    }


    private float clamp(
            float value,
            float min,
            float max) {

        return Math.max(
                min,
                Math.min(max, value)
        );
    }


    // =========================================================
    // BLOCK HIGHLIGHT
    // =========================================================

    private void drawBlockHighlight(
            BlockPos pos,
            Vec3 direction,
            float partialTicks) {

        if (pos == null) {
            return;
        }

        Entity viewer =
                Minecraft.getMinecraft()
                        .getRenderViewEntity();

        if (viewer == null) {
            return;
        }

        GlStateManager.pushMatrix();

        try {

            double viewerX =
                    viewer.lastTickPosX +
                            (viewer.posX -
                                    viewer.lastTickPosX)
                                    * partialTicks;

            double viewerY =
                    viewer.lastTickPosY +
                            (viewer.posY -
                                    viewer.lastTickPosY)
                                    * partialTicks;

            double viewerZ =
                    viewer.lastTickPosZ +
                            (viewer.posZ -
                                    viewer.lastTickPosZ)
                                    * partialTicks;

            GlStateManager.enableBlend();

            GlStateManager.tryBlendFuncSeparate(
                    770,
                    771,
                    1,
                    0
            );

            GlStateManager.disableTexture2D();

            GlStateManager.depthMask(false);

            float r = 1.0f;
            float g = 0.0f;
            float b = 0.0f;
            float a = alpha / 255.0f;

            Tessellator tessellator =
                    Tessellator.getInstance();

            WorldRenderer worldrenderer =
                    tessellator.getWorldRenderer();

            AxisAlignedBB box =
                    new AxisAlignedBB(
                            pos.getX(),
                            pos.getY(),
                            pos.getZ(),

                            pos.getX() + 1,
                            pos.getY() + 1,
                            pos.getZ() + 1
                    )
                    .expand(
                            0.002,
                            0.002,
                            0.002
                    )
                    .offset(
                            -viewerX,
                            -viewerY,
                            -viewerZ
                    );

            String hitFace = "top";

            if (direction != null) {

                double absX =
                        Math.abs(
                                direction.xCoord
                        );

                double absY =
                        Math.abs(
                                direction.yCoord
                        );

                double absZ =
                        Math.abs(
                                direction.zCoord
                        );

                if (absY > absX &&
                        absY > absZ) {

                    hitFace =
                            direction.yCoord > 0
                                    ? "top"
                                    : "bottom";

                } else if (absX > absZ) {

                    hitFace =
                            direction.xCoord > 0
                                    ? "east"
                                    : "west";

                } else {

                    hitFace =
                            direction.zCoord > 0
                                    ? "south"
                                    : "north";
                }
            }

            worldrenderer.begin(
                    GL11.GL_QUADS,
                    DefaultVertexFormats.POSITION_COLOR
            );

            if (hitFace.equals("bottom")) {

                worldrenderer.pos(
                        box.minX,
                        box.minY,
                        box.minZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.maxX,
                        box.minY,
                        box.minZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.maxX,
                        box.minY,
                        box.maxZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.minX,
                        box.minY,
                        box.maxZ
                ).color(r, g, b, a).endVertex();

            } else if (hitFace.equals("top")) {

                worldrenderer.pos(
                        box.minX,
                        box.maxY,
                        box.minZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.minX,
                        box.maxY,
                        box.maxZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.maxX,
                        box.maxY,
                        box.maxZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.maxX,
                        box.maxY,
                        box.minZ
                ).color(r, g, b, a).endVertex();

            } else if (hitFace.equals("north")) {

                worldrenderer.pos(
                        box.minX,
                        box.minY,
                        box.minZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.minX,
                        box.maxY,
                        box.minZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.maxX,
                        box.maxY,
                        box.minZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.maxX,
                        box.minY,
                        box.minZ
                ).color(r, g, b, a).endVertex();

            } else if (hitFace.equals("south")) {

                worldrenderer.pos(
                        box.minX,
                        box.minY,
                        box.maxZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.maxX,
                        box.minY,
                        box.maxZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.maxX,
                        box.maxY,
                        box.maxZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.minX,
                        box.maxY,
                        box.maxZ
                ).color(r, g, b, a).endVertex();

            } else if (hitFace.equals("west")) {

                worldrenderer.pos(
                        box.minX,
                        box.minY,
                        box.minZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.minX,
                        box.minY,
                        box.maxZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.minX,
                        box.maxY,
                        box.maxZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.minX,
                        box.maxY,
                        box.minZ
                ).color(r, g, b, a).endVertex();

            } else {

                worldrenderer.pos(
                        box.maxX,
                        box.minY,
                        box.minZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.maxX,
                        box.maxY,
                        box.minZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.maxX,
                        box.maxY,
                        box.maxZ
                ).color(r, g, b, a).endVertex();

                worldrenderer.pos(
                        box.maxX,
                        box.minY,
                        box.maxZ
                ).color(r, g, b, a).endVertex();
            }

            tessellator.draw();

        } finally {

            GlStateManager.color(
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F
            );

            GlStateManager.depthMask(true);

            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();

            GlStateManager.popMatrix();
        }
    }


    // =========================================================
    // AIM INDICATOR
    // =========================================================

    private void drawAimIndicator(
            int x,
            int y,
            boolean hit) {

        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        float r = hit ? 1.0F : 0.0F;
        float g = hit ? 0.0F : 1.0F;

        drawCustomCrosshair(
                x - 7,
                y - 7,
                r,
                g,
                0
        );

        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }


    // =========================================================
    // SCREEN POSITION
    // =========================================================

    private Pos2 getScreenPosition(
            double x,
            double y,
            double z,
            ScaledResolution res) {

        if (mc.getRenderViewEntity() == null) {
            return new Pos2(0, 0);
        }

        Vec3 eyes =
                mc.getRenderViewEntity()
                        .getPositionEyes(partialTicks);

        viewPort.put(
                0,
                0
        ).put(
                1,
                0
        ).put(
                2,
                res.getScaledWidth()
        ).put(
                3,
                res.getScaledHeight()
        );

        GLU.gluProject(
                (float)
                        (x - eyes.xCoord),

                (float)
                        (y - eyes.yCoord),

                (float)
                        (z - eyes.zCoord),

                modelBuffer,
                projectionBuffer,
                viewPort,
                win_pos
        );

        return new Pos2(
                (int) win_pos.get(0),
                res.getScaledHeight() -
                        (int) win_pos.get(1)
        );
    }


    // =========================================================
    // CROSSHAIR TEXTURE
    // =========================================================

    private void drawCustomCrosshair(
            int x,
            int y,
            float r,
            float g,
            float b) {

        mc.getTextureManager()
                .bindTexture(Gui.icons);

        GlStateManager.color(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );

        WorldRenderer wr =
                Tessellator.getInstance()
                        .getWorldRenderer();

        wr.begin(
                GL11.GL_QUADS,
                DefaultVertexFormats.POSITION_TEX_COLOR
        );

        float uScale =
                0.00390625f;

        float vScale =
                0.00390625f;

        wr.pos(
                x,
                y + 16,
                0
        )
        .tex(
                0 * uScale,
                16 * vScale
        )
        .color(
                r,
                g,
                b,
                1f
        )
        .endVertex();

        wr.pos(
                x + 16,
                y + 16,
                0
        )
        .tex(
                16 * uScale,
                16 * vScale
        )
        .color(
                r,
                g,
                b,
                1f
        )
        .endVertex();

        wr.pos(
                x + 16,
                y,
                0
        )
        .tex(
                16 * uScale,
                0 * vScale
        )
        .color(
                r,
                g,
                b,
                1f
        )
        .endVertex();

        wr.pos(
                x,
                y,
                0
        )
        .tex(
                0 * uScale,
                0 * vScale
        )
        .color(
                r,
                g,
                b,
                1f
        )
        .endVertex();

        Tessellator.getInstance().draw();
    }


    // =========================================================
    // CANDIDATE
    // =========================================================

    private static class CloseEntity
            implements Comparable<CloseEntity> {

        final Entity entity;

        final double distance;

        final Vec3 lookDirection;

        CloseEntity(
                Entity entity,
                double distance,
                Vec3 lookDirection) {

            this.entity = entity;

            this.distance = distance;

            this.lookDirection =
                    lookDirection;
        }

        @Override
        public int compareTo(
                CloseEntity other) {

            return Double.compare(
                    this.distance,
                    other.distance
            );
        }
    }
}
