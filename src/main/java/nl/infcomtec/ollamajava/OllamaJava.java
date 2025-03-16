/*
 * Copyright (c) 2024 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.ollamajava;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 *
 * @author walter
 */
public class OllamaJava {

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .appendPattern("XXX")
            .toFormatter();
    private final LinkedList<JSONIO> jsonIO = new LinkedList<>();

    public OllamaJava() {
    }

    /**
     * Just return a human readable form of the JSON.
     *
     * @param json Must be valid.
     * @return JSON with indents and new lines.
     * @throws java.lang.Exception It was not valid.
     */
    public String pretty(String json) throws Exception {
        ObjectMapper mapper = getMapper();
        JsonNode tree = mapper.readTree(json);
        return mapper.writeValueAsString(tree);
    }

    /**
     * Fetch the currently available (downloaded) models.
     *
     * @param cfg The configuration.
     * @return JSON, one object, one field "models" which is a list of model
     * objects.
     * @throws Exception For reasons.
     */
    public String listModels(Config cfg) throws Exception {
        URL url = cfg.ollamaTagsUrl();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    /**
     * Fetch detailed information on a model.
     *
     * @param cfg The configuration.
     * @param model Name of an existing model.
     * @return JSON describing the model.
     * @throws Exception For reasons.
     */
    public String modelDetails(Config cfg, String model) throws Exception {
        URL url = cfg.ollamaShowUrl();
        return pretty(sendRequest(url, "{ \"model\": \"" + model + "\", \"verbose\": false }"));
    }

    /**
     * Fetch GGML token list for a model.
     *
     * @param cfg The configuration.
     * @param model Name of an existing model.
     * @return JSON describing the model.
     * @throws Exception For reasons.
     */
    public List<String> modelTokenList(Config cfg, String model) throws Exception {
        URL url = cfg.ollamaShowUrl();
        String mi = sendRequest(url, "{ \"model\": \"" + model + "\", \"verbose\": true }");
        ObjectMapper mapper = getMapper();
        JsonNode tree = mapper.readTree(mi);
        JsonNode mInf = tree.get("model_info");
        JsonNode tList = mInf.get("tokenizer.ggml.tokens");
        int n = tList.size();
        ArrayList<String> ret = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            ret.add(tList.get(i).asText());
        }
        return ret;
    }

    /**
     * Fetch the currently available (downloaded) models.
     *
     * @param cfg The configuration.
     * @return List of JSON objects, should at least have a "name" field.
     * @throws Exception For reasons.
     */
    public List<JsonNode> getModels(Config cfg) throws Exception {
        String models = listModels(cfg);
        ObjectMapper mapper = getMapper();
        JsonNode tree = mapper.readTree(models);
        JsonNode node = tree.get("models");
        LinkedList<JsonNode> ret = new LinkedList<>();
        Iterator<JsonNode> it;
        for (it = node.iterator(); it.hasNext();) {
            ret.add(it.next());
        }
        return ret;
    }

    /**
     * Set up the Swing GUI.
     *
     */
    public final static void setupGUI() {
        File props = new File(System.getProperty("user.home") + "/.flatlaf.properties");
        if (!props.exists()) {
            props = new File(System.getProperty("user.home") + "/.config/FlatLaf/flatlaf.properties");
        }
        if (props.exists()) {
            FlatLaf.registerCustomDefaultsSource(props.getAbsolutePath());
        }
        FlatDarculaLaf.setup();
    }

    /**
     * Presents the user with a list of models to select from using JOptionPane.
     *
     * @param cfg The configuration object to fetch models.
     * @param titleMsg Optionally, ["title"[,"message"]]
     * @return The selected model (its name).
     */
    public String selectModel(Config cfg, String... titleMsg) {
        try {
            // Fetch the available models
            List<JsonNode> models = getModels(cfg);

            // Extract model names for display
            String[] modelNames = new String[models.size()];
            for (int i = 0; i < models.size(); i++) {
                modelNames[i] = models.get(i).get("name").asText();
            }
            String message = "Select a model:";
            String title = "Model Selector";
            if (null != titleMsg) {
                if (titleMsg.length > 1) {
                    message = titleMsg[1];
                }
                if (titleMsg.length > 0) {
                    title = titleMsg[0];
                }
            }
            // Show the model selection dialog
            return (String) JOptionPane.showInputDialog(
                    null,
                    message,
                    title,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    modelNames,
                    modelNames.length > 0 ? modelNames[0] : null
            );
        } catch (Exception any) {
            throw new RuntimeException(any);
        }
    }

