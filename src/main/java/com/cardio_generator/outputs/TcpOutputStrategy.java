package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * An {@link OutputStrategy} that streams patient health data to a TCP client.
 * On construction, a {@link ServerSocket} is opened on the specified port and a background
 * thread waits for a single client to connect. Once connected, all subsequent
 * {@link #output} calls send comma-separated records to that client over the socket.
 *
 * <p>This strategy supports exactly one connected client at a time. If no client has
 * connected yet, output calls are silently dropped until the connection is established.
 */
public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;

    /**
     * Creates a TCP server on the specified port and begins listening for a client connection
     * in a background thread. The constructor returns immediately; the client connection is
     * accepted asynchronously.
     *
     * @param port the TCP port number to listen on; must be in the range 1–65535
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a patient health data record to the connected TCP client as a comma-separated string.
     * If no client has connected yet, this method does nothing.
     *
     * <p>The format of each message is: {@code patientId,timestamp,label,data}
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time of the measurement in milliseconds since epoch
     * @param label     the type of health metric (e.g., "ECG", "Saturation")
     * @param data      the measurement value as a string
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
