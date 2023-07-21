package org.cloudbus.cloudsim.taskscheduling.scheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

import java.util.*;

public abstract class TaskScheduler {
    protected double[][] getExecutionTimes(List<Cloudlet> cloudletList, List<Vm> vmList) {
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
        return executionTimes;
    }

    public void schedule(DatacenterBroker broker) {
        List<Cloudlet> cloudletList = broker.getCloudletList();
        List<Vm> vmList = broker.getVmList();
        int[] scheduledVms = getAssignments(cloudletList, vmList);
        for (int i = 0; i < cloudletList.size(); i++) {
            Cloudlet cloudlet = cloudletList.get(i);
            Vm vm = vmList.get(scheduledVms[i]);
            broker.bindCloudletToVm(cloudlet.getCloudletId(), vm.getId());
        }
    }

    protected abstract int[] getAssignments(List<Cloudlet> cloudletList, List<Vm> vmList);

    protected Map<Integer, List<Integer>> getScheduledCloudletsOnVms(int[] scheduledVms) {
        Map<Integer, List<Integer>> scheduledCloudlets = new HashMap<>();
        for (int clIdx = 0; clIdx < scheduledVms.length; clIdx++) {
            int vmIdx = scheduledVms[clIdx];
            scheduledCloudlets.putIfAbsent(vmIdx, new LinkedList<>());
            scheduledCloudlets.get(vmIdx).add(clIdx);
        }
        return scheduledCloudlets;
    }

    protected Map<Integer, Double> getVmTotalExecutionTime(int[] scheduledVms, List<Cloudlet> cloudletList, List<Vm> vmList) {
        Map<Integer, Double> vmTotalExecutionTime = new HashMap<>();
        Map<Integer, List<Integer>> scheduledCloudlets = getScheduledCloudletsOnVms(scheduledVms);
        for (int vmIdx = 0; vmIdx < vmList.size(); vmIdx++) {
            int totalMips = 0;
            if (scheduledCloudlets.containsKey(vmIdx)) {
                for (int clIdx : scheduledCloudlets.get(vmIdx)) {
                    totalMips += cloudletList.get(clIdx).getCloudletTotalLength();
                }
                vmTotalExecutionTime.put(vmIdx, totalMips / vmList.get(vmIdx).getMips());
            }
        }
        return vmTotalExecutionTime;
    }

    protected Map.Entry<Integer, Double> getHeaviestLoadVm(int[] scheduledVms, List<Cloudlet> cloudletList, List<Vm> vmList) {
        double maxTotalExecutionTime = Double.MIN_VALUE;
        int maxVm = -1;

        Map<Integer, Double> vmTotalExecutionTime = getVmTotalExecutionTime(scheduledVms, cloudletList, vmList);

        for (Map.Entry<Integer, Double> entry : vmTotalExecutionTime.entrySet()) {
            int vmIdx = entry.getKey();
            double totalExecutionTime = entry.getValue();
            if (totalExecutionTime > maxTotalExecutionTime) {
                maxTotalExecutionTime = totalExecutionTime;
                maxVm = vmIdx;
            }
        }
        return new AbstractMap.SimpleEntry<>(maxVm, maxTotalExecutionTime);
    }
}
