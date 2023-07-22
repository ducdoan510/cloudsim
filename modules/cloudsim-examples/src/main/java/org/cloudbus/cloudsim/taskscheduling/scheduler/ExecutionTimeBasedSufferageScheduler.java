package org.cloudbus.cloudsim.taskscheduling.scheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.List;
import java.util.Random;

public class ExecutionTimeBasedSufferageScheduler extends TaskScheduler {
    static class SufferageInfo {
        public double mct;
        public double completionSufferage;
        public double executionSufferage;
        int mctVmIdx;
        public SufferageInfo(double mct, double completionSufferage, int mctVmIdx, double executionSufferage) {
            this.mct = mct;
            this.completionSufferage = completionSufferage;
            this.mctVmIdx = mctVmIdx;
            this.executionSufferage = executionSufferage;
        }
    }
    private SufferageInfo getSufferage(int clIdx, double[] vmCompletion, double[][] executionTime) {
        // calculate completion sufferage
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

        // calculate execution sufferage
        double met = Double.MAX_VALUE;
        double secondMet = Double.MAX_VALUE;
        for (int vmIdx = 0; vmIdx < numberOfVms; vmIdx++) {
            double expectedExecutionTime = executionTime[clIdx][vmIdx];
            if (expectedExecutionTime < met) {
                secondMet = met;
                met = expectedExecutionTime;
            } else if (expectedExecutionTime > met && expectedExecutionTime < secondMet) {
                secondMet = expectedExecutionTime;
            }
        }

        return new SufferageInfo(mct, secondMct - mct, mctVmIdx, secondMet - met);
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
                SufferageInfo si = getSufferage(clIdx, vmCompletionTime, executionTime);
                if (si.completionSufferage > si.executionSufferage) {
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
