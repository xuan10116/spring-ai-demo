package com.alibaba.cloud.ai.example.chat.ollama.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 用于配置 Ollama Chat 相关的默认参数。
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "ollama.chat")
public class OllamaChatProperties {

    /**
     * 默认提示语
     */
    private String defaultPrompt;

    /**
     * 系统提示语
     */
    private String systemPrompt;

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    /**
     * 默认使用的模型名称
     */
    private String model;

    /**
     * TopP 参数，默认 0.7
     */
    private Double topP;

    /**
     * 温度参数，控制生成文本的随机性，默认 0.7
     */
    private Double temperature;

    /**
     * 最大生成 token 数量，默认 256
     */
    private Integer maxTokens;

}