package org.cloudbus.cloudsim.taskscheduling.scheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModifiedHEFT extends TaskScheduler {
    @Override
    protected int[] getAssignments(List<Cloudlet> cloudletList, List<Vm> vmList) {
        // calculate average execution time across all VMs
        double[][] executionTimes = getExecutionTimes(cloudletList, vmList);
        double [] avgExecutionTimes = new double[cloudletList.size()];
        for (int clIdx = 0; clIdx < cloudletList.size(); clIdx++) {
            double sumExeTime = 0;
            for (int vmIdx = 0; vmIdx < vmList.size(); vmIdx++) {
                sumExeTime += executionTimes[clIdx][vmIdx];
            }
            avgExecutionTimes[clIdx] = sumExeTime / vmList.size();
        }

        // Proceed as per normal as the standard HEFT but using this average execution time for sorting instead
        List<Map.Entry<Double, Integer>> sortedCloudlets = new ArrayList<>();
        for (int clIdx = 0; clIdx < cloudletList.size(); clIdx++) {
            sortedCloudlets.add(new AbstractMap.SimpleEntry<>(avgExecutionTimes[clIdx], clIdx));
        }
        sortedCloudlets.sort((cl1, cl2) -> {
            if (cl1.getKey() > cl2.getKey()) return -1;
            if (cl1.getKey() < cl2.getKey()) return 1;
            return Integer.compare(cl1.getValue(), cl2.getValue());
        });

        double[] vmCompletionTime = new double[vmList.size()];
        int[] scheduledVms = new int[cloudletList.size()];

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
