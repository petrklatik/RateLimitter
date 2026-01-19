package cz.petrklatik.cyclicdependencies.interfaceextraction;

import org.springframework.stereotype.Service;

/**
 * Implements CommonService - no dependency on ServiceA.
 */
@Service("interfaceExtractionServiceB")
public class ServiceB implements CommonService {

    @Override
    public String getData() {
        return "Data from ServiceB";
    }

    public String processB() {
        return "ServiceB own processing";
    }
}
