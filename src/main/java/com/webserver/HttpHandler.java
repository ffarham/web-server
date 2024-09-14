package com.webserver;

import com.webserver.utils.IntStringPair;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Class to define util methods to handle incoming connections.
 */
public class HttpHandler {

  private static String getStatusCodeMeaning(
      int statusCode
  ) throws IllegalArgumentException {

    return switch (statusCode) {
      case 200 -> "OK";
      case 301 -> "Redirected";
      default -> throw new IllegalArgumentException(
        "Status code " + statusCode + " is not supported!"
      );
    };
  }

  /**
   * Method to parse and create a request object.
   *
   * @param message the message received from the client.
   * @return the HttpRequest object on success, else, empty optional.
   */
  public static Optional<HttpRequest> buildRequestObject(
      List<String> message
  ) {

    if (message.isEmpty()) {
      return Optional.empty();
    }

    String firstLine = message.get(0);
    String[] httpInfo = firstLine.split(" ");

    if (httpInfo.length != 3) {
      return Optional.empty();
    }

    final String httpVerb = httpInfo[0];
    final String uri = httpInfo[1];
    final String httpVersion = httpInfo[2];

    if (!httpVerb.equals("GET")) {
      return Optional.empty();
    }

    if (!httpVersion.equals("HTTP/1.1")) {
      return Optional.empty();
    }

    return Optional.of(new HttpRequest(httpVerb, uri));

  }

  /**
   * Method to build the response object used to write back to the connected client.
   *
   * @param request the constructed request object representing the request from the client.
   * @return the response object, or an empty optional in case of error.
   */
  public static Optional<HttpResponse> buildResponseObject(HttpRequest request) {

    final String requestedUri = request.uri();
    final Optional<IntStringPair> optCodeAndWebpage = RouteManager.getPage(requestedUri);
    if (optCodeAndWebpage.isEmpty()) {
      return Optional.empty();
    }
    final IntStringPair codeAndWebpage = optCodeAndWebpage.get();
    final int statusCode = codeAndWebpage.left();
    final String webpage = codeAndWebpage.right();
    List<String> headers = List.of(
        "Content-Type: text/html\r\n",
        "Server: Farham's Toy WebServer\r\n"
    );
    return Optional.of(
        new HttpResponse(statusCode, getStatusCodeMeaning(statusCode), headers, webpage)
    );
  }

  /**
   * Method to write back to the client when request has been processed successfully.
   *
   * @param response object representing what to write back.
   * @param buffWriter used to write back to the connected client.
   */
  public static void onSuccessWriteBack(
      HttpResponse response,
      BufferedWriter buffWriter
  ) {

    try {

      buffWriter.write(
          "HTTP/1.1 " + response.statusCode() + " " + response.statusCodeMeaning() + "\r\n"
      );

      for (String header : response.headers()) {
        buffWriter.write(header);
      }

      final String encodedString = new String(
          response.body().getBytes(StandardCharsets.UTF_8),
          StandardCharsets.UTF_8
      );
      buffWriter.write("\r\n");
      buffWriter.write(encodedString);

    } catch (IOException ex) {
      System.out.println(
          "Thread " + Thread.currentThread().getId()
          + " caught the following exception when writing back to the client: "
          + ex
      );
    }
  }

  /**
   * Method to the client was not processed successfully.
   *
   * @param buffWriter used to write back to the connected client.
   */
  public static void onFailWriteBack(
      BufferedWriter buffWriter
  ) {

    try {
      buffWriter.write("HTTP/1.1 500 Internal server error.\r\n");
    } catch (IOException ex) {
      System.out.println(
          "Thread " + Thread.currentThread().getId()
          + " caught the following exception when writing back to the client: " + ex
      );
    }
  }
}
