package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;

public class MyPatientDataClient extends WebSocketClient implements DataReader {
    private DataStorage dataStorage;

    public MyPatientDataClient(URI serverUri, DataStorage storage) {
        super(serverUri);
        this.dataStorage = storage;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to the signal generator!");
    }

    @Override
    public void onMessage(String message) {
        try {
            parseAndStore(message);
        } catch (Exception e) {
            System.err.println("Parsing error : " + message);
        }
    }

    private void parseAndStore(String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        String[] parts = message.split(",");
        if (parts.length != 4) {
            System.err.println("Invalid message format: " + message);
            return;
        }

        try {
            int patientId = Integer.parseInt(parts[0].trim());
            long timestamp = Long.parseLong(parts[1].trim());
            String label = parts[2].trim();
            double value = Double.parseDouble(parts[3].trim());

            dataStorage.addPatientData(patientId, value, label, timestamp);
        } catch (NumberFormatException e) {
            System.err.println("Unable to parse message values: " + message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void connect(String url) throws Exception {
        this.connect(); // Calls the no-argument WebSocketClient connect()
    }

    @Override
    public void disconnect() {
        this.close();
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        if (dataStorage == null) {
            throw new IllegalArgumentException("dataStorage cannot be null");
        }

        this.dataStorage = dataStorage;

        if (!this.isOpen()) {
            try {
                super.connect();
            } catch (Exception e) {
                throw new IOException("Unable to connect WebSocket client to read data", e);
            }
        }
    }
}
