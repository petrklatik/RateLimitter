package cz.petrklatik.cyclicdependencies.lazy;

import org.springframework.stereotype.Service;

/**
 * Normal bean - cycle broken by @Lazy in ServiceA.
 */
@Service("lazyServiceB")
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
