import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;

class FloodItWorld extends World {

  public static int BOARD_SIZE;

  public static int WINDOW_WIDTH = 1000;

  public static int WINDOW_HEIGHT = 700;


  // the number of colors used (maximum of 8)
  int colorsUsed;

  // represents the board
  ArrayList<Cell> board;

  // tracks the number of clicks the player has used
  int numClicks = 0;

  int numClicksAllowed;

  int time = 0;

  FloodItWorld(int size, int colorsUsed) {
    BOARD_SIZE = size;
    this.colorsUsed = colorsUsed;
    this.board = new ArrayList<Cell>(BOARD_SIZE);
    generateBoard();
    this.numClicksAllowed = (int) (BOARD_SIZE * this.colorsUsed * 0.3);
  }

  // convenience constructor for testing
  FloodItWorld(int boardSize) {
    board = new ArrayList<Cell>(2);
    this.numClicksAllowed = 2;
  }

  // Creates the world scene which the user sees and interacts with
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(1000, 700);
    scene.placeImageXY(new FromFileImage("src/background.png"), 500, 350);

    // place each cell on the board
    for (Cell c : board) {
      scene.placeImageXY(c.drawCell(), 80 + 20 * c.x, 80 + 20 * c.y);
    }

    // place the current number of clicks
    scene.placeImageXY(
        new TextImage(Integer.toString(numClicks), 30, FontStyle.BOLD, 
            new Color(20, 63, 107)), WINDOW_WIDTH - 205, 100);

    // plays the / + number of clicks allowed
    scene.placeImageXY(
        new TextImage("/ " + Integer.toString(numClicksAllowed), 30, FontStyle.BOLD,
            new Color(20, 63, 107)), WINDOW_WIDTH - 150, 100);

    // place the FLOOD IT title of the game
    scene.placeImageXY(new TextImage("FLOOD IT", 60, 
        FontStyle.BOLD, new Color(245, 83, 83)), WINDOW_WIDTH / 2, 35);
    //    WorldImage outline = 
    //        new RectangleImage((BOARD_SIZE + 2) * 20,(BOARD_SIZE + 2) * 20,
    //            OutlineMode.SOLID, new Color(42, 64, 99));
    //    scene.placeImageXY(outline, ((BOARD_SIZE + 2) * 10) + 50, ((BOARD_SIZE + 2) * 10) + 50);

    // place the time 
    scene.placeImageXY(new TextImage("Time: " + time / 10 + "s", 35,
        FontStyle.BOLD, new Color(20, 63, 107)), WINDOW_WIDTH - 175, 140);

    // if player lost 
    if (numClicks >= numClicksAllowed
        && (!allFlooded())) {
      time = 0;
      // place YOU LOST text
      scene.placeImageXY(new TextImage("YOU LOSE", 45, 
          FontStyle.BOLD, new Color(245, 83, 83)), WINDOW_WIDTH - 170, 200);
    }

