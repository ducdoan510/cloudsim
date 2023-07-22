package org.cloudbus.cloudsim.taskscheduling.scheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.List;
import java.util.Random;

public class CompletionTimeBasedSufferageScheduler extends TaskScheduler {
    static class SufferageInfo {
        public double mct;
        public double sufferage;
        int mctVmIdx;
        public SufferageInfo(double mct, double sufferage, int mctVmIdx) {
            this.mct = mct;
            this.sufferage = sufferage;
            this.mctVmIdx = mctVmIdx;
        }
    }
    private SufferageInfo getMctAndSufferage(int clIdx, double[] vmCompletion, double[][] executionTime) {
        double mct = Double.MAX_VALUE;
        double secondMct = Double.MAX_VALUE;
        int mctVmIdx = 0;
        int numberOfVms = executionTime[0].length;
        for (int vmIdx = 0; vmIdx < numberOfVms; vmIdx++) {
            double expectedCompletionTime = vmCompletion[vmIdx] + executionTime[clIdx][vmIdx];
            if (expectedCompletionTime < mct) {
                secondMct = mct;
                mct = expectedCompletionTime;
                mctVmIdx = vmIdx;
            } else if (expectedCompletionTime > mct && expectedCompletionTime < secondMct) {
                secondMct = expectedCompletionTime;
            }
        }
        return new SufferageInfo(mct, secondMct - mct, mctVmIdx);
    }
    @Override
    protected int[] getAssignments(List<Cloudlet> cloudletList, List<Vm> vmList) {
        int numberOfCloudlets = cloudletList.size();
        double[][] executionTime = getExecutionTimes(cloudletList, vmList);
        int[] scheduledVms = new int[cloudletList.size()];
        double[] vmCompletionTime = new double[vmList.size()];
        boolean[] isCloudletScheduled = new boolean[numberOfCloudlets];

        for (int i = 0; i < numberOfCloudlets; i++) {
            int selectedCloudlet = -1;
            int selectedVm = -1;
            for (int clIdx = 0; clIdx < numberOfCloudlets; clIdx++) {
                if (isCloudletScheduled[clIdx]) continue;
                SufferageInfo si = getMctAndSufferage(clIdx, vmCompletionTime, executionTime);
                if (si.mct < si.sufferage) {
                    selectedCloudlet = clIdx;
                    selectedVm = si.mctVmIdx;
                    break;
                }
            }
            if (selectedCloudlet == -1) {
                // if there is no way the allocation can reduce the sufferage, choose random
                for (int clIdx = 0; clIdx < numberOfCloudlets; clIdx++) {
                    if (!isCloudletScheduled[clIdx]) {
                        selectedCloudlet = clIdx;
                        break;
                    }
                }
                selectedVm = (new Random()).nextInt(vmList.size());
            }
            isCloudletScheduled[selectedCloudlet] = true;
            vmCompletionTime[selectedVm] += executionTime[selectedCloudlet][selectedVm];
            scheduledVms[selectedCloudlet] = selectedVm;
        }

        return scheduledVms;
    }
}
