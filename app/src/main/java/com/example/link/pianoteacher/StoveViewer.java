package com.example.link.pianoteacher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

class StoveViewer extends View {

    Paint p = new Paint();
    Bitmap bassBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bass);
    Bitmap trebleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.treble);
    Bitmap noteBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.quarter_note);
    Bitmap downNoteBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.quarter_note);
    Bitmap sharpBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sharp);
    Bitmap bemolBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bemol);

    public StoveViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        p.setTextSize(166);
        p.setStrokeWidth(5);
    }

    private int noteNumber = MusicBox.INVALIDE_NOTE;

    public enum StaveMode {TREBLE, BASS}
    StaveMode staveMode = StaveMode.BASS;

    private enum NoteModifier {NORMAL, DIES, BEMOL, BECAR}



    class NoteDrawDescriptor {
        public int position;
        public NoteModifier modifier = NoteModifier.NORMAL;
    }

    private static final int MISSED_NOTES_IN_SUB_CONTR_OCTAVE = 9;
    private static final int NOTES_IN_SUB_CONTR_OCTAVE = 3;
    private static final int WHITE_NOTES_IN_SUB_CONTR_OCTAVE = 2;

    private int whiteNoteToLine(int note, StaveMode mode) {

        final int ZERO_POS_NOTE_IN_TREBLE = MusicBox.C1_NOTE_WHITE_INDEX + 6; //note on third line
        final int ZERO_POS_NOTE_IN_BASS = MusicBox.C1_NOTE_WHITE_INDEX - 6;

        if (mode == StaveMode.TREBLE) {
            return note - ZERO_POS_NOTE_IN_TREBLE;
        } else {
            return note - ZERO_POS_NOTE_IN_BASS;
        }
    }


    private NoteDrawDescriptor noteIndexToDescriptor(int index, StaveMode mode) {

        NoteDrawDescriptor result = new NoteDrawDescriptor();
        Log.d("myPiano", "index " + index);

        int octaveIndex;
        if (index < NOTES_IN_SUB_CONTR_OCTAVE) {
            octaveIndex = MISSED_NOTES_IN_SUB_CONTR_OCTAVE + index % MusicBox.NOTES_IN_OCTAVE;
        }
        else {
            octaveIndex = (index-NOTES_IN_SUB_CONTR_OCTAVE) % MusicBox.NOTES_IN_OCTAVE;
        }


        Log.d("myPiano", "octaveIndex "+octaveIndex);

        int noteWhiteIndex;
        if (index >= NOTES_IN_SUB_CONTR_OCTAVE) {
            noteWhiteIndex = (index - NOTES_IN_SUB_CONTR_OCTAVE)/MusicBox.NOTES_IN_OCTAVE *
                    MusicBox.WHITE_NOTES_IN_OCTAVE +
                    MusicBox.OCTAVE_INDEX_TO_WHITE_INDEX[octaveIndex] +
                    WHITE_NOTES_IN_SUB_CONTR_OCTAVE;
        } else {
            noteWhiteIndex = index / 2;
        }
        Log.d("myPiano", "whiteIndex "+noteWhiteIndex);

        if (MusicBox.RELATIVE_BLACK_NOTE_INDEXES.contains(octaveIndex)) {
            result.modifier = NoteModifier.DIES;
        }

        result.position = whiteNoteToLine(noteWhiteIndex, mode);
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float trebleProportion = h * 0.7f / trebleBitmap.getHeight();
        float bassProportion = h * 0.4f / bassBitmap.getHeight();
        float noteProportion = h * 0.45f / noteBitmap.getHeight();
        float alterationProportion = h * 0.15f / sharpBitmap.getHeight();

        trebleBitmap = Bitmap.createScaledBitmap(trebleBitmap, (int)(trebleBitmap.getWidth()*trebleProportion), (int)(trebleBitmap.getHeight()*trebleProportion), false);
        bassBitmap = Bitmap.createScaledBitmap(bassBitmap, (int)(bassBitmap.getWidth()*bassProportion), (int)(bassBitmap.getHeight()*bassProportion), false);
        noteBitmap = Bitmap.createScaledBitmap(noteBitmap, (int) (noteBitmap.getWidth() * noteProportion), (int) (noteBitmap.getHeight() * noteProportion), false);
        Matrix m = new Matrix();
        m.postRotate(180);
        downNoteBitmap = Bitmap.createBitmap(noteBitmap, 0, 0, noteBitmap.getWidth(), noteBitmap.getHeight(), m, true);
        sharpBitmap = Bitmap.createScaledBitmap(sharpBitmap, (int) (sharpBitmap.getWidth() * alterationProportion), (int) (sharpBitmap.getHeight() * alterationProportion), false);
        bemolBitmap = Bitmap.createScaledBitmap(bemolBitmap, (int) (bemolBitmap.getWidth() * alterationProportion), (int) (bemolBitmap.getHeight() * alterationProportion), false);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        int interLineSpace = getHeight() / 12;
        int firstLine = 4*interLineSpace;

        for (int i = 0; i < 5; i++) {
            canvas.drawLine(getWidth()*0.05f, i*interLineSpace+firstLine, getWidth()*0.95f, i*interLineSpace+firstLine, p);
        }

        if (noteNumber == -1)
            return;

        NoteDrawDescriptor desc = noteIndexToDescriptor(noteNumber, staveMode);
        Log.d("myPiano", "position " + desc.position);


        if (staveMode == StaveMode.TREBLE)
            canvas.drawBitmap(trebleBitmap, getWidth()*0.05f, getHeight()*0.15f, p);
        else {
            canvas.drawBitmap(bassBitmap, getWidth() * 0.05f, getHeight() * 0.3f, p);
        }

        if (desc.position < -11 || desc.position > 11)
            return;

        Bitmap note;
        float x = getWidth() * 0.4f;
        float baseY = interLineSpace * 2.5f + firstLine;
        float y;
        float alterY;
        if (desc.position < 0) {
            y = baseY - desc.position*interLineSpace*0.5f - noteBitmap.getHeight() + 16;
            note = noteBitmap;
            alterY = y + note.getHeight() - sharpBitmap.getHeight() - 10;
        }
        else {
            y = baseY - desc.position*interLineSpace*0.5f - 48;
            note = downNoteBitmap;
            alterY = y + 13;
        }

        float centralPoint = getWidth() * 0.4f;
        if (desc.position <= -10)
            canvas.drawLine(centralPoint, 7*interLineSpace+firstLine, centralPoint + 85, 7*interLineSpace+firstLine, p);
        if (desc.position <= -8)
            canvas.drawLine(centralPoint, 6*interLineSpace+firstLine, centralPoint + 85, 6*interLineSpace+firstLine, p);
        if (desc.position <= -6)
            canvas.drawLine(centralPoint, 5*interLineSpace+firstLine, centralPoint + 85, 5*interLineSpace+firstLine, p);

        if (desc.position >= 6)
            canvas.drawLine(centralPoint, firstLine - interLineSpace, centralPoint + 85, firstLine - interLineSpace, p);
        if (desc.position >= 8)
            canvas.drawLine(centralPoint, firstLine - 2*interLineSpace, centralPoint + 85, firstLine - 2*interLineSpace, p);
        if (desc.position >= 10)
            canvas.drawLine(centralPoint, firstLine - 3*interLineSpace, centralPoint + 85, firstLine - 3*interLineSpace, p);


        switch (desc.modifier) {
            case DIES:
                canvas.drawBitmap(sharpBitmap, x - sharpBitmap.getWidth() - 10, alterY, p);
                break;
            case BEMOL:
                canvas.drawBitmap(bemolBitmap, x - bemolBitmap.getWidth() - 10, alterY, p);
                break;
            default:
                break;
        }
        canvas.drawBitmap(note, x, y, p);

    }

    public void setCurrentNote(int noteNumber) {
        this.noteNumber = noteNumber;
        invalidate();
    }

    public void setMode(StaveMode mode)
    {
        if (mode != staveMode) {
            staveMode = mode;
            invalidate();
        }
    }
}
