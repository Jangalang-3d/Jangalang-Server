package jangalang_server.game;

import java.io.Serializable;

public class PlayerInput implements Serializable {
    private int playerId;
    private double velX;
    private double velY;
    private long timestamp;

    public PlayerInput(int playerId, double velX, double velY, long timestamp) {
        this.playerId = playerId;
        this.velX = velX;
        this.velY = velY;
        this.timestamp = timestamp;
    }

    public int getPlayerId() {
        return playerId;
    }

    public double getVelX() {
        return velX;
    }

    public double getVelY() {
        return velY;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
