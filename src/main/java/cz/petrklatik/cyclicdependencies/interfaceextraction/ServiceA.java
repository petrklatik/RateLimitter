package cz.petrklatik.cyclicdependencies.interfaceextraction;

import org.springframework.stereotype.Service;

/**
 * Depends on CommonService interface, not concrete ServiceB.
 */
@Service("interfaceExtractionServiceA")
public class ServiceA {

    private final CommonService commonService;

    public ServiceA(CommonService commonService) {
        this.commonService = commonService;
    }

    public String process() {
        return "ServiceA processing with: " + commonService.getData();
    }
}
