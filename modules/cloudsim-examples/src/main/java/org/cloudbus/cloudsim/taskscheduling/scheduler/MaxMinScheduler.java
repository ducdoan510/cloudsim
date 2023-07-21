package org.cloudbus.cloudsim.taskscheduling.scheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.List;

public class MaxMinScheduler extends TaskScheduler {
    @Override
    protected int[] getAssignments(List<Cloudlet> cloudletList, List<Vm> vmList) {
        int numberOfCloudlets = cloudletList.size();
        int numberOfVms = vmList.size();

        double[][] executionTimes = getExecutionTimes(cloudletList, vmList);
        boolean[] isCloudletScheduled = new boolean[numberOfCloudlets];

        double[] completionTime = new double[numberOfVms];

        int[] scheduledVms = new int[numberOfCloudlets];

        for (int i = 0; i < numberOfCloudlets; i++) {
            int maxCloudletIdx = -1;
            int maxVmIdx = -1;
            double maxCompletionTime = Double.MIN_VALUE;

            for (int clIdx = 0; clIdx < numberOfCloudlets; clIdx++) {
                if (!isCloudletScheduled[clIdx]) {
                    for (int vmIdx = 0; vmIdx < numberOfVms; vmIdx++) {
                        if (executionTimes[clIdx][vmIdx] + completionTime[vmIdx] > maxCompletionTime) {
                            maxCompletionTime = executionTimes[clIdx][vmIdx] + completionTime[vmIdx];
                            maxCloudletIdx = clIdx;
                            maxVmIdx = vmIdx;
                        }
                    }
                }
            }

            isCloudletScheduled[maxCloudletIdx] = true;
            scheduledVms[maxCloudletIdx] = maxVmIdx;
            completionTime[maxVmIdx] = maxCompletionTime;
        }
        return scheduledVms;
    }
}
