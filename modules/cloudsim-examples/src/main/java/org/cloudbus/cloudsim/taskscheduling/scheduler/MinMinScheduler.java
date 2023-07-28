package org.cloudbus.cloudsim.taskscheduling.scheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

import java.util.List;

public class MinMinScheduler extends TaskScheduler {

    @Override
    public int[] getAssignments(List<Cloudlet> cloudletList, List<Vm> vmList) {
        int numberOfCloudlets = cloudletList.size();
        int numberOfVms = vmList.size();

        double[][] executionTimes = getExecutionTimes(cloudletList, vmList);
        boolean[] isCloudletScheduled = new boolean[numberOfCloudlets];

        double[] completionTime = new double[numberOfVms];

        int[] scheduledVms = new int[numberOfCloudlets];

        for (int i = 0; i < numberOfCloudlets; i++) {
            int minCloudletIdx = -1;
            int minVmIdx = -1;
            double minCompletionTime = Double.MAX_VALUE;

            for (int clIdx = 0; clIdx < numberOfCloudlets; clIdx++) {
                if (!isCloudletScheduled[clIdx]) {
                    for (int vmIdx = 0; vmIdx < numberOfVms; vmIdx++) {
                        if (executionTimes[clIdx][vmIdx] + completionTime[vmIdx] < minCompletionTime) {
                            minCompletionTime = executionTimes[clIdx][vmIdx] + completionTime[vmIdx];
                            minCloudletIdx = clIdx;
                            minVmIdx = vmIdx;
                        }
                    }
                }
            }

            isCloudletScheduled[minCloudletIdx] = true;
            scheduledVms[minCloudletIdx] = minVmIdx;
            completionTime[minVmIdx] = minCompletionTime;
        }

        return scheduledVms;
    }
}