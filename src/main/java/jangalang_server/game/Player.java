package jangalang_server.game;

import java.net.InetAddress;

public class Player {
    private int id;
    private double xCoord = 0;
    private double yCoord = 0;
    private double velX = 0;
    private double velY = 0;
    private InetAddress address;

    public Player(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setXCoord(double xCoord) {
        this.xCoord = xCoord;
    }

    public double getXCoord() {
        return xCoord;
    }

    public void setYCoord(double yCoord) {
        this.yCoord = yCoord;
    }

    public double getYCoord() {
        return yCoord;
    }

    public void setVelX(double velX) {
        this.velX = velX;
    }

    public double getVelX() {
        return velX;
    }

    public void setVelY(double velY) {
        this.velY = velY;
    }

    public double getVelY() {
        return velY;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }
}
