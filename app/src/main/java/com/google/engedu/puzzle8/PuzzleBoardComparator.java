package com.google.engedu.puzzle8;

import java.util.Comparator;

public class PuzzleBoardComparator implements Comparator<PuzzleBoard> {
    @Override
    public int compare(PuzzleBoard t0, PuzzleBoard t1) {
        return t0.priority() - t1.priority();
    }
}
