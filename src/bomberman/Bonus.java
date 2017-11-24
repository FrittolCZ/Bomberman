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
public class Bonus {

    private final BonusType type;

    private Brick[][] particles;

    public Bonus(int x, int y) {
        type = BonusType.randomType();
        initBonus(x, y);
    }

    private void initBonus(int posX, int posY) {
        particles = new Brick[MyGameState.SIZE][MyGameState.SIZE];
        for (int x = 0; x < MyGameState.SIZE; x++) {
            for (int y = 0; y < MyGameState.SIZE; y++) {
                particles[x][y] = new Brick(posX + x, posY + y);
            }
        }
    }
}