    /**
     * Reset the trace buffer.
     */
    public void clearTrace() {
        synchronized (jsonIO) {
            jsonIO.clear();
        }
    }

    /**
     * Clear old entries from trace buffer.
     *
     * @param dur This long ago.
     */
    public void clearTrace(Duration dur) {
        clearTrace(System.currentTimeMillis() - dur.toMillis());
    }

    /**
     * Clear old entries from trace buffer.
     *
     * @param before This point in time in milliseconds.
     */
    public void clearTrace(long before) {
        synchronized (jsonIO) {
            for (Iterator<JSONIO> it = jsonIO.iterator(); it.hasNext();) {
                JSONIO jio = it.next();
                if (jio.at < before) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Get a copy of the current call traces.
     *
     * @param andClear (optional) If true, reset the trace buffer.
     * @return A copy of the call traces.
     */
    public LinkedList<JSONIO> getTrace(boolean... andClear) {
        synchronized (jsonIO) {
            LinkedList<JSONIO> ret = new LinkedList<>(jsonIO);
            if (null != andClear && andClear[0]) {
                jsonIO.clear();
            }
            return ret;
        }
    }

    /**
     * Get an "object-aware" version of ObjectMapper.
     *
     * @return Jackson object mapper.
     */
    public ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        mapper.registerModule(module);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Call model via Ollama.
     *
     * @param rq Proper request.
     * @return Response.
     * @throws Exception Or this.
     */
    public Response request(Request rq) throws Exception {
        return request(new Config(), rq);
    }

    /**
     * Call model via Ollama.
     *
     * @param cfg The configuration.
     * @param rq Proper request.
     * @return Response.
     * @throws Exception Or this.
     */
    public Response request(Config cfg, Request rq) throws Exception {
        URL url = cfg.ollamaGenerateUrl();
        ObjectMapper mapper = getMapper();
        String requestBody = mapper.writeValueAsString(rq);
        String response = sendRequest(url, requestBody);
        return mapper.readValue(response, Response.class);
    }

    private String sendRequest(URL url, String requestBody) throws Exception {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        synchronized (jsonIO) {
            jsonIO.add(new JSONIO(true, url.getPath(), requestBody));
        }
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            String ret = response.toString();
            synchronized (jsonIO) {
                jsonIO.add(new JSONIO(false, url.getPath(), ret));
            }
            return ret;
        } finally {
            con.disconnect();
        }
    }

    /**
     * This calls the listener for each word.
     *
     * @param model The model to use.
     * @param prompt The question.
     * @param listener Callback.
     * @param images For vision capable models.
     * @return Unlike the specification at
     * https://github.com/jmorganca/ollama/blob/main/docs/api.md, this will also
     * contain the full (concatenated) response in the response field of the
     * Response object.
     * @throws Exception For reasons.
     */
    public Response askWithStream(String model, String prompt, StreamListener listener, RenderedImage... images) throws Exception {
        if (null == listener) {
            throw (new RuntimeException("Listener is null"));
        }
        ObjectMapper mapper = getMapper();

        Request rq = new Request();
        rq.model = model;
        rq.prompt = prompt;
        rq.stream = true;
        setReqImages(images, rq);
        String requestBody = mapper.writeValueAsString(rq);
        Response resp = sendRequestWithStreaming(requestBody, listener);
        return resp;
    }

    /**
     * Use the chat interface against local host.
     *
     * @param rq The full chat request.
     * @return List of responses.
     * @throws Exception For reasons.
     */
    public List<Response> chat(ChatRequest rq) throws Exception {
        return chat(new Config(), rq, null);
    }

    /**
     * Use the chat interface.
     *
     * @param cfg The configuration.
     * @param rq The full chat request.
     * @return List of responses.
     * @throws Exception For reasons.
     */
    public List<Response> chat(Config cfg, ChatRequest rq) throws Exception {
        return chat(cfg, rq, null);
    }

    /**
     * Use the chat interface.
     *
     * @param cfg The configuration.
     * @param listener If not null, will be called for each partial response.
     * @param rq The full chat request.
     * @return List of responses.
     * @throws Exception For reasons.
     */
    public List<Response> chat(Config cfg, ChatRequest rq, StreamListener listener) throws Exception {
        URL url = cfg.ollamaChatUrl();
        ObjectMapper mapper = getMapper();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        String requestBody = mapper.writeValueAsString(rq);
        synchronized (jsonIO) {
            jsonIO.add(new JSONIO(true, url.getPath(), requestBody));
            System.out.println(requestBody);
        }
        List<Response> ret = new ArrayList<>();

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                synchronized (jsonIO) {
                    jsonIO.add(new JSONIO(false, url.getPath(), responseLine));
                }
                if (!responseLine.trim().isEmpty()) {
                    if (responseLine.startsWith("{\"error")) {
                        ret.add(errorResponse(responseLine));
                        if (null != listener) {
                            listener.onResponseReceived(errorResponse(responseLine));
                        }
                        return ret;
                    }
                    Response val = mapper.readValue(responseLine, Response.class);
                    ret.add(val);
                    if (null != listener) {
                        listener.onResponseReceived(val);
                    }
                    if (val.done) {
                        break;
                    }
                }
            }
        } finally {
            con.disconnect();
        }
        return ret;
    }

