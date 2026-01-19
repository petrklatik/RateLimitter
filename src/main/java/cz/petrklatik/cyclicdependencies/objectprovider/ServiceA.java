package cz.petrklatik.cyclicdependencies.objectprovider;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Uses ObjectProvider for lazy lookup - bean retrieved on demand.
 */
@Service("objectProviderServiceA")
public class ServiceA {

    private final ObjectProvider<ServiceB> serviceBProvider;

    public ServiceA(ObjectProvider<ServiceB> serviceBProvider) {
        this.serviceBProvider = serviceBProvider;
    }

    public String process() {
        ServiceB serviceB = serviceBProvider.getObject();
        return "ServiceA processing with: " + serviceB.getData();
    }

    public String getData() {
        return "Data from ServiceA";
    }
}
