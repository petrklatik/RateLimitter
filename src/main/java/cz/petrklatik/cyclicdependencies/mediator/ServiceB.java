package cz.petrklatik.cyclicdependencies.mediator;

import org.springframework.stereotype.Service;

/**
 * Communicates via Mediator - no direct dependency on ServiceA.
 */
@Service("mediatorServiceB")
public class ServiceB {

    public String getData() {
        return "Data from ServiceB";
    }

    public String processWithData(String externalData) {
        return "ServiceB processed: " + externalData;
    }
}
