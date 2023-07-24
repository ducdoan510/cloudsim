package org.cloudbus.cloudsim.taskscheduling.scheduler;

public class AdaptiveGeneticAlgorithmScheduler extends GeneticAlgorithmScheduler {
    private double k1 = 0.9;
    private double k2 = 0.9;
    private double k3 = 0.6;
    private double k4 = 0.6;

    protected double[] getStat(double[] fitness) {
        double maxFitness = Double.MIN_VALUE;
        double totalFitness = 0;
        for (double f : fitness) {
            maxFitness = Math.max(f, maxFitness);
            totalFitness += f;
        }
        double avgFitness = totalFitness / fitness.length;

        double[] stat = new double[2];
        stat[0] = maxFitness;
        stat[1] = avgFitness;
        return stat;
    }

    @Override
    protected double getCrossoverProb(int parent1, int parent2, double[] fitness, int iter) {
        double fitness1 = fitness[parent1];
        double fitness2 = fitness[parent2];
        double largerFitness = Math.max(fitness1, fitness2);
        double[] stat = getStat(fitness);
        double maxFitness = stat[0];
        double avgFitness = stat[1];
        return largerFitness >= avgFitness ? (k1 * (maxFitness - largerFitness) / (maxFitness - avgFitness)) : k2;
    }

    @Override
    protected double getMutationProb(int idx, double[] fitness, int iter) {
        double[] stat = getStat(fitness);
        double maxFitness = stat[0];
        double avgFitness = stat[1];
        double f = fitness[idx];
        return f >= avgFitness ? (k3 * (maxFitness - f) / (maxFitness - avgFitness)) : k4;
    }
}
