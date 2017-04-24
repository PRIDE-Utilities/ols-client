package uk.ac.ebi.pride.utilities.ols.web.service.client;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.pride.utilities.ols.web.service.config.AbstractOLSWsConfig;
import uk.ac.ebi.pride.utilities.ols.web.service.model.*;
import uk.ac.ebi.pride.utilities.ols.web.service.utils.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;


/**
 * @author ypriverol
 */
public class OLSClient implements Client {

    protected RestTemplate restTemplate;
    protected AbstractOLSWsConfig config;

    private String queryField;
    private String fieldList;

    public static final String DEFAULT_QUERY_FIELD = new QueryFields.QueryFieldBuilder()
            .setLabel()
            .setSynonym()
            .build()
            .toString();
    public static final String DEFAULT_FIELD_LIST = new FieldList.FieldListBuilder()
            .setLabel()
            .setIri()
            .setScore()
            .setOntologyName()
            .setOboId()
            .setOntologyIri()
            .setIsDefiningOntology()
            .setShortForm()
            .setOntologyPrefix()
            .setDescription()
            .setType()
            .build()
            .toString();

    public String getQueryField() {
        if (queryField == null){
            queryField = DEFAULT_QUERY_FIELD;
        }
        return queryField;
    }

    public void setQueryField(String queryField) {
        this.queryField = queryField;
    }

    public String getFieldList() {
        if (fieldList == null){
            fieldList = DEFAULT_FIELD_LIST;
        }
        return fieldList;
    }

    public void setFieldList(String fieldList) {
        this.fieldList = fieldList;
    }

    org.slf4j.Logger logger = LoggerFactory.getLogger(OLSClient.class);


    /**
     * Default constructor for Archive clients
     *
     * @param config configuration to use.
     */
    public OLSClient(AbstractOLSWsConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }


    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AbstractOLSWsConfig getConfig() {
        return config;
    }

    public void setConfig(AbstractOLSWsConfig config) {
        this.config = config;
    }

    /**
     * This function retrieve the term by the accession of the term in the ontogoly and the id of the ontology
     * if the term is not found it, the NULL is returned.
     *
     * @param termId     Term ID in the ontology
     * @param ontologyId The ontology name
     * @return return the name of the Ontology term
     */
    public Term getTermById(Identifier termId, String ontologyId) throws RestClientException {
        if (termId != null && termId.getIdentifier() != null) {
            if (termId.getType() == Identifier.IdentifierType.OBO)
                return getTermByOBOId(termId.getIdentifier(), ontologyId);
            else if (termId.getType() == Identifier.IdentifierType.OWL)
                return getTermByShortName(termId.getIdentifier(), ontologyId);
            else if (termId.getType() == Identifier.IdentifierType.IRI)
                return getTermByIRIId(termId.getIdentifier(), ontologyId);
        }
        return null;
    }

