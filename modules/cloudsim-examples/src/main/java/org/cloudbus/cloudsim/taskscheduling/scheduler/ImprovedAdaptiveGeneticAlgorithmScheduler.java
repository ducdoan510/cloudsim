package org.cloudbus.cloudsim.taskscheduling.scheduler;

public class ImprovedAdaptiveGeneticAlgorithmScheduler extends AdaptiveGeneticAlgorithmScheduler {
    private double k1 = 0.9;
    private double k2 = 0.9;
    private double k3 = 0.9;
    private double k4 = 0.9;

    @Override
    protected double getCrossoverProb(int parent1, int parent2, double[] fitness, int iter) {
        double fitness1 = fitness[parent1];
        double fitness2 = fitness[parent2];
        double largerFitness = Math.max(fitness1, fitness2);
        double[] stat = getStat(fitness);
        double maxFitness = stat[0];
        double avgFitness = stat[1];
        return largerFitness >= avgFitness ? ((k1 * (maxFitness - largerFitness) + Math.pow(k2, iter)) / (maxFitness - avgFitness + Math.pow(k2, iter))) : k2;
    }

    @Override
    protected double getMutationProb(int idx, double[] fitness, int iter) {
        double[] stat = getStat(fitness);
        double maxFitness = stat[0];
        double avgFitness = stat[1];
        double f = fitness[idx];
        return f >= avgFitness ? ((k3 * (maxFitness - f) + Math.pow(k4, iter)) / (maxFitness - avgFitness + Math.pow(k4, iter))) : k4;
    }
}
