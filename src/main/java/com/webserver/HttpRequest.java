package com.webserver;

/**
 * Record to represent a processed HTTP request.
 *
 * @param httpVerb of the request.
 * @param uri of the requested resource.
 */
public record HttpRequest(
    String httpVerb,
    String uri
) {}
