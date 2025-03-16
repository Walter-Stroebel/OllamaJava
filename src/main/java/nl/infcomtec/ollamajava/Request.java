package nl.infcomtec.ollamajava;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a request to an Ollama model, allowing customization of model
 * parameters and inclusion of system and user prompts. This Java class
 * encapsulates various options that can be adjusted to modify how the model
 * generates its response.
 */
public class Request {

    /**
     * The name of the model to use for generating a response. This field is
     * required.
     */
    public String model;

    /**
     * The prompt for which the model should generate a response.
     */
    public String prompt;

    /**
     * Specifies the format of the response. Currently, the only supported value
     * is "json".
     */
    public String format;

    /**
     * An array of base64-encoded images, for use with multimodal models such as
     * llava.
     */
    public String[] images;

    /**
     * System prompt that overrides what is defined in the Modelfile, used to
     * specify custom behavior of the model.
     */
    public String system;

    /**
     * The full prompt or prompt template. Overrides what is defined in the
     * Modelfile.
     */
    public String template;

    /**
     * If true, no formatting will be applied to the prompt, and no context will
     * be returned. Use this when specifying a full templated prompt in your
     * request to the API and managing history yourself.
     */
    public Boolean raw = false;

    /**
     * If false, the response will be returned as a single response object,
     * rather than a stream of objects.
     */
    public Boolean stream = false;

    /**
     * Specifies the context tokens for the model to consider when generating a
     * response.
     */
    public Integer[] context;

    /**
     * Specifies the time the Ollama service should keep the model loaded. Can
     * be specified in minutes (m), seconds (s), or hours (h). The default is
     * 5m. A negative value indicates an infinite keep-alive period.
     */
    @JsonProperty(value = "keep_alive")
    public String keepAlive;

    /**
     * This is defined in the API spec as "the text after the model response".
     */
    @JsonProperty(value = "suffix")
    public String suffix;

    /**
     * Additional model parameters as documented for the Modelfile, such as
     * temperature and other settings that affect the generation.
     */
    public Options options;

}
