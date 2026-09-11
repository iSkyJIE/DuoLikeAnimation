package com.iskyjie.duolikeanimation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;
import android.view.View;

public final class FoldView extends View implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor rotationSensor;
    private final Paint drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint uiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap uiBitmap;
    private Canvas uiCanvas;
    private float angle = 0f;
    private boolean hasReference = false;
    private final float[] referenceQ = new float[4];
    private final float[] currentQ = new float[4];
    private boolean manualMode = false;
    private float manualAngle = 0f;

    private static final float MAX_TILT = (float)Math.toRadians(55.0);

    public FoldView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor s = null;
        if (sensorManager != null) {
            s = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
            if (s == null) s = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }
        rotationSensor = s;
        setBackgroundColor(Color.BLACK);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            if (sensorManager != null && rotationSensor != null) {
                sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
            }
        } catch (Throwable ignored) { }
    }

    @Override protected void onDetachedFromWindow() {
        try { if (sensorManager != null) sensorManager.unregisterListener(this); }
        catch (Throwable ignored) { }
        super.onDetachedFromWindow();
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w <= 0 || h <= 0) return;
        try {
            uiBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            uiCanvas = new Canvas(uiBitmap);
            renderDemoUi(w, h);
        } catch (Throwable t) {
            uiBitmap = null;
            uiCanvas = null;
        }
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (uiBitmap == null) {
            canvas.drawColor(Color.rgb(242, 242, 247));
            return;
        }
        float a = manualMode ? manualAngle : angle;
        drawSafeFold(canvas, a);
        drawOverlay(canvas, a);
    }

    private void drawSafeFold(Canvas canvas, float a) {
        float abs = Math.min(Math.abs(a), MAX_TILT);
        float squeeze = 1f - 0.18f * (abs / MAX_TILT);
        int save = canvas.save();
        if (a >= 0f) canvas.scale(squeeze, 1f, getWidth(), getHeight() * 0.5f);
        else canvas.scale(squeeze, 1f, 0f, getHeight() * 0.5f);
        canvas.drawBitmap(uiBitmap, 0f, 0f, drawPaint);
        canvas.restoreToCount(save);

        if (abs > 0.01f) {
            float strength = Math.min(0.42f, abs / MAX_TILT * 0.42f);
            int alpha = Math.round(255f * strength);
            int width = Math.max(1, getWidth() / 5);
            if (a >= 0f) {
                uiPaint.setShader(new LinearGradient(getWidth()-width, 0, getWidth(), 0,
                        Color.TRANSPARENT, Color.argb(alpha,0,0,0), Shader.TileMode.CLAMP));
                canvas.drawRect(getWidth()-width, 0, getWidth(), getHeight(), uiPaint);
            } else {
                uiPaint.setShader(new LinearGradient(0, 0, width, 0,
                        Color.argb(alpha,0,0,0), Color.TRANSPARENT, Shader.TileMode.CLAMP));
                canvas.drawRect(0, 0, width, getHeight(), uiPaint);
            }
            uiPaint.setShader(null);
        }
    }

    private void renderDemoUi(int w, int h) {
        Canvas c = uiCanvas;
        c.drawColor(Color.rgb(242, 242, 247));
        float d = getResources().getDisplayMetrics().density;
        float x = 20*d;
        float right = w - 20*d;
        float y = 54*d;

        uiPaint.setTypeface(android.graphics.Typeface.DEFAULT);
        uiPaint.setColor(Color.rgb(110,110,118));
        uiPaint.setTextSize(14*d);
        c.drawText("Wednesday, 10 Sep", x, y, uiPaint);
        y += 42*d;
        uiPaint.setColor(Color.BLACK);
        uiPaint.setTextSize(36*d);
        uiPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        c.drawText("Today", x, y, uiPaint);

        uiPaint.setShader(new LinearGradient(right-44*d, y-40*d, right, y, 0xffff2d75, 0xffff9500, Shader.TileMode.CLAMP));
        c.drawCircle(right-22*d, y-18*d, 22*d, uiPaint);
        uiPaint.setShader(null);
        uiPaint.setColor(Color.WHITE);
        uiPaint.setTextSize(14*d);
        c.drawText("ES", right-32*d, y-13*d, uiPaint);

        y += 44*d;
        String[] chips = {"All","Health","Work","Reading"};
        float cx=x;
        uiPaint.setTextSize(14*d);
        for (String chip: chips) {
            float cw = uiPaint.measureText(chip)+28*d;
            uiPaint.setColor(chip.equals("All") ? 0xff0a84ff : 0xffffffff);
            c.drawRoundRect(new RectF(cx,y,cx+cw,y+34*d),17*d,17*d,uiPaint);
            uiPaint.setColor(chip.equals("All") ? Color.WHITE : Color.DKGRAY);
            c.drawText(chip,cx+14*d,y+22*d,uiPaint);
            cx += cw+8*d;
        }

        y += 54*d;
        RectF hero = new RectF(x,y,right,y+205*d);
        uiPaint.setShader(new LinearGradient(hero.left,hero.top,hero.right,hero.bottom,new int[]{0xff0a84ff,0xff6e5cff,0xffff2d75},null,Shader.TileMode.CLAMP));
        c.drawRoundRect(hero,20*d,20*d,uiPaint);
        uiPaint.setShader(null);
        uiPaint.setColor(Color.WHITE);
        uiPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        uiPaint.setTextSize(18*d);
        c.drawText("Safe fold demo",x+18*d,y+35*d,uiPaint);
        uiPaint.setTypeface(android.graphics.Typeface.DEFAULT);
        uiPaint.setTextSize(13*d);
        c.drawText("Tilt the phone around its vertical axis.",x+18*d,y+66*d,uiPaint);
        c.drawText("This build uses standard Android Canvas",x+18*d,y+86*d,uiPaint);
        c.drawText("and contains no RuntimeShader / AGSL.",x+18*d,y+106*d,uiPaint);
        float barX=x+18*d;
        for(int i=0;i<12;i++){
            float bh=(18+((i*37)%46))*d;
            c.drawRoundRect(new RectF(barX,y+178*d-bh,barX+12*d,y+178*d),3*d,3*d,uiPaint);
            barX += 18*d;
        }

        y += 222*d;
        float gap=12*d;
        float tileW=(right-x-gap)/2;
        String[][] stats={{"Steps","8,412"},{"Sleep","7h 20m"},{"Focus","3h 05m"},{"Water","1.8 L"}};
        int[] colors={0xff34c759,0xff5856d6,0xffff9500,0xff32ade6};
        for(int i=0;i<4;i++){
            int row=i/2,col=i%2;
            float tx=x+col*(tileW+gap), ty=y+row*(94*d+gap);
            uiPaint.setColor(Color.WHITE);
            c.drawRoundRect(new RectF(tx,ty,tx+tileW,ty+94*d),16*d,16*d,uiPaint);
            uiPaint.setColor(colors[i]); uiPaint.setTextSize(14*d); c.drawText(stats[i][0],tx+14*d,ty+27*d,uiPaint);
            uiPaint.setColor(Color.BLACK); uiPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD); uiPaint.setTextSize(25*d); c.drawText(stats[i][1],tx+14*d,ty+67*d,uiPaint);
            uiPaint.setTypeface(android.graphics.Typeface.DEFAULT);
        }

        y += 215*d;
        uiPaint.setColor(Color.BLACK); uiPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD); uiPaint.setTextSize(20*d); c.drawText("Recent",x,y,uiPaint);
        y += 16*d;
        String[] rows={"Morning run   ·   5.2 km · 27 min","Design review   ·   10:30 · Room 4B","Flight to Lisbon   ·   Fri 18:45 · Gate 22","Read 20 pages   ·   The Left Hand of Darkness"};
        for(int i=0;i<rows.length;i++){
            float ry=y+i*54*d;
            uiPaint.setColor(Color.WHITE); c.drawRoundRect(new RectF(x,ry,right,ry+50*d),12*d,12*d,uiPaint);
            uiPaint.setColor(Color.DKGRAY); uiPaint.setTypeface(android.graphics.Typeface.DEFAULT); uiPaint.setTextSize(13*d); c.drawText(rows[i],x+14*d,ry+30*d,uiPaint);
        }
    }

    private void drawOverlay(Canvas canvas, float a) {
        float d = getResources().getDisplayMetrics().density;
        uiPaint.setShader(null);
        uiPaint.setColor(0xaa000000);
        canvas.drawRoundRect(new RectF(12*d,getHeight()-68*d,getWidth()-12*d,getHeight()-14*d),16*d,16*d,uiPaint);
        uiPaint.setColor(Color.WHITE);
        uiPaint.setTextSize(13*d);
        uiPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        String mode = manualMode ? "MANUAL" : "MOTION";
        String s = mode + "   tilt " + Math.round(Math.toDegrees(a)) + " deg   -   SAFE CANVAS";
        canvas.drawText(s,26*d,getHeight()-36*d,uiPaint);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        float d = getResources().getDisplayMetrics().density;
        if (e.getY() < getHeight()-90*d) return true;
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            manualMode = true;
            updateManual(e.getX());
            return true;
        }
        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            updateManual(e.getX());
            return true;
        }
        if (e.getAction() == MotionEvent.ACTION_UP) {
            if (Math.abs(e.getX()-getWidth()/2f) < 80*d) {
                hasReference = false;
                manualMode = false;
            }
            invalidate();
            return true;
        }
        return true;
    }

    private void updateManual(float x) {
        float t = (x / Math.max(1f,getWidth())) * 2f - 1f;
        manualAngle = Math.max(-MAX_TILT, Math.min(MAX_TILT, t * MAX_TILT));
        invalidate();
    }

    @Override public void onSensorChanged(SensorEvent event) {
        try {
            SensorManager.getQuaternionFromVector(currentQ, event.values);
            normalize(currentQ);
            if (!hasReference) {
                System.arraycopy(currentQ,0,referenceQ,0,4);
                hasReference = true;
                angle = 0f;
                invalidate();
                return;
            }
            float[] rel = multiply(conjugate(referenceQ), currentQ);
            float w=rel[0], x=rel[1], y=rel[2], z=rel[3];
            float sinY = 2f*(w*y - z*x);
            sinY = Math.max(-1f, Math.min(1f, sinY));
            float measured = (float)Math.asin(sinY);
            measured = Math.max(-MAX_TILT, Math.min(MAX_TILT, measured));
            angle = angle*0.82f + measured*0.18f;
            if (!manualMode) invalidate();
        } catch (Throwable ignored) { }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private static float[] conjugate(float[] q) { return new float[]{q[0],-q[1],-q[2],-q[3]}; }
    private static float[] multiply(float[] a,float[] b){
        return new float[]{
            a[0]*b[0]-a[1]*b[1]-a[2]*b[2]-a[3]*b[3],
            a[0]*b[1]+a[1]*b[0]+a[2]*b[3]-a[3]*b[2],
            a[0]*b[2]-a[1]*b[3]+a[2]*b[0]+a[3]*b[1],
            a[0]*b[3]+a[1]*b[2]-a[2]*b[1]+a[3]*b[0]
        };
    }
    private static void normalize(float[] q){
        float n=(float)Math.sqrt(q[0]*q[0]+q[1]*q[1]+q[2]*q[2]+q[3]*q[3]);
        if(n>0){ for(int i=0;i<4;i++) q[i]/=n; }
    }
}
