package net.vreeken.quickmsg;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.style.LineBackgroundSpan;

public class line_background_span implements LineBackgroundSpan {
		private final int color;
		private final int bordercolor;
		private final Boolean op;
		
        public line_background_span(int color, int border_color, Boolean oposite) {
            this.color = color;
            this.bordercolor = border_color;
            this.op = oposite;
        }

        @Override
        public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline,
                int bottom, CharSequence text, int start, int end, int lnum) {
            final int paintColor = p.getColor();

            p.setColor(color);
            c.drawRect(new Rect(left, top, right, bottom), p);
            p.setColor(bordercolor);
            if (op)
            	c.drawLine(right - 1, top, right - 1, bottom, p);
            else
            	c.drawLine(left, top, left, bottom, p);
            c.drawLine(left, bottom + 1, right, bottom + 1, p);
            p.setColor(paintColor);
        }
}
