package com.spring.ai.azure_learning.controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import reactor.core.publisher.Flux;

@RestController
public class AzureChatController {
	
    private final AzureOpenAiChatModel chatModel;
    
    public AzureChatController(AzureOpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }
    
    @GetMapping("azure/ai/generate")
    public Map generateWithCustomInputParams(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message, @RequestParam(value = "modelName", defaultValue = "gpt-4-turbo-preview") String modelName, 
    		@RequestParam(value = "temparature", defaultValue = "0.7") Float temparature, @RequestParam(value = "presencePenalty", defaultValue = "1.0") Float presencePenalty,
    		@RequestParam(value = "frequencePenalty", defaultValue = "1.0") Float frequencePenalty, @RequestParam(value = "maxToken", defaultValue = "500") Integer maxToken) {
        
    	return Map.of("generation", chatModel.call(
    		    new Prompt(
    		            "Generate the names of 5 famous pirates.",
    		            AzureOpenAiChatOptions.builder()
    		            	.withDeploymentName(modelName)
    		            	.withTemperature(temparature)
    		            	.withFrequencyPenalty(frequencePenalty)
    		            	.withPresencePenalty(presencePenalty)
    		            	.withMaxTokens(maxToken)
    		            .build()
    		        )));
    }
    

    @GetMapping("azure/ai/generateStream")
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt);
    }

    
    @GetMapping("azure/ai/multimodality")
	public String generateMultimodality(@RequestParam(value = "message", defaultValue = "Explain what do you see on this picture?") String question, @RequestParam(value = "modelName", defaultValue = "gpt-4-turbo-2024-04-09") String modelName,
			@RequestParam(value = "imageURL", defaultValue = "https://docs.spring.io/spring-ai/reference/_images/multimodal.test.png") String imageURL) throws MalformedURLException {
    	URL url = new URL(imageURL);
    	String response = ChatClient.create(chatModel).prompt()
    	        .options(AzureOpenAiChatOptions.builder().withDeploymentName(modelName).build())
    	        .user(u -> u.text(question).media(MimeTypeUtils.IMAGE_PNG, url))
    	        .call()
    	        .content();
    	return response;
    }
}
