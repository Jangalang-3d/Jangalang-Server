package jangalang_server.server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import jangalang_server.game.Player;
import jangalang_server.game.PlayerInput;
import jangalang_server.util.ServerProperties;

public class Server {
    private static final int PORT = ServerProperties.getInt("server.port");
    private static final double MAX_SPEED = 5.0;
    private static final double PREDICTION_THRESHOLD = 0.1; // Time threshold to predict movement
    private static final int MAX_PACKET_HISTORY = ServerProperties.getInt("server.buffer.size");
    private static final int MAX_CLIENTS = ServerProperties.getInt("server.clients.max");

    private DatagramSocket socket;
    private Map<Integer, Player> players;
    private Map<Integer, LinkedList<UDPPacket>> playerPacketHistory;


    public Server() throws SocketException {
        socket = new DatagramSocket(PORT);
        players = new ConcurrentHashMap<>();
        playerPacketHistory = new ConcurrentHashMap<>();
        System.out.println("Server started on port " + PORT);
    }

    public void start() {
        byte[] receiveData = new byte[1024];

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);
                handlePacket(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Handle incoming UDP packets from clients
    private void handlePacket(DatagramPacket packet) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(packet.getData()));
            PlayerInput input = (PlayerInput) ois.readObject();

            int playerId = input.getPlayerId();
            Player player = players.getOrDefault(playerId, new Player(playerId));
            players.putIfAbsent(playerId, player);

            // Handle lost or out-of-order packets using sequence numbers
            long timestamp = input.getTimestamp();
            LinkedList<UDPPacket> packetHistory = playerPacketHistory.computeIfAbsent(playerId, k -> new LinkedList<>());

            // Remove old packets from history
            while (!packetHistory.isEmpty() && packetHistory.peekFirst().timestamp < timestamp - MAX_PACKET_HISTORY) {
                packetHistory.pollFirst();
            }

            // Add the current packet to the history
            packetHistory.add(new UDPPacket(timestamp, input));

            // Predict player's movement if packet is missing
            if (packetHistory.size() > 1) {
                predictAndUpdatePlayerState(player, packetHistory);
            }

            // Broadcast the updated state to all players
            sendStateToClients();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Predict and update the player's state based on previous packets
    private void predictAndUpdatePlayerState(Player player, LinkedList<UDPPacket> packetHistory) {
        // Get the most recent two packets for prediction
        UDPPacket latest = packetHistory.getLast();
        UDPPacket secondLatest = packetHistory.size() > 1 ? packetHistory.get(packetHistory.size() - 2) : null;

        // If there's a second latest packet, use it to predict movement
        if (secondLatest != null) {
            long timeDiff = latest.timestamp - secondLatest.timestamp;
            double predictedPosX = player.getXCoord() + (latest.input.getVelX() * timeDiff);
            double predictedPosY = player.getYCoord() + (latest.input.getVelY() * timeDiff);

            // Update the player's position based on prediction
            player.setXCoord(predictedPosX);
            player.setYCoord(predictedPosY);
        }
    }

    // Send the updated game state to all players
    private void sendStateToClients() {
        try {
            for (Player player : players.values()) {
                // Broadcast the updated player positions
                for (Player targetPlayer : players.values()) {
                    if (player != targetPlayer) {
                        byte[] sendData = serializeGameState(targetPlayer);
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, targetPlayer.getAddress(), PORT);
                        socket.send(sendPacket);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Serialize the game state to send to clients
    private byte[] serializeGameState(Player player) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byteStream);
        oos.writeObject(player);
        return byteStream.toByteArray();
    }
}
