package org.cloudbus.cloudsim.taskscheduling.scheduler;

public class TaskSchedulerFactory {
    public static TaskScheduler createScheduler(String schedulerType, Object... args) {
        return switch (schedulerType) {
            case "mm" -> new MinMinScheduler();
            case "lbimm" -> new LoadBalanceImprovedMinMinScheduler();
            case "remm" -> new ReschedulingEnhancedMinMinScheduler();
            case "sjf" -> new ShortestJobFirstScheduler();
            case "mxm" -> new MaxMinScheduler();
            case "heft" -> new HEFTScheduler();
            case "mheft" -> new ModifiedHEFT();
            case "ctsa" -> new CompletionTimeBasedSufferageScheduler();
            case "etsa" -> new ExecutionTimeBasedSufferageScheduler();
            case "aco" -> new AntColonyOptimizationScheduler();
            case "ga" -> new GeneticAlgorithmScheduler();
            case "aga" -> new AdaptiveGeneticAlgorithmScheduler();
            case "iaga" -> new ImprovedAdaptiveGeneticAlgorithmScheduler();
            default -> throw new IllegalArgumentException("Unknown scheduler type");
        };
    }
}
