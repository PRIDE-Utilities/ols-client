package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Creation date 03/03/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {

    @JsonProperty("id")
    String id;

    @JsonProperty("iri")
    Identifier iri;

    @JsonProperty("short_form")
    Identifier shortName;

    @JsonProperty("obo_id")
    Identifier oboId;

    @JsonProperty("label")
    String name;

    @JsonProperty("description")
    String[] description;

    @JsonProperty("ontology_name")
    String ontologyName;

    @JsonProperty("score")
    String score;

    @JsonProperty("ontology_iri")
    String ontologyIri;

    @JsonProperty("is_defining_ontology")
    boolean definedOntology;

    @JsonProperty("is_obsolete")
    boolean obsolete;

    @JsonProperty("annotation")
    Annotation annotation;


    @JsonProperty("obo_definition_citation")
    OboDefinitionCitation[] oboDefinitionCitation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Identifier getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = new Identifier(iri, Identifier.IdentifierType.IRI);
    }

    public Identifier getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = new Identifier(shortName, Identifier.IdentifierType.OWL);
    }

    public Identifier getOboId() {
        return oboId;
    }

    public void setOboId(String oboId) {
        this.oboId = new Identifier(oboId, Identifier.IdentifierType.OBO);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getDescription() {
        return description;
    }

    public void setDescription(String[] description) {
        this.description = description;
    }

    public String getOntologyName() {
        return ontologyName;
    }

    public void setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getOntologyIri() {
        return ontologyIri;
    }

    public void setOntologyIri(String ontologyIri) {
        this.ontologyIri = ontologyIri;
    }

    public boolean getIsDefiningOntology() {
        return definedOntology;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public void setIsDefiningOntology(boolean definedOntology) {
        this.definedOntology = definedOntology;
    }

    public OboDefinitionCitation[] getOboDefinitionCitation() {
        return oboDefinitionCitation;
    }

    public void setOboDefinitionCitation(OboDefinitionCitation[] oboDefinitionCitation) {
        this.oboDefinitionCitation = oboDefinitionCitation;
    }
}
