package eu.amidst.tutorials.usingAmidst.examples;

import eu.amidst.core.distribution.Distribution;
import eu.amidst.core.inference.InferenceAlgorithm;
import eu.amidst.core.io.BayesianNetworkLoader;
import eu.amidst.core.models.BayesianNetwork;
import eu.amidst.core.variables.Assignment;
import eu.amidst.core.variables.HashMapAssignment;
import eu.amidst.core.variables.Variable;
import eu.amidst.core.variables.Variables;
import eu.amidst.huginlink.inference.HuginInference;

import java.io.IOException;

/**
 * Created by rcabanas on 23/05/16.
 */
public class StaticModelInference {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        BayesianNetwork bn  = BayesianNetworkLoader.loadFromFile("networks/simulated/exampleBN.bn");
        Variables variables = bn.getVariables();

        //Variabeles of interest
        Variable varTarget = variables.getVariableByName("LatentVar1");
        Variable varObserved = null;

        //we set the evidence
        Assignment assignment = new HashMapAssignment(2);
        varObserved = variables.getVariableByName("GaussianVar1");
        assignment.setValue(varObserved,6.5);

        //we set the algorithm
        InferenceAlgorithm infer = new HuginInference();
        //((ImportanceSampling)infer).setSampleSize(10000);
        //((VMP)infer).setThreshold(0.00001);
        //((VMP)infer).setMaxIter(10000000);
        //((VMP)infer).setOutput(true);
        infer.setModel(bn);
        infer.setEvidence(assignment);

        //query
        infer.runInference();
        Distribution p = infer.getPosterior(varTarget);
        System.out.println("P(LatentVar1|GaussianVar1=6.5) = "+p);

        //Or some more refined queries
        System.out.println("P(0.7<LatentVar1<6.59 |GaussianVar1=6.5) = " + infer.getExpectedValue(varTarget, v -> (0.7 < v && v < 6.59) ? 1.0 : 0.0 ));

    }

}
