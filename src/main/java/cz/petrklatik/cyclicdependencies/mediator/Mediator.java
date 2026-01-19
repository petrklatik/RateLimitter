package cz.petrklatik.cyclicdependencies.mediator;

import org.springframework.stereotype.Service;

/**
 * Central coordinator - services communicate through this.
 */
@Service
public class Mediator {

    private final ServiceA serviceA;
    private final ServiceB serviceB;

    public Mediator(ServiceA serviceA, ServiceB serviceB) {
        this.serviceA = serviceA;
        this.serviceB = serviceB;
    }

    public String coordinateAtoB() {
        String dataFromA = serviceA.getData();
        return serviceB.processWithData(dataFromA);
    }

    public String coordinateBtoA() {
        String dataFromB = serviceB.getData();
        return serviceA.processWithData(dataFromB);
    }
}
