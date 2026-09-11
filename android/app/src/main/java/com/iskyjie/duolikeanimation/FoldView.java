package com.iskyjie.duolikeanimation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public final class FoldView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public FoldView(Context context) {
        super(context);
        setBackgroundColor(Color.rgb(242, 242, 247));
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
        canvas.drawText("STATIC CUSTOM VIEW TEST", left, 125f * d, paint);

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
        canvas.drawText("No sensor", card.left + 22f * d, card.top + 88f * d, paint);
        canvas.drawText("No shader / AGSL", card.left + 22f * d, card.top + 118f * d, paint);
        canvas.drawText("No animation", card.left + 22f * d, card.top + 148f * d, paint);
        canvas.drawText("No fullscreen APIs", card.left + 22f * d, card.top + 178f * d, paint);

        paint.setColor(Color.rgb(52, 199, 89));
        canvas.drawCircle(card.right - 48f * d, card.top + 48f * d, 16f * d, paint);
    }
}
