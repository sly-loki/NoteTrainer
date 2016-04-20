package com.example.link.pianoteacher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

class TimeLine extends View {


    private Paint mainPaint = new Paint();
    private Paint bluePaint = new Paint();
    private Paint cyanPaint = new Paint();

    private float startTime;
    private float timeLenght;

    private class Event {
        public int note = 0;
        public float startTime = 0;
        public float endTime = -1;
    }
    private ArrayList<Event>[] eventLists = new ArrayList[MusicBox.NOTE_COUNT];
    private Event[] events = new Event[MusicBox.NOTE_COUNT];

    public TimeLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        startTime = 0.0f;
        timeLenght = 5.0f;
        bluePaint.setColor(Color.BLUE);
        cyanPaint.setColor(Color.CYAN);
        for (int i = 0; i < MusicBox.NOTE_COUNT; i++) {
            eventLists[i] = new ArrayList<>();
        }
    }

    protected void onDraw(Canvas canvas) {
        float endTime = startTime + timeLenght;
        float noteWidth = getWidth() / MusicBox.WHITE_NOTE_COUNT;
        float blackNoteWidth = noteWidth * 0.6f;
        float height = getHeight();

        for (int i = 1; i < MusicBox.WHITE_NOTE_COUNT; i++) {
            canvas.drawLine(noteWidth*i, 0, noteWidth*i, getHeight(), mainPaint);
        }

        for (int i = 0; i < MusicBox.NOTE_COUNT; i++) {
            if (events[i] == null)
                continue;
            if (events[i].endTime > 0 && events[i].endTime < startTime - timeLenght) {
                events[i] = null;
                continue;
            }

            float x = MusicBox.getWhiteIndex(i) * noteWidth;
            float start_y = 0;
            if (events[i].startTime < endTime)
                start_y = height - (height / timeLenght * (startTime - events[i].startTime));

            float end_y = height;
            if (events[i].endTime > 0)
                end_y = height - (height / timeLenght * (startTime - events[i].endTime));
            if (MusicBox.keyIsWhite(i)) {
                canvas.drawRect(x, start_y, x + noteWidth, end_y, bluePaint);
            } else {
                canvas.drawRect(x + noteWidth - blackNoteWidth * 0.5f, start_y, x + noteWidth + blackNoteWidth * 0.5f, end_y, cyanPaint);
            }
        }

        final int LINE_DIST = 3;
        int intTime = (int)startTime;
        if (intTime % LINE_DIST != 0)
            intTime -= intTime % LINE_DIST;

        while (intTime < startTime + timeLenght) {
            float y = height / timeLenght * (intTime - startTime);
            canvas.drawLine(0, y, getWidth(), y, mainPaint);
            intTime += LINE_DIST;
        }
    }

    public void addEvent(int note, int time, int status) {

    }

    public static final int NOTE_PRESSED = 1;
    public static final int NOTE_RELEASED = 0;

    public void setKeyState(int key, int state) {
        if (state == NOTE_PRESSED) {
            events[key] = new Event();
            events[key].startTime = startTime;
        }
        else if (events[key] != null) {
            events[key].endTime = startTime;
        }
        invalidate();
    }

    public void timePassed(float time) {
        startTime += time;
        invalidate();
    }

    public void synchronizeTime(float time) {
        startTime = time;
        invalidate();
    }

}