    /**
     * Merge a (processed) chat request with the reply.
     *
     * @param in The request as it was send.
     * @param answer The answer that was received.
     * @return The chat request with the answers appended to messages.
     */
    public ChatRequest merge(ChatRequest in, List<Response> answer) {
        List<Message> messages = new LinkedList<>();
        if (null != in.messages && in.messages.length > 0) {
            messages.addAll(Arrays.asList(in.messages));
        }
        for (Response resp : answer) {
            if (null != resp.messages && resp.messages.length > 0) {
                messages.addAll(Arrays.asList(resp.messages));
            }
        }
        in.messages = messages.toArray(new Message[0]);
        return in;
    }

    private void setReqImages(RenderedImage[] images, Request rq) throws IOException {
        if (null != images) {
            for (RenderedImage im : images) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(im, "png", baos);
                    baos.flush();
                    String enc = Base64.getEncoder().encodeToString(baos.toByteArray());
                    if (null == rq.images) {
                        rq.images = new String[1];
                        rq.images[0] = enc;
                    } else {
                        String[] oi = rq.images;
                        rq.images = new String[oi.length + 1];
                        System.arraycopy(oi, 0, rq.images, 0, oi.length);
                        rq.images[oi.length] = enc;
                    }
                }
            }
        }
    }

    private Response sendRequestWithStreaming(String requestBody, StreamListener listener) throws Exception {
        URL url = new Config().ollamaGenerateUrl();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        synchronized (jsonIO) {
            jsonIO.add(new JSONIO(true, url.getPath(), requestBody));
        }

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            ObjectMapper mapper = getMapper();
            String responseLine;
            StringBuilder fullResponse = new StringBuilder();
            while ((responseLine = br.readLine()) != null) {
                synchronized (jsonIO) {
                    jsonIO.add(new JSONIO(false, url.getPath(), responseLine));
                }
                if (!responseLine.trim().isEmpty()) {
                    if (responseLine.startsWith("{\"error")) {
                        Response err = errorResponse(responseLine);
                        listener.onResponseReceived(err);
                        return err;
                    }
                    Response val = mapper.readValue(responseLine, Response.class);
                    if (val.done) {
                        val.response = fullResponse.toString();
                        return val;
                    } else {
                        fullResponse.append(val.response);
                        if (!listener.onResponseReceived(val)) {
                            return null;
                        }
                    }
                }
            }
            return mapper.readValue(responseLine, Response.class);
        } finally {
            con.disconnect();
        }
    }

    private Response errorResponse(String responseLine) {
        Response err = new Response();
        err.context = new LinkedList<>();
        err.createdAt = LocalDateTime.now();
        err.done = true;
        err.evalCount = 0;
        err.evalDuration = 1;
        err.loadDuration = 1;
        err.model = "?";
        err.promptEvalCount = 0;
        err.promptEvalDuration = 1;
        err.sampleCount = 0;
        err.sampleDuration = 1;
        err.totalDuration = 3;
        err.response = responseLine;
        return err;
    }

    /**
     * Trace buffer element.
     */
    public static class JSONIO {

        /**
         * Time the call was made or a response arrived
         */
        public final long at = System.currentTimeMillis();
        /**
         * True for requests, false for responses.
         */
        public final boolean isOut;
        /**
         * Endpoint used.
         */
        public final String endPoint;
        /**
         * Actual JSON
         */
        public final String json;

        public JSONIO(boolean isOut, String endPoint, String json) {
            this.isOut = isOut;
            this.endPoint = endPoint;
            this.json = json;
        }
    }

    public interface StreamListener {

        /**
         * Called for each piece of the response.
         *
         * @param responsePart next part.
         * @return true to continue, false to stop.
         */
        boolean onResponseReceived(StreamedResponse responsePart);
    }
}
