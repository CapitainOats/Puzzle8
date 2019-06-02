/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.puzzle8;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

public class PuzzleBoardView extends View {
    public static final int NUM_SHUFFLE_STEPS = 40;
    private Activity activity;
    private PuzzleBoard puzzleBoard;
    private ArrayList<PuzzleBoard> animation;
    private Random random = new Random();

    public PuzzleBoardView(Context context) {
        super(context);
        activity = (Activity) context;
        animation = null;
    }

    public void initialize(Bitmap imageBitmap) {
        int width = getWidth();
        puzzleBoard = new PuzzleBoard(imageBitmap, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (puzzleBoard != null) {
            if (animation != null && animation.size() > 0) {
                puzzleBoard = animation.remove(0);
                puzzleBoard.draw(canvas);
                if (animation.size() == 0) {
                    animation = null;
                    puzzleBoard.reset();
                    Toast toast = Toast.makeText(activity, "Solved! ", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    this.postInvalidateDelayed(500);
                }
            } else {
                puzzleBoard.draw(canvas);
            }
        }
    }

    public void shuffle() {
        if (animation == null && puzzleBoard != null) {
            for (int i = 0; i < NUM_SHUFFLE_STEPS; i++) {
                ArrayList<PuzzleBoard> neighbours = puzzleBoard.neighbours();
                // Update to randomly selected value from puzzleBoard.neighbours.
                puzzleBoard = neighbours.get(random.nextInt(neighbours.size()));
            }
            // Do something. Then:
            puzzleBoard.reset();
            // Call in order to update the UI.
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animation == null && puzzleBoard != null) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (puzzleBoard.click(event.getX(), event.getY())) {
                        invalidate();
                        if (puzzleBoard.resolved()) {
                            Toast toast = Toast.makeText(activity, "Congratulations!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        return true;
                    }
            }
        }
        return super.onTouchEvent(event);
    }

    public void solve() {
        Comparator<PuzzleBoard> puzzleBoardComparator = new PuzzleBoardComparator();
        PriorityQueue<PuzzleBoard> priorityQueue = new PriorityQueue<>(puzzleBoardComparator);
        priorityQueue.add(puzzleBoard);

        while (!priorityQueue.isEmpty()) {
            PuzzleBoard lowestPriorityBoard = priorityQueue.poll();
            if (lowestPriorityBoard.resolved()) {
                // Create an ArrayList of all the PuzzleBoards leading to this solution
                ArrayList<PuzzleBoard> solution = new ArrayList<>();
                while (lowestPriorityBoard.getPreviousBoard() != null) {
                    solution.add(lowestPriorityBoard);
                    lowestPriorityBoard = lowestPriorityBoard.getPreviousBoard();
                }
                // Turn it into an in-order sequence of all the steps to solving the puzzle.
                Collections.reverse(solution);
                // The given implementation of onDraw will animate the sequence of steps to solve the puzzle.
                animation = solution;
                // Call in order to update the UI.
                invalidate();
                break;
            } else {
                // If the removed PuzzleBoard is not the solution, insert onto the PriorityQueue.
                // To prevent unnecessary exploration of useless states, when considering
                // the neighbours of a state, don't enqueue the neighbour if its board position
                // is the same as the previous state.
                ArrayList<PuzzleBoard> possibleNeighbours = lowestPriorityBoard.neighbours();
                for (PuzzleBoard neighbour : possibleNeighbours) {
                    if (!neighbour.equals(lowestPriorityBoard.getPreviousBoard())) {
                        priorityQueue.add(neighbour);
                    }
                }
            }
        }

    }
}
