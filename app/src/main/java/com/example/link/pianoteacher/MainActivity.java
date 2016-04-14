package com.example.link.pianoteacher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.LogRecord;

class MusicBox {
    public static final int WHITE_NOTE_COUNT = 52;
    public static final int NOTE_COUNT = 88;
    public static final int BLACK_NOTE_COUNT = NOTE_COUNT - WHITE_NOTE_COUNT;
    public static final int NOTES_IN_OCTAVE = 12;
    public static final int WHITE_NOTES_IN_OCTAVE = 7;
    public static final int C1_NOTE_INDEX = 40;
    public static final int C2_NOTE_INDEX = C1_NOTE_INDEX + NOTES_IN_OCTAVE;
    public static final int C3_NOTE_INDEX = C2_NOTE_INDEX + NOTES_IN_OCTAVE;
    public static final int C4_NOTE_INDEX = C3_NOTE_INDEX + NOTES_IN_OCTAVE;
    public static final int C5_NOTE_INDEX = C4_NOTE_INDEX + NOTES_IN_OCTAVE;
    public static final ArrayList<Integer> RELATIVE_BLACK_NOTE_INDEXES = new ArrayList<>(Arrays.asList(1,3,6,8,10));
    public static final int[] OCTAVE_INDEX_TO_WHITE_INDEX = {0,0,1,1,2,3,3,4,4,5,5,6,6,7};
}

class StoveViewer extends View {

