/*
 * Copyright (c) 2024 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.ollamajava;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * This is for "chat" mode.
 *
 * @author walter
 */
public class ChatRequest {

    /**
     * The name of the model to use for generating a response. This field is
     * required.
     */
    public String model;

    /**
     * The messages of the chat, this can be used to keep a chat memory.
     */
    public Message[] messages;
    /**
     * Tools for the model to use if supported. Requires stream to be set to
     * false.
     */
    public JsonNode[] tools;
    /**
     * Specifies the format of the response. Currently, the only supported value
     * is "json".
     */
    public String format;

    /**
     * If false, the response will be returned as a single response object,
     * rather than a stream of objects.
     */
    public Boolean stream = false;

    /**
     * Specifies the time the Ollama service should keep the model loaded. Can
     * be specified in minutes (m), seconds (s), or hours (h). The default is
     * 5m. A negative value indicates an infinite keep-alive period.
     */
    @JsonProperty(value = "keep_alive")
    public String keepAlive = "5m";

    /**
     * Additional model parameters as documented for the Modelfile, such as
     * temperature and other settings that affect the generation.
     */
    public Options options;

    public void addUserMessage(String message) {
        Message[] cur = messages;
        messages = new Message[cur.length + 1];
        int i;
        for (i = 0; i < cur.length; i++) {
            messages[i] = cur[i];
        }
        messages[i] = new Message(Message.Roles.user, message);
    }
}
