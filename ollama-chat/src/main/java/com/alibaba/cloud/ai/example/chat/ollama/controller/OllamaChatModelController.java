package com.alibaba.cloud.ai.example.chat.ollama.controller;

import com.alibaba.cloud.ai.example.chat.ollama.config.OllamaChatProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.alibaba.cloud.ai.example.chat.ollama.controller.OllamaChatClientController.DEFAULT_PROMPT;

 // ============ Spring AI ChatModel 使用模式说明 ============ //
 /**
 * 1. 构建方式：通过构造函数注入 ChatModel 实例。
 * 2. 提示词构造：支持直接传入字符串或使用 Builder 构建包含 system/user/assistant 角色的复杂 prompt。
 * 3. 调用方式：同步 call() 或异步 stream()。
 * 4. 参数设置：可通过 Prompt 构造器注入自定义 OllamaOptions。
 * 5. 输出处理：返回 ChatResponse 对象，可提取文本内容或结构化数据。
 */
@RestController
@RequestMapping("/model")
public class OllamaChatModelController {

    private final ChatModel ollamaChatModel;
    private final OllamaChatProperties chatProperties;
    private final ChatModel chatModel;

    public OllamaChatModelController(ChatModel chatModel, OllamaChatProperties chatProperties) {
        this.ollamaChatModel = chatModel;
		this.chatProperties = chatProperties;
        this.chatModel = chatModel;
    }

    /**
     * 最简单的使用方式，没有任何 LLMs 参数注入。
     *
     * @return String types.
     */
    @GetMapping("/simple/chat")
    public String simpleChat() {
        return ollamaChatModel.call(new Prompt(DEFAULT_PROMPT)).getResult().getOutput().getText();
    }

    /**
     * Stream 流式调用。可以使大模型的输出信息实现打字机效果。
     *
     * @return Flux<String> types.
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(HttpServletResponse response) {

        // 避免返回乱码
        response.setCharacterEncoding("UTF-8");

        Flux<ChatResponse> stream = ollamaChatModel.stream(new Prompt(DEFAULT_PROMPT));
        return stream.map(resp -> resp.getResult().getOutput().getText());
    }

    // ============ 新增功能与使用模式 ============ //

    /**
     * 带用户输入的 Prompt 调用
     * 
     * 示例：/model/chat?userPrompt=帮我写一首诗
     */
    @GetMapping("/chat")
    public String chatWithUserInput(String userPrompt) {
        return ollamaChatModel.call(new Prompt(userPrompt != null ? userPrompt : DEFAULT_PROMPT))
            .getResult()
            .getOutput()
            .getText();
    }

    /**
     * Server-Sent Events (SSE) 流式输出
     * 
     * 示例：/model/sse/chat?userPrompt=帮我写一首诗
     */
    @GetMapping(value = "/sse/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sseStreamChat(String userPrompt) {
        return ollamaChatModel.stream(new Prompt(userPrompt != null ? userPrompt : DEFAULT_PROMPT))
            .map(resp -> resp.getResult().getOutput().getText());
    }

    /**
     * 带系统提示词（System Prompt）和用户提示词（User Prompt）交互
     */
    @GetMapping("/with/system/prompt")
    public String chatWithSystemPrompt(String userPromptmessage) {
        String systemPrompt = chatProperties.getSystemPrompt();
        if (systemPrompt == null || systemPrompt.isEmpty()) {
            systemPrompt = "你是一个专业的助手，请用中文回答。";
        }
        String userPrompt = userPromptmessage != null ? userPromptmessage : chatProperties.getDefaultPrompt();
        final UserMessage userMessage = new UserMessage(userPrompt);
        final SystemMessage systemMessage = new SystemMessage(systemPrompt);
        final Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    /**
     * 设置单次调用的 Options 参数（覆盖默认值）
     * 
     * 示例：/model/custom/options?userPrompt=帮我写一首诗
     */
    @GetMapping("/custom/options")
    public String customOptionsCall(String userPrompt) {
        OllamaOptions options = OllamaOptions.builder()
            .temperature(chatProperties.getTemperature())
            .topP(chatProperties.getTopP())
            .model(chatProperties.getModel())
            .build();

        return ollamaChatModel.call(new Prompt(userPrompt != null ? userPrompt : DEFAULT_PROMPT, options))
            .getResult()
            .getOutput()
            .getText();
    }
}
