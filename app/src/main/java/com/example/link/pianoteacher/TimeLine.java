package com.example.link.pianoteacher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class TimeLine extends View {


    private Paint mainPaint = new Paint();
    private Paint bluePaint = new Paint();
    private Paint cyanPaint = new Paint();

    private float startTime;
    private float timeLenght;

    private class Event {
        public int key = 0;
        public float startTime = 0;
        public float endTime = -1;
    }
    private LinkedList<Event> eventList = new LinkedList<>();
    private Event[] currentEvents = new Event[MusicBox.NOTE_COUNT];

    public TimeLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        startTime = 0.0f;
        timeLenght = 5.0f;
        bluePaint.setColor(Color.BLUE);
        cyanPaint.setColor(Color.CYAN);
    }

    protected void onDraw(Canvas canvas) {
        float endTime = startTime + timeLenght;
        float noteWidth = getWidth() / MusicBox.WHITE_NOTE_COUNT;
        float blackNoteWidth = noteWidth * 0.6f;
        float height = getHeight();

        for (int i = 1; i < MusicBox.WHITE_NOTE_COUNT; i++) {
            canvas.drawLine(noteWidth*i, 0, noteWidth*i, getHeight(), mainPaint);
        }

        for (Iterator<Event> it = eventList.iterator(); it.hasNext();) {
            Event e = it.next();
            if (e == null)
                continue;
            if (e.endTime > 0 && e.endTime < startTime - timeLenght) {
                it.remove();
                continue;
            }

            float x = MusicBox.getWhiteIndex(e.key) * noteWidth;
            float start_y = 0;
            if (e.startTime < endTime)
                start_y = height - (height / timeLenght * (startTime - e.startTime));

            float end_y = height;
            if (e.endTime > 0)
                end_y = height - (height / timeLenght * (startTime - e.endTime));
            if (MusicBox.keyIsWhite(e.key)) {
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

    public void setKeyState(int key, int state, float eventTime) {
        if (state == NOTE_PRESSED) {
            Event newEvent = new Event();
            newEvent.startTime = eventTime;
            newEvent.key = key;

            currentEvents[key] = newEvent;
            eventList.add(newEvent);
        }
        else if (currentEvents[key] != null) {
            currentEvents[key].endTime = eventTime;
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
