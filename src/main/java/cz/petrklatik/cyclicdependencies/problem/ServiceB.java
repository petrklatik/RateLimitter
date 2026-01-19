package cz.petrklatik.cyclicdependencies.problem;

/**
 * Cyclic dependency example - ServiceB depends on ServiceA.
 * Not a @Service - just demonstrates the problem.
 */
public class ServiceB {

    private final ServiceA serviceA;

    public ServiceB(ServiceA serviceA) {
        this.serviceA = serviceA;
    }

    public String processB() {
        return "B processing with: " + serviceA.getDataFromA();
    }

    public String getDataFromB() {
        return "Data from B";
    }
}
