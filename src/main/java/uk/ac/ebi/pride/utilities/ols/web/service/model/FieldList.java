package uk.ac.ebi.pride.utilities.ols.web.service.model;

/**
 * @author olgavrou
 */
public class FieldList implements Joinable {

    private String iri;
    private String label;
    private String shortForm;
    private String oboId;
    private String ontologyName;
    private String ontologyPrefix;
    private String description;
    private String type;
    private String synonym;
    private String score;
    private String ontologyIri;
    private String isDefiningOntology;

    private FieldList(String iri, String label, String shortForm, String oboId, String ontologyName, String ontologyPrefix, String description, String type, String synonym, String score, String ontologyIri, String isDefiningOntology) {
        this.iri = iri;
        this.label = label;
        this.shortForm = shortForm;
        this.oboId = oboId;
        this.ontologyName = ontologyName;
        this.ontologyPrefix = ontologyPrefix;
        this.description = description;
        this.type = type;
        this.synonym = synonym;
        this.score = score;
        this.ontologyIri = ontologyIri;
        this.isDefiningOntology = isDefiningOntology;
    }

    private String getIri() {
        return iri;
    }

    private String getLabel() {
        return label;
    }

    private String getShortForm() {
        return shortForm;
    }

    private String getOboId() {
        return oboId;
    }

    private String getOntologyName() {
        return ontologyName;
    }

    private String getOntologyPrefix() {
        return ontologyPrefix;
    }

    private String getDescription() {
        return description;
    }

    private String getType() {
        return type;
    }

    private String getSynonym() {
        return synonym;
    }

    private String getScore() {
        return score;
    }

    private String getOntologyIri() {
        return ontologyIri;
    }

    private String getIsDefiningOntology() {
        return isDefiningOntology;
    }


    @Override
    public String toString() {
        return "fieldList=" + this.join();
    }

    public static class FieldListBuilder {

        private String iri;
        private String label;
        private String shortForm;
        private String oboId;
        private String ontologyName;
        private String ontologyPrefix;
        private String description;
        private String type;
        private String synonym;
        private String score;
        private String ontologyIri;
        private String isDefiningOntology;

        public FieldListBuilder() {
        }

        public FieldList build() {
            return new FieldList(iri, label, shortForm, oboId, ontologyName, ontologyPrefix, description, type, synonym, score, ontologyIri, isDefiningOntology);
        }

        public FieldListBuilder setIri() {
            this.iri = "iri";
            return this;
        }

        public FieldListBuilder setLabel() {
            this.label = "label";
            return this;
        }

        public FieldListBuilder setShortForm() {
            this.shortForm = "short_form";
            return this;
        }

        public FieldListBuilder setOboId() {
            this.oboId = "obo_id";
            return this;
        }

        public FieldListBuilder setOntologyName() {
            this.ontologyName = "ontology_name";
            return this;
        }

        public FieldListBuilder setOntologyPrefix() {
            this.ontologyPrefix = "ontology_prefix";
            return this;
        }

        public FieldListBuilder setDescription() {
            this.description = "description";
            return this;
        }

        public FieldListBuilder setType() {
            this.type = "type";
            return this;
        }

        public FieldListBuilder setSynonyms() {
            this.synonym = "synonym";
            return this;
        }

        public FieldListBuilder setScore() {
            this.score = "score";
            return this;
        }

        public FieldListBuilder setOntologyIri() {
            this.ontologyIri = "ontology_iri";
            return this;
        }

        public FieldListBuilder setIsDefiningOntology() {
            this.isDefiningOntology = "is_defining_ontology";
            return this;
        }


    }

}