    // if player won 
    else if (numClicks <= numClicksAllowed
        && allFlooded()) {
      time = 0;
      // place YOU WIN text
      scene.placeImageXY(new TextImage("YOU WIN", 45, FontStyle.BOLD, Color.GREEN),
          WINDOW_WIDTH - 170, 200);
    }
    return scene;
  }

  // generates a randomly colored board with cells lined together
  void generateBoard() {
    for (int r = 0; r < FloodItWorld.BOARD_SIZE; r++) {
      for (int c = 0; c < FloodItWorld.BOARD_SIZE; c++) {
        if (r == 0 && c == 0) {
          board.add(new Cell(0, 0, true, this.colorsUsed));
        }
        else {
          board.add(new Cell(r, c, false, this.colorsUsed));
        }
      }
    }
    // link each cell to the right top, bottom, left and right
    for (int i = 0; i < board.size(); i++) {
      Cell currentCell = board.get(i);
      // if this cell is the leftmost of its row
      if (board.get(i).x == 0) {
        currentCell.left = null;
      }
      else {
        currentCell.left = board.get(i - FloodItWorld.BOARD_SIZE);
      }
      // if this cell is the rightmost of its row
      if (board.get(i).x == FloodItWorld.BOARD_SIZE - 1) {
        currentCell.right = null;
      }
      else {
        currentCell.right = board.get(i + FloodItWorld.BOARD_SIZE);
      }
      // if this cell is in the top row
      if (board.get(i).y == 0) {
        currentCell.top = null;
      }
      else {
        currentCell.top = board.get(i - 1);
      }
      // if this cell is in the bottom row
      if (board.get(i).y == FloodItWorld.BOARD_SIZE - 1) {
        currentCell.bottom = null;
      }
      else {
        currentCell.bottom = board.get(i + 1);
      }
    }
  }

  // returns the cell that is clicked
  public Cell clickedCell(Posn pos) {
    Cell cell = null;
    for (Cell c : board) {
      if ((c.x <= ((pos.x - 70) / 20)) && (((pos.x - 70) / 20) <= c.x) 
          && (c.y <= ((pos.y - 70) / 20))
          && (((pos.y - 70) / 20) <= c.y)) {
        cell = c;
      }
    }
    return cell;
  }

  // Floods the first cell in the board
  // EFFECT: Changes the first cell to have the color that has been clicked
  public void updateOnClick(Cell cell) {
    if (cell != null) {
      board.get(0).color = cell.color;
      board.set(0, board.get(0));
    }
  }

  // Handles mouse clicks
  // EFFECT: Changes the world state accordingly to the mouse click.
  public void onMouseClicked(Posn pos) {
    // if the click is within the board
    if ((pos.x >= 80 && pos.x <= (BOARD_SIZE * 20 + 80))
        && (pos.y >= 80 && pos.y <= (BOARD_SIZE * 20 + 80))
        && !(numClicks >= numClicksAllowed || allFlooded())) {
      this.updateOnClick(this.clickedCell(pos));
      numClicks++;
    }
  }

  // Updates the world
  // EFFECT: floods the neighboring cells in the board to have the color
  // of the first cell
  public void updateWorld() {
    Cell c = this.board.get(0);
    Color floodColor = c.color;
    for (int i = 0; i < board.size(); i++) {
      Cell cell = board.get(i);
      if (cell.flooded) {
        cell.color = floodColor;
        cell.floodNeighbors(floodColor);
      }
      makeScene();
    }
  }

  // determines if every cell in the board is flooded
  boolean allFlooded() {
    for (Cell c: board) {
      if (!c.flooded) {
        return false;
      }
    }
    return true;
  }

  // Updates the world at each tick
  public void onTick() {
    // haven't lost yet
    if (!(numClicks <= numClicksAllowed
        && allFlooded())) {
      time++;
    } 
    updateWorld();
  }

  // Resets the game and creates a new board when the player presses r.
  // EFFECT: clears the board of cells and re-generates cells
  public void onKeyEvent(String k) {
    if (k.equals("r")) {
      this.board.clear();
      this.numClicks = 0;
      this.time = 0;
      generateBoard();
    }
  }

  // Starts the game with the given player restrictions
  // or throws an exception if too many colors wanted
  public void startGame(int size, int numColors) {
    if (numColors <= 8) {
      BOARD_SIZE = size;
      this.colorsUsed = numColors;
      FloodItWorld world = new FloodItWorld(size, numColors);
      world.bigBang(WINDOW_WIDTH, WINDOW_HEIGHT, 0.1);
    }
    else {
      throw new IllegalArgumentException("Too many colors"); 
    }
  }
}

class ExamplesFloodItWorld {

  FloodItWorld exampleWorld;
  ArrayList<Cell> exampleBoard;
  FloodItWorld exampleWorldTest;
  Cell blueCell;
  Cell redCell;
  Cell yellowCell;
  Cell orangeCell;

