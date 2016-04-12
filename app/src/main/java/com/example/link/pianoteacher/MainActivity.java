package com.example.link.pianoteacher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.LogRecord;

class StoveViewer extends View {
    Paint p = new Paint();
    private static final int LINE_SPACE = 50;

    public StoveViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        p.setTextSize(300);
        p.setStrokeWidth(5);
    }

    private int noteNumer = 0;

    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < 5; i++) {
            canvas.drawLine(100, i*LINE_SPACE+100, getWidth()-100, i*LINE_SPACE+100, p);
        }

        canvas.drawText("\u2669", 100+(5*60), 100+(noteNumer*LINE_SPACE/2), p);
    }

    public void setCurrentNote(int noteNumber) {
        this.noteNumer = noteNumber;
        invalidate();
    }
}

class PianoKeyboard extends View {

    static final int NOTE_COUNT = 88;

    private Paint p = new Paint();
    private Paint strokePaint = new Paint();
    private Paint activePaint = new Paint();

    private int notes[] = new int[NOTE_COUNT];

    public static final int NOTE_PRESSED = 1;
    public static final int NOTE_RELEASED = 0;

    public PianoKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        p.setStrokeWidth(5);
        strokePaint.setStrokeWidth(5);
        strokePaint.setStyle(Paint.Style.STROKE);
        activePaint.setColor(Color.MAGENTA);

        for(int i = 0; i < NOTE_COUNT; i++) {
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
        int start = 4 + 12 * octave;
        int[] helperWhite = {0,2,4,5,7,9,11};
        int[] helperBlack = {1,3,0,6,8,10,0};

        if (isBlack)
            start += helperBlack[note];
        else
            start += helperWhite[note];
        if (start >= NOTE_COUNT)
            return NOTE_RELEASED;
        return notes[start];
    }

    private void drawOctave(Canvas canvas, int num, float left, float bottom, float width, float height) {
        float note_width = width / 7.0f;
        float black_note_width = note_width * 0.6f;
        float black_note_height = height * 0.7f;
        float black_note_bottom = bottom + height - black_note_height;

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

public class MainActivity extends AppCompatActivity {

    private StoveViewer stove;
    private EditText text;
    private TextView connectLabel;
    private PianoKeyboard keyboard;
    protected Handler messageHandler = new Handler() {

        @Override
        public void handleMessage(Message inputMessage) {
            super.handleMessage(inputMessage);
            String e = inputMessage.getData().getString("error");
            if (e != null && !e.equals(""))
                connectLabel.setText(e);
            else {
                int noteNum;
                int state;
                String m = inputMessage.getData().getString("key");
                if (m != null) {
                    text.append(m);
                    text.append("\n");
                    String s = m.substring(0,2);
                    noteNum = Integer.parseInt(s, 16) - 20;
                    if (m.getBytes()[3] == 'u')
                        state = PianoKeyboard.NOTE_RELEASED;
                    else
                        state = PianoKeyboard.NOTE_PRESSED;
                    keyboard.setNoteState(noteNum, state);
                }
            }


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stove = (StoveViewer)findViewById(R.id.stove);
        text = (EditText)findViewById(R.id.editText);
        connectLabel = (TextView)findViewById(R.id.statusLabel);
        keyboard = (PianoKeyboard)findViewById(R.id.keyboard);
        text.append("start");
        text.setFocusable(false);
        stove.setCurrentNote(5);
    }

    private Socket socket = null;
    private InputStream is = null;
    private OutputStream out = null;
    private Thread connectionThread = null;

    public void connectButtonClicked(View view) {
        if (connectionThread == null) {
            connectionThread = new Thread(serverConnectionTask);
            connectionThread.start();
        }
    }

    private Runnable serverConnectionTask = new Runnable() {
        private byte message[] = new byte[1000];
        @Override
        public void run() {
            if (socket == null) {
                try {
                    socket = new Socket("192.168.0.100", 1289);
                    is = socket.getInputStream();
                    out = socket.getOutputStream();
                }
                catch (IOException e)
                {
                    e.getMessage();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            while(true) {
                try {
                    is.read(message, 0, 5);
                    Message mess = new Message();
                    mess.getData().putString("key", new String(message,0,5));
                    messageHandler.sendMessage(mess);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };


}
