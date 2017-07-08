package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by olgavrou on 10/11/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RetrieveTermQuery {

    @JsonProperty("_embedded")
    TermEmbeddedSearchResult response;

    public TermEmbeddedSearchResult getResponse() {
        return response;
    }

    public void setResponse(TermEmbeddedSearchResult response) {
        this.response = response;
    }
}
