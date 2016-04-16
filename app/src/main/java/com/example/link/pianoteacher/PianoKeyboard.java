package com.example.link.pianoteacher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

class PianoKeyboard extends View {

    private Paint p = new Paint();
    private Paint strokePaint = new Paint();
    private Paint activePaint = new Paint();

    private int notes[] = new int[MusicBox.NOTE_COUNT];

    public static final int NOTE_PRESSED = 1;
    public static final int NOTE_RELEASED = 0;

    public PianoKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        p.setStrokeWidth(5);
        strokePaint.setStrokeWidth(5);
        strokePaint.setStyle(Paint.Style.STROKE);
        activePaint.setColor(Color.MAGENTA);

        for(int i = 0; i < MusicBox.NOTE_COUNT; i++) {
            notes[i] = NOTE_RELEASED;
        }
    }

    private void drawBlackNote(Canvas canvas, int state, float left, float bottom, float width, float height) {
        Paint paint;
        if (state == NOTE_RELEASED)
            paint = p;
        else
            paint = activePaint;
        canvas.drawRect(left, bottom, left + width, bottom + height, paint);
        canvas.drawRect(left, bottom, left + width, bottom + height, strokePaint);
    }

    private void drawWhiteNote(Canvas canvas, int state, float left, float bottom, float width, float height) {
        Paint paint;
        if (state == NOTE_RELEASED)
            paint = strokePaint;
        else
            paint = activePaint;
        canvas.drawRect(left, bottom, left+width, bottom+height, paint);
        canvas.drawRect(left, bottom, left + width, bottom + height, strokePaint);
    }

    private int calcNoteState(int octave, int note, boolean isBlack)
    {
        int start = 3 + 12 * octave;
        int[] helperWhite = {0,2,4,5,7,9,11};
        int[] helperBlack = {1,3,0,6,8,10,0};

        if (isBlack)
            start += helperBlack[note];
        else
            start += helperWhite[note];
        if (start >= MusicBox.NOTE_COUNT)
            return NOTE_RELEASED;
        return notes[start];
    }

    private void drawOctave(Canvas canvas, int num, float left, float bottom, float width, float height) {
        float note_width = width / 7.0f;
        float black_note_width = note_width * 0.6f;
        float black_note_height = height * 0.7f;

        for (int i = 0; i < 7; i++) {
            float currentNoteLeft = left + note_width * i;
            drawWhiteNote(canvas, calcNoteState(num, i, false), currentNoteLeft, bottom, note_width, height);
        }

        for (int i = 0; i < 7; i++) {
            float currentNoteLeft = left + note_width * i;
            if (i != 2 && i != 6)
                drawBlackNote(canvas, calcNoteState(num, i, true), currentNoteLeft + note_width * 0.6f, bottom,
                        black_note_width, black_note_height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float octaveWidth = (getWidth()-20) / 8;
        for (int i = 0; i < 8; i++) {
            drawOctave(canvas, i, 10 + octaveWidth * i, 10, octaveWidth, getHeight() - 10);
        }
    }

    public void setNoteState(int note, int state) {
        if (notes[note] != state) {
            notes[note] = state;
            invalidate();
        }
    }
}