    /**
     * Return a Term for an OBO Identifier and the ontology Identifier.
     *
     * @param termOBOId  OBO Identifier in OLS
     * @param ontologyId ontology Identifier
     * @return Term
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public Term getTermByOBOId(String termOBOId, String ontologyId) throws RestClientException {

        String url = String.format("%s://%s/api/ontologies/%s/terms?obo_id=%s",
                config.getProtocol(), config.getHostName(), ontologyId, termOBOId);

        logger.debug(url);

        TermQuery result = this.restTemplate.getForObject(url, TermQuery.class);

        if (result != null && result.getTerms() != null && result.getTerms().length == 1) {
            return result.getTerms()[0];
        }

        return null;
    }

    /**
     * Return a Term for a short name Identifier and the ontology Identifier.
     *
     * @param shortTerm  short term Identifier in OLS
     * @param ontologyId ontology Identifier
     * @return Term
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public Term getTermByShortName(String shortTerm, String ontologyId) throws RestClientException {

        String url = String.format("%s://%s/api/ontologies/%s/terms?short_term=%s",
                config.getProtocol(), config.getHostName(), ontologyId, shortTerm);

        logger.debug(url);

        TermQuery result = this.restTemplate.getForObject(url, TermQuery.class);

        if (result != null && result.getTerms() != null && result.getTerms().length == 1) {
            return result.getTerms()[0];
        }

        return null;
    }

    /**
     * Return a Term for an IRI identifier and the ontology Identifier.
     *
     * @param iriId      RI Identifier in OLS
     * @param ontologyId ontology Identifier
     * @return Term result term from OLS
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public Term getTermByIRIId(String iriId, String ontologyId) throws RestClientException {
        Term result = null;
        iriId = iriId.replaceAll(":", "%253A");
        iriId = iriId.replaceAll("/", "%252F");
        String url = config.getProtocol() + "://" + config.getHostName() +
            "api/ontologies/" + ontologyId.toLowerCase() + "/terms/";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).path(iriId);
        UriComponents components = builder.build(true);
        URI uri = components.toUri();
        logger.debug("" + uri);
        Term term = this.restTemplate.getForObject(uri, Term.class);
        if (term != null) {
            result = term;
        }
        return result;
    }

    public List<String> getTermDescription(Identifier termId, String ontologyId) throws RestClientException {
        Term term = getTermById(termId, ontologyId);
        List<String> description = new ArrayList<String>();
        if (term != null && term.getDescription() != null)
            for (String subDescription : term.getDescription())
                if (subDescription != null && !subDescription.isEmpty())
                    description.add(subDescription);
        return description;
    }

    /**
     * Get all annotations in a way Annotation ID and the list of values
     *
     * @param termId     Term ID in the ontology
     * @param ontologyId The ontology name
     * @return map of annotation IDs and their corresponding values.
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public Map<String, List<String>> getAnnotations(Identifier termId, String ontologyId) throws RestClientException {
        Term term = getTermById(termId, ontologyId);
        if (term != null && term.getAnnotation() != null)
            return term.getAnnotation().getAnnotation();
        return null;
    }

    /**
     * This function returns the current ontologies in OLS.
     *
     * @return List
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public List<Ontology> getOntologies() throws RestClientException {
        OntologyQuery currentOntologyQuery = getOntologyQuery(0);
        List<Ontology> ontologies = new ArrayList<Ontology>();
        ontologies.addAll(Arrays.asList(currentOntologyQuery.getOntolgoies()));
        if (currentOntologyQuery != null) {
            if (currentOntologyQuery.getOntolgoies().length < currentOntologyQuery.getPage().getTotalElements()) {
                for (int i = 1; i < currentOntologyQuery.getPage().getTotalElements() / currentOntologyQuery.getOntolgoies().length + 1; i++) {
                    OntologyQuery ontologyQuery = getOntologyQuery(i);
                    if (ontologyQuery != null && ontologyQuery.getOntolgoies() != null)
                        ontologies.addAll(Arrays.asList(ontologyQuery.getOntolgoies()));
                }
            }
        }
        return ontologies;
    }

    public Ontology getOntologyFromId(URI id){
        List<Ontology> ontologyList = getOntologies();
        for (Ontology ontology : ontologyList){
            if (ontology.getConfig().getId().equals(id.toString())){
                return ontology;
            }
        }
        return null;
    }

    /**
     * Retrieve the List of Term for an specific Identifier.
     *
     * @param termOBOId  Term Identifier
     * @param ontologyId Ontology Name
     * @param distance   Distance to the child (1..n) where the distance is the step to the children.
     * @return list of Terms.
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public List<Term> getTermChildren(Identifier termOBOId, String ontologyId, int distance) throws RestClientException {
        List<Term> terms = new ArrayList<Term>();
        String query = String.format("%s://%s/api/ontologies/%s/terms?obo_id=%s",
                config.getProtocol(), config.getHostName(), ontologyId, termOBOId.getIdentifier());

        logger.debug(query);

        TermQuery termQuery = this.restTemplate.getForObject(query, TermQuery.class);

        if (termQuery != null && termQuery.getTerms() != null && termQuery.getTerms().length == 1 &&
                termQuery.getTerms()[0] != null && termQuery.getTerms()[0].getLink() != null &&
                termQuery.getTerms()[0].getLink().getChildrenRef() != null)
            terms = getTermChildrenMap(termQuery.getTerms()[0].getLink().getAllChildrenRef(), distance);
        return terms;
    }

    /**
     * Retrieve the List of Term for an specific Identifier.
     *
     * @param termOBOId  Term Identifier
     * @param ontologyId Ontology Name
     * @param distance   Distance to the child (1..n) where the distance is the step to the children.
     * @return list of Terms.
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public List<Term> getTermParents(Identifier termOBOId, String ontologyId, int distance) throws RestClientException {
        List<Term> terms = new ArrayList<Term>();
        String query = String.format("%s://%s/api/ontologies/%s/terms?obo_id=%s",
                config.getProtocol(), config.getHostName(), ontologyId, termOBOId.getIdentifier());

        logger.debug(query);
        TermQuery termQuery = this.restTemplate.getForObject(query, TermQuery.class);

        if (termQuery != null && termQuery.getTerms() != null && termQuery.getTerms().length == 1 &&
                termQuery.getTerms()[0] != null && termQuery.getTerms()[0].getLink() != null &&
                termQuery.getTerms()[0].getLink().getParentsRef() != null)
            terms = getTermParentsMap(termQuery.getTerms()[0].getLink().getAllParentsRef(), distance);
        return terms;
    }

    /**
     * Check if an specific Term is obsolete in the OLS
     *
     * @param termId     Term id term identifier
     * @param ontologyId ontology Database
     * @return true if the term is annotated as obsolete
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public Boolean isObsolete(Identifier termId, String ontologyId) throws RestClientException {
        Term term = getTermById(termId, ontologyId);
        return isObsolete(term);
    }

    public List<Term> searchTermById(String identifier, String ontologyID) throws RestClientException {
        List<Term> termResults = new ArrayList<Term>();
        SearchQuery currentTermQuery = searchIdQuery(identifier, ontologyID, 0);
        List<SearchResult> terms = new ArrayList<SearchResult>();
        if (currentTermQuery != null && currentTermQuery.getResponse() != null && currentTermQuery.getResponse().getSearchResults() != null) {
            terms.addAll(Arrays.asList(currentTermQuery.getResponse().getSearchResults()));
            if (currentTermQuery.getResponse().getSearchResults().length < currentTermQuery.getResponse().getNumFound()) {
                for (int i = 1; i < currentTermQuery.getResponse().getNumFound() / currentTermQuery.getResponse().getSearchResults().length + 1; i++) {
                    SearchQuery termQuery = searchIdQuery(identifier,  ontologyID, i);
                    if (termQuery != null && termQuery.getResponse() != null && termQuery.getResponse().getSearchResults() != null)
                        terms.addAll(Arrays.asList(termQuery.getResponse().getSearchResults()));
                }
            }
        }
        for (int i = 0; i < terms.size(); i++)
            if (terms.get(i).getName() != null) {
                SearchResult termResult = terms.get(i);
                termResults.add(new Term(termResult.getIri(), termResult.getName(), termResult.getDescription(), termResult.getShortName(), termResult.getOboId(), termResult.getOntologyName(), termResult.getScore(), termResult.getOntologyIri(), termResult.getIsDefiningOntology(), termResult.getOboDefinitionCitation()));
            }

        return termResults;
    }

    private SearchQuery searchIdQuery(String identifier, String ontologyID, int page) throws RestClientException {


        String query = String.format("%s://%s/api/search?q=*%s*&" + getFieldList()
                + "&rows=%s&start=%s",
                config.getProtocol(), config.getHostName(), identifier, Constants.SEARCH_PAGE_SIZE, page);



        if (ontologyID != null && !ontologyID.isEmpty())
            query = String.format("%s://%s/api/search?q=%s&exact=on&" + getFieldList()
                + "&rows=%s&start=%s&ontology=%s",
                config.getProtocol(), config.getHostName(), identifier, Constants.SEARCH_PAGE_SIZE, page, ontologyID);

        logger.debug(query);
        return this.restTemplate.getForObject(query, SearchQuery.class);
    }


    public List<String> getTermDescription(String termId, String ontologyId) throws RestClientException {
        Term term = getTermQueryByOBOId(termId, ontologyId);
        if (term != null)
            return Arrays.asList(term.getDescription());
        return null;
    }

    /**
     * This function returns a Term for an obo ID. If a different ID is provided the function will return
     * NULL value. If the user is interested to use a general identifer it should use the generic
     * getTermById using an Identifier.
     *
     * @param termOBOId  obo ontology ID
     * @param ontologyId ontology name
     * @return Term
     */
    public Term getTermQueryByOBOId(String termOBOId, String ontologyId) {
        String url = String.format("%s://%s/api/ontologies/%s/terms?obo_id=%s",
                config.getProtocol(), config.getHostName(), ontologyId, termOBOId);

        logger.debug(url);

        TermQuery result = this.restTemplate.getForObject(url, TermQuery.class);
        if (result != null && result.getTerms() != null && result.getTerms().length == 1 && result.getTerms()[0] != null)
            return result.getTerms()[0];
        return null;
    }

