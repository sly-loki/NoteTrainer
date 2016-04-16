package com.example.link.pianoteacher;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

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

        private final int NOTE_SHIFT = 21;
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
