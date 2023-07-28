package org.cloudbus.cloudsim.taskscheduling.scheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MaxMinScheduler extends TaskScheduler {
    @Override
    protected int[] getAssignments(List<Cloudlet> cloudletList, List<Vm> vmList) {
        int numberOfCloudlets = cloudletList.size();
        int numberOfVms = vmList.size();

        double[][] executionTimes = getExecutionTimes(cloudletList, vmList);
        int[] scheduledVms = new int[cloudletList.size()];
        double[] completionTime = new double[vmList.size()];

        List<Map.Entry<Integer, Long>> sortedCloudList = new ArrayList<>();
        for (int i = 0; i < cloudletList.size(); i++) {
            sortedCloudList.add(new AbstractMap.SimpleEntry<>(i, cloudletList.get(i).getCloudletTotalLength()));
        }
        sortedCloudList.sort((cl1, cl2) -> {
            if (cl1.getValue() > cl2.getValue()) return -1;
            if (cl1.getValue() < cl2.getValue()) return 1;
            return Integer.compare(cl1.getKey(), cl2.getKey());
        });

        for (int i = 0; i < cloudletList.size(); i++) {
            int clIdx = sortedCloudList.get(i).getKey();
            double minCompletionTime = Double.MAX_VALUE;
            int minVmIdx = -1;
            for (int vmIdx = 0; vmIdx < vmList.size(); vmIdx++) {
                if (completionTime[vmIdx] + executionTimes[clIdx][vmIdx] < minCompletionTime) {
                    minCompletionTime = completionTime[vmIdx] + executionTimes[clIdx][vmIdx];
                    minVmIdx = vmIdx;
                }
            }
            completionTime[minVmIdx] += executionTimes[clIdx][minVmIdx];
            scheduledVms[clIdx] = minVmIdx;
        }

        return scheduledVms;
    }
}