    public Map<String, String> getTermXrefs(Identifier termId, String ontologyId) throws RestClientException {
        Term term = getTermById(termId, ontologyId);
        Map<String, String> xrefs = new HashMap<String, String>();
        if (term != null && term.getOboXRefs() != null) {
            for (OBOXRef xref : term.getOboXRefs()) {
                if (xref.getDatabase() != null)
                    xrefs.put(xref.getDatabase(), xref.getDescription());
            }
        }
        if(term != null && term.getOboDefinitionCitation() != null){
            xrefs.putAll(this.getOboDefinitionCitationXRef(term));
        }
        return xrefs;
    }

    public Map<String, String> getOBOSynonyms(Identifier identifier, String ontology) throws RestClientException {
        Term term = getTermById(identifier, ontology);
        Map<String, String> xrefs = new HashMap<String, String>();
        if (term != null && term.getOboSynonyms() != null) {
            xrefs.putAll(term.getOboSynonyms());
        }
        return xrefs;
    }

    private OntologyQuery getOntologyQuery(int page) throws RestClientException {
        String query = String.format("%s://%s/api/ontologies?page=%s&size=%s",
                config.getProtocol(), config.getHostName(), page, Constants.ONTOLOGY_PAGE_SIZE);
        logger.debug(query);

        return this.restTemplate.getForObject(query, OntologyQuery.class);
    }

