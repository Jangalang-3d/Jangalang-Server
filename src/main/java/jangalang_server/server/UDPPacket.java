package jangalang_server.server;

import jangalang_server.game.PlayerInput;

public class UDPPacket {
    long timestamp;
    PlayerInput input;

    public UDPPacket(long timestamp, PlayerInput input) {
        this.timestamp = timestamp;
        this.input = input;
    }
}
