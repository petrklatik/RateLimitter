package cz.petrklatik.cyclicdependencies.objectprovider;

import org.springframework.stereotype.Service;

/**
 * Normal bean - cycle broken by ObjectProvider in ServiceA.
 */
@Service("objectProviderServiceB")
public class ServiceB {

    private final ServiceA serviceA;

    public ServiceB(ServiceA serviceA) {
        this.serviceA = serviceA;
    }

    public String process() {
        return "ServiceB processing with: " + serviceA.getData();
    }

    public String getData() {
        return "Data from ServiceB";
    }
}
