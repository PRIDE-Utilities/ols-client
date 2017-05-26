package uk.ac.ebi.pride.utilities.ols.web.service.config;

import uk.ac.ebi.pride.utilities.ols.web.service.utils.Constants;

/**
 * Created by olgavrou on 05/12/2016.
 */
public class OLSWsConfig extends AbstractOLSWsConfig {

    public OLSWsConfig() {
        super(Constants.OLS_PROTOCOL, Constants.OLS_SERVER);
    }

    public OLSWsConfig(String hostName){
        super(Constants.OLS_PROTOCOL, hostName);
    }

    public OLSWsConfig(String protocol, String hostName){
        super(protocol, hostName);
    }

}