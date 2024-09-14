package com.webserver.utils;

/**
 * Record for a 'Pair' of an integer and string.
 *
 * @param left value holding the integer.
 * @param right value holding the string.
 */
public record IntStringPair(
    int left,
    String right
) {}