    Paint p = new Paint();
    Bitmap bassBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bass);
    Bitmap trebleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.treble);
    Bitmap noteBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.quarter_note);
    Bitmap downNoteBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.quarter_note);

    private static final int LINE_SPACE = 50;

    public StoveViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        p.setTextSize(166);
        p.setStrokeWidth(5);
    }

    private int noteNumer = -1;
    private enum StaveMode {TREBLE, BASS};
    private enum NoteModifier {DIES, BEMOLE, BECAR}

    class NoteDrawDescriptor {
        public int position;
        public NoteModifier modifier;
        public StaveMode mode;
    }

    private NoteDrawDescriptor notIndexToDescriptor(int index) {

        NoteDrawDescriptor result = new NoteDrawDescriptor();

        if (index>=MusicBox.C1_NOTE_INDEX)
            result.mode = StaveMode.TREBLE;
        else {
            result.mode = StaveMode.BASS;
        }

        int notePosition = -1;
        if (result.mode == StaveMode.TREBLE) {
            int c1Shift = index - MusicBox.C1_NOTE_INDEX;
            int octaveIndex = c1Shift % MusicBox.NOTES_IN_OCTAVE;
            notePosition = c1Shift / MusicBox.NOTES_IN_OCTAVE * MusicBox.WHITE_NOTES_IN_OCTAVE +
                    MusicBox.OCTAVE_INDEX_TO_WHITE_INDEX[octaveIndex];
            if (MusicBox.RELATIVE_BLACK_NOTE_INDEXES.contains(octaveIndex)) {
                result.modifier = NoteModifier.DIES;
            }

        } else {
            int c1Shift = MusicBox.C1_NOTE_INDEX - index;
            int octaveIndex = 11 - c1Shift % MusicBox.NOTES_IN_OCTAVE;

            notePosition = 11 - ((c1Shift / MusicBox.NOTES_IN_OCTAVE + 1) * MusicBox.WHITE_NOTES_IN_OCTAVE) +
                    MusicBox.OCTAVE_INDEX_TO_WHITE_INDEX[octaveIndex] + 2;
            if (MusicBox.RELATIVE_BLACK_NOTE_INDEXES.contains(octaveIndex)) {
                result.modifier = NoteModifier.DIES;
            }
        }
        if (notePosition > 11 || notePosition < 0)
            notePosition = -1;

        result.position = notePosition;
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float trebleProportion = h * 0.7f / trebleBitmap.getHeight();
        float bassProportion = h * 0.4f / bassBitmap.getHeight();
        float noteProportion = h * 0.5f / noteBitmap.getHeight();

        trebleBitmap = Bitmap.createScaledBitmap(trebleBitmap, (int)(trebleBitmap.getWidth()*trebleProportion), (int)(trebleBitmap.getHeight()*trebleProportion), false);
        bassBitmap = Bitmap.createScaledBitmap(bassBitmap, (int)(bassBitmap.getWidth()*bassProportion), (int)(bassBitmap.getHeight()*bassProportion), false);
        noteBitmap = Bitmap.createScaledBitmap(noteBitmap, (int) (noteBitmap.getWidth() * noteProportion), (int) (noteBitmap.getHeight() * noteProportion), false);
        Matrix m = new Matrix();
        m.postRotate(180);
        downNoteBitmap = Bitmap.createBitmap(noteBitmap, 0, 0, noteBitmap.getWidth(), noteBitmap.getHeight(), m, true);

    }

    protected void onDraw(Canvas canvas) {

        int interLineSpace = getHeight() / 10;
        int firstLine = 3*interLineSpace;

        for (int i = 0; i < 5; i++) {
            canvas.drawLine(getWidth()*0.05f, i*interLineSpace+3*interLineSpace, getWidth()*0.95f, i*interLineSpace+3*interLineSpace, p);
        }

        if (noteNumer == -1)
            return;

        NoteDrawDescriptor desc = notIndexToDescriptor(noteNumer);

        if (desc.mode == StaveMode.TREBLE)
            canvas.drawBitmap(trebleBitmap, getWidth()*0.05f, getHeight()*0.1f, p);
        else {
            canvas.drawBitmap(bassBitmap, getWidth()*0.05f, getHeight()*0.1f, p);
        }

        if (desc.position == 0) {
            float centralPoint = getWidth() * 0.4f;
            canvas.drawLine(centralPoint, 5*interLineSpace+firstLine, centralPoint + 85, 5*interLineSpace+firstLine, p);
        }

        if (desc.position != -1) {
            if (desc.position < 6)
                canvas.drawBitmap(noteBitmap, getWidth() * 0.4f, (11-desc.position)*interLineSpace*0.5f + firstLine - noteBitmap.getHeight() + 19, p);
            else
                canvas.drawBitmap(downNoteBitmap, getWidth() * 0.4f, (11 - desc.position) * interLineSpace * 0.5f + firstLine - 55, p);
        }

    }

    public void setCurrentNote(int noteNumber) {
        this.noteNumer = noteNumber;
        invalidate();
    }
}


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
    private StoveViewer staticStove;
    private EditText text;
    private TextView connectLabel;
    private PianoKeyboard keyboard;

    public void startGame(View view) {
        if (currentMode != ApplicationMode.GAME) {
            gameController.startGame();
        }
        else {
            gameController.stopGame();
            currentMode = ApplicationMode.START;
        }
    }

    enum ApplicationMode {START, GAME}
    private ApplicationMode currentMode = ApplicationMode.START;

    class GameController {
        Random r = new Random();

        private int nextNote = MusicBox.C1_NOTE_INDEX;

        private int getRandomNote(int startIndex, int endIndex) {
            return r.nextInt(endIndex - startIndex) + startIndex;
        }

        public void startGame() {
            currentMode = ApplicationMode.GAME;
            requestNextNote();
        }

        private void requestNextNote() {
            nextNote = getRandomNote(MusicBox.C1_NOTE_INDEX - 12, MusicBox.C1_NOTE_INDEX + 12);
            staticStove.setCurrentNote(nextNote);
        }

        void notePressed(int noteNum) {
            if (noteNum == nextNote)
                requestNextNote();
        }

        public void stopGame() {
            staticStove.setCurrentNote(-1);
        }
    }

    private GameController gameController = new GameController();

    protected Handler messageHandler = new Handler() {

        private final int NOTE_SHIFT = 20;
        @Override
        public void handleMessage(Message inputMessage) {
            super.handleMessage(inputMessage);
            String e = inputMessage.getData().getString("error");
            if (e != null && !e.equals("")) {
                connectLabel.setText(e);
                connectionThread = null;
                return;
            }

            e = inputMessage.getData().getString("success");
            if (e != null && !e.equals("")) {
                connectLabel.setText(e);
            }

            int noteNum;
            int state;
            String m = inputMessage.getData().getString("key");
            if (m != null) {
                text.append(m);
                text.append("\n");
                String s = m.substring(0,2);
                noteNum = Integer.parseInt(s, 16) - NOTE_SHIFT;
                if (m.getBytes()[3] == 'u') {
                    state = PianoKeyboard.NOTE_RELEASED;
                }
                else {
                    state = PianoKeyboard.NOTE_PRESSED;
                    stove.setCurrentNote(noteNum);
                    if (currentMode == ApplicationMode.GAME)
                        gameController.notePressed(noteNum);
                }
                keyboard.setNoteState(noteNum, state);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stove = (StoveViewer)findViewById(R.id.stove);
        staticStove = (StoveViewer)findViewById(R.id.stove_2);
        text = (EditText)findViewById(R.id.editText);
        connectLabel = (TextView)findViewById(R.id.statusLabel);
        keyboard = (PianoKeyboard)findViewById(R.id.keyboard);
        text.append("start");
        text.setFocusable(false);

        tryConnectToServer();
    }

    private Socket socket = null;
    private InputStream is = null;
    private OutputStream out = null;
    private Thread connectionThread = null;

    public void connectButtonClicked(View view) {
        tryConnectToServer();
    }

    private void tryConnectToServer() {
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
                String errorMessage = "Connection failed";
                try {
                    socket = new Socket("192.168.0.100", 1289);
                    is = socket.getInputStream();
                    out = socket.getOutputStream();
                }
                catch (IOException e)
                {
                    errorMessage = e.getMessage();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if (socket == null) {
                    Message mess = new Message();
                    mess.getData().putString("error", errorMessage);
                    messageHandler.sendMessage(mess);
                    return;
                }
                else {
                    Message mess = new Message();
                    mess.getData().putString("success", "Connected");
                    messageHandler.sendMessage(mess);
                }
            }

            while(true) {
                try {
                    if (is.read(message, 0, 5) == 5) {
                        Message mess = new Message();
                        mess.getData().putString("key", new String(message, 0, 5));
                        messageHandler.sendMessage(mess);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
