package org.cloudbus.cloudsim.taskscheduling.scheduler;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithmScheduler extends TaskScheduler {
    protected int pop_size = 100;
    private double mutationProb = 0.1;
    private double crossoverProb = 0.7;

    protected int maxIter = 100;

    private double fitnessFunction(List<Integer> schedule, double[][] executionTime) {
        int ncloudlets = executionTime.length;
        int nvms = executionTime[0].length;

        double[] vmExecutionTime = new double[nvms];
        for (int clIdx = 0; clIdx < ncloudlets; clIdx++) {
            int vmIdx = schedule.get(clIdx);
            vmExecutionTime[vmIdx] += executionTime[clIdx][vmIdx];
        }

        double maxVmExecutionTime = Double.MIN_VALUE;
        double totalVmExecutionTime = 0;
        for (int vmIdx = 0; vmIdx < nvms; vmIdx++) {
            maxVmExecutionTime = Math.max(maxVmExecutionTime, vmExecutionTime[vmIdx]);
            totalVmExecutionTime += vmExecutionTime[vmIdx];
        }

        double exeVar = 0;
        for (int vmIdx = 0; vmIdx < nvms; vmIdx++) {
            double vmTime = vmExecutionTime[vmIdx];
            exeVar += Math.pow((vmTime - totalVmExecutionTime / nvms), 2);
        }
        exeVar /= nvms;
        double exeStd = Math.sqrt(exeVar);
        return 1 / (maxVmExecutionTime + exeStd);
    }

    private List<Integer> generateRandomSchedule(int ncloudlets, int nvms) {
        List<Integer> schedule = new ArrayList<>();
        Random rand = new Random();
        for (int clIdx = 0; clIdx < ncloudlets; clIdx++) {
            schedule.add(rand.nextInt(nvms));
        }
        return schedule;
    }

    private List<List<Integer>> initPopulation(int ncloudlets, int nvms) {
        List<List<Integer>> population = new ArrayList<>();

        for (int i = 0; i < pop_size; i++) {
            population.add(generateRandomSchedule(ncloudlets, nvms));
        }
        return population;
    }

    protected double getCrossoverProb(int parent1, int parent2, double[] fitness, int iter) {
        return crossoverProb;
    }

    private void crossover(List<Integer> parent1, List<Integer> parent2, int parentIdx1, int parentIdx2, double[] fitness, int iter) {
        // perform 1-point crossover
        double prob = getCrossoverProb(parentIdx1, parentIdx2, fitness, iter);
        Random rand = new Random();
        if (rand.nextDouble() < prob) {
            int mutationPoint = rand.nextInt(parent1.size());
            for (int i = mutationPoint; i < parent1.size(); i++) {
                int tmp = parent1.get(i);
                parent1.set(i, parent2.get(i));
                parent2.set(i, tmp);
            }
        }
    }

    protected double getMutationProb(int idx, double[] fitness, int iter) {
        return mutationProb;
    }

    private void mutate(int nvms, List<Integer> schedule, int idx, double[] fitness, int iter) {
        // randomly mutation the schedule
        double prob = getMutationProb(idx, fitness, iter);
        Random rand = new Random();
        for (int i = 0; i < schedule.size(); i++) {
            if (rand.nextDouble() < prob) {
                schedule.set(i, rand.nextInt(nvms));
            }
        }
    }

    private double[] calcFitness(List<List<Integer>> population, double[][] executionTime) {
        double[] fitness = new double[population.size()];
        for (int i = 0; i < population.size(); i++) {
            fitness[i] = fitnessFunction(population.get(i), executionTime);
        }
        return fitness;
    }

    @Override
    protected int[] getAssignments(List<Cloudlet> cloudletList, List<Vm> vmList) {
        int ncloudlets = cloudletList.size();
        int nvms = vmList.size();
        int[] scheduledVms = new int[ncloudlets];
        double maxFitness = Double.MIN_VALUE;
        double[][] executionTime = getExecutionTimes(cloudletList, vmList);

        List<List<Integer>> population = initPopulation(ncloudlets, nvms);
        Random rand = new Random();
        double[] fitness = calcFitness(population, executionTime);

        for (int iter = 0; iter < maxIter; iter++) {
            // attempt to update the solution from the current population
            double totalFitness = 0;
            for (int i = 0; i < pop_size; i++) {
                totalFitness += fitness[i];
                if (fitness[i] > maxFitness) {
                    maxFitness = fitness[i];
                    scheduledVms = population.get(i).stream().mapToInt(Integer::intValue).toArray();
                }
            }
            // selection
            List<List<Integer>> nextPopulation = new ArrayList<>();
            for (int i = 0; i < pop_size; i++) {
                if (rand.nextDouble() < fitness[i] / totalFitness) {
                    nextPopulation.add(population.get(i));
                }
            }

            // crossover
            fitness = calcFitness(nextPopulation, executionTime);
            for (int i = 0; i < nextPopulation.size() - 1; i += 2) {
                crossover(nextPopulation.get(i), nextPopulation.get(i + 1), i, i + 1, fitness, iter);
            }

            // mutation
            fitness = calcFitness(nextPopulation, executionTime);
            for (int i = 0; i < nextPopulation.size(); i++) {
                mutate(nvms, nextPopulation.get(i), i, fitness, iter);
            }

            // add items to fill in the population
            while (nextPopulation.size() < pop_size) {
                nextPopulation.add(generateRandomSchedule(ncloudlets, nvms));
            }
            population = nextPopulation;
            fitness = calcFitness(population, executionTime);
        }


        return scheduledVms;
    }
}
