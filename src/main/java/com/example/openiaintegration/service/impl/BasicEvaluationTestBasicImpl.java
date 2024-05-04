package com.example.openiaintegration.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BasicEvaluationTestBasicImpl implements com.example.openiaintegration.service.BasicEvaluationTest {

	private static final Logger logger = LoggerFactory.getLogger(BasicEvaluationTestBasicImpl.class);

	private final ChatClient openAiChatClient;

	@Value("classpath:/prompts/evaluation/qa-evaluator-accurate-answer.st")
	protected Resource qaEvaluatorAccurateAnswerResource;

	@Value("classpath:/prompts/evaluation/qa-evaluator-not-related-message.st")
	protected Resource qaEvaluatorNotRelatedResource;

	@Value("classpath:/prompts/evaluation/qa-evaluator-fact-based-answer.st")
	protected Resource qaEvaluatorFactBasedAnswerResource;

	@Value("classpath:/prompts/evaluation/user-evaluator-message.st")
	protected Resource userEvaluatorResource;

    public BasicEvaluationTestBasicImpl(ChatClient openAiChatClient) {
        this.openAiChatClient = openAiChatClient;
    }

    @Override
	public void evaluateQuestionAndAnswer(String question, ChatResponse response, boolean factBased) {

		String answer = response.getResult().getOutput().getContent();
		logger.info("Question: {}" , question);
		logger.info("Answer: {}" , answer);


		PromptTemplate userPromptTemplate = new PromptTemplate(userEvaluatorResource,
				Map.of("question", question, "answer", answer));


		SystemMessage systemMessage;
		if (factBased) {
			systemMessage = new SystemMessage(qaEvaluatorFactBasedAnswerResource);
		}
		else {
			systemMessage = new SystemMessage(qaEvaluatorAccurateAnswerResource);
		}


		Message userMessage = userPromptTemplate.createMessage();
		Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

		String yesOrNo = openAiChatClient.call(prompt).getResult().getOutput().getContent();


		logger.info("Is Answer related to question: {}" , yesOrNo);

		if (yesOrNo.equalsIgnoreCase("no")) {
			SystemMessage notRelatedSystemMessage = new SystemMessage(qaEvaluatorNotRelatedResource);
			prompt = new Prompt(List.of(userMessage, notRelatedSystemMessage));

			String reasonForFailure = openAiChatClient.call(prompt).getResult().getOutput().getContent();
			logger.info("Reason for failure: {}", reasonForFailure);
			logger.info("Answer is NOT related to question.");
		}
		else {
			logger.info("Answer is related to question.");
		}
	}

}