package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OBODefinitionCitation {

    @JsonProperty("definition")
    String definition;

    @JsonProperty("oboXrefs")
    OBOXRefs[] oboXrefs;

    public OBODefinitionCitation() {
    }

    public OBODefinitionCitation(String definition, OBOXRefs[] oboXrefs) {
        this.definition = definition;
        this.oboXrefs = oboXrefs;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public OBOXRefs[] getOboXrefs() {
        return oboXrefs;
    }

    public void setOboXrefs(OBOXRefs[] oboXrefs) {
        this.oboXrefs = oboXrefs;
    }
}
