package org.cloudbus.cloudsim.taskscheduling.scheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.List;

public class ShortestJobFirstScheduler extends TaskScheduler {
    @Override
    public int[] getAssignments(List<Cloudlet> cloudletList, List<Vm> vmList) {
        int numberOfCloudlets = cloudletList.size();
        int numberOfVms = vmList.size();

        double[][] executionTimes = getExecutionTimes(cloudletList, vmList);
        boolean[] isCloudletScheduled = new boolean[numberOfCloudlets];
        int[] scheduledVms = new int[numberOfCloudlets];

        for (int i = 0; i < numberOfCloudlets; i++) {
            int minCloudletIdx = -1;
            int minVmIdx = -1;
            double minExecutionTime = Double.MAX_VALUE;

            for (int j = 0; j < numberOfCloudlets; j++) {
                if (!isCloudletScheduled[j]) {
                    for (int k = 0; k < numberOfVms; k++) {
                        if (executionTimes[j][k] < minExecutionTime) {
                            minExecutionTime = executionTimes[j][k];
                            minCloudletIdx = j;
                            minVmIdx = k;
                        }
                    }
                }
            }

            isCloudletScheduled[minCloudletIdx] = true;
            scheduledVms[minCloudletIdx] = minVmIdx;
        }

        return scheduledVms;
    }
}