  void initWorld() {
    exampleWorld = new FloodItWorld(22, 8);

    exampleWorldTest = new FloodItWorld(2);
    blueCell = new Cell(0, 0, Color.BLUE, true, null, null, null, null);
    redCell = new Cell(1, 0, Color.RED, true, null, null, null, null);
    yellowCell = new Cell(0, 1, Color.YELLOW, true, null, null, null, null);
    orangeCell = new Cell(1, 1, Color.ORANGE, true, null, null, null, null);
    blueCell.right = redCell;
    blueCell.bottom = yellowCell;
    redCell.left = blueCell;
    redCell.bottom = orangeCell;
    yellowCell.top = blueCell;
    yellowCell.right = orangeCell;
    orangeCell.top = redCell;
    orangeCell.left = yellowCell;
    exampleWorldTest.board.add(blueCell);
    exampleWorldTest.board.add(yellowCell);
    exampleWorldTest.board.add(redCell);
    exampleWorldTest.board.add(orangeCell);
  }

  void testGenerateBoard(Tester t) {
    initWorld();

    // loop through each tile in the board
    for (Cell c : exampleWorld.board) {

      t.checkExpect(c.colors.contains(c.color), true);

      // if this cell is not the very first cell
      if (!(c.x == 0 && c.y == 0)) {
        // this cell should not be flooded
        t.checkExpect(c.flooded, false);
      }

      // if this tile is leftmost of its row
      if (c.x == 0) {
        // make sure that there is no tile to its left
        t.checkExpect(c.left, null);
        // make sure there is something to its right
        t.checkExpect(c.right == null, false);
      }
      // if this tile is on the top row
      if (c.y == 0) {
        // make sure no tile above
        t.checkExpect(c.top, null);
        // make sure tile below exists
        t.checkExpect(c.bottom == null, false);
      }
      // if the tile is on the bottom row
      if (c.y == FloodItWorld.BOARD_SIZE - 1) {
        // there should be no tile below
        t.checkExpect(c.bottom, null);
        // there should be a tile above
        t.checkExpect(c.top == null, false);
      }
      // if the tile is the rightmost of its row
      if (c.x == FloodItWorld.BOARD_SIZE - 1) {
        // there should be no tile below
        t.checkExpect(c.right, null);
        // there should be a tile above
        t.checkExpect(c.left == null, false);
      }

    }
    // first cell should always be flooded
    t.checkExpect(exampleWorld.board.get(0).flooded, true);
  }

  void testMakeScene(Tester t) {
    initWorld();
    // create scene in which a 2x2 board is generated over the background
    // with the given blue, red, yellow, and orange cells
    WorldScene exampleScene = new WorldScene(1000, 700);
    exampleScene.placeImageXY(new FromFileImage("src/background.png"), 500, 350);
    exampleScene.placeImageXY(blueCell.drawCell(), 80, 80);
    exampleScene.placeImageXY(yellowCell.drawCell(), 80, 100);
    exampleScene.placeImageXY(redCell.drawCell(), 100, 80);
    exampleScene.placeImageXY(orangeCell.drawCell(), 100, 100);
    exampleScene.placeImageXY(
        new TextImage(Integer.toString(0), 30, FontStyle.BOLD, new Color(20, 63, 107)), 1000 - 205,
        100);
    exampleScene.placeImageXY(
        new TextImage("/ " + Integer.toString(2), 30, FontStyle.BOLD, new Color(20, 63, 107)),
        1000 - 150, 100);
    // place the FLOOD IT title of the game
    exampleScene.placeImageXY(new TextImage("FLOOD IT", 60, FontStyle.BOLD, new Color(245, 83, 83)),
        1000 / 2, 35);
    // place the time
    exampleScene.placeImageXY(
        new TextImage("Time: " + 0 / 10 + "s", 35, FontStyle.BOLD, new Color(20, 63, 107)),
        1000 - 175, 140);
    // place YOU WIN text
    exampleScene.placeImageXY(new TextImage("YOU WIN", 45, FontStyle.BOLD, Color.GREEN),
        1000 - 170, 200);
    // call makeScene on an example world and determine if it equals
    // the scene we created above
    t.checkExpect(exampleWorldTest.makeScene(), exampleScene);
    WorldScene exampleDifferentScene = new WorldScene(1500, 1000);
    // ensure that calling makeScene on an example word does not equals
    // an empty Scene
    t.checkExpect(exampleWorldTest.makeScene() == exampleDifferentScene, false);
  }

