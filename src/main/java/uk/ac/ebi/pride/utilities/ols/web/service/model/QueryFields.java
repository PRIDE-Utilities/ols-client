package uk.ac.ebi.pride.utilities.ols.web.service.model;

/**
 * @author olgavrou
 */
public class QueryFields implements Joinable {

    private String label;
    private String synonym;
    private String description;
    private String shortForm;
    private String oboId;
    private String annotations;
    private String logicalDescription;
    private String iri;


    private QueryFields(String iri, String label, String shortForm, String oboId, String description, String annotations, String synonym, String logicalDescription) {
        this.iri = iri;
        this.label = label;
        this.shortForm = shortForm;
        this.oboId = oboId;
        this.description = description;
        this.annotations = annotations;
        this.synonym = synonym;
        this.logicalDescription = logicalDescription;
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


    private String getDescription() {
        return description;
    }

    private String getAnnotations() {
        return annotations;
    }

    private String getSynonym() {
        return synonym;
    }

    private String getLogicalDescription() {
        return logicalDescription;
    }

    @Override
    public String toString() {
        return "queryFields=" + this.join();
    }
    public static class QueryFieldBuilder {

        private String label;
        private String synonym;
        private String description;
        private String shortForm;
        private String oboId;
        private String annotations;
        private String logicalDescription;
        private String iri;

        public QueryFieldBuilder() {
        }

        public QueryFields build() {
            return new QueryFields(iri, label, shortForm, oboId, description, annotations, synonym, logicalDescription);
        }
        public QueryFieldBuilder setIri() {
            this.iri = "iri";
            return this;
        }

        public QueryFieldBuilder setLabel() {
            this.label = "label";
            return this;
        }

        public QueryFieldBuilder setShortForm() {
            this.shortForm = "short_form";
            return this;
        }

        public QueryFieldBuilder setOboId() {
            this.oboId = "obo_id";
            return this;
        }

        public QueryFieldBuilder setDescription() {
            this.description = "description";
            return this;
        }

        public QueryFieldBuilder setAnnotations() {
            this.annotations = "annotations";
            return this;
        }

        public QueryFieldBuilder setSynonym() {
            this.synonym = "synonym";
            return this;
        }

        public QueryFieldBuilder setLogicalDescription() {
            this.logicalDescription = "logical_description";
            return this;
        }


    }

}
