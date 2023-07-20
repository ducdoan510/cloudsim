package org.cloudbus.cloudsim.taskscheduling.scheduler;

public class TaskSchedulerFactory {
    public static TaskScheduler createScheduler(String schedulerType, Object... args) {
        switch (schedulerType) {
            case "minmin":
                return new MinMinScheduler();
            default:
                throw new IllegalArgumentException("Unknown scheduler type");
        }
    }
}
