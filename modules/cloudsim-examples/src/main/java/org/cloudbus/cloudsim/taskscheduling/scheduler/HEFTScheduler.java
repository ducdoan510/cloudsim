package org.cloudbus.cloudsim.taskscheduling.scheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.*;

public class HEFTScheduler extends TaskScheduler {

    @Override
    protected int[] getAssignments(List<Cloudlet> cloudletList, List<Vm> vmList) {
        double[][] executionTimes = getExecutionTimes(cloudletList, vmList);
        int[] scheduledVms = new int[cloudletList.size()];
        List<Map.Entry<Long, Integer>> sortedCloudlets = new ArrayList<>();
        for (int clIdx = 0; clIdx < cloudletList.size(); clIdx++) {
            sortedCloudlets.add(new AbstractMap.SimpleEntry<>(cloudletList.get(clIdx).getCloudletTotalLength(), clIdx));
        }
        sortedCloudlets.sort((cl1, cl2) -> {
            if (cl1.getKey() > cl2.getKey()) return -1;
            if (cl1.getKey() < cl2.getKey()) return 1;
            return Integer.compare(cl1.getValue(), cl2.getValue());
        });

        double[] vmCompletionTime = new double[vmList.size()];
        for (int clIdx = 0; clIdx < cloudletList.size(); clIdx++) {
            int origClIdx = sortedCloudlets.get(clIdx).getValue();
            int minVmIdx = 0;
            double minCompletionTime = Double.MAX_VALUE;
            for (int vmIdx = 0; vmIdx < vmList.size(); vmIdx++) {
                if (minCompletionTime > executionTimes[origClIdx][vmIdx] + vmCompletionTime[vmIdx]) {
                    minCompletionTime = executionTimes[origClIdx][vmIdx] + vmCompletionTime[vmIdx];
                    minVmIdx = vmIdx;
                }
            }
            vmCompletionTime[minVmIdx] += executionTimes[origClIdx][minVmIdx];
            scheduledVms[origClIdx] = minVmIdx;
        }
        return scheduledVms;
    }
}
