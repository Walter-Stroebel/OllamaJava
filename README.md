# OllamaJava
Easy use of Ollama from Java. No Spring nonsense, unreadable Lambda's or weird dependencies.

[View the Javadoc](https://walter-stroebel.github.io/OllamaJava)

## **Features**
- Simple, **direct HTTP interaction** with Ollama (local AI model runner).
- **Clean and efficient** Java 7+ compatible API.
- **Swing GUI support** for model selection.
- **Streaming API** for processing responses incrementally.
- **Model management** (list, details, tokens).
- **Chat interface** with automatic history merging.


## Usage Examples
### Initialize and List Available Models
```
OllamaJava ollama = new OllamaJava();
Config config = new Config();
String modelsJson = ollama.listModels(config);
System.out.println("Available models: " + modelsJson);
```

### Fetch Model Details
```
String modelInfo = ollama.modelDetails(config, "mistral");
System.out.println("Mistral model info:\n" + modelInfo);
```
### Basic Request
```
Request rq = new Request();
rq.model = "mistral";
rq.prompt = "What is the capital of France?";

Response response = ollama.request(config, rq);
System.out.println("Response: " + response.response);
```
### Streaming Request
```
ollama.askWithStream("mistral", "Tell me a joke.", new OllamaJava.StreamListener() {
    @Override
    public boolean onResponseReceived(StreamedResponse responsePart) {
        System.out.print(responsePart.response);
        return true; // Return false to stop early.
    }
});
```
### Chat Interaction
```
ChatRequest chat = new ChatRequest();
chat.model = "mistral";
chat.messages = new Message[]{ new Message("user", "Hello, how are you?") };

List<Response> chatResponses = ollama.chat(config, chat);
for (Response r : chatResponses) {
    System.out.println("Chatbot: " + r.response);
}
```
### Selecting a Model via GUI
```
String selectedModel = ollama.selectModel(config);
System.out.println("User selected model: " + selectedModel);
```
