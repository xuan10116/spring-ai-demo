package com.alibaba.cloud.ai.example.chat.ollama.controller;

import com.alibaba.cloud.ai.example.chat.ollama.config.OllamaChatProperties;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class OllamaChatClientController {

	static final String DEFAULT_PROMPT = "你好，介绍下你自己！请用中文回答。";

	private final ChatModel chatModel;
	private final OllamaChatProperties chatProperties;

	private ChatClient ollamaiChatClient;

	@PostConstruct
	public void initChatClient() {
		this.ollamaiChatClient = ChatClient.builder(chatModel)
			.defaultAdvisors(new SimpleLoggerAdvisor())
			.defaultOptions(OllamaOptions.builder()
				.topP(chatProperties.getTopP())
				.model(chatProperties.getModel())
				.build())
			.build();
	}

	/**
	 * ChatClient 简单调用 - 使用默认 Prompt
	 */
	@GetMapping("/simple/chat")
	public String simpleChat() {
		return ollamaiChatClient.prompt(DEFAULT_PROMPT).call().content();
	}

	/**
	 * ChatClient 流式调用 - 返回 Flux<String>，适用于 Reactor 响应式编程
	 */
	@GetMapping("/stream/chat")
	public Flux<String> streamChat(HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		return ollamaiChatClient.prompt(DEFAULT_PROMPT).stream().content();
	}

	// ============ 新增功能与使用模式 ============ //

	/**
	 * 带用户输入的 Prompt 调用
	 * 
	 * 示例：/client/chat?userPrompt=帮我写一首诗
	 */
	@GetMapping("/chat")
	public String chatWithUserInput(String userPrompt) {
		return ollamaiChatClient.prompt(userPrompt != null ? userPrompt : DEFAULT_PROMPT)
				.call()
				.content();
	}

	/**
	 * Server-Sent Events (SSE) 流式输出
	 * 
	 * 示例：/client/sse/chat?userPrompt=帮我写一首诗
	 */
	@GetMapping(value = "/sse/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> sseStreamChat(String userPrompt) {
		return ollamaiChatClient.prompt(userPrompt != null ? userPrompt : DEFAULT_PROMPT)
				.stream()
				.content();
	}

	/**
	 * 带系统提示词（System Prompt）和用户提示词（User Prompt）交互
	 * 
	 * 示例：/client/with/system/prompt?userPrompt=帮我写一首诗
	 */
	@GetMapping("/with/system/prompt")
	public String chatWithSystemPrompt(String userPrompt) {
		return ollamaiChatClient
			.prompt()
			.system("你是一个专业的助手，请用中文回答。")
			.user(userPrompt != null ? userPrompt : DEFAULT_PROMPT)
			.call()
			.content();
	}

	/**
	 * 设置单次调用的 Options 参数（覆盖默认值）
	 * 
	 * 示例：/client/custom/options?userPrompt=帮我写一首诗
	 */
	@GetMapping("/custom/options")
	public String customOptionsCall(String userPrompt) {
		return ollamaiChatClient
			.prompt(userPrompt != null ? userPrompt : DEFAULT_PROMPT)
			.options(OllamaOptions.builder().temperature(0.9).topP(0.85).build())
			.call()
			.content();
	}

}
