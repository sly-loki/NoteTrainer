package com.example.link.pianoteacher;

import android.content.Context;
import android.graphics.Canvas;
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


public class MainActivity extends AppCompatActivity {

    private StoveViewer stove;
    private EditText text;
    private TextView connectLabel;
    protected Handler messageHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stove = (StoveViewer)findViewById(R.id.stove);
        text = (EditText)findViewById(R.id.editText);
        connectLabel = (TextView)findViewById(R.id.statusLabel);
        text.append("start");
        text.setFocusable(false);
        stove.setCurrentNote(5);

        messageHandler = new Handler() {

            @Override
            public void handleMessage(Message inputMessage) {
                super.handleMessage(inputMessage);
                String e = inputMessage.getData().getString("error");
                if (e != null && !e.equals(""))
                    connectLabel.setText(e);
                else {
                    String m = inputMessage.getData().getString("key");
                    if (m != null) {
                        text.append(m);
                        text.append("\n");
                    }
                }
            }
        };
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