    /**
     * Return a Map with all terms for an ontology where the key is the obo ontology id and
     * the value is the name of the term, also called label in current OLS
     *
     * @param ontologyID Ontology reference
     * @return A map with the Terms
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public List<Term> getAllTermsFromOntology(String ontologyID) throws RestClientException {
        return getAllOBOTermsFromOntology(ontologyID);
    }

    private List<Term> getAllOBOTermsFromOntology(String ontologyID) throws RestClientException {
        TermQuery currentTermQuery = getTermQuery(0, ontologyID);
        List<Term> terms = new ArrayList<Term>();
        if (currentTermQuery != null && currentTermQuery.getTerms() != null) {
            terms.addAll(Arrays.asList(currentTermQuery.getTerms()));
            if (currentTermQuery.getTerms().length < currentTermQuery.getPage().getTotalElements()) {
                for (int i = 1; i < currentTermQuery.getPage().getTotalElements() / currentTermQuery.getTerms().length + 1; i++) {
                    TermQuery termQuery = getTermQuery(i, ontologyID);
                    if (termQuery != null && termQuery.getTerms() != null)
                        terms.addAll(Arrays.asList(termQuery.getTerms()));
                }
            }
        }
        return terms;
    }


    private TermQuery getRootQuery(int page, String ontologyID) {

        String query = String.format("%s://%s/api/ontologies/%s/terms/roots/?page=%s&size=%s",
                config.getProtocol(), config.getHostName(), ontologyID, page, Constants.TERM_PAGE_SIZE);

        logger.debug(query);

        return this.restTemplate.getForObject(query, TermQuery.class);
    }

    private TermQuery getTermQuery(int page, String ontologyID) {

        String query = String.format("%s://%s/api/ontologies/%s/terms/?page=%s&size=%s",
                config.getProtocol(), config.getHostName(), ontologyID, page, Constants.TERM_PAGE_SIZE);

        logger.debug(query);

        return this.restTemplate.getForObject(query, TermQuery.class);
    }

    /**
     * Return all Root Terms for an specific ontology including.
     *
     * @param ontologyID ontology Id to be search
     * @return List of Term
     */
    public List<Term> getRootTerms(String ontologyID) {
        return getAllRootTerns(ontologyID);
    }

    private List<Term> getAllRootTerns(String ontologyID){
        TermQuery currentTermQuery = getRootQuery(0, ontologyID);
        List<Term> terms = new ArrayList<Term>();
        if (currentTermQuery != null && currentTermQuery.getTerms() != null) {
            terms.addAll(Arrays.asList(currentTermQuery.getTerms()));
            if (currentTermQuery.getTerms().length < currentTermQuery.getPage().getTotalElements()) {
                for (int i = 1; i < currentTermQuery.getPage().getTotalElements() / currentTermQuery.getTerms().length + 1; i++) {
                    TermQuery termQuery = getRootQuery(i, ontologyID);
                    if (termQuery != null && termQuery.getTerms() != null)
                        terms.addAll(Arrays.asList(termQuery.getTerms()));
                }
            }
        }
        return terms;
    }


    public List<Term> getTermsByName(String partialName, String ontologyID, boolean reverseKeyOrder) {
        return getTermsByName(partialName, ontologyID, reverseKeyOrder, null);
    }

    public List<Term> getTermsByNameFromParent(String partialName, String ontologyID, boolean reverseKeyOrder, String childrenOf) {
        return  getTermsByName(partialName, ontologyID, reverseKeyOrder, childrenOf);
    }

    /**
     * This function retrieve all the terms from an specific ontology and perform a search in the client side.
     * In the future would be great to replace the current functionality with the search capabilities in the ols.
     *
     * @param partialName     Substring to lookup in the name term
     * @param ontologyID the ontology ID.
     * @param reverseKeyOrder sort the hash in a reverse order
     * @return list of Terms.
     */

