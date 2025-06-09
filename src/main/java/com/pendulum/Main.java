package com.pendulum;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public class Main {
    private Window window;

    private Shader pendulumShader;
    private LineStrip lineStrip;
    private Pendulum pendulum;

    private Shader juliaShader;
    private Quad juliaSet;

    private Matrix4f projection;

    private static final int TRAIL_LENGTH = 200;
    private static final int PENDULUM_LENGTH = 20;
    private double[][][] trailBuffer;
    private int trailIndex = 0;

    public Main() {
        window = new Window("n-pendulum");
        window.create();

        // Enable blending for transparency
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        pendulumShader = new Shader("pendulum");
        pendulumShader.compile();

        juliaShader = new Shader("julia");
        juliaShader.compile();

        float aspectRatio = (float) window.getWidth() / (float) window.getHeight();

        // Projection matrix
        projection = new Matrix4f().ortho2D(
            (float) -PENDULUM_LENGTH * aspectRatio, (float) PENDULUM_LENGTH * aspectRatio,
            (float) -PENDULUM_LENGTH, (float) PENDULUM_LENGTH
        );

        // Initialize Pendulum
        pendulum = new Pendulum(PENDULUM_LENGTH, 0.0001, Math.PI / 2);

        int pointsCount = pendulum.getNumPendulums() + 1;
        lineStrip = new LineStrip(pointsCount);

        // Initialize trail buffer
        trailBuffer = new double[TRAIL_LENGTH][pointsCount][2];

        juliaSet = new Quad();

        loop();

        pendulumShader.delete();
        juliaShader.delete();
        juliaSet.delete();
        lineStrip.cleanup();
        window.destroy();
    }

    private void loop() {
        while (!window.shouldClose()) {
            window.clear();

            // Update simulation multiple times for smoothness
            for (int i = 0; i < 100; i++) {
                pendulum.update();
            }

            // Get current pendulum endpoint
            double[][] coords = pendulum.getEndPointCoordinates();
            int pointsCount = coords.length;

            // Store only the last point coords in the trail buffer
            trailBuffer[trailIndex][0][0] = coords[pointsCount - 1][0];
            trailBuffer[trailIndex][0][1] = coords[pointsCount - 1][1];
            trailIndex = (trailIndex + 1) % TRAIL_LENGTH;

            // --- Julia Set Rendering --- //
            juliaShader.bind();

            // Compute final pendulum point and use as complex number c
            double real = coords[pointsCount - 1][0] / (double) (PENDULUM_LENGTH);
            double imag = coords[pointsCount - 1][1] / (double) (PENDULUM_LENGTH);
            juliaShader.setUniform2f("u_c", (float) real, (float) imag);

            // Send screen resolution
            juliaShader.setUniform2f("u_resolution", window.getWidth(), window.getHeight());

            // Render the full-screen quad
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            juliaSet.render();
            juliaShader.unbind();

            // --- Pendulum Rendering --- //
            pendulumShader.bind();

            pendulumShader.setUniformMatrix4f("u_projection", projection);

            // Render the main pendulum line with current points
            GL11.glLineWidth(2.0f);
            pendulumShader.setUniform3f("u_color", new Vector3f(1.0f, 1.0f, 1.0f));
            pendulumShader.setUniform1f("u_alpha", 1.0f);
            lineStrip.updatePoints(coords);
            lineStrip.render(pointsCount);

            // Render the trail for the last point only as a fading line strip
            pendulumShader.setUniform3f("u_color", new Vector3f(0.4f, 1.0f, 0.8f));

            for (int i = 0; i < TRAIL_LENGTH - 1; i++) {
                int idx1 = (trailIndex + i) % TRAIL_LENGTH;
                int idx2 = (trailIndex + i + 1) % TRAIL_LENGTH;

                double x1 = trailBuffer[idx1][0][0];
                double y1 = trailBuffer[idx1][0][1];
                double x2 = trailBuffer[idx2][0][0];
                double y2 = trailBuffer[idx2][0][1];

                // Skip segments where either point is at origin (0,0)
                if ((x1 == 0 && y1 == 0) || (x2 == 0 && y2 == 0)) {
                    continue;
                }

                double[][] segment = new double[2][2];
                segment[0][0] = x1;
                segment[0][1] = y1;
                segment[1][0] = x2;
                segment[1][1] = y2;

                float alpha = (float) i / TRAIL_LENGTH;
                pendulumShader.setUniform1f("u_alpha", alpha);

                lineStrip.updatePoints(segment);
                lineStrip.render(2);
            }

            pendulumShader.unbind();

            window.refresh();
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}
