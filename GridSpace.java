package Summer2022Project;

import java.util.Random;
import javafx.scene.Group;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;


/**
 * GridSpace Class sets up the space within the window GridSquares Class manages each square
 */

public class GridSpace extends GridPane {

  Text header = new Text();

  MenuItem p1MenuItem = new MenuItem("As Player 1");
  MenuItem p2MenuItem = new MenuItem("As Player 2");
  MenuButton newGameMenuBtn = new MenuButton("New Game", null, p1MenuItem, p2MenuItem);

  GridSquares[][] grid;
  int gameMode;
  boolean userPlayer1 = true;


  public GridSpace(int side) {

    grid = new GridSquares[3][3];

    for (int i = 0; i < grid.length; i++) {
      for (int j = 0; j < grid[i].length; j++) {
        grid[i][j] = new GridSquares(side);
        this.add(grid[i][j], side * i, side * j);
      }
    }
    this.setStyle("-fx-padding: 10 10 0 10;");
  }


  public class GridSquares extends Pane {

    static int rawPriorityValues[][][] = new int[3][3][4]; // [row of square][column of
                                                           // square][values of checking through the
                                                           // 4 directions (row, column, and both
                                                           // diagonals) corresponding to each
                                                           // location on the square]

    static int squarePriorities[][] = new int[3][3]; // Most significant rawPriorityValue of each
                                                     // square

    static int prioritySums[][] = new int[3][3]; // Sum of rawPriorityValue in each square
    static int coords[] = new int[2]; // Coords of the best/tied for best move

    static char charGrid[][] = {{'-', '-', '-'}, {'-', '-', '-'}, {'-', '-', '-'}};


    static int tiedChoices[][]; // Indeces of squares that are viewed as equally important
                                // squares/good moves.
                                // This array changes as methods inside cpuTurn
                                // runs until multipleChoices is set to false.

    static boolean multipleChoices = true; // A boolean that is only set to false when there is only
                                           // one square that is considered the best move. Upon
                                           // setting it to false, methods that check for which
                                           // square is disabled until it is reset to true on the
                                           // next turn.

    static boolean gameOver = false;
    static String winner = "Undetermined";
    static int largestValue;
    static int largestSum;
    static int turn = 0;
    static char playerSymbol = 'x';
    static char cpuSymbol = 'o';

    boolean marked = false;
    boolean p1Square;
    boolean p2Square;

    Group X;
    Circle O;

    public GridSquares(int side) {
      setPrefSize(side, side);
      setStyle("-fx-border-color: black;-fx-background-color: white;");


      setOnMouseClicked(e -> click());
      p1MenuItem.setOnAction(e -> newGameAs('x'));
      p2MenuItem.setOnAction(e -> newGameAs('o'));
    }

    public void newGameAs(char c) {
      reset();
      if (c == 'x') {
        userPlayer1 = true;
        playerSymbol = 'x';
        cpuSymbol = 'o';
      } else if (c == 'o') {
        userPlayer1 = false;
        playerSymbol = 'o';
        cpuSymbol = 'x';
        cpuOpenning();
      }
    }

    /*
     * Draws an X on whatever object invokes this method
     */
    public void drawX() {
      Line line1 = new Line(1, 1, 198, 199);
      Line line2 = new Line(198, 1, 1, 199);
      Group X = new Group(line1, line2);

      getChildren().add(X);
    }

    /*
     * Draws an O on whatever object invokes this method
     */
    public void drawO() {
      Circle O = new Circle(97.5);
      O.setFill(Color.WHITE);
      O.setStroke(Color.BLACK);
      O.setCenterX(100);
      O.setCenterY(100);

      getChildren().add(O);
    }

    /*
     * Method calls and if statements that determine what happens every time the user clicks on any
     * square.
     */
    public void click() {
      if (!gameOver) {
        if (!marked) {
          placeMarker(playerSymbol);
          currentStatusCheck();
          if (!gameOver) {
            currentStatusCheck();
            cpuTurn();
          }
        }
        currentStatusCheck();
      }
    }

    /*
     * Draws the appropriate symbol and sets the corresponding variables depending on the parameter
     * 
     * @param c: The symbol that is to be drawn
     */
    public void placeMarker(char c) {
      if (c == 'x') {
        drawX();
        p1Square = true;
      } else if (c == 'o') {
        drawO();
        p2Square = true;
      }
      marked = true;
      turn++;
    }

