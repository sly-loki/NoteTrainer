package com.example.link.pianoteacher;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private StoveViewer stove;
    private StoveViewer staticStove;
    private EditText text;
    private TextView connectLabel;
    private PianoKeyboard keyboard;
    private TimeLine timeLine;

    public void startGame(View view) {
        if (currentMode != ApplicationMode.GAME) {
            gameController.startGame();
        }
        else {
            gameController.stopGame();
            currentMode = ApplicationMode.START;
        }
    }

    public void gameSettingsOK(View view) {
        gameSettingWindow.dismiss();
        CheckBox trebleBox = (CheckBox)gameSettingWindow.getContentView().findViewById(R.id.trebleCheck);
        CheckBox bassBox = (CheckBox)gameSettingWindow.getContentView().findViewById(R.id.bassCheck);
        if (trebleBox.isChecked() && bassBox.isChecked())
            gameSettings.clefMode = ClefMode.BOTH;
        else if (trebleBox.isChecked())
            gameSettings.clefMode = ClefMode.TREBLE;
        else if (bassBox.isChecked())
            gameSettings.clefMode = ClefMode.BASS;
    }

    public void gameSettingsCancel(View view) {
        gameSettingWindow.dismiss();
    }

    public void nextButtonClicked(View view) {
    }

    public void startAudioGame(View view) {
        
    }

    enum ClefMode {BASS, TREBLE, BOTH}
    class GameSettings {
        ClefMode clefMode = ClefMode.TREBLE;
        boolean modifiers = false;
    }

    GameSettings gameSettings = new GameSettings();
    PopupWindow gameSettingWindow;

    public void openSettings(View view) {
        if (gameSettingWindow.isShowing()) {
            gameSettingsCancel(view);
        } else {
            if (currentMode != ApplicationMode.GAME) {
                CheckBox trebleBox = (CheckBox)gameSettingWindow.getContentView().findViewById(R.id.trebleCheck);
                CheckBox bassBox = (CheckBox)gameSettingWindow.getContentView().findViewById(R.id.bassCheck);

                trebleBox.setChecked(gameSettings.clefMode == ClefMode.BOTH || gameSettings.clefMode == ClefMode.TREBLE);
                bassBox.setChecked(gameSettings.clefMode == ClefMode.BOTH || gameSettings.clefMode == ClefMode.BASS);

                gameSettingWindow.showAsDropDown(findViewById(R.id.startButton));
            }
        }
    }

    enum ApplicationMode {START, GAME}
    private ApplicationMode currentMode = ApplicationMode.START;

    class GameController {
        private Random r = new Random();
        private int score = 0;
        private int nextNote = MusicBox.C1_NOTE_INDEX;
        private int minNote = 0;
        private int maxNote = 87;

        private int getRandomNote(int startIndex, int endIndex) {
            return r.nextInt(endIndex - startIndex) + startIndex;
        }

        public void startGame() {
            score = 0;
            printMessage("Started", true);
            currentMode = ApplicationMode.GAME;
            StoveViewer.StaveMode mode;
            if (gameSettings.clefMode == ClefMode.BASS) {
                mode = StoveViewer.StaveMode.BASS;

                minNote = 10;
                maxNote = 49;
            } else {
                mode = StoveViewer.StaveMode.TREBLE;

                minNote = 31;
                maxNote = 69;
            }
            stove.setMode(mode);
            staticStove.setMode(mode);

            requestNextNote();
        }

        private void requestNextNote() {
            int oldNote = nextNote;
            while (oldNote == nextNote)
                nextNote = getRandomNote(minNote, maxNote);
            staticStove.setCurrentNote(nextNote);
        }

        void notePressed(int noteNum) {
            if (noteNum == nextNote) {
                stove.setCurrentNote(MusicBox.INVALIDE_NOTE);
                score++;
                printMessage("score: "+score, true);
                requestNextNote();
            } else {
                score--;
                printMessage("incorrect", false);
            }
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
                String s = m.substring(0,2);
                noteNum = Integer.parseInt(s, 16) - NOTE_SHIFT;

                String time = m.substring(5);
                String[] times = time.split("[ ]+");
                float[] multipliers = {3600, 60, 1, 0.001f};
                float eventTime = 0;
                for (int i = 0; i < 4; i++)
                {
                    eventTime += Integer.parseInt(times[i]) * multipliers[i];
                }

                if (m.getBytes()[3] == 'u') {
                    state = PianoKeyboard.NOTE_RELEASED;
                }
                else if (m.getBytes()[3] == 'd') {
                    state = PianoKeyboard.NOTE_PRESSED;
                    stove.setCurrentNote(noteNum);
                    if (currentMode == ApplicationMode.GAME)
                        gameController.notePressed(noteNum);
                }
                else {
                    timeLine.synchronizeTime(eventTime);
                    return;
                }
                keyboard.setNoteState(noteNum, state);
                timeLine.setKeyState(noteNum, state, eventTime);
            }
        }
    };

    Timer timer = new Timer();
    class Timeout extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timeLine.timePassed(0.1f);
                }
            });
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
        timeLine = (TimeLine)findViewById(R.id.timeLine);
        text.append("start");
        text.setFocusable(false);
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View settingsView = layoutInflater.inflate(R.layout.game_settings, null);

        gameSettingWindow = new PopupWindow(settingsView, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        gameSettingWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        tryConnectToServer();
        timer.schedule(new Timeout(), 0, 100);
    }

    protected void printMessage(String message, boolean clean) {
        if (clean) {
            text.setText("");
        }
        text.append(message);
        text.append("\n");
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
                    if (is.read(message, 0, 17) == 17) {
                        Message mess = new Message();
                        mess.getData().putString("key", new String(message, 0, 17));
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
