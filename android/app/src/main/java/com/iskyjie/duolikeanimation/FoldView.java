package com.iskyjie.duolikeanimation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;

public final class FoldView extends View implements SensorEventListener {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final SensorManager sensorManager;
    private final Sensor rotationSensor;
    private String sensorStatus = "Sensor initializing...";
    private int sensorEvents = 0;

    private final float[] referenceQ = new float[4];
    private final float[] currentQ = new float[4];
    private boolean hasReference = false;
    private float tiltRadians = 0f;
    private static final float MAX_TILT = (float) Math.toRadians(50.0);

    private final Matrix foldMatrix = new Matrix();
    private final float[] src = new float[8];
    private final float[] dst = new float[8];

    public FoldView(Context context) {
        super(context);
        setBackgroundColor(Color.BLACK);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor s = null;
        if (sensorManager != null) {
            s = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
            if (s == null) s = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }
        rotationSensor = s;
        sensorStatus = rotationSensor == null ? "Rotation sensor unavailable" : "Rotation sensor found";
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            if (sensorManager != null && rotationSensor != null) {
                boolean ok = sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
                sensorStatus = ok ? "Sensor registered" : "Sensor register failed";
                invalidate();
            }
        } catch (Throwable t) {
            sensorStatus = "Sensor exception: " + t.getClass().getSimpleName();
            invalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        try {
            if (sensorManager != null) sensorManager.unregisterListener(this);
        } catch (Throwable ignored) { }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float d = getResources().getDisplayMetrics().density;
        float w = getWidth();
        float h = getHeight();
        if (w <= 0f || h <= 0f) return;

        canvas.drawColor(Color.BLACK);

        buildFoldMatrix(w, h, tiltRadians);
        int save = canvas.save();
        canvas.concat(foldMatrix);
        drawDashboard(canvas, d, w, h);
        canvas.restoreToCount(save);

        drawDebugOverlay(canvas, d, w, h);
    }

    private void buildFoldMatrix(float w, float h, float angle) {
        float normalized = Math.min(1f, Math.abs(angle) / MAX_TILT);

        // Stable projective approximation of a vertical-axis fold.
        // The far edge remains fixed. The near edge moves inward and becomes shorter.
        float horizontalInset = w * (0.34f * normalized);
        float verticalInset = h * (0.07f * normalized);

        src[0] = 0f; src[1] = 0f;
        src[2] = w;  src[3] = 0f;
        src[4] = w;  src[5] = h;
        src[6] = 0f; src[7] = h;

        if (angle >= 0f) {
            // Right edge is the hinge / far edge.
            dst[0] = horizontalInset; dst[1] = verticalInset;
            dst[2] = w;               dst[3] = 0f;
            dst[4] = w;               dst[5] = h;
            dst[6] = horizontalInset; dst[7] = h - verticalInset;
        } else {
            // Left edge is the hinge / far edge.
            dst[0] = 0f;                    dst[1] = 0f;
            dst[2] = w - horizontalInset;   dst[3] = verticalInset;
            dst[4] = w - horizontalInset;   dst[5] = h - verticalInset;
            dst[6] = 0f;                    dst[7] = h;
        }

        foldMatrix.reset();
        boolean ok = foldMatrix.setPolyToPoly(src, 0, dst, 0, 4);
        if (!ok) foldMatrix.reset();
    }

