package edu.orangecoastcollege.escapethecatcher;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/***
 * This is the entry point for "The Escape" application. While it was based on the class app, it has
 * been heavily modified. The field of play (i.e. obstacles) is randomized, as are the locations for
 * the exit, player and zombie. In addition, the use of a double tap will "Freeze" the zombie for two
 * turns. This can be used only once in 5 turns. In case of a win, the zombie turns into a bunny,
 * while in case of a loss the zombie and player both trun into a puddle of blood. It also has a
 * button to get a new game for those cases where the obstacles pin the player.
 */

public class GameActivity extends AppCompatActivity 
        implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private GestureDetector gestureDetector;

    //FLING THRESHOLD VELOCITY
    final int FLING_THRESHOLD = 200;

    //BOARD INFORMATION
    final int OFFSET = 5;
    final int COLS = 8;
    final int ROWS = 9;
    final int MAX_OBSTACLES = 12;
    private int gameBoard[][] = {
            {1, 1, 1, 1, 1, 1, 1, 1},
            {1, 2, 2, 1, 2, 2, 1, 1},
            {1, 2, 2, 2, 2, 2, 2, 1},
            {1, 2, 1, 2, 2, 2, 2, 1},
            {1, 2, 2, 2, 2, 2, 1, 1},
            {1, 2, 2, 1, 2, 2, 2, 3},
            {1, 2, 1, 2, 2, 2, 2, 1},
            {1, 2, 2, 2, 2, 2, 2, 1},
            {1, 1, 1, 1, 1, 1, 1, 1}};

    private List<ImageView> allGameObjects;
    private Player player;
    private Zombie zombie;
    Point[] obstacles;

    //LAYOUT AND INTERACTIVE INFORMATION
    private RelativeLayout activityGameRelativeLayout;
    private ImageView zombieImageView;
    private ImageView playerImageView;
    private ImageView bloodImageView;
    private ImageView bunnyImageView;
    private TextView winsTextView;
    private TextView lossesTextView;

    private int exitRow;
    private int exitCol;

    private int obstacleRow;
    private int obstacleCol;

    private int playerRow;
    private int playerCol;

    private int zombieRow;
    private int zombieCol;

    private int square;
    private int numObstacles;

    private int doubleTapCount = 0;

    //  WINS AND LOSSES
    private int wins = 0;
    private int losses = 0;

    private Handler handler;
    Random random = new Random();

    private LayoutInflater layoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        activityGameRelativeLayout = (RelativeLayout) findViewById(R.id.activity_game);
        winsTextView = (TextView) findViewById(R.id.winsTextView);
        lossesTextView = (TextView) findViewById(R.id.lossesTextView);

        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        allGameObjects = new ArrayList<>();

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        square = size.x / COLS;

        handler = new Handler();

        gestureDetector = new GestureDetector(this, this);

        startNewGame();
    }

    /***
     *  This method starts a new game by clearing the playing field, filling in the outside
     *  obstacles to define the playing field, then placing a random number of obstacles on the field.
     *  To improve the game, the obstacles should not create a "dead zone". The exit row is randomly
     *  generated with only the requirement that an obstacle does not sit in fron of it. The player
     *  and zombie are placed so that thay are not on an obstacle,and are seperated by at least 2
     *  cells. The layout is then created with the position of all the objects in the playing field.
     */

    private void startNewGame() {
        //TASK 1:  CLEAR THE BOARD (ALL IMAGE VIEWS)

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++)
                gameBoard[i][j] = BoardCodes.EMPTY;
        }

        for (int i = 0; i < COLS; i++) {
            gameBoard[0][i] = 1;
            gameBoard[ROWS-1][i] = BoardCodes.OBSTACLE;
        }

        for (int i = 1; i < ROWS-1; i++) {
            gameBoard[i][0] = 1;
            gameBoard[i][COLS-1] = BoardCodes.OBSTACLE;
        }

        numObstacles = random.nextInt(MAX_OBSTACLES-6) + 6;
        for (int i = 0; i < numObstacles; i++) {
            obstacleRow = random.nextInt(ROWS-2) + 1;
            obstacleCol = random.nextInt(COLS-2) + 1;
            gameBoard[obstacleRow][obstacleCol] = BoardCodes.OBSTACLE;
        }

        int exitSide = random.nextInt(4);
        switch (exitSide) {
            case 0:
                do exitRow = random.nextInt(ROWS-2) + 1;
                while (gameBoard[exitRow][COLS - 2] != 2);
                exitCol = COLS - 1;
                break;

            case 1:
                exitRow = ROWS - 1;
                do exitCol = random.nextInt(COLS-2) + 1;
                while (gameBoard[ROWS-2][exitCol] != 2);
                break;

            case 2:
                do exitRow = random.nextInt(ROWS - 2) + 1;
                while (gameBoard[exitRow][1] != 2);
                exitCol = 0;
                break;

            case 3:
                exitRow = 0;
                do exitCol = random.nextInt(COLS - 2 + 1);
                while (gameBoard[1][exitCol] != 2);
                break;
        }

        gameBoard[exitRow][exitCol] = BoardCodes.EXIT;

        do  {
            playerRow = random.nextInt(ROWS-2) + 1;
            playerCol = random.nextInt(COLS-2) + 1;
        }
        while (gameBoard[playerRow][playerCol] != BoardCodes.EMPTY);

        do  {
            zombieRow = random.nextInt(ROWS-2) + 1;
            zombieCol = random.nextInt(COLS-2) + 1;
        }
        while ((gameBoard[zombieRow][zombieCol] != BoardCodes.EMPTY) || (Math.abs(zombieRow - playerRow) < 2) || (Math.abs(zombieCol - playerCol) < 2));

        for (ImageView iv: allGameObjects)
            activityGameRelativeLayout.removeView(iv);
        allGameObjects.clear();

        //TASK 2:  REBUILD THE  BOARD
        buildGameBoard();

        //TASK 3:  ADD THE PLAYERS
        createZombie();
        createPlayer();

        winsTextView.setText(getString(R.string.wins, wins));
        lossesTextView.setText(getString(R.string.losses, losses));
    }

    private void buildGameBoard() {
        // Inflate the entire game board (obstacles and exit)
        // (everything but the player and zombie)
        ImageView viewToInflate;
        // Loop through the board
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                viewToInflate = null;
                if (gameBoard[row][col] == BoardCodes.OBSTACLE) {
                    viewToInflate = (ImageView) layoutInflater.inflate(R.layout.obstacle_layout, null);
                }
                else if (gameBoard[row][col] == BoardCodes.EXIT) {
                    viewToInflate = (ImageView) layoutInflater.inflate(R.layout.exit_layout, null);
                    exitRow = row;
                    exitCol = col;
                }

                if (viewToInflate != null) {
                    viewToInflate.setX(col * square + OFFSET);
                    viewToInflate.setY(row * square + OFFSET);
                    activityGameRelativeLayout.addView(viewToInflate);
                    allGameObjects.add(viewToInflate);
                }
            }
        }
    }

    private void createZombie() {
        // Determine where to place the Zombie (at game start)
        // Then, inflate the zombie layout

        zombieImageView = (ImageView) layoutInflater.inflate(R.layout.zombie_layout, null);
        zombieImageView.setX(zombieCol * square + OFFSET);
        zombieImageView.setY(zombieRow * square + OFFSET);
        activityGameRelativeLayout.addView(zombieImageView);
        allGameObjects.add(zombieImageView);

        zombie = new Zombie();
        zombie.setRow(zombieRow);
        zombie.setCol(zombieCol);
    }

    private void createPlayer() {
        // Determine where to place the Player (at game start)
        // Then, inflate the player layout

        playerImageView = (ImageView) layoutInflater.inflate(R.layout.player_layout, null);
        playerImageView.setX(playerCol * square + OFFSET);
        playerImageView.setY(playerRow * square + OFFSET);
        activityGameRelativeLayout.addView(playerImageView);
        allGameObjects.add(playerImageView);

        player = new Player();
        player.setRow(playerRow);
        player.setCol(playerCol);
    }

    private void movePlayer(float velocityX, float velocityY) {
        // This method gets called by the onFling event
        // Be sure to implement the move method in the Player (model) class

        float absX = Math.abs(velocityX);
        float absY = Math.abs(velocityY);
        String direction = "UNKNOWN";

        // Determine which absolute velocity is greater (x or y)
        // If x is negative, move player left.  Else if x is positive, move player right.
        // If y is negative, move player down.  Else if y is positive, move player up.
        if ((absX > FLING_THRESHOLD) && (absY > FLING_THRESHOLD)) {
            if (absX >= absY) {
                if (velocityX < 0)
                    direction = "LEFT";
                else
                    direction = "RIGHT";
            } else {
                if (velocityY < 0)
                    direction = "UP";
                else
                    direction = "DOWN";
            }
        }

        // Then move the zombie, using the player's row and column position.
        if (!direction.equals("UNKNOWN")) {

            player.move(gameBoard, direction);
            playerImageView.setX(player.getCol() * square + OFFSET);
            playerImageView.setY(player.getRow() * square + OFFSET);

            if (doubleTapCount < 4) {
                zombie.move(gameBoard, player.getCol(), player.getRow());
                zombieImageView.setX(zombie.getCol() * square + OFFSET);
                zombieImageView.setY(zombie.getRow() * square + OFFSET);
            }
            if (doubleTapCount > 0) doubleTapCount--;
        }

        // make 2 decisions
        // 1) Check to see if player has reached the exit row and column (WIN)
        // 2) OR if the Player and Zombies are touching (LOSE)
        if (player.getCol() == exitCol && player.getRow() == exitRow) {
            wins++;
            bunnyImageView = (ImageView) layoutInflater.inflate(R.layout.bunny_layout, null);
            bunnyImageView.setX(zombie.getCol() * square + OFFSET);
            bunnyImageView.setY(zombie.getRow() * square + OFFSET);
            activityGameRelativeLayout.removeView(zombieImageView);
            activityGameRelativeLayout.addView(bunnyImageView);
            allGameObjects.add(bunnyImageView);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startNewGame();
                }
            }, 3000);
        }
        else if (player.getCol() == zombie.getCol() && player.getRow() == zombie.getRow()) {
            losses++;
            bloodImageView = (ImageView) layoutInflater.inflate(R.layout.blood_layout, null);
            bloodImageView.setX(zombie.getCol() * square + OFFSET);
            bloodImageView.setY(zombie.getRow() * square + OFFSET);
            activityGameRelativeLayout.removeView(zombieImageView);
            activityGameRelativeLayout.removeView(playerImageView);
            activityGameRelativeLayout.addView(bloodImageView);
            allGameObjects.add(bloodImageView);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startNewGame();
                }
            }, 2000);
        }
    }

    public void buttonClick(View view) {
        startNewGame();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    /***
     * Responds to double tap event, i.e. two single tap gestures within some duration. Used in this
     * game to "Freeze" the zombie for one move.
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        if (doubleTapCount == 0) doubleTapCount = 5;
        return true;
    }

    /***
     * During a double tap, another event occurs (including move).
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {}

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) { return false; }

    @Override
    public void onLongPress(MotionEvent motionEvent) {}

    /***
     * Upon a fling event a move is triggered.
     * @param motionEvent
     * @param motionEvent1
     * @param v
     * @param v1
     * @return
     */
    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        movePlayer(v, v1);
        return true;
    }
}
