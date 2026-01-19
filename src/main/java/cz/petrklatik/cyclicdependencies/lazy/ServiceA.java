package cz.petrklatik.cyclicdependencies.lazy;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Uses @Lazy to break the cycle - proxy injected at startup, real bean on first use.
 */
@Service("lazyServiceA")
public class ServiceA {

    private final ServiceB serviceB;

    public ServiceA(@Lazy ServiceB serviceB) {
        this.serviceB = serviceB;
    }

    public String process() {
        return "ServiceA processing with: " + serviceB.getData();
    }

    public String getData() {
        return "Data from ServiceA";
    }
}
