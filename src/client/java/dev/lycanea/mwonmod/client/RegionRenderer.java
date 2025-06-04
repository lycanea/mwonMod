package dev.lycanea.mwonmod.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;

import java.util.OptionalDouble;
import java.util.function.Function;

import static net.minecraft.client.render.RenderPhase.*;

public class RegionRenderer {
    private static final Function<Double, RenderLayer.MultiPhase> asd;
    static {
        asd = Util.memoize((lineWidth) -> RenderLayer.of("debug_line_strip", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES, 1536, RenderLayer.MultiPhaseParameters.builder().program(POSITION_COLOR_PROGRAM).lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(lineWidth))).transparency(NO_TRANSPARENCY).cull(DISABLE_CULLING).depthTest(ALWAYS_DEPTH_TEST).build(false)));
    }
    public static void init() {
        WorldRenderEvents.LAST.register((context) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null || !Config.HANDLER.instance().debugMode || !MwonmodClient.onMelonKing() || !MinecraftClient.getInstance().getDebugHud().shouldShowDebugHud()) return;

            Vec3d cameraPos = context.camera().getPos();

            Region currentRegion = MwonmodClient.getCurrentRegion();
            if (currentRegion == null) return;

            Box box = new Box(new Vec3d(currentRegion.min.getX(), currentRegion.min.getY(), currentRegion.min.getZ()),
                              new Vec3d(currentRegion.max.getX()+1, currentRegion.max.getY()+1, currentRegion.max.getZ()+1)).expand(0.01); // Small expand to prevent z-fighting

            box = box.offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            RenderLayer renderLayer = asd.apply(10.0);
            VertexConsumer cons = context.consumers().getBuffer(renderLayer);
            drawBoxOutline(context.matrixStack(), cons, box, 1f, 0f, 0f, 1f);
        });
    }

    private static void drawBoxOutline(MatrixStack matrices, VertexConsumer consumer, Box box, float r, float g, float b, float a) {
        MatrixStack.Entry entry = matrices.peek();
        Vec3d[] corners = {
                new Vec3d(box.minX, box.minY, box.minZ),
                new Vec3d(box.maxX, box.minY, box.minZ),
                new Vec3d(box.maxX, box.maxY, box.minZ),
                new Vec3d(box.minX, box.maxY, box.minZ),
                new Vec3d(box.minX, box.minY, box.maxZ),
                new Vec3d(box.maxX, box.minY, box.maxZ),
                new Vec3d(box.maxX, box.maxY, box.maxZ),
                new Vec3d(box.minX, box.maxY, box.maxZ)
        };

        int[][] edges = {
                {0,1}, {1,2}, {2,3}, {3,0},
                {4,5}, {5,6}, {6,7}, {7,4},
                {0,4}, {1,5}, {2,6}, {3,7}
        };

        for (int[] edge : edges) {
            Vec3d start = corners[edge[0]];
            Vec3d end = corners[edge[1]];
            consumer.vertex(entry.getPositionMatrix(), (float) start.getX(), (float) start.getY(), (float) start.getZ()).color(r, g, b, a).normal(0,0,0);
            consumer.vertex(entry.getPositionMatrix(), (float) end.getX(), (float) end.getY(), (float) end.getZ()).color(r, g, b, a).normal(0,0,0);
        }
    }
}
