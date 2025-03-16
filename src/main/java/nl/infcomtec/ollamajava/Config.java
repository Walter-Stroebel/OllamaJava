/*
 * Copyright (c) 2024 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.ollamajava;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * There may be more then one machine running LLM(s), more than one API, some
 * encryption ... this is supposed to catch them all. Not calling it "PokeBall"!
 *
 * @author walter
 */
public class Config {

    private static final String API_GENERATE = "/api/generate";
    private static final String API_CHAT = "/api/chat";
    private static final String API_TAGS = "/api/tags";
    private static final String API_SHOW = "/api/show";

    public String hostOrIP = "127.0.0.1";
    public String proto = "http://";
    public int port = 11434;

    public Config() {
    }

    public Config(String hostOrIP) {
        this.hostOrIP = hostOrIP;
    }

    /**
     * List Local Models.
     *
     * @return The URL.
     * @throws MalformedURLException
     */
    public URL ollamaTagsUrl() throws MalformedURLException {
        return new URL(proto + hostOrIP + ':' + port + API_TAGS);
    }

    /**
     * Show Model Information.
     *
     * @return The URL.
     * @throws MalformedURLException
     */
    public URL ollamaShowUrl() throws MalformedURLException {
        return new URL(proto + hostOrIP + ':' + port + API_SHOW);
    }

    /**
     * Generate a completion.
     *
     * @return The URL.
     * @throws MalformedURLException
     */
    public URL ollamaGenerateUrl() throws MalformedURLException {
        return new URL(proto + hostOrIP + ':' + port + API_GENERATE);
    }

    /**
     * Generate a completion.
     *
     * @return The URL.
     * @throws MalformedURLException
     */
    public URL ollamaChatUrl() throws MalformedURLException {
        return new URL(proto + hostOrIP + ':' + port + API_CHAT);
    }
}
