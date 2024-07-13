package com.spring.ai.azure_learning.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
public class AzurePromptController {
	
	@Value("classpath:/prompts/joke-prompt.st")
	private Resource jokeResource;
	
	@Value("classpath:/docs/wikipedia-curling.md")
	private Resource docsToStuffResource;

	@Value("classpath:/prompts/qa-prompt.st")
	private Resource qaPromptResource;
		

	@Autowired
	private AzureOpenAiChatModel chatClient;
	
	
	@GetMapping("azure/ai/prompt")
    public AssistantMessage getPrompt(@RequestParam(value = "modelName", defaultValue = "gpt-4-turbo-2024-04-09") String modelName, @RequestParam(value = "adjective", defaultValue = "funny") String adjective,
			@RequestParam(value = "topic", defaultValue = "cows") String topic ) {
		PromptTemplate promptTemplate = new PromptTemplate(jokeResource);
		Prompt prompt = promptTemplate.create(Map.of("adjective", adjective, "topic", topic));
		return chatClient.call(prompt).getResult().getOutput();

	}
	
	@GetMapping("azure/ai/prompt/roles")
	public AssistantMessage generate(@RequestParam(value = "modelName", defaultValue = "gpt-4-turbo-2024-04-09") String modelName,
			@RequestParam(value = "message", defaultValue = "Tell me about three famous pirates from the Golden Age of Piracy and why they did.  Write at least a sentence for each pirate.") String message,
			@RequestParam(value = "name", defaultValue = "Bob") String name,
			@RequestParam(value = "voice", defaultValue = "pirate") String voice) {
		    String userText = """
			    Tell me about three famous pirates from the Golden Age of Piracy and why they did.
			    Write at least a sentence for each pirate.
			    """;

			Message userMessage = new UserMessage(userText);

			String systemText = """
			  You are a helpful AI assistant that helps people find information.
			  Your name is {name}
			  You should reply to the user's request with your name and also in the style of a {voice}.
			  """;

			SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
			Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", name, "voice", voice));

			Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

		    return chatClient.call(prompt).getResult().getOutput();
	}
	
	@GetMapping("azure/ai/prompt/qa")
	public Completion completion(@RequestParam(value = "message",
			defaultValue = "Which athletes won the mixed doubles gold medal in curling at the 2022 Winter Olympics?'") String message,
			@RequestParam(value = "stuffit", defaultValue = "false") boolean stuffit) {
		PromptTemplate promptTemplate = new PromptTemplate(qaPromptResource);
		Map<String, Object> map = new HashMap<>();
		map.put("question", message);
		if (stuffit) {
			map.put("context", docsToStuffResource);
		}
		else {
			map.put("context", "");
		}
		Prompt prompt = promptTemplate.create(map);
		Generation generation = chatClient.call(prompt).getResult();
		return new Completion(generation.getOutput().getContent());
	}
	
	@GetMapping("azure/ai/zeroshot")
    public Map generateStream(@RequestParam(value = "message", defaultValue = "Classify the text into neutral, negative or positive. \n"
    		+ "\n"
    		+ "Text: I think the vacation is okay.\n"
    		+ "Sentiment:") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return Map.of("generation",chatClient.call(prompt));
	}


}