  void testClickedCell(Tester t) {
    initWorld();
    t.checkExpect(exampleWorldTest.clickedCell(new Posn(FloodItWorld.WINDOW_WIDTH, 
        FloodItWorld.WINDOW_HEIGHT)), null);
    t.checkExpect(exampleWorldTest.clickedCell(new Posn(FloodItWorld.WINDOW_WIDTH - 250, 
        FloodItWorld.WINDOW_HEIGHT - 250)), null);
    t.checkExpect(exampleWorldTest.clickedCell(new Posn(80, 
        80)), blueCell);
    t.checkExpect(exampleWorldTest.clickedCell(new Posn(100, 
        80)), redCell);
    t.checkExpect(exampleWorldTest.clickedCell(new Posn(80, 
        100)), yellowCell);
    t.checkExpect(exampleWorldTest.clickedCell(new Posn(100, 
        100)), orangeCell);
  }

  void testUpdateOnClick(Tester t) {
    initWorld();
    t.checkExpect(exampleWorldTest.board, 
        new ArrayList<Cell>(Arrays.asList(blueCell, yellowCell, redCell, orangeCell)));
    exampleWorldTest.updateOnClick(null);
    t.checkExpect(exampleWorldTest.board, 
        new ArrayList<Cell>(Arrays.asList(blueCell, yellowCell, redCell, orangeCell)));
    exampleWorldTest.updateOnClick(blueCell);
    t.checkExpect(exampleWorldTest.board, 
        new ArrayList<Cell>(Arrays.asList(blueCell, yellowCell, redCell, orangeCell)));
    exampleWorldTest.updateOnClick(yellowCell);
    Cell changedCell = new Cell(blueCell.x, blueCell.y, Color.YELLOW, blueCell.flooded,
        blueCell.left, blueCell.top, blueCell.right,  blueCell.bottom);
    t.checkExpect(exampleWorldTest.board, 
        new ArrayList<Cell>(Arrays.asList(changedCell, yellowCell, redCell, orangeCell)));
    exampleWorldTest.updateOnClick(redCell);
    changedCell.color = redCell.color;
    t.checkExpect(exampleWorldTest.board, 
        new ArrayList<Cell>(Arrays.asList(changedCell, yellowCell, redCell, orangeCell)));
    exampleWorldTest.updateOnClick(orangeCell);
    changedCell.color = orangeCell.color;
    t.checkExpect(exampleWorldTest.board, 
        new ArrayList<Cell>(Arrays.asList(changedCell, yellowCell, redCell, orangeCell)));
  }

  void testOnMouseClicked(Tester t) {
    initWorld();
    this.orangeCell.flooded = false;
    this.redCell.flooded = false;
    this.yellowCell.flooded = false;
    t.checkExpect(exampleWorldTest.allFlooded(), false);
    t.checkExpect(exampleWorldTest.numClicks, 0);
    t.checkExpect(exampleWorldTest.numClicksAllowed, 2);
    t.checkExpect(exampleWorldTest.board.get(0), blueCell);
    exampleWorldTest.onMouseClicked(new Posn(50, 90));
    t.checkExpect(exampleWorldTest.numClicks, 0);
    t.checkExpect(exampleWorldTest.board.get(0), blueCell);
    exampleWorldTest.onMouseClicked(new Posn(90, 50));
    t.checkExpect(exampleWorldTest.numClicks, 0);
    t.checkExpect(exampleWorldTest.board.get(0), blueCell);
    exampleWorldTest.onMouseClicked(new Posn(90, 90));
    t.checkExpect(exampleWorldTest.numClicks, 1);
    t.checkExpect(exampleWorldTest.board.get(0), 
        new Cell(blueCell.x, blueCell.y, Color.ORANGE, blueCell.flooded,
            blueCell.left, blueCell.top, blueCell.right,  blueCell.bottom));
    this.orangeCell.flooded = true;
    this.redCell.flooded = true;
    this.yellowCell.flooded = true;
    exampleWorldTest.onMouseClicked(new Posn(100, 100));
    t.checkExpect(exampleWorldTest.numClicks, 1);
    this.orangeCell.flooded = false;
    exampleWorldTest.onMouseClicked(new Posn(95, 95));
    t.checkExpect(exampleWorldTest.numClicks, 2);
    exampleWorldTest.onMouseClicked(new Posn(92, 97));
    t.checkExpect(exampleWorldTest.numClicks, 2);
  }

