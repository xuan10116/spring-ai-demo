package org.springframework.ai.mcp.samples.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class QuestionController {

    private final ChatClient chatClient;

    public QuestionController(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools) {
        this.chatClient = chatClientBuilder
                .defaultTools(tools)
                .build();
    }

    @GetMapping("/ask")
    public Mono<String> askQuestion(@RequestParam(required = false, defaultValue = "北京的天气如何？") String question) {
        System.out.println("question: " + question);

        return Mono.fromCallable(() -> {
                    // 阻塞代码在这里执行
                    return chatClient.prompt(question).call().content();
                })
                .subscribeOn(Schedulers.boundedElastic()); // 调度到阻塞线程池
    }
}