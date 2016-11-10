package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by olgavrou on 10/11/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObsoleteTerm extends Term {

    @JsonProperty("is_obsolete")
    boolean obsolete;

    public ObsoleteTerm(Identifier iri, String label, String[] description,
                Identifier shortForm, Identifier oboId, String ontologyName, String score, String ontologyIri,
                boolean definedOntology,
                OboDefinitionCitation[] oboDefinitionCitation,
                Annotation annotation, boolean obsolete) {
        super(iri, label, description, shortForm, oboId, ontologyName, score, ontologyIri, definedOntology, oboDefinitionCitation, annotation);
        this.obsolete = obsolete;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }
}
