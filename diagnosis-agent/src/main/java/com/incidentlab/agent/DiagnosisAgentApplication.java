package com.incidentlab.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Week 2 (Nov 29 - Dec 5): the diagnosis agent.
 *
 * <p>TODO:
 * <ul>
 *   <li>{@code ObservabilityTools} with Spring AI {@code @Tool} methods:
 *       searchErrorLogs (Loki), getTrace (Tempo), queryMetric (Prometheus),
 *       listRecentChanges (Loki, logger "deployments").</li>
 *   <li>Filter out {@code /admin/faults} spans and logs so ground truth never leaks.</li>
 *   <li>Use a {@code ChatClient} with a system prompt that asks for a ranked list of
 *       root causes, each citing its evidence, as structured output.</li>
 *   <li>A CLI entry point: {@code diagnose --since 10m --symptom "orders failing"}.</li>
 * </ul>
 */
@SpringBootApplication
public class DiagnosisAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiagnosisAgentApplication.class, args);
    }
}
