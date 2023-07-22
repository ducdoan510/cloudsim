package org.cloudbus.cloudsim.taskscheduling.scheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.List;
import java.util.Map;

public class ReschedulingEnhancedMinMinScheduler extends MinMinScheduler {
    private boolean reschedule(int[] scheduledVms, List<Cloudlet> cloudletList, List<Vm> vmList) {
        Map.Entry<Integer, Double> heaviestVm = getHeaviestLoadVm(scheduledVms, cloudletList, vmList);
        int maxVm = heaviestVm.getKey();
        double makespan = heaviestVm.getValue();

        // get lightest task in heaviest vm
        int maxCloudlet = 0;
        long maxCloudletLength = Long.MIN_VALUE;
        for (int clIdx = 0; clIdx < scheduledVms.length; clIdx++) {
            Cloudlet cloudlet = cloudletList.get(clIdx);
            if (scheduledVms[clIdx] == maxVm) {
                if (cloudlet.getCloudletTotalLength() > maxCloudletLength) {
                    maxCloudletLength = cloudlet.getCloudletTotalLength();
                    maxCloudlet = clIdx;
                }
            }
        }

        // try rescheduling that longest task to other vm to see if makespan is improved
        double minMakespan = makespan;
        int rescheduledVm = -1;
        for (int vmIdx = 0; vmIdx < vmList.size(); vmIdx++) {
            if (vmIdx == maxVm) continue;
            scheduledVms[maxCloudlet] = vmIdx;
            double newMakespan = getHeaviestLoadVm(scheduledVms, cloudletList, vmList).getValue();
            if (newMakespan < minMakespan) {
                minMakespan = newMakespan;
                rescheduledVm = vmIdx;
            }
            scheduledVms[maxCloudlet] = maxVm;
        }
        if (rescheduledVm != -1) {
            scheduledVms[maxCloudlet] = rescheduledVm;
            return true;
        }
        return false;
    }

    @Override
    public int[] getAssignments(List<Cloudlet> cloudletList, List<Vm> vmList) {
        // Stage 1: Get the assignments from parent class
        int[] scheduledVms = super.getAssignments(cloudletList, vmList);

        // Stage 2: Rearrange the assignments until no more reassignment improves the makespan
        int iter = 0;
        while (iter < 60 && reschedule(scheduledVms, cloudletList, vmList)) {
//            System.out.println(Arrays.toStrinmmg(scheduledVms));
            iter++;
        }
        System.out.println("Number of rescheduling rounds: " + iter);

        return scheduledVms;
    }
}