    private void drawDashboard(Canvas canvas, float d, float w, float h) {
        canvas.drawColor(Color.rgb(242, 242, 247));
        float left = 24f * d;
        float right = w - 24f * d;

        paint.setColor(Color.rgb(35, 35, 40));
        paint.setTextSize(30f * d);
        paint.setFakeBoldText(true);
        canvas.drawText("DuoLikeAnimation", left, 90f * d, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(16f * d);
        paint.setColor(Color.rgb(90, 90, 100));
        canvas.drawText("PERSPECTIVE FOLD TEST", left, 125f * d, paint);

        paint.setColor(Color.WHITE);
        RectF card = new RectF(left, 165f * d, right, Math.min(h - 120f * d, 455f * d));
        canvas.drawRoundRect(card, 24f * d, 24f * d, paint);

        paint.setColor(Color.rgb(0, 122, 255));
        paint.setTextSize(22f * d);
        paint.setFakeBoldText(true);
        canvas.drawText("Perspective OK", card.left + 22f * d, card.top + 48f * d, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(15f * d);
        paint.setColor(Color.rgb(70, 70, 80));
        canvas.drawText("Far edge remains fixed", card.left + 22f * d, card.top + 88f * d, paint);
        canvas.drawText("Near edge folds inward", card.left + 22f * d, card.top + 118f * d, paint);
        canvas.drawText("Projective Canvas matrix", card.left + 22f * d, card.top + 148f * d, paint);
        canvas.drawText("No shader / blur / refraction", card.left + 22f * d, card.top + 178f * d, paint);

        paint.setColor(Color.rgb(52, 199, 89));
        canvas.drawCircle(card.right - 48f * d, card.top + 48f * d, 16f * d, paint);

        float y = card.bottom + 28f * d;
        float gap = 12f * d;
        float tileW = (right - left - gap) / 2f;
        String[] labels = {"Steps", "Focus", "Sleep", "Water"};
        String[] values = {"8,412", "3h 05m", "7h 20m", "1.8 L"};
        for (int i = 0; i < 4; i++) {
            int row = i / 2;
            int col = i % 2;
            float x = left + col * (tileW + gap);
            float ty = y + row * (88f * d + gap);
            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(new RectF(x, ty, x + tileW, ty + 88f * d), 16f * d, 16f * d, paint);
            paint.setColor(Color.rgb(90, 90, 100));
            paint.setTextSize(13f * d);
            canvas.drawText(labels[i], x + 14f * d, ty + 26f * d, paint);
            paint.setColor(Color.BLACK);
            paint.setTextSize(23f * d);
            paint.setFakeBoldText(true);
            canvas.drawText(values[i], x + 14f * d, ty + 62f * d, paint);
            paint.setFakeBoldText(false);
        }
    }

    private void drawDebugOverlay(Canvas canvas, float d, float w, float h) {
        paint.setColor(0xCC000000);
        RectF overlay = new RectF(12f * d, h - 76f * d, w - 12f * d, h - 14f * d);
        canvas.drawRoundRect(overlay, 16f * d, 16f * d, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(13f * d);
        paint.setFakeBoldText(true);
        canvas.drawText(sensorStatus, overlay.left + 14f * d, overlay.top + 23f * d, paint);
        paint.setFakeBoldText(false);
        String hinge = tiltRadians >= 0f ? "right hinge" : "left hinge";
        String line = "events " + sensorEvents + "   tilt " + Math.round(Math.toDegrees(tiltRadians)) + " deg   " + hinge;
        canvas.drawText(line, overlay.left + 14f * d, overlay.top + 46f * d, paint);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            SensorManager.getQuaternionFromVector(currentQ, event.values);
            normalize(currentQ);
            sensorEvents++;

            if (!hasReference) {
                System.arraycopy(currentQ, 0, referenceQ, 0, 4);
                hasReference = true;
                sensorStatus = "Sensor + perspective active";
                tiltRadians = 0f;
                invalidate();
                return;
            }

            float[] rel = multiply(conjugate(referenceQ), currentQ);
            float sinY = 2f * (rel[0] * rel[2] - rel[3] * rel[1]);
            sinY = Math.max(-1f, Math.min(1f, sinY));
            float measured = (float) Math.asin(sinY);
            measured = Math.max(-MAX_TILT, Math.min(MAX_TILT, measured));
            tiltRadians = tiltRadians * 0.82f + measured * 0.18f;
            invalidate();
        } catch (Throwable t) {
            sensorStatus = "Motion exception: " + t.getClass().getSimpleName();
            invalidate();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private static float[] conjugate(float[] q) {
        return new float[]{q[0], -q[1], -q[2], -q[3]};
    }

    private static float[] multiply(float[] a, float[] b) {
        return new float[]{
                a[0] * b[0] - a[1] * b[1] - a[2] * b[2] - a[3] * b[3],
                a[0] * b[1] + a[1] * b[0] + a[2] * b[3] - a[3] * b[2],
                a[0] * b[2] - a[1] * b[3] + a[2] * b[0] + a[3] * b[1],
                a[0] * b[3] + a[1] * b[2] - a[2] * b[1] + a[3] * b[0]
        };
    }

    private static void normalize(float[] q) {
        float n = (float) Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
        if (n > 0f) {
            for (int i = 0; i < 4; i++) q[i] /= n;
        }
    }
}
