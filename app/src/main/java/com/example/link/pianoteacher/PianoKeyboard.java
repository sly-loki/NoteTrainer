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
    public static final int FULL_OCTAVE_COUNT = 7;

    abstract class Octave {
        protected int startNote;
        Octave(int startNote) {
            this.startNote = startNote;
        }

        abstract public void draw(Canvas canvas, float x);

        protected void drawBlackNote(Canvas canvas, int state, float left, float bottom, float width, float height) {
            Paint paint;
            if (state == NOTE_RELEASED)
                paint = p;
            else
                paint = activePaint;
            canvas.drawRect(left, bottom, left + width, bottom + height, paint);
            canvas.drawRect(left, bottom, left + width, bottom + height, strokePaint);
        }

        protected void drawWhiteNote(Canvas canvas, int state, float left, float bottom, float width, float height) {
            Paint paint;
            if (state == NOTE_RELEASED)
                paint = strokePaint;
            else
                paint = activePaint;
            canvas.drawRect(left, bottom, left+width, bottom+height, paint);
            canvas.drawRect(left, bottom, left + width, bottom + height, strokePaint);
        }
    }

    class FullOctave extends Octave {
        FullOctave(int startNote) {
            super(startNote);
        }

        @Override
        public void draw(Canvas canvas, float x) {
            float note_width = getWidth() / MusicBox.WHITE_NOTE_COUNT;
            float black_note_width = note_width * 0.6f;
            float black_note_height = getHeight() * 0.7f;

            float currentNoteLeft = x;
            for (int i = 0; i < MusicBox.NOTES_IN_OCTAVE; i++) {
                if (!MusicBox.RELATIVE_BLACK_NOTE_INDEXES.contains(i)) {
                    drawWhiteNote(canvas, notes[startNote + i], currentNoteLeft, 0, note_width, getHeight());
                    currentNoteLeft += note_width;
                }
            }

            currentNoteLeft = x;
            for (int i = 0; i < MusicBox.NOTES_IN_OCTAVE; i++) {
                if (MusicBox.RELATIVE_BLACK_NOTE_INDEXES.contains(i)) {
                    drawBlackNote(canvas, notes[startNote + i], currentNoteLeft + note_width * 0.6f, 0,
                            black_note_width, black_note_height);
                    currentNoteLeft += note_width;
                } else if (i == 4) {
                    currentNoteLeft += note_width;
                }
            }
        }
    }

    class SubContrOctave extends Octave {
        SubContrOctave() {
            super(0);
        }

        @Override
        public void draw(Canvas canvas, float x) {
            float note_width = getWidth() / MusicBox.WHITE_NOTE_COUNT;
            float black_note_width = note_width * 0.6f;
            float black_note_height = getHeight() * 0.7f;

            drawWhiteNote(canvas, notes[0], x, 0, note_width, getHeight());
            drawWhiteNote(canvas, notes[2], x+note_width, 0, note_width, getHeight());
            drawBlackNote(canvas, notes[1], x + note_width * 0.6f, 0,
                    black_note_width, black_note_height);
        }
    }

    class FifthOctave extends Octave {
        FifthOctave(int startNote) {
            super(startNote);
        }

        @Override
        public void draw(Canvas canvas, float x) {
            float note_width = getWidth() / MusicBox.WHITE_NOTE_COUNT;
            drawWhiteNote(canvas, notes[MusicBox.NOTE_COUNT - 1], x, 0, note_width, getHeight());
        }
    }

    SubContrOctave subContrOctave = new SubContrOctave();
    FullOctave[] fullOctaves = new FullOctave[8];
    FifthOctave fifthOctave = new FifthOctave(87);

    public PianoKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        p.setStrokeWidth(5);
        strokePaint.setStrokeWidth(5);
        strokePaint.setStyle(Paint.Style.STROKE);
        activePaint.setColor(Color.MAGENTA);

        for (int i = 0; i < FULL_OCTAVE_COUNT; i++) {
            fullOctaves[i] = new FullOctave(3 + i*MusicBox.NOTES_IN_OCTAVE);
        }

        for(int i = 0; i < MusicBox.NOTE_COUNT; i++) {
            notes[i] = NOTE_RELEASED;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float noteWidth = (getWidth()) / MusicBox.WHITE_NOTE_COUNT;
        subContrOctave.draw(canvas, 0);
        for (int i = 0; i < FULL_OCTAVE_COUNT; i++) {
            fullOctaves[i].draw(canvas, noteWidth*2 + i * MusicBox.WHITE_NOTES_IN_OCTAVE * noteWidth);
        }
        fifthOctave.draw(canvas, noteWidth*2 + FULL_OCTAVE_COUNT * MusicBox.WHITE_NOTES_IN_OCTAVE * noteWidth);
    }

    public void setNoteState(int note, int state) {
        if (notes[note] != state) {
            notes[note] = state;
            invalidate();
        }
    }
}
