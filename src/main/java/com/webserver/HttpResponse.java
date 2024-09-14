package com.webserver;

import java.util.List;

/**
 * Record to represent the processed response to write back to the client.
 *
 * @param statusCode of the response.
 * @param statusCodeMeaning of the response.
 * @param headers list of headers to write in the response.
 * @param body holds the requested html document.
 */
public record HttpResponse(
    int statusCode,
    String statusCodeMeaning,
    List<String> headers,
    String body
) {}
