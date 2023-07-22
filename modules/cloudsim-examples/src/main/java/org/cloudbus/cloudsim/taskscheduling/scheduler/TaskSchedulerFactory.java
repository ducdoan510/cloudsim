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
            default -> throw new IllegalArgumentException("Unknown scheduler type");
        };
    }
}
