package com.webserver;

import com.webserver.utils.IntStringPair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RouteManagerTest {

  private static Map<String,String> store;

  @BeforeAll
  static void setUp() throws IOException {

    store = new HashMap<>();

    final String dir = "src/main/java/com/webserver/webpages/";
    final List<String> webpages = List.of(
      "index.html",
      "pagenotfound.html"
    );

    for (String webpage : webpages ) {
      final Path webpagePath = Paths.get(dir + webpage);
      StringBuilder document = new StringBuilder();
      try (Scanner scanner = new Scanner(webpagePath) ) {
        while ( scanner.hasNextLine() ) {
          document.append(scanner.nextLine());
        }
      }
      store.put(webpage, document.toString());
    }

  }

  @Test
  void getPage() {
//  testing the / URI
    Optional<IntStringPair> result = RouteManager.getPage("/");
    assert result.isPresent() : "Expected URI '/' to return a document";
    assertEquals(200, result.get().left(),
      "Expected a successful retrieval of the document."
    );
    assertEquals(store.get("index.html"), result.get().right(),
    "The retrieved document is inaccurate!"
    );


  }
}