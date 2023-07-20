package org.cloudbus.cloudsim.taskscheduling.scheduler;

import org.cloudbus.cloudsim.DatacenterBroker;

public interface TaskScheduler {
    void schedule(DatacenterBroker broker);
}
