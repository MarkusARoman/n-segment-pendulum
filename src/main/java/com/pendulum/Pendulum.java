package com.pendulum;

public class Pendulum 
{
    private final int NUM_PENDULUMS;       // Number of pendulums
    private final double DELTA_TIME;         // Time step (delta t)
    private final double GRAVITY = -10.0;    // Gravitational acceleration (m/s^2)

    private final double[] angles;            // Angles in radians
    private final double[] angularVelocities; // Angular velocities

    public Pendulum(int numPendulums, double deltaTime, double initialAngle)
    {
        NUM_PENDULUMS = numPendulums;
        DELTA_TIME = deltaTime;

        angles = new double[NUM_PENDULUMS];
        angularVelocities = new double[NUM_PENDULUMS];

        for (int i = 0; i < NUM_PENDULUMS; i++)
        {
            angles[i] = initialAngle;
            angularVelocities[i] = 0;
        }
    }

    public int getNumPendulums()
    {
        return NUM_PENDULUMS;
    }

    // --- PHYSICS SIMULATION --- //

    public void update()
    {
        leapfrogStep();
    }

    private void leapfrogStep()
    {
        double[] acc = computeAccelerations();
        double[] halfStepVelocity = new double[NUM_PENDULUMS];

        // Half-step velocity update
        for (int i = 0; i < NUM_PENDULUMS; i++)
        {
            halfStepVelocity[i] = angularVelocities[i] + acc[i] * DELTA_TIME / 2.0;
        }

        // Full-step angle update
        for (int i = 0; i < NUM_PENDULUMS; i++)
        {
            angles[i] = wrapAngle(angles[i] + halfStepVelocity[i] * DELTA_TIME);
        }

        // Recalculate acceleration
        double[] newAcc = computeAccelerations();

        // Complete velocity step
        for (int i = 0; i < NUM_PENDULUMS; i++)
        {
            angularVelocities[i] = halfStepVelocity[i] + newAcc[i] * DELTA_TIME / 2.0;
        }
    }

    private double[] computeAccelerations()
    {
        double[][] A = buildMatrixA();
        double[] b = buildVectorB();
        return solveLinearSystem(A, b);
    }

    // --- LINEAR SYSTEM SOLVER --- //

    private double[][] buildMatrixA()
    {
        double[][] A = new double[NUM_PENDULUMS][NUM_PENDULUMS];
        for (int i = 0; i < NUM_PENDULUMS; i++)
        {
            for (int j = 0; j < NUM_PENDULUMS; j++)
            {
                A[i][j] = (NUM_PENDULUMS - Math.max(i, j)) * Math.cos(angles[i] - angles[j]);
            }
        }
        return A;
    }

    private double[] buildVectorB()
    {
        double[] b = new double[NUM_PENDULUMS];
        for (int i = 0; i < NUM_PENDULUMS; i++)
        {
            double sum = 0;
            for (int j = 0; j < NUM_PENDULUMS; j++)
            {
                int weight = NUM_PENDULUMS - Math.max(i, j);
                double delta = angles[i] - angles[j];
                sum -= weight * Math.sin(delta) * Math.pow(angularVelocities[j], 2);
            }
            sum -= GRAVITY * (NUM_PENDULUMS - i) * Math.sin(angles[i]);
            b[i] = sum;
        }
        return b;
    }

    private double[] solveLinearSystem(double[][] A, double[] b)
    {
        int N = b.length;
        double[][] M = new double[N][N + 1];

        for (int i = 0; i < N; i++)
        {
            System.arraycopy(A[i], 0, M[i], 0, N);
            M[i][N] = b[i];
        }

        // Gaussian elimination
        for (int i = 0; i < N; i++)
        {
            int maxRow = i;
            for (int k = i + 1; k < N; k++)
            {
                if (Math.abs(M[k][i]) > Math.abs(M[maxRow][i]))
                    maxRow = k;
            }

            double[] temp = M[i];
            M[i] = M[maxRow];
            M[maxRow] = temp;

            if (Math.abs(M[i][i]) < 1e-10)
                continue;

            for (int k = i + 1; k < N; k++)
            {
                double factor = M[k][i] / M[i][i];
                for (int j = i; j < N + 1; j++)
                {
                    M[k][j] -= factor * M[i][j];
                }
            }
        }

        double[] x = new double[N];
        for (int i = N - 1; i >= 0; i--)
        {
            double sum = 0;
            for (int j = i + 1; j < N; j++)
                sum += M[i][j] * x[j];

            x[i] = Math.abs(M[i][i]) < 1e-10 ? 0 : (M[i][N] - sum) / M[i][i];
        }

        return x;
    }

    // --- UTILITIES --- //

    public double[][] getEndPointCoordinates()
    {
        double x = 0, y = 0;
        double[][] coords = new double[NUM_PENDULUMS+1][2];

        coords[0][0] = x;
        coords[0][1] = y;
        for (int i = 0; i < NUM_PENDULUMS; i++)
        {
            x += Math.sin(angles[i]);
            y += Math.cos(angles[i]);
            coords[i+1][0] = x;
            coords[i+1][1] = y;
        }
        return coords;
    }

    private double wrapAngle(double angle)
    {
        double twoPi = 2 * Math.PI;
        return ((angle + Math.PI) % twoPi + twoPi) % twoPi - Math.PI;
    }
}
