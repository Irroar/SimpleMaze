package com.example.maze;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class GameView extends View {

    private enum Direction{
        UP, DOWN, LEFT, RIGHT
    }

    //private SensorManager sensorManager;
   // private Sensor accelerometer;

    float [] history = new float[2];

    private Cell[][] cells;
    private Cell player, exit;
    private static final int COLS = 7, ROWS = 10;
    private static final float WALL_THICKNESS = 4;
    private float cellSize, hMargin, vMargin;
    private Paint wallPaint, playerPaint, exitPaint;
    private Random random;

    public GameView(Context context) {
        super(context);
        //this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        //if (sensorManager != null) {
         //   this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //}

        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        playerPaint = new Paint();
        playerPaint.setColor(Color.YELLOW);

        exitPaint = new Paint();
        exitPaint.setColor(Color.GREEN);

        random = new Random();

        createMaze();
    }

    private Cell getNeighbour(Cell cell)
    {
        ArrayList<Cell> neighbours = new ArrayList<>();

        // left
        if (cell.col > 0)
            if (!cells[cell.col - 1][cell.row].visited)
                neighbours.add(cells[cell.col - 1][cell.row]);

        // right
        if (cell.col < COLS - 1)
            if (!cells[cell.col + 1][cell.row].visited)
                neighbours.add(cells[cell.col + 1][cell.row]);

        // top
        if (cell.row > 0)
            if (!cells[cell.col][cell.row - 1].visited)
                neighbours.add(cells[cell.col][cell.row - 1]);

        // bottom
        if (cell.row < ROWS - 1)
            if (!cells[cell.col][cell.row + 1].visited)
                neighbours.add(cells[cell.col][cell.row + 1]);

        if (neighbours.size() > 0) {
            int index = random.nextInt(neighbours.size());
            return neighbours.get(index);
        }
        return null;
    }

    private void removeWall(Cell current, Cell next)
    {
        if (current.col == next.col && current.row == next.row + 1)
        {
            current.topWall = false;
            next.bottomWall = false;
        }

        if (current.col == next.col && current.row == next.row - 1)
        {
            current.bottomWall = false;
            next.topWall = false;
        }

        if (current.col == next.col + 1 && current.row == next.row)
        {
            current.leftWall = false;
            next.rightWall = false;
        }

        if (current.col == next.col - 1 && current.row == next.row)
        {
            current.rightWall = false;
            next.leftWall = false;
        }
    }

    private void createMaze(){
        Stack<Cell> stack = new Stack<>();
        Cell current, next;

        cells = new Cell[COLS][ROWS];

        for(int x = 0; x < COLS; x++)
        {
            for(int y = 0; y < ROWS; y++)
            {
                cells[x][y] = new Cell(x, y);
            }
        }

        player = cells[0][0];
        exit = cells[COLS - 1][ROWS - 1];

        current = cells[0][0];
        current.visited = true;

        do {
            next = getNeighbour(current);
            if (next != null) {
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
            } else
                current = stack.pop();
        } while (!stack.empty());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLUE);

        int width = getWidth();
        int height = getHeight();

        if (width/height < COLS/ROWS)
            cellSize = width/(COLS + 1);
        else
            cellSize = height/(ROWS + 1);

        hMargin = (width - COLS * cellSize) / 2;
        vMargin = (height - ROWS * cellSize) / 2;

        canvas.translate(hMargin, vMargin);

        for(int x = 0; x < COLS; x++)
        {
            for(int y = 0; y < ROWS; y++)
            {
                if (cells[x][y].topWall)
                    canvas.drawLine(
                            x * cellSize,
                            y * cellSize,
                            (x + 1) * cellSize,
                            y * cellSize,
                            wallPaint
                    );
                if (cells[x][y].leftWall)
                    canvas.drawLine(
                            x * cellSize,
                            y * cellSize,
                            x * cellSize,
                            (y + 1) * cellSize,
                            wallPaint
                    );
                if (cells[x][y].bottomWall)
                    canvas.drawLine(
                            x * cellSize,
                            (y + 1) * cellSize,
                            (x + 1) * cellSize,
                            (y + 1) * cellSize,
                            wallPaint
                    );
                if (cells[x][y].rightWall)
                    canvas.drawLine(
                            (x + 1) * cellSize,
                            y * cellSize,
                            (x + 1) * cellSize,
                            (y + 1) * cellSize,
                            wallPaint
                    );
            }
        }

        float playerMargin = cellSize / 5;
        float exitMargin = cellSize / 10;

        canvas.drawCircle(
                ((player.col) * cellSize) + cellSize/2,
                ((player.row ) * cellSize) + cellSize/2,
                (cellSize - playerMargin) / 2,
                playerPaint

        );

        canvas.drawRect(
                exit.col * cellSize + exitMargin,
                exit.row * cellSize + exitMargin,
                (exit.col + 1) * cellSize - exitMargin,
                (exit.row + 1) * cellSize - exitMargin,
                exitPaint
        );

    }

    private void movePlayer(Direction direction)
    {
        switch (direction)
        {
            case UP:
                if (!player.topWall)
                    player = cells[player.col][player.row - 1];
                break;
            case DOWN:
                if (!player.bottomWall)
                    player = cells[player.col][player.row + 1];
                break;
            case LEFT:
                if (!player.leftWall)
                    player = cells[player.col - 1][player.row];
                break;
            case RIGHT:
                if (!player.rightWall)
                    player = cells[player.col + 1][player.row];
        }
        checkExit();
        invalidate();
    }

    private void checkExit()
    {
        if (player == exit)
        {
            Toast toast = Toast.makeText(getContext(),
                    "WIN!",
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            //createMaze();
        }
    }

    public void onSensorEvent(SensorEvent event) {
        float xChange = history[0] - event.values[0];
        float yChange = history[1] - event.values[1];

        history[0] = event.values[0];
        history[1] = event.values[1];

        if (xChange > 1){
            movePlayer(Direction.RIGHT);
        }
        else if (xChange < -1){
            movePlayer(Direction.LEFT);
        }

        if (yChange > 1){
            movePlayer(Direction.UP);
        }
        else if (yChange < -0.5){
            movePlayer(Direction.DOWN);
        }
    }


    private class Cell{
        boolean
            topWall = true,
            leftWall = true,
            bottomWall = true,
            rightWall = true,
            visited = false;

        int col, row;

        public Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }
}