    private List<Term> getTermsByName(String partialName, String ontologyID, boolean reverseKeyOrder, String childrenOf) {
        List<Term> resultTerms;
        if (partialName == null || partialName.isEmpty())
            return Collections.emptyList();

        resultTerms = searchByPartialTerm(partialName, ontologyID, childrenOf);

        if (reverseKeyOrder) {
            Set<Term> newMap = new TreeSet<Term>(Collections.reverseOrder());
            newMap.addAll(resultTerms);
            resultTerms = new ArrayList<Term>(newMap);
        }
        return resultTerms;
    }

    /**
     * Searches for exact term matches that belong to a specific parent.
     * You can restrict a search to children of a given term.
     *
     * @param exactName the term we are looking for
     * @param ontologyId the ontology that the term belongs to
     * @param childrenOf a list of IRI for the terms that you want to search under, comma separated
     * @return a list of Terms found
     */
    public List<Term> getExactTermsByNameFromParent(String exactName, String ontologyId, String childrenOf) {
        return  getExactTermsByName(exactName, ontologyId, childrenOf);
    }

    public List<Term> getExactTermsByName(String exactName, String ontologyId) {
        return getExactTermsByName(exactName, ontologyId, null);
    }

    public List<Term> getExactTermsByNameWithObsolete(String exactName, String ontologyId) {
        return getExactTermsByNameWithObsolete(exactName, ontologyId, null);
    }

    private List<Term> getExactTermsByName(String exactName, String ontologyId, String childrenOf) {

        if (exactName == null || exactName.isEmpty()){
            return null;
        }

        return  searchByExactTerm(exactName, ontologyId, childrenOf);

    }

    private List<Term> getExactTermsByNameWithObsolete(String exactName, String ontologyId, String childrenOf) {


        if (exactName == null || exactName.isEmpty()){
            return null;
        }
        if (ontologyId == null || ontologyId.isEmpty()) {
            return searchByExactTermWithObsolete(exactName, null, childrenOf);
        }
        else {
            return  searchByExactTermWithObsolete(exactName, ontologyId, childrenOf);
        }

    }


    /**
     * This function retrieve the term from an specific ontology and perform a search in the client side.
     * In the future would be great to repleace the current functionality with the search capabilities in the ols.
     *
     * @param exactName     String to lookup in the name term
     * @param ontologyId the ontology ID.
     * @return the identified term
     */
    public Term getExactTermByName(String exactName, String ontologyId) {

        List<Term> termResults;

        if (exactName == null || exactName.isEmpty()){
            return null;
        }

        termResults = searchByExactTerm(exactName, ontologyId, null);


        if (termResults != null && !termResults.isEmpty()) {
            return termResults.get(0);
        }

        return null;
    }

    public List<Term> getExactTermsByIriString(String iri) {
        String customQueryField = new QueryFields.QueryFieldBuilder()
                .setIri()
                .build()
                .toString();
        this.setQueryField(customQueryField);
        List<Term> terms = getExactTermsByName(iri, null);
        //restore olsClient search to it's default query field and field list
        this.setQueryField(DEFAULT_QUERY_FIELD);
        return terms;
    }

    public List<Term> getExactTermsByIriStringWithObsolete(String iri) {
        String customQueryField = new QueryFields.QueryFieldBuilder()
                .setIri()
                .build()
                .toString();
        this.setQueryField(customQueryField);
        List<Term> terms = getExactTermsByNameWithObsolete(iri, null);
        //restore olsClient search to it's default query field and field list
        this.setQueryField(DEFAULT_QUERY_FIELD);
        return terms;
    } //getExactTermsByNameWithObsolete


    private List<Term> searchByPartialTerm(String partialName, String ontology, String childrenOf) throws RestClientException {
        return searchByTerm(partialName, ontology, false, childrenOf, false);
    }

    private List<Term> searchByExactTerm(String exactName, String ontologyId, String childrenOf) throws RestClientException {
        return searchByTerm(exactName, ontologyId, true, childrenOf, false);
    }

    private List<Term> searchByExactTermWithObsolete(String exactName, String ontologyId, String childrenOf) throws RestClientException {
        return searchByTerm(exactName, ontologyId, true, childrenOf, true);
    }


