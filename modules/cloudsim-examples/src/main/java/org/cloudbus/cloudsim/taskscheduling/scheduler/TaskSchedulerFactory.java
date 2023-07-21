package org.cloudbus.cloudsim.taskscheduling.scheduler;

public class TaskSchedulerFactory {
    public static TaskScheduler createScheduler(String schedulerType, Object... args) {
        switch (schedulerType) {
            case "mm":
                return new MinMinScheduler();
            case "lbimm":
                return new LoadBalanceImprovedMinMinScheduler();
            case "remm":
                return new ReschedulingEnhancedMinMinScheduler();
            case "sjf":
                return new ShortestJobFirstScheduler();
            case "mxm":
                return new MaxMinScheduler();
            default:
                throw new IllegalArgumentException("Unknown scheduler type");
        }
    }
}
