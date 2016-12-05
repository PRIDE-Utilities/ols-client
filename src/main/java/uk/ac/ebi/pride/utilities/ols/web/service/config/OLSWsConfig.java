package uk.ac.ebi.pride.utilities.ols.web.service.config;

import uk.ac.ebi.pride.utilities.ols.web.service.utils.Constants;
import uk.ac.ebi.pride.utilities.ols.web.service.utils.PropertiesManager;

/**
 * @author ypriverol
 *
 */
public class OLSWsConfig extends AbstractOLSWsConfig{
    private String hostName;
    private String protocol;

    public OLSWsConfig() {
        if ( PropertiesManager.getPropertyValue("ols.server") != null) {
            this.hostName = PropertiesManager.getPropertyValue("ols.server");
        } else {
            this.hostName = Constants.OLS_SERVER;
        }
        if (PropertiesManager.getPropertyValue("ols.protocol") != null){
            this.protocol = PropertiesManager.getPropertyValue("ols.protocol");
        } else {
            this.protocol = Constants.OLS_PROTOCOL;
        }
    }

    public OLSWsConfig(String hostName){
        this.hostName = hostName;
        if (PropertiesManager.getPropertyValue("ols.protocol") != null){
            this.protocol = PropertiesManager.getPropertyValue("ols.protocol");
        } else {
            this.protocol = Constants.OLS_PROTOCOL;
        }
    }

    public OLSWsConfig(String hostName, String protocol){
        this.hostName = hostName;
        this.protocol = protocol;
    }

    public String getHostName() {
        return hostName;
    }


    public String getProtocol() {
        return protocol;
    }

}
