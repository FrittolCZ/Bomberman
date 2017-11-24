/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bomberman;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author fanda
 */
public class GamePane extends Pane {

    private final MyGameState game;

    public GamePane(int unitsize, MyGameState game) {
        this.game = game;
        super.setWidth(unitsize * MyGameState.WIDTH * MyGameState.SIZE);
        super.setHeight(unitsize * MyGameState.HEIGHT * MyGameState.SIZE);
        createGameMap();
        initPlayers();
    }

    private void createGameMap() {
        for (int x = 0; x < MyGameState.WIDTH; x++) {
            for (int y = 0; y < MyGameState.HEIGHT; y++) {
                if (x / MyGameState.SIZE == 0 || y / MyGameState.SIZE == 0
                        || x / MyGameState.SIZE == (MyGameState.WIDTH - 1) / MyGameState.SIZE
                        || y / MyGameState.SIZE == (MyGameState.HEIGHT - 1) / MyGameState.SIZE
                        || ((x / MyGameState.SIZE) % 2 == 0 && (y / MyGameState.SIZE) % 2 == 0)) {
                    Brick brick = new Brick(x, y);
                    addBrickToPane(brick, Color.GREY);
                    game.addSolidBrick(brick);
                } else {
                    Brick brick = new Brick(x, y);
                    addBrickToPane(brick, Color.ORANGE);
                    game.addDestructibleBrick(brick);
                }
            }
        }
    }

    private void addBrickToPane(Brick brick, Color color) {
        Rectangle rect = new Rectangle(brick.getX() * Bomberman.UNIT_SIZE, brick.getY() * Bomberman.UNIT_SIZE, Bomberman.UNIT_SIZE, Bomberman.UNIT_SIZE);
        rect.visibleProperty().bind(brick.activeProperty());
        rect.setFill(color);
        this.getChildren().add(rect);
    }

    /**
     *
     * @param bomb
     */
    public void drawBomb(Bomb bomb) {
        for (Brick[] parts : bomb.getParticles()) {
            for (Brick part : parts) {
                Rectangle rect = new Rectangle(part.getX() * Bomberman.UNIT_SIZE, part.getY() * Bomberman.UNIT_SIZE, Bomberman.UNIT_SIZE, Bomberman.UNIT_SIZE);
                rect.visibleProperty().bind(part.activeProperty());
                rect.setFill(Color.BLACK);
                this.getChildren().add(rect);
            }
        }
        bomb.getFlames().stream().map((flame) -> {
            Rectangle rect = new Rectangle(flame.getX() * Bomberman.UNIT_SIZE, flame.getY() * Bomberman.UNIT_SIZE, Bomberman.UNIT_SIZE, Bomberman.UNIT_SIZE);
            rect.visibleProperty().bind(flame.activeProperty());
            return rect;
        }).map((rect) -> {
            rect.setFill(Color.RED);
            return rect;
        }).forEachOrdered((rect) -> {
            this.getChildren().add(rect);
        });
    }

    private void initPlayers() {
        game.getPlayers().forEach((p) -> {
            Brick[][] particles = p.getParticles();
            for (Brick[] parts : particles) {
                for (Brick b : parts) {
                    Rectangle rect = new Rectangle(Bomberman.UNIT_SIZE, Bomberman.UNIT_SIZE);
                    rect.xProperty().bind(b.xProperty().multiply(Bomberman.UNIT_SIZE));
                    rect.yProperty().bind(b.yProperty().multiply(Bomberman.UNIT_SIZE));
                    rect.visibleProperty().bind(b.activeProperty());
                    rect.setFill(Color.YELLOW);
                    this.getChildren().add(rect);
                }
            }
        });
    }
}