    /**
     * Searches for terms in the OLS
     *
     * @param termToSearch the name of the term (partial or exact) that we want to find
     * @param ontology  optional ontology to search the term in, null if not specified
     * @param exact true if we want an exact string match
     * @param childrenOf will restrict a search to children of a given term.
     *                   Supply a list of IRI for the terms that you want to search under, comma separated
     * @param obsolete  true if you want to look into obsolete terms
     * @return a list of Terms found
     * @throws RestClientException
     */
    private List<Term> searchByTerm(String termToSearch, String ontology, boolean exact, String childrenOf, boolean obsolete) throws RestClientException {
        List<Term> termResults = new ArrayList<Term>();
        SearchQuery currentTermQuery = getSearchQuery(0, termToSearch, ontology, exact, childrenOf, obsolete);
        List<SearchResult> terms = new ArrayList<SearchResult>();
        if (currentTermQuery != null && currentTermQuery.getResponse() != null && currentTermQuery.getResponse().getSearchResults() != null) {
            terms.addAll(Arrays.asList(currentTermQuery.getResponse().getSearchResults()));
            if (currentTermQuery.getResponse().getSearchResults().length < currentTermQuery.getResponse().getNumFound()) {
                for (int i = 1; i < currentTermQuery.getResponse().getNumFound() / currentTermQuery.getResponse().getSearchResults().length + 1; i++) {
                    SearchQuery termQuery = getSearchQuery(i, termToSearch, ontology, exact, childrenOf, obsolete);
                    if (termQuery != null && termQuery.getResponse() != null && termQuery.getResponse().getSearchResults() != null)
                        terms.addAll(Arrays.asList(termQuery.getResponse().getSearchResults()));
                }
            }
        }
        for (int i = 0; i < terms.size(); i++)
            if (terms.get(i).getName() != null) {
                SearchResult termResult = terms.get(i);
                termResults.add(new Term(termResult.getIri(), termResult.getName(), termResult.getDescription(),
                        termResult.getShortName(),
                        termResult.getOboId(),
                        termResult.getOntologyName(),
                        termResult.getScore(),
                        termResult.getOntologyIri(),
                        termResult.getIsDefiningOntology(),
                        termResult.getOboDefinitionCitation()));
            }

        return termResults;
    }

    /**
     * Retrieves a specific term given its iri as a String and the ontology it belongs to
     *
     * @param iri the terms iri, i.e. http://www.ebi.ac.uk/efo/EFO_0000635
     * @param ontology the ontology the term belongs to, i.e. efo
     * @return the Term we are looking for. Can also be an ObsoleteTerm
     * @throws RestClientException
     */
    public Term retrieveTerm(String iri, String ontology) throws RestClientException {

        RetrieveTermQuery currentTermQuery = getRetrieveQuery(iri, ontology);
        List<SearchResult> terms = new ArrayList<SearchResult>();
        if (currentTermQuery != null && currentTermQuery.getResponse() != null && currentTermQuery.getResponse().getSearchResults() != null) {
            terms.addAll(Arrays.asList(currentTermQuery.getResponse().getSearchResults()));
        }
        Term term = null;
        for (int i = 0; i < terms.size(); i++)
            if (terms.get(i).getName() != null) {
                SearchResult termResult = terms.get(i);
                if (termResult.isObsolete()){
                    term = new ObsoleteTerm(termResult.getIri(), termResult.getName(), termResult.getDescription(),
                            termResult.getShortName(),
                            termResult.getOboId(),
                            termResult.getOntologyName(),
                            termResult.getScore(),
                            termResult.getOntologyIri(),
                            termResult.getIsDefiningOntology(),
                            termResult.getOboDefinitionCitation(),
                            termResult.getAnnotation(),
                            true);
                }
                else {
                    term = new Term(termResult.getIri(), termResult.getName(), termResult.getDescription(),
                            termResult.getShortName(),
                            termResult.getOboId(),
                            termResult.getOntologyName(),
                            termResult.getScore(),
                            termResult.getOntologyIri(),
                            termResult.getIsDefiningOntology(),
                            termResult.getOboDefinitionCitation(),
                            termResult.getAnnotation());
                }
            }

        return term;
    }

