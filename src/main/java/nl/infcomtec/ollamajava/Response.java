package nl.infcomtec.ollamajava;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * This is the full response, returned in synchronous mode or at the end of a
 * streamed interaction.
 */
public class Response extends StreamedResponse {

    /**
     * An encoding of the conversation used in this response, this can be sent
     * in the next request to keep a conversational memory.
     */
    public List<Integer> context;

    /**
     * Time spent generating the response in nanoseconds.
     */
    @JsonProperty(value = "total_duration")
    public long totalDuration;

    /**
     * Time spent in nanoseconds loading the model.
     */
    @JsonProperty(value = "load_duration")
    public long loadDuration;

    /**
     * Number of samples generated.
     */
    @JsonProperty(value = "sample_count")
    public int sampleCount;

    /**
     * Time spent generating samples in nanoseconds.
     */
    @JsonProperty(value = "sample_duration")
    public long sampleDuration;

    /**
     * Number of tokens in the prompt.
     */
    @JsonProperty(value = "prompt_eval_count")
    public int promptEvalCount;

    /**
     * Time spent in nanoseconds evaluating the prompt.
     */
    @JsonProperty(value = "prompt_eval_duration")
    public long promptEvalDuration;

    /**
     * Number of tokens in the response.
     */
    @JsonProperty(value = "eval_count")
    public int evalCount;

    /**
     * Time in nanoseconds spent generating the response.
     */
    @JsonProperty(value = "eval_duration")
    public long evalDuration;

    /**
     * This seems to be a list of filled-in (by the model) templates as given in
     * the "tools" member of the ChatRequest.
     */
    @JsonProperty(value = "tool_calls")
    public JsonNode[] toolCalls;

    /**
     * The messages of the chat, this can be used to keep a chat memory.
     *
     * Only if the endpoint used was the chat endpoint.
     */
    public Message[] messages;

    /**
     * Calculates how fast the response is generated in tokens per second
     * (token/s).
     *
     * @return Tokens per second.
     */
    public double tokensPerSecond() {
        return 1E9 * evalCount / evalDuration;
    }
}