  void testUpdateWorld(Tester t) {
    initWorld();
    this.blueCell.flooded = false;
    this.redCell.flooded = false;
    this.orangeCell.flooded = false;
    this.yellowCell.flooded = false;
    t.checkExpect(blueCell.color, Color.BLUE);
    t.checkExpect(redCell.color, Color.RED);
    t.checkExpect(orangeCell.color, Color.ORANGE);
    t.checkExpect(yellowCell.color, Color.YELLOW);
    exampleWorldTest.updateWorld();
    t.checkExpect(blueCell.color, Color.BLUE);
    t.checkExpect(redCell.color, Color.RED);
    t.checkExpect(orangeCell.color, Color.ORANGE);
    t.checkExpect(yellowCell.color, Color.YELLOW);
    this.redCell.flooded = true;
    exampleWorldTest.updateWorld();
    t.checkExpect(blueCell.color, Color.BLUE);
    t.checkExpect(redCell.color, Color.BLUE);
    t.checkExpect(orangeCell.color, Color.ORANGE);
    t.checkExpect(yellowCell.color, Color.YELLOW);
    this.orangeCell.flooded = true;
    exampleWorldTest.updateWorld();
    t.checkExpect(blueCell.color, Color.BLUE);
    t.checkExpect(redCell.color, Color.BLUE);
    t.checkExpect(orangeCell.color, Color.BLUE);
    t.checkExpect(yellowCell.color, Color.YELLOW);
    this.yellowCell.flooded = true;
    exampleWorldTest.updateWorld();
    t.checkExpect(blueCell.color, Color.BLUE);
    t.checkExpect(redCell.color, Color.BLUE);
    t.checkExpect(orangeCell.color, Color.BLUE);
    t.checkExpect(yellowCell.color, Color.BLUE);
  }

  void testAllFlooded(Tester t) {
    initWorld();
    t.checkExpect(exampleWorldTest.allFlooded(), true);
    this.blueCell.flooded = false;
    t.checkExpect(exampleWorldTest.allFlooded(), false);
    this.redCell.flooded = false;
    t.checkExpect(exampleWorldTest.allFlooded(), false);
    this.blueCell.flooded = true;
    this.redCell.flooded = true;
    t.checkExpect(exampleWorldTest.allFlooded(), true);
  }

  void testOnKeyEvent(Tester t) {
    initWorld();
    t.checkExpect(exampleWorldTest.board, 
        new ArrayList<Cell>(Arrays.asList(blueCell, yellowCell, redCell, orangeCell)));
    t.checkExpect(exampleWorld.numClicks, 0);
    t.checkExpect(exampleWorld.time, 0);
    exampleWorld.numClicks++;
    exampleWorld.time = 50;
    t.checkExpect(exampleWorld.numClicks, 1);
    t.checkExpect(exampleWorld.time, 50);
    exampleWorld.onKeyEvent("j");
    t.checkExpect(exampleWorldTest.board, 
        new ArrayList<Cell>(Arrays.asList(blueCell, yellowCell, redCell, orangeCell)));
    t.checkExpect(exampleWorld.numClicks, 1);
    t.checkExpect(exampleWorld.time, 50);
    exampleWorld.onKeyEvent("r");
    t.checkExpect(exampleWorldTest.board, 
        new ArrayList<Cell>(Arrays.asList(blueCell, yellowCell, redCell, orangeCell)));
    t.checkExpect(exampleWorld.numClicks, 0);
    t.checkExpect(exampleWorld.time, 0);
  }

  void testStartGame(Tester t) {
    initWorld();
    t.checkExpect(FloodItWorld.BOARD_SIZE, 22);
    t.checkExpect(exampleWorld.colorsUsed, 8);
    exampleWorld.startGame(3, 4);
    t.checkExpect(FloodItWorld.BOARD_SIZE, 3);
    t.checkExpect(exampleWorld.colorsUsed, 4);
  }

  void testGame(Tester t) {
    FloodItWorld game = new FloodItWorld(22, 8);
    game.startGame(22, 8);
  }
}
