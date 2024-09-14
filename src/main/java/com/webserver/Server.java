package com.webserver;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Class representing the web server.
 */
public class Server {

  private final ServerSocket socket;
  private final Executor threadpool;

  /**
   * Constructor to initialise the server.
   * NOTES:
   * - port 80 is the default network port for webservers using HTTP
   * - binding to address 0.0.0.0 allows the server to listen on all network interfaces
   *   including the loopback/local-only interface (i.e. 127.0.0.1)
   *
   * @param port the port the server listens on.
   * @param maxWorkerThreads the maximum number of worker threads to spawn in the thread pool in
   *                         order to handle incoming connections.
   * @throws IOException if an I/O error occurs when opening the socket.
   */
  public Server(
      final int port,
      final int maxWorkerThreads
  ) throws IOException {

    this.socket = new ServerSocket(
      port,
      2 * maxWorkerThreads,
      InetAddress.getByAddress(new byte[] {0, 0, 0, 0})
    );

    this.threadpool = Executors.newFixedThreadPool(maxWorkerThreads);
  }

  /**
   * Method to start the server.
   * The connections are handled by worker threads, where the primary thread only
   * handles accepting and delegating the connection.
   */
  public void start() throws IOException {

    System.out.println(
        "Server " + socket.getInetAddress().getHostName()
        + " is listening on port " + socket.getLocalPort()
    );

    while (true) {

      Socket conn = socket.accept(); // blocking call

      Runnable work = () -> {
        try {

          System.out.println(
              "Thread " + Thread.currentThread().getId()
              + ": Accepted incoming connection from <"
              + conn.getInetAddress().getHostName()
              + ">"
          );

          connectionHandler(conn.getInputStream(), conn.getOutputStream());

        } catch (IOException ex) {
          System.out.println(
              "Thread " + Thread.currentThread().getId()
              + " caught an exception:"
          );
          System.out.println(ex);
        }
      };

      this.threadpool.execute(work);
    }
  }

  /**
   * Method to handle the established connection with the client.
   *
   * @param inputStream of the respective client.
   * @param outputStream of the respective client.
   * @throws IOException if an I/O error occurs during closing of buffer writer.
   */
  public void connectionHandler(
      final InputStream inputStream,
      final OutputStream outputStream
  ) throws IOException {

    // TODO: initialise buffWriter asynchronously
    final BufferedWriter buffWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

    readMessage(inputStream)
        .flatMap(HttpHandler::buildRequestObject)
        .flatMap(HttpHandler::buildResponseObject)
        .ifPresentOrElse(
          value -> HttpHandler.onSuccessWriteBack(value, buffWriter),
          () -> HttpHandler.onFailWriteBack(buffWriter)
        );

    buffWriter.close();

  }

  /**
   * Method to read the received message from a client.
   *
   * @param inputStream of the connected client.
   * @return the message received as a list containing each line as element.
   */
  private static Optional<List<String>> readMessage(
      final InputStream inputStream
  ) {

    try {

      if (inputStream.available() <= 0) {
        return Optional.empty();
      }

      final char[] inBuffer = new char[inputStream.available()];
      final InputStreamReader inReader = new InputStreamReader(inputStream);
      if (inReader.read(inBuffer) <= 0) {
        return Optional.empty();
      }

      List<String> message = new ArrayList<>();
      try (Scanner scanner = new Scanner(new String(inBuffer))) {
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          message.add(line);
        }
      }

      return Optional.of(message);

    } catch (IOException ex) {

      System.out.println(
          "Thread " + Thread.currentThread().getId()
          + " caught the following exception when reading client's message:\n"
          + ex
      );

      return Optional.empty();

    }
  }
}
