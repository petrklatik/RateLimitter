package cz.petrklatik.cyclicdependencies.mediator;

import org.springframework.stereotype.Service;

/**
 * Communicates via Mediator - no direct dependency on ServiceB.
 */
@Service("mediatorServiceA")
public class ServiceA {

    public String getData() {
        return "Data from ServiceA";
    }

    public String processWithData(String externalData) {
        return "ServiceA processed: " + externalData;
    }
}
