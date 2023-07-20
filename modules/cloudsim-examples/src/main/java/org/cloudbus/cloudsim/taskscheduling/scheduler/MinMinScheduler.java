package org.cloudbus.cloudsim.taskscheduling.scheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

import java.util.List;

public class MinMinScheduler implements TaskScheduler {
    @Override
    public void schedule(DatacenterBroker broker) {
        List<Cloudlet> cloudletList = broker.getCloudletList();
        List<Vm> vmList = broker.getVmList();

        int numberOfCloudlets = cloudletList.size();
        int numberOfVms = vmList.size();

        double[][] executionTimes = new double[numberOfCloudlets][numberOfVms];

        for (int i = 0; i < numberOfCloudlets; i++) {
            Cloudlet cloudlet = cloudletList.get(i);
            for (int j = 0; j < numberOfVms; j++) {
                Vm vm = vmList.get(j);
                executionTimes[i][j] = cloudlet.getCloudletTotalLength() / vm.getMips();
            }
        }

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

        for (int i = 0; i < numberOfCloudlets; i++) {
            Cloudlet cloudlet = cloudletList.get(i);
            Vm vm = vmList.get(scheduledVms[i]);
            broker.bindCloudletToVm(cloudlet.getCloudletId(), vm.getId());
        }
    }
}
