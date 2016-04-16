package com.example.link.pianoteacher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

class TimeLine extends View {


    private Paint mainPaint = new Paint();
    private Paint bluePaint = new Paint();

    private float startTime;
    private float timeLenght;

    private class Event {
        public int note = 0;
        public float startTime = 0;
        public float endTime = -1;

        public Event() {

        }
    }
    private ArrayList<Event>[] eventLists = new ArrayList[MusicBox.NOTE_COUNT];

    public TimeLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        startTime = 0.0f;
        timeLenght = 10.0f;
        bluePaint.setColor(Color.BLACK);

    }

    protected void onDraw(Canvas canvas) {
        float endTime = startTime + timeLenght;
        float noteWidth = getWidth() / MusicBox.WHITE_NOTE_COUNT;
        float height = getHeight();

        for (int i = 0; i < MusicBox.NOTE_COUNT; i++) {
            for (Event e : eventLists[i]) {
                if (e.startTime >= startTime && e.startTime < endTime && (e.endTime > e.startTime || e.endTime < 0)) {
                    float startY = height / timeLenght * e.startTime;
                    float endY = height;
                    if (e.endTime > 0) {
                        endY = height / timeLenght * e.startTime;
                    }
                    canvas.drawRect(i*noteWidth, startY, i*noteWidth + noteWidth, endY, bluePaint);

                }
            }
        }

        for (int i = 1; i < MusicBox.WHITE_NOTE_COUNT; i++) {
            canvas.drawLine(noteWidth*i, 0, noteWidth*i, getHeight(), mainPaint);
        }
    }

    public void addEvent(int note, int time, int status) {

    }

}