    /**
     * Check if an specific Term is obsolete in the OLS
     *
     * @param term the Term under inspection
     * @return true if the term is annotated as obsolete
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public boolean isObsolete(Term term){
        Term obsoleteTerm = retrieveTerm(term.getIri().getIdentifier(), term.getOntologyName());
        if (obsoleteTerm != null && obsoleteTerm instanceof ObsoleteTerm){
            return true;
        } else {
            return false;
        }
    }

    private Term searchByExactTerm(String exactName, String ontologyId) throws RestClientException {
        SearchQuery currentTermQuery = getSearchQuery(0, exactName, ontologyId, true, null, false);
        if (currentTermQuery.getResponse().getNumFound() != 0) {
            SearchResult termResult = Arrays.asList(currentTermQuery.getResponse().getSearchResults()).get(0);
            return new Term(termResult.getIri(), termResult.getName(), termResult.getDescription(), termResult.getShortName(), termResult.getOboId(), termResult.getOntologyName(), termResult.getScore(), termResult.getOntologyIri(), termResult.getIsDefiningOntology(), termResult.getOboDefinitionCitation());
        }
        return null;
    }

    private SearchQuery getSearchQuery(int page, String name, String ontology, boolean exactMatch, String childrenOf, boolean obsolete) throws RestClientException {
        String query;

        query = String.format("%s://%s/api/search?q=%s&" +
                this.getQueryField()
                + "&rows=%s&start=%s&"
                + this.getFieldList() ,
                config.getProtocol(), config.getHostName(), name, Constants.SEARCH_PAGE_SIZE, page);

        if (ontology != null && !ontology.isEmpty())
            query += "&ontology=" + ontology;

        if(exactMatch){
            query += "&exact=true";
        }

        if (childrenOf != null && !childrenOf.isEmpty())
            query += "&childrenOf=" + childrenOf;

        if (obsolete)
            query += "&obsoletes=true";

        logger.debug(query);
        return this.restTemplate.getForObject(query, SearchQuery.class);
    }

    private RetrieveTermQuery getRetrieveQuery(String iri, String ontology) throws RestClientException {
        String query;

        query = String.format("%s://%s/api/ontologies/%s/terms?iri=%s",
                config.getProtocol(), config.getHostName(), ontology, iri);

        logger.debug(query);
        return this.restTemplate.getForObject(query, RetrieveTermQuery.class);
    }

    private SearchQuery getSearchQuerySimple(int page, String name, String ontology, boolean exactMatch, String childrenOf, String queryField, String fieldList) throws RestClientException {
        String query;

        query = String.format("%s://%s/api/search?q=%s&" +
                        queryField
                        + "&rows=%s&start=%s&"
                        + fieldList ,
                config.getProtocol(), config.getHostName(), name, Constants.SEARCH_PAGE_SIZE, page);

        if (ontology != null && !ontology.isEmpty())
            query += "&ontology=" + ontology;

        if(exactMatch){
            query += "&exact=true";
        }

        if (childrenOf != null && !childrenOf.isEmpty())
            query += "&childrenOf=" + childrenOf;

        logger.debug(query);
        return this.restTemplate.getForObject(query, SearchQuery.class);
    }

    private List<Term> getTermChildrenMap(Href childrenHRef, int distance) {
        List<Term> children = new ArrayList<Term>();
        if (distance == 0)
            return Collections.emptyList();
        List<Term> childTerms = getTermChildren(childrenHRef, distance);
        children.addAll(childTerms);
        return children;
    }

    private List<Term> getTermParentsMap(Href parentsHRef, int distance) {
        List<Term> parents = new ArrayList<Term>();
        if (distance == 0)
            return Collections.emptyList();
        List<Term> parentTerms = getTermParents(parentsHRef, distance);
        parents.addAll(parentTerms);
        return parents;
    }

    private List<Term> getTermChildren(Href hrefChildren, int distance) {
        if (distance == 0)
            return new ArrayList<Term>();
        List<Term> childTerms = new ArrayList<Term>();
        childTerms.addAll(getTermQuery(hrefChildren));
        distance--;
        List<Term> currentChild = new ArrayList<Term>();
        for (Term child : childTerms)
            currentChild.addAll(getTermChildren(child.getLink().getAllChildrenRef(), distance));
        childTerms.addAll(currentChild);
        return childTerms;
    }

    private List<Term> getTermParents(Href hrefParents, int distance) {
        if (distance == 0)
            return new ArrayList<Term>();
        List<Term> parentTerms = new ArrayList<Term>();
        parentTerms.addAll(getTermQuery(hrefParents));
        distance--;
        List<Term> currentParent = new ArrayList<Term>();
        for (Term parent : parentTerms)
            currentParent.addAll(getTermParents(parent.getLink().getAllParentsRef(), distance));
        parentTerms.addAll(currentParent);
        return parentTerms;
    }

    private List<Term> getTermQuery(Href href) throws RestClientException {
        if (href == null)
            return new ArrayList<Term>();
        List<Term> terms = new ArrayList<Term>();
        try {
            String query = href.getHref();
            String url = URLDecoder.decode(query, "UTF-8");
            TermQuery termQuery = this.restTemplate.getForObject(url, TermQuery.class);
            if (termQuery != null && termQuery.getTerms() != null) {
                terms.addAll(Arrays.asList(termQuery.getTerms()));
            }
            if (termQuery != null && termQuery.getLink() != null && termQuery.getLink().next() != null)
                terms.addAll(getTermQuery(termQuery.getLink().next()));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return terms;
    }

    /**
     * This method checks if a term is obsolete or not.
     * @param termOBOId  The OBOId of the Term in the ols ontology
     * @param ontologyID the ontology ID
     * @return true if the term is obsolete, if the term is not found in the ontology the function
     * return null, also if the value is not found.
     */
    public Boolean isObsolete(String termOBOId, String ontologyID) throws RestClientException {
        Term term = getTermById(new Identifier(termOBOId, Identifier.IdentifierType.OBO), ontologyID);

        return isObsolete(term);
    }

