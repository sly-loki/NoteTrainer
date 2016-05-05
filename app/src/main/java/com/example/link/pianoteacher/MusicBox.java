package com.example.link.pianoteacher;

import java.util.ArrayList;
import java.util.Arrays;

class MusicBox {
    public static final int INVALIDE_NOTE = -1;
    public static final int WHITE_NOTE_COUNT = 52;
    public static final int NOTE_COUNT = 88;
    public static final int BLACK_NOTE_COUNT = NOTE_COUNT - WHITE_NOTE_COUNT;
    public static final int NOTES_IN_OCTAVE = 12;
    public static final int WHITE_NOTES_IN_OCTAVE = 7;
    public static final int C1_NOTE_INDEX = 39;
    public static final int C1_NOTE_WHITE_INDEX = 23;
    public static final int C2_NOTE_INDEX = C1_NOTE_INDEX + NOTES_IN_OCTAVE;
    public static final int C3_NOTE_INDEX = C2_NOTE_INDEX + NOTES_IN_OCTAVE;
    public static final int C4_NOTE_INDEX = C3_NOTE_INDEX + NOTES_IN_OCTAVE;
    public static final int C5_NOTE_INDEX = C4_NOTE_INDEX + NOTES_IN_OCTAVE;
    public static final ArrayList<Integer> RELATIVE_BLACK_NOTE_INDEXES = new ArrayList<>(Arrays.asList(1, 3, 6, 8, 10));
    public static final int[] OCTAVE_INDEX_TO_WHITE_INDEX = {0,0,1,1,2,3,3,4,4,5,5,6,6,7};

    public enum Octaves {SUBCONTR, CONTR, BIG, SMALL, FIRST, SECOND, THIRD, FOURTH, FIFTH}

    public static boolean keyIsWhite(int key) {
        if (key < 3)
            return key != 1;
        key = (key - 3) % MusicBox.NOTES_IN_OCTAVE;
        return !MusicBox.RELATIVE_BLACK_NOTE_INDEXES.contains(key);
    }

    public static int getWhiteIndex(int key) {
        if (key < 3)
            return key / 2;
        int octave = (key - 3) / MusicBox.NOTES_IN_OCTAVE;
        return octave * MusicBox.WHITE_NOTES_IN_OCTAVE +
                MusicBox.OCTAVE_INDEX_TO_WHITE_INDEX[(key - 3) % MusicBox.NOTES_IN_OCTAVE] + 2;
    }
}