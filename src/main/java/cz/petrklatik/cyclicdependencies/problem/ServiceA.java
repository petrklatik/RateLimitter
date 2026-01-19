package cz.petrklatik.cyclicdependencies.problem;

/**
 * Cyclic dependency example - ServiceA depends on ServiceB.
 * Not a @Service - just demonstrates the problem.
 */
public class ServiceA {

    private final ServiceB serviceB;

    public ServiceA(ServiceB serviceB) {
        this.serviceB = serviceB;
    }

    public String processA() {
        return "A processing with: " + serviceB.getDataFromB();
    }

    public String getDataFromA() {
        return "Data from A";
    }
}
