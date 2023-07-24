package org.cloudbus.cloudsim.taskscheduling.scheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.*;

public class AntColonyOptimizationScheduler extends TaskScheduler {
    private double alpha;
    private double beta;
    private double ro;
    private int Q;
    private int tmax;
    private int numAnts;

    public AntColonyOptimizationScheduler() {
        this.alpha = 0.3;
        this.beta = 1;
        this.ro = 0.4;
        this.Q = 100;
        this.tmax = 100;
        this.numAnts = 8;
    }

    public AntColonyOptimizationScheduler(double alpha, double beta, double ro, int Q, int tmax, int m) {
        this.alpha = alpha;
        this.beta = beta;
        this.ro = ro;
        this.Q = Q;
        this.tmax = tmax;
        this.numAnts = m;
    }

    static private List<Integer> generateRandomNumbers(int m, int n) {
        Random rand = new Random();
        Set<Integer> ans = new HashSet<>();
        while (ans.size() < m) {
            int randNum = rand.nextInt(n);
            ans.add(randNum);
        }
        return List.copyOf(ans);
    }

    static private double getMakespan(double[][] executionTime, int startClIdx, List<Integer> assignment) {
        double makespan = 0;
        int ncloudlets = executionTime.length;
        int nvms = executionTime[0].length;
        double[] vmExecutionTime = new double[nvms];
        for (int clIdx = startClIdx; clIdx < Math.min(ncloudlets, startClIdx + nvms); clIdx++) {
            int vmIdx = assignment.get(clIdx - startClIdx);
            vmExecutionTime[vmIdx] += executionTime[clIdx][vmIdx];
            makespan = Math.max(makespan, vmExecutionTime[vmIdx]);
        }
        return makespan;
    }

    private List<Integer> acoAssignment(double[][] pheromones, double[][] executionTime, int startClIdx) {
        int ncloudlets = executionTime.length;
        int nvms = executionTime[0].length;
        int iter = 1;
        List<Integer> optimalAssignment = new LinkedList<>();
        double minMakespan = Double.MAX_VALUE;
        List<List<Integer>> antTours = new ArrayList<>();
        for (int m = 0; m < this.numAnts; m++) {
            antTours.add(new LinkedList<>());
        }

        while (iter < this.tmax) {
            List<Integer> initialVms = generateRandomNumbers(this.numAnts, nvms);
            for (int ant = 0; ant < this.numAnts; ant++) {
                antTours.get(ant).add(initialVms.get(ant));
            }

            // Each ant will finish a tour of all VMs
            for (int clIdx = startClIdx + 1; clIdx < Math.min(ncloudlets, startClIdx + nvms); clIdx++) {
                for (int ant = 0; ant < this.numAnts; ant++) {
                    int nextVm = -1;
                    double maxProb = Double.MIN_VALUE;
                    for (int vmIdx = 0; vmIdx < nvms; vmIdx++) {
                        if (antTours.get(ant).contains(vmIdx)) continue;
                        double prob = Math.pow(pheromones[clIdx][vmIdx], alpha) *
                                      Math.pow(1 / executionTime[clIdx][vmIdx], beta);
                        if (prob >= maxProb) {
                            maxProb = prob;
                            nextVm = vmIdx;
                        }
                    }
                    antTours.get(ant).add(nextVm);
                }
            }

            // Find the best solution and local update pheromones
            for (int clIdx = 0; clIdx < ncloudlets; clIdx++) {
                for (int vmIdx = 0; vmIdx < nvms; vmIdx++) {
                    pheromones[clIdx][vmIdx] *= (1 - ro);
                }
            }


            for (int ant = 0; ant < numAnts; ant++) {
                double makespan = getMakespan(executionTime, startClIdx, antTours.get(ant));
                for (int i = 0; i < antTours.get(ant).size(); i++) {
                    int vmIdx = antTours.get(ant).get(i);
                    int clIdx = startClIdx + i;
                    pheromones[clIdx][vmIdx] += Q / makespan;
                }
                if (minMakespan > makespan) {
                    minMakespan = makespan;
                    optimalAssignment = List.copyOf(antTours.get(ant));
                }
            }

            // Global pheromone update
            for (int clIdx = startClIdx; clIdx < Math.min(startClIdx + nvms, ncloudlets); clIdx++) {
                int vmIdx = optimalAssignment.get(clIdx - startClIdx);
                pheromones[clIdx][vmIdx] += Q / minMakespan;
            }

            // Reset tours
            for (int ant = 0; ant < this.numAnts; ant++) {
                antTours.get(ant).clear();
            }
            iter++;
        }
        return optimalAssignment;
    }
    @Override
    protected int[] getAssignments(List<Cloudlet> cloudletList, List<Vm> vmList) {
        int ncloudlets = cloudletList.size();
        int nvms = vmList.size();
        double[][] pheromones = new double[cloudletList.size()][vmList.size()];
        for (int clIx = 0; clIx < ncloudlets; clIx++) {
            for (int vmIdx = 0; vmIdx < nvms; vmIdx++) {
                pheromones[clIx][vmIdx] = 0.01;
            }
        }

        int[] scheduledVms = new int[ncloudlets];
        double[][] executionTime = getExecutionTimes(cloudletList, vmList);
        for (int startClIdx = 0; startClIdx < ncloudlets; startClIdx += nvms) {
            List<Integer> optimalAssignment = acoAssignment(pheromones, executionTime, startClIdx);
            for (int clIdx = startClIdx; clIdx < Math.min(startClIdx + nvms, ncloudlets); clIdx++)  {
                int vmIdx = optimalAssignment.get(clIdx - startClIdx);
                scheduledVms[clIdx] = vmIdx;
            }
        }

        return scheduledVms;
    }
}
