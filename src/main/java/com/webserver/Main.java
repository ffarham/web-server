package com.webserver;

import java.io.IOException;

/**
 * Main Class.
 */
public class Main {

  /**
   * Entry point to the program.
   *
   * @param args holding any CLI arguments.
   * @throws IOException if an I/O error occurs.
   */
  public static void main(String[] args) throws IOException {

    Server server = new Server(80, 100);
    server.start();

  }
}