    /*
     * A series of methods that check to see if the game is over or not
     */
    public void currentStatusCheck() {
      gridToCharGrid();
      scanForWins();
      tieCheck();
      endOfGameConversion();
    }


    public void tieCheck() {
      if (winner.equals("Undetermined") && turn == 9)
        winner = "Neither";
    }

    /*
     * A series of mostly method calls that plays out the cpu's turn
     */
    public void cpuTurn() {
      calculatePriorityValues();
      findHighestPriorityPerSquare();
      findTotalPriority();

      largestValue = findLargest(squarePriorities);
      largestSum = findLargest(prioritySums);
      if (largestValue == 0 && largestSum == 0) { // if there is no priority on anything
        identifyEmptySpaces();
      } else { // Normal circumstances
        identifyValueAndSum();
      }

      // Hardcoding the CPU to play the corner method
      if (turn == 2 && cpuSymbol == 'x' && charGrid[1][1] == playerSymbol) {
        int[][] temp = new int[4][2];
        if (threeEmptyCorners(temp)) {
          copy2DArray(tiedChoices, temp);
          findOpposingCorner();
        }
      }
      // Hardcoding the CPU to detect corner method
      else if (turn == 3 && detectCornerMethod() && cpuSymbol == 'o') {
        identifyEmptySpaces();
        prioritizeOrthagonals();

      } else { // Normal circumstances
        prioritizeDiagonals();
      }

      finalizePlacement();
    }

