package com.data_management;

import java.io.IOException;
import java.net.URI;

public class WebSocketDataReader implements DataReader {
    private MyPatientDataClient client;
    private DataStorage storage;

    public WebSocketDataReader(DataStorage storage) {
        this.storage = storage;
    }

    @Override
    public void connect(String url) throws Exception {
        this.client = new MyPatientDataClient(new URI(url), storage);
        this.client.connect();
    }

    @Override
    public void disconnect() {
        if (client != null) client.close();
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        if (dataStorage == null) {
            throw new IllegalArgumentException("dataStorage cannot be null");
        }
        this.storage = dataStorage;

        if (client == null) {
            throw new IOException("WebSocket client is not connected. Call connect(url) before readData().");
        }

        // for a WebSocket reader, data is received asynchronously
        // incoming messages are processed in MyPatientDataClient.onMessage()
    }
}