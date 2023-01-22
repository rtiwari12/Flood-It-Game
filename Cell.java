import java.util.ArrayList;
import tester.*;
import java.awt.Color;
import javalib.worldimages.*;
//import javalib.impworld.*;
//import javalib.worldcanvas.WorldCanvas;

// Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;
  ArrayList<Color> colors;
  Posn pos;

  Cell(int x, int y, Color color, boolean flooded, Cell left, Cell top, Cell right, Cell bottom) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
    this.pos = new Posn(this.x, this.y);
  }

  Cell(int x, int y, boolean flooded, int colorNumber) {
    initializeColors();
    // randomly assigns the color from our list of colors
    this.color = colors.get((int) (Math.random() * colorNumber));
    this.x = x;
    this.y = y;
    this.flooded = flooded;
  }

  // convenience constructor for testing
  Cell(int x, int y, Color color) {
    initializeColors();
    // randomly assigns the color from our list of colors
    this.color = color;
    this.x = x;
    this.y = y;
  }

  // convenience constructor for testing purposes
  Cell() {
    this (0, 0, null, false, null, null, null, null);
  }


  // draws this cell on the given scene
  WorldImage drawCell() {
    return new RectangleImage(20, 20, OutlineMode.SOLID, this.color);
  }

  // initializes the list of all 8 colors
  void initializeColors() {
    colors = new ArrayList<Color>();
    colors.add(Color.RED);
    colors.add(Color.GREEN);
    colors.add(Color.PINK);
    colors.add(Color.BLUE);
    colors.add(Color.YELLOW);
    colors.add(Color.ORANGE);
    colors.add(Color.BLACK);
    colors.add(Color.GRAY);
  }

  // floods the neighboring cells
  void floodNeighbors(Color color) {
    if (this.right != null
        && !this.right.flooded
        && this.right.color == color) {
      this.right.flooded = true;
    }
    if (this.left != null
        && !this.left.flooded
        && this.left.color == color) {
      this.left.flooded = true;
    }
    if (this.bottom != null
        && !this.bottom.flooded 
        && this.bottom.color == color) {
      this.bottom.flooded = true;
    }
    if (this.top != null
        && !this.top.flooded 
        && this.top.color == color) {
      this.top.flooded = true;
    }
  }
}

class ExamplesCell {

  Cell blueCell = new Cell(0, 0, Color.BLUE);
  Cell redCell = new Cell(20, 0, Color.RED);
  Cell blueCell2 = new Cell(0, 20, Color.BLUE);
  Cell greenCell = new Cell(20, 20, Color.GREEN);
  Cell redCell2 = new Cell(20, 20, Color.RED);

  void initConditions() {
    blueCell.top = null;
    blueCell.bottom = blueCell2;
    blueCell.right = redCell;
    blueCell.left = null;
    blueCell.flooded = true;
    redCell.top = null;
    redCell.left = blueCell;
    redCell.bottom = redCell2;
    redCell.right = null;
    redCell.flooded = false;
    blueCell2.top = blueCell;
    blueCell2.bottom = null;
    blueCell2.left = null;
    blueCell2.right = redCell2;
    blueCell2.flooded = false;
    redCell2.top = redCell;
    redCell2.bottom = null;
    redCell2.left = blueCell2;
    redCell2.right = null;
    redCell2.flooded = false;
  }

  void testDrawCell(Tester t) {
    t.checkExpect(blueCell.drawCell(), new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE));
    t.checkExpect(redCell.drawCell(), new RectangleImage(20, 20, OutlineMode.SOLID, Color.RED));
    t.checkExpect(greenCell.drawCell(), new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN));
  }

  void testInitializeColors(Tester t) {
    Cell cell = new Cell();
    t.checkExpect(cell.colors, null);
    cell.initializeColors();
    t.checkExpect(cell.colors == null, false);
    t.checkFail(cell.colors, null);
    t.checkExpect(cell.colors.contains(Color.BLUE), true);
    t.checkExpect(cell.colors.contains(Color.RED), true);
    t.checkExpect(cell.colors.contains(Color.GREEN), true);
    t.checkExpect(cell.colors.contains(Color.PINK), true);
    t.checkExpect(cell.colors.contains(Color.YELLOW), true);
    t.checkExpect(cell.colors.contains(Color.ORANGE), true);
    t.checkExpect(cell.colors.contains(Color.BLACK), true);
    t.checkExpect(cell.colors.contains(Color.GRAY), true);
  }

  void testFloodNeighbors(Tester t) {
    initConditions();
    t.checkExpect(blueCell.flooded, true);
    t.checkExpect(redCell.flooded, false);
    t.checkExpect(redCell2.flooded, false);
    t.checkExpect(blueCell2.flooded, false);
    blueCell.floodNeighbors(blueCell.color);
    t.checkExpect(blueCell.flooded, true);
    t.checkExpect(redCell.flooded, false);
    t.checkExpect(redCell2.flooded, false);
    t.checkExpect(blueCell2.flooded, true);
    redCell.floodNeighbors(redCell.color);
    t.checkExpect(blueCell.flooded, true);
    t.checkExpect(redCell.flooded, false);
    t.checkExpect(redCell2.flooded, true);
    t.checkExpect(blueCell2.flooded, true);  

  }
}
