/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bomberman;

import java.util.ArrayList;

/**
 *
 * @author fanda
 */
class Bomb {

    private float timeToExplode = 2500;
    private float flamesTimer = 1000;

    private Brick[][] particles;

    private ArrayList<Brick> flames;

    private boolean dead = false;

    public Bomb(Brick[][] parts) {
        initParts(parts);
    }

    private void initParts(Brick[][] parts) {
        particles = new Brick[MyGameState.SIZE][MyGameState.SIZE];
        int posX = (int) Math.ceil(parts[MyGameState.SIZE - 1][MyGameState.SIZE - 1].getX() / MyGameState.SIZE) * MyGameState.SIZE;
        int posY = (int) Math.ceil(parts[MyGameState.SIZE - 1][MyGameState.SIZE - 1].getY() / MyGameState.SIZE) * MyGameState.SIZE;
        for (int x = 0; x < parts.length; x++) {
            for (int y = 0; y < parts[x].length; y++) {
                particles[x][y] = new Brick(posX + x, posY + y);
            }
        }

    }

    public void setFlames(ArrayList<Brick> flames) {
        this.flames = flames;
    }

    public ArrayList<Brick> getFlames() {
        return flames;
    }

    public Brick[][] getParticles() {
        return particles;
    }

    /**
     *
     * @return - True pokud bomba již vybouchla
     */
    public boolean isDead() {
        return dead;
    }

    public void update(float delta) {

        if (timeToExplode <= 0) {
            for (Brick[] parts : particles) {
                for (Brick part : parts) {
                    part.deactive();
                }
            }
            flames.forEach((flame) -> {
                flame.active();
            });
            if (this.flamesTimer <= 0) {
                dead = true;
            } else {
                flamesTimer -= delta;
            }
        } else {
            this.timeToExplode -= delta;
        }
    }

    public void kill() {
        dead = true;
        for (Brick[] parts : particles) {
            for (Brick part : parts) {
                part.deactive();
            }
        }
        flames.forEach((flame) -> {
            flame.deactive();
        });
    }

}