    /*
     * Converts the p1Square and p2Square variables into an easier to use grid of "x", "o", and "-"
     */
    public void gridToCharGrid() {
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
          if (grid[i][j].p1Square)
            charGrid[i][j] = 'x';
          else if (grid[i][j].p2Square)
            charGrid[i][j] = 'o';
        }
      }
    }

    /*
     * Goes through each column, row, and diagonals of charGrid and storing the information in a
     * temporary array.
     */
    public void scanForWins() {

      char tempCharGrid[] = new char[3];

      // for rows
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
          tempCharGrid[j] = charGrid[i][j];
        }
        ifThreeConsecutive(tempCharGrid);
      }

      // for columns
      for (int j = 0; j < 3; j++) {
        for (int i = 0; i < 3; i++) {
          tempCharGrid[i] = charGrid[i][j];
        }
        ifThreeConsecutive(tempCharGrid);

      }

      // for first diagonal (\)
      for (int i = 0; i < 3; i++)
        tempCharGrid[i] = charGrid[i][i];
      ifThreeConsecutive(tempCharGrid);

      // for second diagonal (/)
      for (int i = 0, j = 2; i < 3; i++, j--)
        tempCharGrid[i] = charGrid[i][j];
      ifThreeConsecutive(tempCharGrid);
    }

    /*
     * Increments count if it is equal to a, and exits out of method it is equal to b
     * 
     * @param temp: An array consisting of three elements that act as a temporary array representing
     * a single row/column/diagonal
     * 
     * @param a: The character that count will increment based on
     * 
     * @param b: The character that exits the method while returning false
     * 
     * @return count: An integer representing how many char a's are in the temporary array
     * 
     */
    public int countToThree(char[] temp, char a, char b) {

      int count = 0;

      for (int i = 0; i < 3; i++) {
        if (temp[i] == b)
          return 0;
        else if (temp[i] == a) {
          count++;
        }
      }
      return count;
    }

    public void ifThreeConsecutive(char[] temp) {
      if (countToThree(temp, playerSymbol, cpuSymbol) == 3)
        winner = "Player";
      else if (countToThree(temp, cpuSymbol, playerSymbol) == 3)
        winner = "CPU";
    }

    public void endOfGameConversion() {
      gameOver = true;
      if (winner.equals("Player")) {
        header.setText("You Win!");
      } else if (winner.equals("CPU")) {
        header.setText("You Lose!");
      } else if (winner.equals("Neither")) {
        header.setText("Tie Game!");
      } else {
        gameOver = false;
      }
    }

    /*
     * Goes through each row, column, and diagonal and increments/decrements each value of each
     * element in rawPriorityValue[][][].
     */
    public void calculatePriorityValues() {

      fill3DArray(rawPriorityValues); // Fills the array with 0's

      for (int x = 0; x < 3; x++) {
        for (int y = 0; y < 3; y++) {

          // for rows
          if (charGrid[x][y] == '-') {
            for (int a = 0; a < 3; a++)
              if (charGrid[a][y] == cpuSymbol) {
                rawPriorityValues[x][y][0]++;
              } else if (charGrid[a][y] == playerSymbol) {
                rawPriorityValues[x][y][0]--;
              }

            // for columns
            for (int b = 0; b < 3; b++) {
              if (charGrid[x][b] == cpuSymbol) {
                rawPriorityValues[x][y][1]++;
              } else if (charGrid[x][b] == playerSymbol) {
                rawPriorityValues[x][y][1]--;
              }
            }

            // for \ diagonal
            if (onLeftDiagonal(x, y))
              for (int c = 0; c < 3; c++) {
                if (charGrid[c][c] == cpuSymbol) {
                  rawPriorityValues[x][y][2]++;
                } else if (charGrid[c][c] == playerSymbol) {
                  rawPriorityValues[x][y][2]--;
                }
              }
            // for / diagonal
            if (onRightDiagonal(x, y))
              for (int d = 0, e = 2; d < 3; d++, e--) {
                if (charGrid[d][e] == cpuSymbol) {
                  rawPriorityValues[x][y][3]++;
                } else if (charGrid[d][e] == playerSymbol) {
                  rawPriorityValues[x][y][3]--;
                }
              }
          }
        }
      }
    }

    public void fill3DArray(int[][][] arr) {
      for (int x = 0; x < arr.length; x++) {
        for (int y = 0; y < arr[x].length; y++) {
          for (int z = 0; z < arr[x][y].length; z++) {
            arr[x][y][z] = 0;
          }
        }
      }
    }

    public void fill2DArray(int[][] arr) {
      for (int i = 0; i < arr.length; i++) {
        for (int j = 0; j < arr[i].length; j++) {
          arr[i][j] = 0;
        }
      }
    }

    /*
     * Returns a boolean to indicate if the coordinates are on the \ diagonal
     * 
     * @param x: X coordinate
     * 
     * @param y: Y coordinate
     */
    public boolean onLeftDiagonal(int x, int y) {
      if (x == y)
        return true;
      else
        return false;
    }

    /*
     * Returns a boolean to indicate if the coordinates are on the / diagonal
     * 
     * @param x: X coordinate
     * 
     * @param y: Y coordinate
     */
    public boolean onRightDiagonal(int x, int y) {
      if (x - y == 2 || y - x == 2)
        return true;
      else if (x == 1 && y == 1)
        return true;
      else
        return false;
    }

    /*
     * Runs through each element of rawPriorityValues[][][] and checks to see which is the most
     * significant value of each square to put into squarePriorities[][]
     */
    public void findHighestPriorityPerSquare() {

      // Counts negatives and positives values seperately
      int max = 0;
      int min = 0;

      for (int x = 0; x < rawPriorityValues.length; x++) {
        for (int y = 0; y < rawPriorityValues[x].length; y++) {
          for (int z = 0; z < rawPriorityValues[x][y].length; z++) {
            if (rawPriorityValues[x][y][z] >= max)
              max = rawPriorityValues[x][y][z];
            if (rawPriorityValues[x][y][z] <= min)
              min = rawPriorityValues[x][y][z];
          }
          if (max >= Math.abs(min)) // Example: max of 2 > min of -2
            squarePriorities[x][y] = max;
          else if (Math.abs(min) > max) // Example: max of 1 < min of -2
            squarePriorities[x][y] = min;
          // Resets min and max to go through the loop again
          min = 0;
          max = 0;
        }
      }
    }

    /*
     * Runs through each element of rawPriorityValues[][][] and sums the elements at each square and
     * puts it into prioritySums[][]
     */
    public void findTotalPriority() {
      fill2DArray(prioritySums);
      for (int i = 0; i < rawPriorityValues.length; i++) {
        for (int j = 0; j < rawPriorityValues[i].length; j++) {
          for (int k = 0; k < rawPriorityValues[i][j].length; k++) {
            prioritySums[i][j] += Math.abs(rawPriorityValues[i][j][k]);
          }
        }
      }
    }

    /*
     * Goes through an array and finds the largest/most significant value
     * 
     * @return x: The largest absolute value. If absolute value is tied, positive > negative
     */
    public int findLargest(int arr[][]) {
      int x = 0;
      if (multipleChoices) {
        for (int i = 0; i < 3; i++) {
          for (int j = 0; j < 3; j++) {
            if (Math.abs(arr[i][j]) > Math.abs(x)) {
              x = arr[i][j];
              coords[0] = i;
              coords[1] = j;
              multipleChoices = false;
            } else if (Math.abs(arr[i][j]) == Math.abs(x) && arr[i][j] > x) {
              x = arr[i][j];
              coords[0] = i;
              coords[1] = j;
              multipleChoices = false;
            } else if (x != 0 && x == arr[i][j]) {
              multipleChoices = true;
            }
          }
        }
      }
      return x;
    }

    /*
     * Identify empty spaces marked by "-" in charGrid and puts the coordinates of each empty space
     * into a newly created tiedChoices[x][2] array with x being the exact amount of indeces for
     * each empty space
     */
    public void identifyEmptySpaces() {
      int x = 0;
      int[][] temp = new int[9][2];
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
          if (charGrid[i][j] == '-') {
            temp[x][0] = i;
            temp[x][1] = j;
            x++;
          }
        }
      }
      tiedChoices = new int[x][2];
      copy2DArray(tiedChoices, temp);
    }

    /*
     * Identify each index with squarePriorities and prioritySums equal to largestValue and
     * largestSum respectively. Then, it puts said index into a new tiedChoices with the exact
     * number of indeces as needed
     */
    public void identifyValueAndSum() {
      if (multipleChoices) {
        int x = 0;
        int[][] temp = new int[9][2];
        for (int i = 0; i < 3; i++) {
          for (int j = 0; j < 3; j++) {
            if (squarePriorities[i][j] == largestValue && prioritySums[i][j] == largestSum) {
              temp[x][0] = i;
              temp[x][1] = j;
              x++;
            }
          }
        }
        tiedChoices = new int[x][2];
        copy2DArray(tiedChoices, temp);
      }
    }

    /*
     * Iterates over tiedChoices[][] and stores all the indeces of squares that are on diagonals to
     * put into a new tiedChoices[][] array with the necessary index. If one of the indeces in
     * tiedChoices represent the center square, it takes priority over the corners.
     */
    public void prioritizeDiagonals() {

      if (multipleChoices) {

        int[][] temp = new int[tiedChoices.length][2];
        int x = 0;
        boolean isCenter = false;

        for (int i = 0; i < tiedChoices.length; i++) {
          if (onLeftDiagonal(tiedChoices[i][0], tiedChoices[i][1])
              && onRightDiagonal(tiedChoices[i][0], tiedChoices[i][1])) {
            coords[0] = 1;
            coords[1] = 1;
            isCenter = true;
            break;
          } else if (onLeftDiagonal(tiedChoices[i][0], tiedChoices[i][1])
              || onRightDiagonal(tiedChoices[i][0], tiedChoices[i][1])) {
            temp[x][0] = tiedChoices[i][0];
            temp[x][1] = tiedChoices[i][1];
            x++;
          }
        }
        if (isCenter) {
          multipleChoices = false;
        } else if (x > 1) {
          tiedChoices = new int[x][2];
          copy2DArray(tiedChoices, temp);
          multipleChoices = true;
        } else if (x == 1) {
          coords[0] = temp[0][0];
          coords[1] = temp[0][1];
          multipleChoices = false;
        }
      }
    }

    /*
     * Same as prioritizeDiagonals but for orthagonals (cardinal directions, +)
     */
    public void prioritizeOrthagonals() {

      int[][] temp = new int[tiedChoices.length][2];
      int x = 0;

      for (int i = 0; i < tiedChoices.length; i++) {
        if (onOrthagonal(tiedChoices[i][0], tiedChoices[i][1])) {
          temp[x][0] = tiedChoices[i][0];
          temp[x][1] = tiedChoices[i][1];
          x++;
        }
      }
      tiedChoices = new int[x][2];
      copy2DArray(tiedChoices, temp);
      multipleChoices = true;
    }

    /*
     * Returns a boolean based on whether or not the indeces are equal to those on any of the
     * orthagonal squares
     */
    public boolean onOrthagonal(int x, int y) {
      if (x == 1 || y == 1)
        return true;
      else
        return false;
    }

    public int generateRandInt(int x) {
      Random rand = new Random();
      return rand.nextInt(x);
    }

    /*
     * Determines where the CPU will place its marker. If multipleChoices is true, it will randomize
     * and choose a random element in tiedChoices, else it uses the coordinates in coords.
     */
    public void finalizePlacement() {
      if (multipleChoices) {
        int random = generateRandInt(tiedChoices.length);
        coords[0] = tiedChoices[random][0];
        coords[1] = tiedChoices[random][1];
      }

      grid[coords[0]][coords[1]].placeMarker(cpuSymbol);
      multipleChoices = true;
    }

    /*
     * Adds variety to the cpu's opening move by using rng
     */
    public void cpuOpenning() {
      int random = generateRandInt(3);
      if (random == 0)
        grid[1][1].placeMarker(cpuSymbol);
      else
        randomCornerPlacement(generateRandInt(4));
    }

    /*
     * If/else statements to convert the random number into one of the options
     * 
     * @param x: randomly generated number
     */
    public void randomCornerPlacement(int x) {
      if (x == 0)
        grid[0][0].placeMarker(cpuSymbol);
      else if (x == 1)
        grid[2][0].placeMarker(cpuSymbol);
      else if (x == 2)
        grid[0][2].placeMarker(cpuSymbol);
      else if (x == 3)
        grid[2][2].placeMarker(cpuSymbol);
    }

    /*
     * Checks to see if the user is playing the corner method against the CPU by looking at the
     * current state of the grid. If player placed two markers in opposite corners and the cpu has
     * placed one marker in the middle, then it is true.
     */
    public boolean detectCornerMethod() {
      if (charGrid[0][0] == playerSymbol && charGrid[1][1] == cpuSymbol
          && charGrid[2][2] == playerSymbol)
        return true;
      else if (charGrid[0][2] == playerSymbol && charGrid[1][1] == cpuSymbol
          && charGrid[2][0] == playerSymbol)
        return true;
      else
        return false;
    }

    /*
     * Returns a boolean based on whether or not the corner method is playable by the CPU
     * 
     * @param threeEC: threeEmptyCorners()
     */
    public boolean cornerMethodPlayable(boolean threeEC) {
      if (charGrid[1][1] == playerSymbol && threeEC) {
        return true;
      }
      return false;
    }

    /*
     * Returns true if there are three empty corners
     * 
     * @param arr: the array containing the indeces of the squares
     */
    public boolean threeEmptyCorners(int arr[][]) {
      int[][] corners = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
      int x = 0;

      for (int i = 0; i < corners.length; i++) {
        if (!grid[corners[i][0]][corners[i][1]].marked) {
          arr[x][0] = corners[i][0];
          arr[x][1] = corners[i][1];
          x++;
        }
      }
      if (x == 3)
        return true;
      else
        return false;
    }

    /*
     * Finds the coordinates of the opposite corner to play the corner method
     */
    public void findOpposingCorner() {
      int x;
      int y;
      int[][] corners = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
      for (int i = 0; i < corners.length; i++) {
        if (grid[corners[i][0]][corners[i][1]].marked) {
          x = adjustToOppositeCoord(corners[i][0]);
          y = adjustToOppositeCoord(corners[i][1]);
          coords[0] = x;
          coords[1] = y;
          multipleChoices = false;
        }
      }
    }

    /*
     * Adjusts the index to match the coordinates of the opposing empty corner
     * 
     * @param x: An index
     */
    public int adjustToOppositeCoord(int x) {
      if (x == 0)
        x = 2;
      else if (x == 2)
        x = 0;
      return x;
    }

    /*
     * Copies everything from oldArr into newArr until newArr is full
     */
    public void copy2DArray(int newArr[][], int oldArr[][]) {
      for (int i = 0; i < newArr.length; i++) {
        for (int j = 0; j < newArr[i].length; j++) {
          newArr[i][j] = oldArr[i][j];
        }
      }
    }

    /*
     * Contains method calls to reset variables and the grid back to its original states
     */
    public void reset() {
      resetStaticBooleans();
      reset3DArray(rawPriorityValues);
      boardReset();
      clearNumGrid();
      header.setText("");
    }


    public void resetStaticBooleans() {
      gameOver = false;
      winner = "Undetermined";
      multipleChoices = true;
      turn = 0;
    }

    public void reset3DArray(int arr[][][]) {
      for (int i = 0; i < arr.length; i++) {
        for (int j = 0; j < arr[i].length; j++) {
          for (int k = 0; k < arr[i][j].length; k++) {
            arr[i][j][k] = 0;
          }
        }
      }
    }

    public void clearNumGrid() {
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++)
          charGrid[i][j] = '-';
      }
    }

    public void boardReset() {
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
          grid[i][j].getChildren().clear();
          grid[i][j].resetNonStaticBooleans();
        }
      }
    }

    public void resetNonStaticBooleans() {
      marked = false;
      p1Square = false;
      p2Square = false;
    }

    public void fillIntArray(int arr[]) {
      for (int i = 0; i < arr.length; i++) {
        arr[i] = 0;
      }
    }
  }
}
