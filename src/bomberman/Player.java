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
public class Player {

    private String name;
    private int speed = 1;
    private int bombMax = 1;
    private int bombFlame = 1;
    private int bombs = 0;
    private boolean alive = false;

    private final int startX;
    private final int startY;

    private Brick[][] particles;

    public Player(int x, int y) {
        this.startX = x;
        this.startY = y;
        initParts();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        for (Brick[] parts : particles) {
            for (Brick part : parts) {
                part.setActive(alive);
            }
        }
        this.alive = alive;
    }

    private void initParts() {
        particles = new Brick[MyGameState.SIZE][MyGameState.SIZE];
        for (int x = 0; x < MyGameState.SIZE; x++) {
            for (int y = 0; y < MyGameState.SIZE; y++) {
                particles[x][y] = new Brick(startX + x, startY + y);
            }
        }
    }

    public Brick[][] getParticles() {
        return particles;
    }

    public float getSpeed() {
        return speed;
    }

    public int getBombMax() {
        return bombMax;
    }

    public int getBombFlame() {
        return bombFlame;
    }

    /**
     * Zvýší rychlost hráče
     *
     * @param speed
     */
    public void addSpeed(float speed) {
        this.speed += speed;
    }

    /**
     * Navýší maximální počet bomb které hráč může umýstit
     *
     * @param bombMax
     */
    public void addBombMax(int bombMax) {
        this.bombMax += bombMax;
    }

    /**
     * Zvětší dosah plamene hráčových bomb
     *
     * @param bombFlame
     */
    public void addBombFlame(int bombFlame) {
        this.bombFlame += bombFlame;
    }

    /**
     * Zvýší počet bomb které hráč umýstil a ještě nevybuchly
     *
     * @return Vrací true pokud bomba byla vytvořena
     */
    public boolean placeBomb() {
        if (bombs < bombMax) {
            bombs++;
            return true;
        }
        return false;
    }

    public void resetPosition() {
        for (int x = 0; x < particles.length; x++) {
            for (int y = 0; y < particles[x].length; y++) {
                particles[x][y].setX(this.startX + x);
                particles[x][y].setY(this.startY + y);
            }
        }
    }

    public void move(int x, int y) {
        for (Brick[] parts : particles) {
            for (Brick part : parts) {
                part.setX(part.getX() + x);
                part.setY(part.getY() + y);
            }
        }
    }

}
