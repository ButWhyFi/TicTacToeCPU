package Summer2022Project;

import java.util.Random;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

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
    static int rawPriorityValues[][][] = new int[3][3][4];
    static int squarePriorities[][] = new int[3][3];
    static int prioritySums[][] = new int[3][3];
    static int coords[] = new int[2];

    static char charGrid[][] = {{'-', '-', '-'}, {'-', '-', '-'}, {'-', '-', '-'}};
    static int tiedChoices[][];

    static boolean gameOver = false;
    static boolean multipleChoices = true;
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


    public void drawX() {
      Line line1 = new Line(1, 1, 198, 199);
      Line line2 = new Line(198, 1, 1, 199);
      Group X = new Group(line1, line2);

      getChildren().add(X);
    }

    public void drawO() {
      Circle O = new Circle(97.5);
      O.setFill(Color.WHITE);
      O.setStroke(Color.BLACK);
      O.setCenterX(100);
      O.setCenterY(100);

      getChildren().add(O);
    }


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


    public void cpuTurn() {
      calculatePriorityValues();
      findHighestPriorityPerSquare();
      findTotalPriority();

      largestValue = findLargest(squarePriorities);
      largestSum = findLargest(prioritySums);
      if (largestValue == 0 && largestSum == 0) {
        identifyEmptySpaces();
      } else {
        identifyValueAndSum();
      }

      if (turn == 2 && cpuSymbol == 'x' && charGrid[1][1] == playerSymbol) {
        int[][] temp = new int[4][2];
        if (threeEmptyCorners(temp)) {
          copy2DArray(tiedChoices, temp);
          findOpposingCorner();
        }
      }

      else if (turn == 3 && detectCornerMethod() && cpuSymbol == 'o') {
        identifyEmptySpaces();
        prioritizeOrthagonals();

      } else {
        prioritizeDiagonals();
      }

      finalizePlacement();
    }

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


    public void scanForWins() {

      char tempCharGrid[] = new char[3];

      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
          tempCharGrid[j] = charGrid[i][j];
        }
        ifThreeConsecutive(tempCharGrid);
      }

      for (int j = 0; j < 3; j++) {
        for (int i = 0; i < 3; i++) {
          tempCharGrid[i] = charGrid[i][j];
        }
        ifThreeConsecutive(tempCharGrid);

      }

      for (int i = 0; i < 3; i++)
        tempCharGrid[i] = charGrid[i][i];
      ifThreeConsecutive(tempCharGrid);


      for (int i = 0, j = 2; i < 3; i++, j--)
        tempCharGrid[i] = charGrid[i][j];
      ifThreeConsecutive(tempCharGrid);
    }


    public void ifThreeConsecutive(char[] temp) {
      if (countToThree(temp, playerSymbol, cpuSymbol) == 3)
        winner = "Player";
      else if (countToThree(temp, cpuSymbol, playerSymbol) == 3)
        winner = "CPU";
    }


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


    public void calculatePriorityValues() {

      fill3DArray(rawPriorityValues);

      for (int x = 0; x < 3; x++) {
        for (int y = 0; y < 3; y++) {

          if (charGrid[x][y] == '-') {
            for (int a = 0; a < 3; a++)
              if (charGrid[a][y] == cpuSymbol) {
                rawPriorityValues[x][y][0]++;
              } else if (charGrid[a][y] == playerSymbol) {
                rawPriorityValues[x][y][0]--;
              }



            for (int b = 0; b < 3; b++) {
              if (charGrid[x][b] == cpuSymbol) {
                rawPriorityValues[x][y][1]++;
              } else if (charGrid[x][b] == playerSymbol) {
                rawPriorityValues[x][y][1]--;
              }
            }


            if (onLeftDiagonal(x, y))
              for (int c = 0; c < 3; c++) {
                if (charGrid[c][c] == cpuSymbol) {
                  rawPriorityValues[x][y][2]++;
                } else if (charGrid[c][c] == playerSymbol) {
                  rawPriorityValues[x][y][2]--;
                }
              }

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

    public boolean onLeftDiagonal(int x, int y) {
      if (x == y)
        return true;
      else
        return false;
    }

    public boolean onRightDiagonal(int x, int y) {
      if (x - y == 2 || y - x == 2)
        return true;
      else if (x == 1 && y == 1)
        return true;
      else
        return false;
    }



    public void findHighestPriorityPerSquare() {

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
          if (max >= Math.abs(min))
            squarePriorities[x][y] = max;
          else if (Math.abs(min) > max)
            squarePriorities[x][y] = min;
          min = 0;
          max = 0;
        }
      }
    }

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


    public int findLargest(int arr[][]) { // For value and sum, squarePriorities and prioritySums
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

    public void finalizePlacement() {
      if (multipleChoices) {
        int random = generateRandInt(tiedChoices.length);
        coords[0] = tiedChoices[random][0];
        coords[1] = tiedChoices[random][1];
      }

      grid[coords[0]][coords[1]].placeMarker(cpuSymbol);
      multipleChoices = true;
    }


    public void cpuOpenning() {
      int random = generateRandInt(3);
      if (random == 0)
        grid[1][1].placeMarker(cpuSymbol);
      else
        randomCornerPlacement(generateRandInt(4));
    }

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

    public boolean cornerMethodPlayable(boolean threeEC) {
      if (charGrid[1][1] == playerSymbol && threeEC) {
        return true;
      }
      return false;
    }

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

    public int adjustToOppositeCoord(int x) {
      if (x == 0)
        x = 2;
      else if (x == 2)
        x = 0;
      return x;
    }



    public void copy2DArray(int newArr[][], int oldArr[][]) {
      for (int i = 0; i < newArr.length; i++) {
        for (int j = 0; j < newArr[i].length; j++) {
          newArr[i][j] = oldArr[i][j];
        }
      }
    }


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