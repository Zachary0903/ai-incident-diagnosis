package com.incidentlab.agent;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Command-line incident diagnosis agent: an LLM behind Spring AI's ChatModel (Claude, or a local
 * model via Ollama) investigates the lab through read-only tools over Loki, Tempo and Prometheus,
 * then submits ranked root causes with evidence. See {@code cli.DiagnoseCommand} for usage.
 */
@SpringBootApplication
public class DiagnosisAgentApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(DiagnosisAgentApplication.class, withProvider(args))));
    }

    /**
     * Turns {@code --provider X} into {@code --spring.ai.model.chat=X}: Spring AI decides which
     * ChatModel to create while the application starts, before the command line is interpreted.
     */
    static String[] withProvider(String[] args) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--provider=")) {
                out.add("--spring.ai.model.chat=" + args[i].substring("--provider=".length()));
            } else if (args[i].equals("--provider") && i + 1 < args.length) {
                out.add("--spring.ai.model.chat=" + args[++i]);
            } else {
                out.add(args[i]);
            }
        }
        return out.toArray(String[]::new);
    }
}