    public List<Term> getTermsByAnnotationData(String ontologyID, String annotationType, String strValue) {
        return Collections.emptyList();
    }

    public List<Term> getTermsByAnnotationData(String ontologyID, String annotationType, double fromDblValue, double toDblValue) {
        List<Term> terms = getAllOBOTermsFromOntology(ontologyID);
        List<Term> termResult = new ArrayList<Term>();
        for (Term term : terms) {
            if (term != null && term.getOboXRefs() != null && term.containsXref(annotationType)) {
                String termValue = term.getXRefValue(annotationType);
                if (NumberUtils.isNumber(termValue) && Double.parseDouble(termValue) >= fromDblValue && Double.parseDouble(termValue) <= toDblValue)
                    termResult.add(term);
            }
        }
        return termResult;
    }

    public Ontology getOntology(String ontologyId) throws RestClientException {
        String query = String.format("%s://%s/api/ontologies/%s",
                config.getProtocol(), config.getHostName(), ontologyId);
        logger.debug(query);
        Ontology ontology = this.restTemplate.getForObject(query, Ontology.class);
        if (ontology != null) {
            return ontology;
        }
        return null;
    }

    public Set<String> getSynonyms(Identifier identifier, String ontology) throws RestClientException {
        Set<String> synonyms = new HashSet<>();
        Term term = getTermById(identifier, ontology);
        Collections.addAll(synonyms, term.getSynonyms());
        return synonyms;
    }

    public Map getMetaData(Identifier identifier, String ontologyId){
        HashMap<String, Object> metaData = new HashMap<>();
        Map synonym = this.getOBOSynonyms(identifier, ontologyId) == null ? Collections.emptyMap() : this.getOBOSynonyms(identifier, ontologyId);
        String definition = this.getFirstTermDescription(identifier, ontologyId);
        String comment = this.getComment(identifier, ontologyId);

        if(synonym != null && !synonym.isEmpty()){
            metaData.put("synonym", synonym);
        }
        if(definition != null && !definition.isEmpty()){
            metaData.put("definition", definition);
        }
        if (comment != null && !comment.isEmpty()) {
            metaData.put("comment", comment);
        }

        if(metaData.isEmpty()){
            return new HashMap<>();
        }
        return metaData;
    }

    public String getComment(Identifier identifier, String ontologyId){
        Map<String, List<String>> annotations = this.getAnnotations(identifier, ontologyId);
        if (!annotations.isEmpty() && annotations.keySet().contains("comment")) {
            return annotations.get("comment").get(0);
        }
        return null;
    }

    /**
     * This method gets the description of the first term identified by term ID and ontology ID.
     * @param termId the term ID.
     * @param ontologyId the ontology ID.
     * @return the first term's description.
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public String getFirstTermDescription(Identifier termId, String ontologyId) throws RestClientException {
        Term term = getTermById(termId, ontologyId);
        String  description = null;
        if (term != null && term.getDescription() != null){
            description = term.getDescription()[0];
        }
        return description;
    }

    private Map<String, String> getOboDefinitionCitationXRef(Term term) {
        Map<String, String> xrefs = new HashMap<String, String>();
        for (OboDefinitionCitation citation : term.getOboDefinitionCitation()) {
            OBOXRef[] oboxRef = citation.getOboXrefs();
            for (OBOXRef xref : oboxRef) {
                if (xref.getId() != null && !xref.getId().isEmpty()) {
                    if(xref.getDatabase() != null ){
                        xrefs.put("xref_definition_" + xref.getId(), xref.getDatabase() + ":" + xref.getId());
                        continue;
                    }
                    xrefs.put("xref_definition_" + xref.getId(), xref.getId());
                }
            }
        }
        return xrefs;
    }
}