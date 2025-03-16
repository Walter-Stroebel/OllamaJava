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
