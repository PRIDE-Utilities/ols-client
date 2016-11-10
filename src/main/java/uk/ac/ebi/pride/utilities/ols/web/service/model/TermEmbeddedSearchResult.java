package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by olgavrou on 10/11/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TermEmbeddedSearchResult{

    @JsonProperty("terms")
    SearchResult[] searchResults;

    public SearchResult[] getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(SearchResult[] searchResults) {
        this.searchResults = searchResults;
    }
}
