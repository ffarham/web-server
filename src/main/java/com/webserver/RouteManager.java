package com.webserver;

import com.webserver.utils.IntStringPair;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;

/**
 * Class to manage routing.
 */
public class RouteManager {

  /**
   * Method to retrieve the request html page. If the uri does not exist,
   * return the pagenotfound.html.
   *
   * @param requestedUri html document requested by the client.
   * @return Optional representation of the html page.
   */
  public static Optional<IntStringPair> getPage(
      String requestedUri
  ) {

    int statusCode = 200;

    if (requestedUri.equals("/")) {
      requestedUri = "/index";
    }

    // TODO: implement security checks against the inputted uri
    String path = "src/main/java/com/webserver/webpages" + requestedUri + ".html";
    final Path filePath = Paths.get(path);
    if (!Files.exists(filePath)) {
      path = "src/main/java/com/webserver/webpages/pagenotfound.html";
      statusCode = 301; // redirected
    }

    try {

      File file = new File(path);
      try (Scanner scanner = new Scanner(file)) {
        StringBuilder webPage = new StringBuilder();
        while (scanner.hasNextLine()) {
          webPage.append(scanner.nextLine());
        }
        return Optional.of(new IntStringPair(statusCode, webPage.toString()));
      }

    } catch (FileNotFoundException ex) {
      System.out.println(
          "Thread " + Thread.currentThread().getId()
          + " caught the following exception when reading file :"
          + path + "\n" + ex
      );
      return Optional.empty();
    }
  }

}
