/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bomberman;

/**
 *
 * @author fanda
 */
public enum Direction {
    UP("UP", 0, -1),
    DOWN("DOWN", 0, 1),
    LEFT("LEFT", -1, 0),
    RIGHT("RIGHT", 1, 0);

    private final int x;
    private final int y;
    private final String name;

    Direction(String name, int x, int y) {
        this.x = x;
        this.y = y;
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getName() {
        return name;
    }
}
