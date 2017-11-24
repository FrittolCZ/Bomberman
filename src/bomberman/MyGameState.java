/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bomberman;

import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author fanda
 */
class MyGameState {

    /**
     * Velikost jednoho objektu (hráče, kostky, bomby...)
     */
    public static final int SIZE = 4;

    /**
     * Výška a šírka herní mapy
     */
    public static final int WIDTH = 60;
    public static final int HEIGHT = 60;

    /**
     * Seznam kostek které jsou nezničitelné
     */
    private final ArrayList<Brick> solidBricks = new ArrayList<>();

    /**
     * Seznam kostek které lze rozbít
     */
    private final ArrayList<Brick> destructibleBricks = new ArrayList<>();

    /**
     * Hlavní hráč
     */
    /**
     * Hráči
     */
    private final ArrayList<Player> players = new ArrayList<>();

    private final ArrayList<Player> playingPlayers = new ArrayList<>();

    /**
     * Volné startovní pozice
     */
    private final ArrayList<Point> startPositions = new ArrayList<>();

    /**
     * Bomby umístěné na hrací ploše
     */
    private final ArrayList<Bomb> bombs = new ArrayList<>();

    public MyGameState() {
        initStartPositions();
        initPlayers();
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    /**
     *
     * @return Seznam nerozbitných kostek
     */
    public ArrayList<Brick> getSolidBricks() {
        return solidBricks;
    }

    /**
     * @return Seznam rozbitných kostek
     */
    public ArrayList<Brick> getDestructibleBricks() {
        return destructibleBricks;
    }

    /**
     *
     * @return Vrací bomby ve hře
     */
    public ArrayList<Bomb> getBombs() {
        return bombs;
    }

    /**
     * Aktualizuje hru
     *
     * @param delta
     */
    public boolean update(float delta) {
        for (int i = 0; i < bombs.size(); i++) {
            Bomb bomb = bombs.get(i);
            if (!bomb.isDead()) {
                bomb.update(delta);
            } else {
                destructibleBricks.forEach((brick) -> {
                    if (bomb.getFlames().contains(brick)) {
                        brick.deactive();
                    }
                });
                // zkontroluj zda byl zasažen hráč 
                bombs.remove(bomb);
                i--;
                bomb.getFlames().forEach((flame) -> {
                    flame.deactive();
                });
            }
        }
        playingPlayers.forEach((player) -> {
            bombs.stream().filter((bomb) -> (player.isAlive())).filter((bomb) -> (checkCollisionWithFlame(player, bomb))).forEachOrdered((_item) -> {
                player.setAlive(false);
            });
        });
        return playersAlive() >= 2;
    }

    private int playersAlive() {
        int i = 0;
        i = playingPlayers.stream().filter((p) -> (p.isAlive())).map((_item) -> 1).reduce(i, Integer::sum);
        return i;
    }

    public String lastPlayerName() {
        for (Player p : playingPlayers) {
            if (p.isAlive()) {
                return p.getName();
            }
        }
        return null;
    }

    /**
     * Inicializuje startovaní pozice
     */
    private void initStartPositions() {
        startPositions.clear();
        startPositions.add(new Point(1, 1));
        startPositions.add(new Point(WIDTH / SIZE - 2, 1));
        startPositions.add(new Point(1, HEIGHT / SIZE - 2));
        startPositions.add(new Point(WIDTH / SIZE - 2, HEIGHT / SIZE - 2));
    }

    /**
     * Vytvoří plamen který se objeví po výbuchu bomby
     *
     * @param player Hráč který bombu umístil - určuje délku plamene
     * @param bomb Bomba kterou hráč umístil
     */
    private void createFlames(Player player, Bomb bomb) {
        boolean breakFlame = false;

        ArrayList<Brick> flames = new ArrayList<>();

        Point[] directions = {new Point(0, 1), new Point(0, -1), new Point(1, 0), new Point(-1, 0)}; // směry šíření plamene

        Brick flame;

        for (Point p : directions) // projdu všechny směry 
        {
            for (int i = 1; i <= player.getBombFlame(); i++) {
                for (Brick[] parts : bomb.getParticles()) {
                    for (Brick part : parts) {
                        flame = new Brick(part.getX() + ((int) p.getX() * i) * SIZE, part.getY() + ((int) p.getY() * i) * SIZE);
                        flame.deactive();
                        if (!solidBricks.contains(flame)) {
                            for (Brick b : destructibleBricks) {
                                if (b.equals(flame)) {
                                    breakFlame = true;
                                }
                            }
                            flames.add(flame);
                        } else {
                            breakFlame = true;
                            break;
                        }
                    }
                }

                // pokud jsem v tomto směru narazil na překážku již v tomto směru nepřidávám
                if (breakFlame) {
                    break;
                }
            }
        }
        for (Brick[] parts : bomb.getParticles()) {
            for (Brick part : parts) {
                flame = new Brick(part.getX(), part.getY());
                flame.deactive();
                flames.add(flame);
            }
        }

        bomb.setFlames(flames);
    }

    public void addSolidBrick(Brick brick) {
        solidBricks.add(brick);
    }

    public void addDestructibleBrick(Brick brick) {
        destructibleBricks.add(brick);
    }

    public boolean end() {
        return false;
    }

    public void movePlayer(String player, String dir) {
        Player p = getPlayerByName(player);
        Direction d = getDirectionByName(dir);
        p.move(d.getX(), d.getY());
    }

    public Bomb plantBomb(String player) {
        Player p = getPlayerByName(player);
        if (!noBomb(p)) {
            Bomb bomb = new Bomb(p.getParticles());
            createFlames(p, bomb);
            bombs.add(bomb);
            return bomb;
        }
        return null;
    }

    /**
     * Inicializuje hru pro hráče Podle jmen klientů vezme hráče, propojí je se
     * jménem klienta a oživí
     *
     * @param players Jména hráčů
     */
    void initGame(ArrayList<String> playerNames) {
        resetPlayers(); // vyresetuje pozice hráčů
        playingPlayers.clear(); // vyprázdní hrající hráče
        resetBombs();
        resetBricks(); // opraví všechny rozbité kostky
        for (int i = 0; i < playerNames.size(); i++) {
            String name = playerNames.get(i);
            Player p = players.get(i);
            p.setName(name);
            initPlayer(p);
            p.setAlive(true);
            playingPlayers.add(p);
        }
    }

    private void resetBombs() {
        bombs.forEach((b) -> {
            b.kill();
        });
        bombs.clear();
    }

    private void resetBricks() {
        destructibleBricks.forEach((b) -> {
            b.active();
        });
    }

    /**
     * Odstraní rozbitelné kostky kolem hráče
     *
     * @param player
     */
    public void initPlayer(Player player) {
        for (Direction dir : Direction.values()) {
            destructibleBricks.forEach((b) -> {
                for (Brick[] parts : player.getParticles()) {
                    for (Brick part : parts) {
                        if (part.getX() == b.getX() && part.getY() == b.getY()
                                || part.getX() == b.getX() && b.getY() == part.getY() + (SIZE * dir.getY())
                                || part.getX() + (SIZE * dir.getX()) == b.getX() && b.getY() == part.getY()) {
                            b.deactive();
                        }
                    }
                }

            });
        }
    }

    /**
     * Vyhledá hráče podle jména
     *
     * @param playerName
     * @return Vrací hráče jenž odpovídá jménem
     */
    public Player getPlayerByName(String playerName) {
        for (Player player : playingPlayers) {
            if (player.getName().equals(playerName)) {
                return player;
            }
        }
        return null;
    }

    /**
     * Ověří zda hráč se jménem player se může pohnout ve směru direction
     *
     * @param player
     * @param direction
     * @return
     */
    public boolean checkMove(String playerName, Direction direction) {
        Player player = getPlayerByName(playerName);
        int x = direction.getX(), y = direction.getY();
        for (Brick[] parts : player.getParticles()) {
            for (Brick part : parts) {
                if (solidBricks.stream().anyMatch((b) -> (part.getX() + x == b.getX() && part.getY() + y == b.getY() && b.isActive()))) {
                    return true;
                }
                if (destructibleBricks.stream().anyMatch((b) -> (part.getX() + x == b.getX() && part.getY() + y == b.getY() && b.isActive()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Na všech pozcích inicializuje hráče Na začátku všichni mrtví
     */
    private void initPlayers() {
        startPositions.stream().map((p) -> new Player((int) p.getX() * SIZE, (int) p.getY() * SIZE)).forEachOrdered((player) -> {
            player.setAlive(false);
            players.add(player);
        });
    }

    /**
     * Resetuje hráče, vrátí hrače ze se seznamu hrajících
     */
    private void resetPlayers() {
        players.stream().map((p) -> {
            p.setAlive(false);
            return p;
        }).forEachOrdered((p) -> {
            p.resetPosition();
        });
    }

    private Direction getDirectionByName(String dir) {
        for (Direction d : Direction.values()) {
            if (d.getName().equals(dir)) {
                return d;
            }
        }
        return null;
    }

    private boolean checkCollisionWithFlame(Player p, Bomb b) {
        for (Brick[] parts : p.getParticles()) {
            for (Brick part : parts) {
                if (b.getFlames().stream().filter((f) -> (part.getX() == f.getX() && part.getY() == f.getY())).anyMatch((f) -> (f.isActive()))) {
                    return true;
                }
            }
        }
        return false;
    }

    void killPlayer(String username) {
        getPlayerByName(username).setAlive(false);
    }

    private boolean noBomb(Player player) {
        for (Bomb b : bombs) {
            if (!b.isDead()) {
                for (int x = 0; x < MyGameState.SIZE; x++) {
                    for (int y = 0; y < MyGameState.SIZE; y++) {
                        if (b.getParticles()[x][y].getX() == player.getParticles()[x][y].getY()
                                && b.getParticles()[x][y].getY() == player.getParticles()[x][y].getY()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
