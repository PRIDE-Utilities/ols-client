package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OBOXRefs {

    @JsonProperty("database")
    String database;

    @JsonProperty("id")
    String id;

    @JsonProperty("description")
    String description;

    public OBOXRefs() {
    }

    public OBOXRefs(String database, String id, String description) {
        this.database = database;
        this.id = id;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
