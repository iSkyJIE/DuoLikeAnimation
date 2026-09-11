package com.iskyjie.duolikeanimation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

    public FoldView(Context context) {
        super(context);
        setBackgroundColor(Color.rgb(242, 242, 247));
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
                boolean ok = sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
        float left = 24f * d;
        float right = w - 24f * d;

        paint.setColor(Color.rgb(35, 35, 40));
        paint.setTextSize(30f * d);
        paint.setFakeBoldText(true);
        canvas.drawText("DuoLikeAnimation", left, 90f * d, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(16f * d);
        paint.setColor(Color.rgb(90, 90, 100));
        canvas.drawText("SENSOR READ TEST", left, 125f * d, paint);

        paint.setColor(Color.WHITE);
        RectF card = new RectF(left, 165f * d, right, Math.min(h - 80f * d, 430f * d));
        canvas.drawRoundRect(card, 24f * d, 24f * d, paint);

        paint.setColor(Color.rgb(0, 122, 255));
        paint.setTextSize(22f * d);
        paint.setFakeBoldText(true);
        canvas.drawText("Canvas OK", card.left + 22f * d, card.top + 48f * d, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(15f * d);
        paint.setColor(Color.rgb(70, 70, 80));
        canvas.drawText(sensorStatus, card.left + 22f * d, card.top + 88f * d, paint);
        canvas.drawText("Sensor events: " + sensorEvents, card.left + 22f * d, card.top + 118f * d, paint);
        canvas.drawText("No fold transform", card.left + 22f * d, card.top + 148f * d, paint);
        canvas.drawText("No shader / AGSL", card.left + 22f * d, card.top + 178f * d, paint);

        paint.setColor(sensorEvents > 0 ? Color.rgb(52, 199, 89) : Color.rgb(255, 149, 0));
        canvas.drawCircle(card.right - 48f * d, card.top + 48f * d, 16f * d, paint);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorEvents++;
        if (sensorEvents == 1) sensorStatus = "Sensor events received";
        if ((sensorEvents % 5) == 0 || sensorEvents < 5) invalidate();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
