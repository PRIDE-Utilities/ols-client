package uk.ac.ebi.pride.utilities.ols.web.service.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.client.RestClientException;
import uk.ac.ebi.pride.utilities.ols.web.service.config.AbstractOLSWsConfig;
import uk.ac.ebi.pride.utilities.ols.web.service.model.*;
import uk.ac.ebi.pride.utilities.ols.web.service.utils.Constants;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


/**
 * This class allows to query the Ontology Lockup service to retrieve information about
 * CVTerms.
 *
 * @author ypriverol
 */
@Slf4j
public class OLSClient implements Client {

    private ObjectMapper mapper;

    private AbstractOLSWsConfig config;

    private String queryField;
    private String fieldList;
    private int searchPageSize;
    private int searchPageNum;

    private static int TIME_OUT = 5000;

    private static final String DEFAULT_QUERY_FIELD = new QueryFields.QueryFieldBuilder()
            .setLabel()
            .setSynonym()
            .build()
            .toString();
    private static final String DEFAULT_FIELD_LIST = new FieldList.FieldListBuilder()
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
        if (queryField == null) {
            queryField = DEFAULT_QUERY_FIELD;
        }
        return queryField;
    }

    public void setQueryField(String queryField) {
        this.queryField = queryField;
    }

    public String getFieldList() {
        if (fieldList == null) {
            fieldList = DEFAULT_FIELD_LIST;
        }
        return fieldList;
    }

    public int getSearchPageSize() {
        return searchPageSize;
    }

    public void setSearchPageSize(int searchPageSize) {
        this.searchPageSize = searchPageSize;
    }

    public int getSearchPageNum() {
        return searchPageNum;
    }

    public void setSearchPageNum(int searchPageNum) {
        this.searchPageNum = searchPageNum;
    }

    public void setFieldList(String fieldList) {
        this.fieldList = fieldList;
    }

    /**
     * Default constructor for Archive clients
     *
     * @param config configuration to use.
     */
    public OLSClient(AbstractOLSWsConfig config) {
        this.config = config;
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

//        this.restTemplate = new RestTemplate(getClientHttpRequestFactory());
        this.searchPageSize = Constants.SEARCH_PAGE_SIZE;
        this.searchPageNum = -1;
    }

//    private ClientHttpRequestFactory getClientHttpRequestFactory() {
//        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
//        clientHttpRequestFactory.setConnectTimeout(TIME_OUT);
//        return clientHttpRequestFactory;
//    }


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

        String query = String.format("obo_id=%s", termOBOId);

        log.debug(query);

        URI uri = encodeURL("/api/ontologies/" + ontologyId + "/terms", query);
        TermQuery result = getForObject(uri, TermQuery.class);

        if (result != null && result.getTerms() != null && result.getTerms().length == 1) {
            return result.getTerms()[0];
        }

        return null;
    }

    /**
     * Return a Term for a short name Identifier and the ontology Identifier.
     *
     * @param shortForm  short term Identifier in OLS
     * @param ontologyId ontology Identifier
     * @return Term
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public Term getTermByShortName(String shortForm, String ontologyId) throws RestClientException {

        String query = String.format("short_form=%s",
                shortForm);

        log.debug(query);

        URI uri = encodeURL("/api/ontologies/" + ontologyId + "/terms", query);
        TermQuery result = getForObject(uri, TermQuery.class);

        if (result != null && result.getTerms() != null && result.getTerms().length == 1) {
            return result.getTerms()[0];
        }

        return null;
    }

    /**
     * Return a Term for an IRI identifier and the ontology Identifier.
     *
     * @param iriId      IRI Identifier in OLS
     * @param ontologyId ontology Identifier
     * @return Term result term from OLS
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public Term getTermByIRIId(String iriId, String ontologyId) throws RestClientException {

        String query = String.format("iri=%s", iriId);
        log.debug(query);
        URI uri = encodeURL("/api/ontologies/" + ontologyId + "/terms", query);
        TermQuery result = getForObject(uri, TermQuery.class);

        if (result != null && result.getTerms() != null && result.getTerms().length == 1) {
            return result.getTerms()[0];
        }

        return null;
    }

    public List<String> getTermDescription(Identifier termId, String ontologyId) throws RestClientException {
        Term term = getTermById(termId, ontologyId);
        List<String> description = new ArrayList<>();
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
        List<Ontology> ontologies = new ArrayList<>();
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

    public Ontology getOntologyFromId(String id) {
        return getOntologies().stream().filter(ontology -> ontology.getId().equals(id)).findAny().orElse(null);
    }

    /**
     * @param id
     * @return {@link Ontology}
     * @deprecated Former id is now FilePath.<br> To use the same input, use {@link OLSClient#getOntologyFromFilePath(URI filePath)}.<br> To use new id definition (short letter code), use  {@link OLSClient#getOntologyFromId(String id)}
     */
    @Deprecated
    public Ontology getOntologyFromId(URI id) {
        return getOntologyFromFilePath(id);
    }

    public Ontology getOntologyFromFilePath(URI filePath) {
        List<Ontology> ontologyList = getOntologies();
        for (Ontology ontology : ontologyList) {
            String fileLocation = ontology.getConfig().getFileLocation();
            log.debug(fileLocation);
            if (fileLocation != null && fileLocation.equals(filePath.toString())) {
                return ontology;
            }
        }
        return null;
    }

    /**
     * Retrieve the List of Term for an specific Identifier.
     *
     * @param termId     Term Identifier
     * @param ontologyId Ontology Name
     * @param distance   Distance to the child (1..n) where the distance is the step to the children.
     * @return list of Terms.
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public List<Term> getTermChildren(Identifier termId, String ontologyId, int distance) throws RestClientException {
        List<Term> terms = new ArrayList<>();
        Term term = getTermById(termId, ontologyId);
        if (term != null && term.getLink() != null && term.getLink().getAllChildrenRef() != null) {
            terms = getTermChildrenMap(term.getLink().getAllChildrenRef(), distance);
        }
        return terms;
    }

    /**
     * Retrieve the List of Term for an specific Identifier.
     *
     * @param termId     Term Identifier
     * @param ontologyId Ontology Name
     * @param distance   Distance to the child (1..n) where the distance is the step to the children.
     * @return list of Terms.
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public List<Term> getTermParents(Identifier termId, String ontologyId, int distance) throws RestClientException {
        List<Term> terms = new ArrayList<>();
        Term term = getTermById(termId, ontologyId);
        if (term != null && term.getLink() != null && term.getLink().getAllParentsRef() != null)
            terms = getTermParentsMap(term.getLink().getAllParentsRef(), distance);
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
        List<Term> termResults = new ArrayList<>();
        SearchQuery currentTermQuery = searchIdQuery(identifier, ontologyID, 0);
        List<SearchResult> terms = new ArrayList<>();
        if (currentTermQuery != null && currentTermQuery.getResponse() != null && currentTermQuery.getResponse().getSearchResults() != null) {
            terms.addAll(Arrays.asList(currentTermQuery.getResponse().getSearchResults()));
            if (currentTermQuery.getResponse().getSearchResults().length < currentTermQuery.getResponse().getNumFound()) {
                for (int i = 1; i < currentTermQuery.getResponse().getNumFound() / currentTermQuery.getResponse().getSearchResults().length + 1; i++) {
                    SearchQuery termQuery = searchIdQuery(identifier, ontologyID, i);
                    if (termQuery != null && termQuery.getResponse() != null && termQuery.getResponse().getSearchResults() != null)
                        terms.addAll(Arrays.asList(termQuery.getResponse().getSearchResults()));
                }
            }
        }
        for (SearchResult term : terms)
            if (term.getName() != null) {
                termResults.add(new Term(term.getIri(), term.getName(), term.getDescription(), term.getShortName(), term.getOboId(), term.getOntologyName(), term.getScore(), term.getOntologyIri(), term.getIsDefiningOntology(), term.getOboDefinitionCitation()));
            }

        return termResults;
    }

    private SearchQuery searchIdQuery(String identifier, String ontologyID, int page) throws RestClientException {
        String query = String.format("q=*%s*&" + getFieldList() + "&rows=%s&start=%s", identifier, Constants.SEARCH_PAGE_SIZE, page);
        if (ontologyID != null && !ontologyID.isEmpty())
            query = String.format("q=%s&exact=on&" + getFieldList() + "&rows=%s&start=%s&ontology=%s", identifier, Constants.SEARCH_PAGE_SIZE, page, ontologyID);
        log.debug(query);
        URI uri = encodeURL("/api/search", query);
        return getForObject(uri, SearchQuery.class);
    }


    public List<String> getTermDescription(String termId, String ontologyId) throws RestClientException {
        Term term = getTermByOBOIdString(termId, ontologyId);
        if (term != null)
            return Arrays.asList(term.getDescription());
        return null;
    }

    /**
     * This function returns a Term for an obo ID. If a different ID is provided the function will return
     * NULL value.
     *
     * @param termOBOId  obo ontology ID
     * @param ontologyId ontology name
     * @return Term
     */
    public Term getTermByOBOIdString(String termOBOId, String ontologyId) {
        return getTermById(new Identifier(termOBOId, Identifier.IdentifierType.OBO), ontologyId);
    }


    public Map<String, String> getTermXrefs(Identifier termId, String ontologyId) throws RestClientException {
        Term term = getTermById(termId, ontologyId);
        Map<String, String> xrefs = new HashMap<>();
        if (term != null && term.getOboXRefs() != null) {
            for (OBOXRef xref : term.getOboXRefs()) {
                if (xref.getDatabase() != null) {
                    if (xref.getId() != null && !xref.getId().isEmpty()) {
                        xrefs.put(xref.getDatabase(), xref.getId());
                    } else if (xref.getDescription() != null && !xref.getDescription().isEmpty()) {
                        xrefs.put(xref.getDatabase(), xref.getDescription());
                    }
                } else if (xref.getId() != null && !xref.getId().isEmpty() && xref.getDescription() != null && !xref.getDescription().isEmpty()) {
                    xrefs.put(xref.getId(), xref.getDescription());
                }
            }
        }
        if (term != null && term.getOboDefinitionCitation() != null) {
            xrefs.putAll(this.getOboDefinitionCitationXRef(term));
        }
        return xrefs;
    }

    public Map<String, String> getOBOSynonyms(Identifier identifier, String ontology) throws RestClientException {
        Term term = getTermById(identifier, ontology);
        Map<String, String> xrefs = new HashMap<>();
        if (term != null && term.getOboSynonyms() != null) {
            xrefs.putAll(term.getOboSynonyms());
        }
        return xrefs;
    }

    private OntologyQuery getOntologyQuery(int page) throws RestClientException {
        String query = String.format("page=%s&size=%s",
                page, Constants.ONTOLOGY_PAGE_SIZE);
        log.debug(query);
        URI uri = encodeURL("/api/ontologies", query);
        return getForObject(uri, OntologyQuery.class);
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
        List<Term> terms = new ArrayList<>();
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

        String query = String.format("page=%s&size=%s",
                page, Constants.TERM_PAGE_SIZE);

        log.debug(query);

        URI uri = encodeURL("/api/ontologies/" + ontologyID + "/terms/roots/", query);
        return getForObject(uri, TermQuery.class);
    }

    private TermQuery getTermQuery(int page, String ontologyID) {

        String query = String.format("page=%s&size=%s",
                page, Constants.TERM_PAGE_SIZE);

        log.debug(query);

        URI uri = encodeURL("/api/ontologies/" + ontologyID + "/terms", query);
        return getForObject(uri, TermQuery.class);
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

    private List<Term> getAllRootTerns(String ontologyID) {
        TermQuery currentTermQuery = getRootQuery(0, ontologyID);
        List<Term> terms = new ArrayList<>();
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
        return getTermsByName(partialName, ontologyID, reverseKeyOrder, childrenOf);
    }

    /**
     * This function retrieve all the terms from an specific ontology and perform a search in the client side.
     * In the future would be great to replace the current functionality with the search capabilities in the ols.
     *
     * @param partialName     Substring to lookup in the name term
     * @param ontologyID      the ontology ID.
     * @param reverseKeyOrder sort the hash in a reverse order
     * @return list of Terms.
     */

    private List<Term> getTermsByName(String partialName, String ontologyID, boolean reverseKeyOrder, String childrenOf) {
        List<Term> resultTerms;
        if (partialName == null || partialName.isEmpty())
            return Collections.emptyList();

        resultTerms = searchByPartialTerm(partialName, ontologyID, childrenOf);

        if (reverseKeyOrder) {
            Set<Term> newMap = new TreeSet<>(Collections.reverseOrder());
            newMap.addAll(resultTerms);
            resultTerms = new ArrayList<>(newMap);
        }
        return resultTerms;
    }

    /**
     * Searches for exact term matches that belong to a specific parent.
     * You can restrict a search to children of a given term.
     *
     * @param exactName  the term we are looking for
     * @param ontologyId the ontology that the term belongs to
     * @param childrenOf a list of IRI for the terms that you want to search under, comma separated
     * @return a list of Terms found
     */
    public List<Term> getExactTermsByNameFromParent(String exactName, String ontologyId, String childrenOf) {
        return getExactTermsByName(exactName, ontologyId, childrenOf);
    }

    public List<Term> getExactTermsByName(String exactName, String ontologyId) {
        return getExactTermsByName(exactName, ontologyId, null);
    }

    public List<Term> getExactTermsByNameWithObsolete(String exactName, String ontologyId) {
        return getExactTermsByNameWithObsolete(exactName, ontologyId, null);
    }

    private List<Term> getExactTermsByName(String exactName, String ontologyId, String childrenOf) {

        if (exactName == null || exactName.isEmpty()) {
            return null;
        }

        return searchByExactTerm(exactName, ontologyId, childrenOf);

    }

    private List<Term> getExactTermsByNameWithObsolete(String exactName, String ontologyId, String childrenOf) {


        if (exactName == null || exactName.isEmpty()) {
            return null;
        }
        if (ontologyId == null || ontologyId.isEmpty()) {
            return searchByExactTermWithObsolete(exactName, null, childrenOf);
        } else {
            return searchByExactTermWithObsolete(exactName, ontologyId, childrenOf);
        }

    }


    /**
     * This function retrieve the term from an specific ontology and perform a search in the client side.
     * In the future would be great to repleace the current functionality with the search capabilities in the ols.
     *
     * @param exactName  String to lookup in the name term
     * @param ontologyId the ontology ID.
     * @return the identified term
     */
    public Term getExactTermByName(String exactName, String ontologyId) {

        List<Term> termResults;

        if (exactName == null || exactName.isEmpty()) {
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
     * @param ontology     optional ontology to search the term in, null if not specified
     * @param exact        true if we want an exact string match
     * @param childrenOf   will restrict a search to children of a given term.
     *                     Supply a list of IRI for the terms that you want to search under, comma separated
     * @param obsolete     true if you want to look into obsolete terms
     * @return a list of Terms found
     * @throws RestClientException Rest Exception
     */
    private List<Term> searchByTerm(String termToSearch, String ontology, boolean exact, String childrenOf, boolean obsolete) throws RestClientException {
        List<Term> termResults = new ArrayList<>();
        List<SearchResult> terms = new ArrayList<>();

        int pageSize = getSearchPageSize();
        if (pageSize <= 0) {
            pageSize = Constants.SEARCH_PAGE_SIZE;
        }

        SearchQuery currentTermQuery = getSearchQuery(0, termToSearch, ontology, exact, childrenOf, obsolete, pageSize);

        int pageNum = getSearchPageNum();
        if (pageNum < 0) {
            pageNum = currentTermQuery.getResponse().getNumFound() / pageSize;
        }

        if (currentTermQuery != null && currentTermQuery.getResponse() != null && currentTermQuery.getResponse().getSearchResults() != null) {
            terms.addAll(Arrays.asList(currentTermQuery.getResponse().getSearchResults()));
            if (currentTermQuery.getResponse().getSearchResults().length < currentTermQuery.getResponse().getNumFound()) {
                int start = 0;
                for (int i = 0; i < pageNum; i++) {
                    start = start + pageSize;
                    SearchQuery termQuery = getSearchQuery(start, termToSearch, ontology, exact, childrenOf, obsolete, pageSize);
                    if (termQuery != null && termQuery.getResponse() != null && termQuery.getResponse().getSearchResults() != null) {
                        if (termQuery.getResponse().getSearchResults().length == 0) {
                            break;
                        }
                        terms.addAll(Arrays.asList(termQuery.getResponse().getSearchResults()));
                    }
                }
            }
        }
        for (SearchResult term : terms)
            if (term.getName() != null) {
                termResults.add(new Term(term.getIri(), term.getName(), term.getDescription(),
                        term.getShortName(),
                        term.getOboId(),
                        term.getOntologyName(),
                        term.getScore(),
                        term.getOntologyIri(),
                        term.getIsDefiningOntology(),
                        term.getOboDefinitionCitation()));
            }

        return termResults;
    }

    /**
     * Retrieves a specific term given its iri as a String and the ontology it belongs to
     *
     * @param id       the term id, whether it is obo, short or iri i.e. http://www.ebi.ac.uk/efo/EFO_0000635, EFO_0000635 or EFO:0000635
     * @param ontology the ontology the term belongs to, i.e. efo
     * @return the Term we are looking for. Can also be an ObsoleteTerm
     * @throws RestClientException Rest Exception
     */
    public Term retrieveTerm(String id, String ontology) throws RestClientException {
        RetrieveTermQuery currentTermQuery = getRetrieveQuery(id, ontology);

        List<SearchResult> terms = new ArrayList<>();
        if (currentTermQuery != null && currentTermQuery.getResponse() != null && currentTermQuery.getResponse().getSearchResults() != null) {
            terms.addAll(Arrays.asList(currentTermQuery.getResponse().getSearchResults()));
        }
        Term term = null;
        for (SearchResult term1 : terms)
            if (term1.getName() != null) {
                if (term1.isObsolete()) {
                    term = new ObsoleteTerm(term1.getIri(), term1.getName(), term1.getDescription(),
                            term1.getShortName(),
                            term1.getOboId(),
                            term1.getOntologyName(),
                            term1.getScore(),
                            term1.getOntologyIri(),
                            term1.getIsDefiningOntology(),
                            term1.getOboDefinitionCitation(),
                            term1.getAnnotation(),
                            true, term1.getTermReplacedBy());
                } else {
                    term = new Term(term1.getIri(), term1.getName(), term1.getDescription(),
                            term1.getShortName(),
                            term1.getOboId(),
                            term1.getOntologyName(),
                            term1.getScore(),
                            term1.getOntologyIri(),
                            term1.getIsDefiningOntology(),
                            term1.getOboDefinitionCitation(),
                            term1.getAnnotation());
                }
            }

        if (ontology != null && !ontology.isEmpty() && term != null && term.getOntologyName() != null) {
            if (!term.getOntologyName().equalsIgnoreCase(ontology)) {
                return null;
            }
        }
        return term;
    }

    /**
     * Retrieves a specific term given its id as a String
     * If it is obsolete then it will be an ObsoleteTerm
     *
     * @param id the term id, whether it is obo, shor or iri i.e. http://www.ebi.ac.uk/efo/EFO_0000635, EFO_0000635 or EFO:0000635
     * @return the an ObsoleteTerm we are looking for or null if not obsolete
     * @throws RestClientException Rest Exception
     */
    public ObsoleteTerm retrieveObsoleteTerm(String id) throws RestClientException {
        RetrieveTermQuery currentTermQuery = getRetrieveQuery(id);

        List<SearchResult> terms = new ArrayList<>();
        if (currentTermQuery != null && currentTermQuery.getResponse() != null && currentTermQuery.getResponse().getSearchResults() != null) {
            terms.addAll(Arrays.asList(currentTermQuery.getResponse().getSearchResults()));
        }
        ObsoleteTerm term = null;
        for (SearchResult term1 : terms) {
            if (term1.getName() != null) {
                if (term1.isObsolete()) {
                    if (term1.getIsDefiningOntology()) {
                        return new ObsoleteTerm(term1.getIri(), term1.getName(), term1.getDescription(),
                                term1.getShortName(),
                                term1.getOboId(),
                                term1.getOntologyName(),
                                term1.getScore(),
                                term1.getOntologyIri(),
                                term1.getIsDefiningOntology(),
                                term1.getOboDefinitionCitation(),
                                term1.getAnnotation(),
                                true, term1.getTermReplacedBy());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Check if an specific Term is obsolete in the OLS
     *
     * @param term the Term under inspection
     * @return true if the term is annotated as obsolete
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public boolean isObsolete(Term term) throws RestClientException {
        if (term == null || term.getIri() == null) {
            return false;
        }
        Term obsoleteTerm = retrieveTerm(term.getIri().getIdentifier(), term.getOntologyName());
        return obsoleteTerm != null && obsoleteTerm instanceof ObsoleteTerm;
    }

    /**
     * If the given term is obsolete and has a reference to it's replacement
     * return the replacement
     *
     * @param termId   the String of the id of the term we are looking for a replacement for
     * @param ontology the ontology the term belongs to
     * @return the term to be replaced with, null if nothing found
     */
    public Term getReplacedBy(String termId, String ontology) {
        if (isObsolete(termId, ontology)) {
            Term term = retrieveTerm(termId, ontology);
            String termReplacedBy = ((ObsoleteTerm) term).getTermReplacedBy();
            if (termReplacedBy == null || termReplacedBy.isEmpty()) {
                return null;
            }
            return retrieveTerm(termReplacedBy, term.getOntologyName());
        }
        return null;
    }

    /**
     * If the given term is obsolete and has a reference to it's replacement
     * return the replacement
     *
     * @param termId the String of the id of the term we are looking for a replacement for
     * @return the term to be replaced with, null if nothing found
     */
    public Term getReplacedBy(String termId) {
        ObsoleteTerm term = retrieveObsoleteTerm(termId);
        String termReplacedBy = null;
        if (term != null) {
            termReplacedBy = term.getTermReplacedBy();
        }
        if (termReplacedBy == null || termReplacedBy.isEmpty()) {
            return null;
        }
        return retrieveTerm(termReplacedBy.trim(), term.getOntologyName());
    }

    private Term searchByExactTerm(String exactName, String ontologyId) throws RestClientException {
        SearchQuery currentTermQuery = getSearchQuery(0, exactName, ontologyId, true, null, false, Constants.SEARCH_PAGE_SIZE);
        if (currentTermQuery.getResponse().getNumFound() != 0) {
            SearchResult termResult = Arrays.asList(currentTermQuery.getResponse().getSearchResults()).get(0);
            return new Term(termResult.getIri(), termResult.getName(), termResult.getDescription(), termResult.getShortName(), termResult.getOboId(), termResult.getOntologyName(), termResult.getScore(), termResult.getOntologyIri(), termResult.getIsDefiningOntology(), termResult.getOboDefinitionCitation());
        }
        return null;
    }

    /**
     * Raw query to OLS Service
     *
     * @param page       number of the page
     * @param name       term to be query
     * @param ontology   ontology name
     * @param exactMatch if is exact match
     * @param childrenOf including children
     * @param obsolete   including obsolete terms
     * @param size       size of the query
     * @return Return results
     * @throws RestClientException RestClient Exception
     */
    public SearchQuery getSearchQuery(int page, String name, String ontology, boolean exactMatch, String childrenOf, boolean obsolete, int size) throws RestClientException {
        String query;

        query = String.format("q=%s&" +
                        this.getQueryField()
                        + "&rows=%s&start=%s&"
                        + this.getFieldList(),
                name, size, page);

        if (ontology != null && !ontology.isEmpty())
            query += "&ontology=" + ontology;

        if (exactMatch) {
            query += "&exact=true";
        }

        if (childrenOf != null && !childrenOf.isEmpty())
            query += "&childrenOf=" + childrenOf;

        if (obsolete)
            query += "&obsoletes=true";

        log.debug(query);
        URI uri = encodeURL("/api/search", query);
        return getForObject(uri, SearchQuery.class);
    }


    private RetrieveTermQuery getRetrieveQuery(String id, String ontology) throws RestClientException {

        if (ontology == null || ontology.isEmpty()) {
            throw new IllegalArgumentException("The term's ontology must not be null or empty!");
        }

        String iri = null;
        if (!id.contains("http") && !id.contains("/")) {
            iri = resolveIri(id);
        }

        if (iri == null) {
            iri = id;
        }

        String query = String.format("iri=%s",
                iri);

        log.debug(query);
        URI uri = encodeURL("/api/ontologies/" + ontology + "/terms", query);
        return getForObject(uri, RetrieveTermQuery.class);
    }

    private RetrieveTermQuery getRetrieveQuery(String id) throws RestClientException {
        String query;

        query = String.format("id=%s",
                id);

        log.debug(query);
        URI uri = encodeURL("/api/terms", query);
        return getForObject(uri, RetrieveTermQuery.class);
    }

    private String resolveIri(String id) throws RestClientException {
        RetrieveTermQuery currentTermQuery = getRetrieveQuery(id);
        List<SearchResult> terms = new ArrayList<>();
        if (currentTermQuery != null && currentTermQuery.getResponse() != null && currentTermQuery.getResponse().getSearchResults() != null) {
            terms.addAll(Arrays.asList(currentTermQuery.getResponse().getSearchResults()));
        }

        for (SearchResult term : terms) {
            if (term.getName() != null) {
                return term.getIri().getIdentifier();
            }
        }
        return null;
    }


    private List<Term> getTermChildrenMap(Href childrenHRef, int distance) {
        List<Term> children = new ArrayList<>();
        if (distance == 0)
            return Collections.emptyList();
        List<Term> childTerms = getTermChildren(childrenHRef, distance);
        children.addAll(childTerms);
        return children;
    }

    private List<Term> getTermParentsMap(Href parentsHRef, int distance) {
        List<Term> parents = new ArrayList<>();
        if (distance == 0)
            return Collections.emptyList();
        List<Term> parentTerms = getTermParents(parentsHRef, distance);
        parents.addAll(parentTerms);
        return parents;
    }

    private List<Term> getTermChildren(Href hrefChildren, int distance) {
        if (distance == 0)
            return new ArrayList<>();
        List<Term> childTerms = new ArrayList<>(getTermQuery(hrefChildren));
        distance--;
        List<Term> currentChild = new ArrayList<>();
        for (Term child : childTerms)
            currentChild.addAll(getTermChildren(child.getLink().getAllChildrenRef(), distance));
        childTerms.addAll(currentChild);
        return childTerms;
    }

    private List<Term> getTermParents(Href hrefParents, int distance) {
        if (distance == 0)
            return new ArrayList<>();
        List<Term> parentTerms = new ArrayList<>();
        parentTerms.addAll(getTermQuery(hrefParents));
        distance--;
        List<Term> currentParent = new ArrayList<>();
        for (Term parent : parentTerms)
            currentParent.addAll(getTermParents(parent.getLink().getAllParentsRef(), distance));
        parentTerms.addAll(currentParent);
        return parentTerms;
    }

    private List<Term> getTermQuery(Href href) throws RestClientException {
        if (href == null)
            return new ArrayList<>();
        List<Term> terms = new ArrayList<>();
        String query = href.getHref();
        TermQuery termQuery = getForObject(query, TermQuery.class);
        if (termQuery != null && termQuery.getTerms() != null) {
            terms.addAll(Arrays.asList(termQuery.getTerms()));
        }
        if (termQuery != null && termQuery.getLink() != null && termQuery.getLink().next() != null)
            terms.addAll(getTermQuery(termQuery.getLink().next()));


        return terms;
    }

    /**
     * This method checks if a term is obsolete or not.
     *
     * @param termId     The Id of the Term in the ols ontology (short, obo, iri)
     * @param ontologyID the ontology ID
     * @return true if the term is obsolete, if the term is not found in the ontology the function
     * return null, also if the value is not found.
     */
    public Boolean isObsolete(String termId, String ontologyID) throws RestClientException {
        Term term = retrieveTerm(termId, ontologyID);
        return isObsolete(term);
    }

    public Boolean isObsolete(String termId) throws RestClientException {
        ObsoleteTerm term = retrieveObsoleteTerm(termId);
        return term != null;
    }

    public List<Term> getTermsByAnnotationData(String ontologyID, String annotationType, String strValue) {
        return Collections.emptyList();
    }

    public List<Term> getTermsByAnnotationData(String ontologyID, String annotationType, double fromDblValue, double toDblValue) {
        List<Term> terms = getAllOBOTermsFromOntology(ontologyID);
        List<Term> termResult = new ArrayList<>();
        for (Term term : terms) {
            if (term != null && term.getAnnotation() != null && term.getAnnotation().containsAnnotation(annotationType)) {
                List<String> termValues = term.getAnnotation().getAnnotation(annotationType);
                if (termValues != null && !termValues.isEmpty() && NumberUtils.isNumber(termValues.get(0))) {
                    double value = Double.parseDouble(termValues.get(0));
                    if (value >= fromDblValue && value <= toDblValue)
                        termResult.add(term);
                }
            }
        }
        return termResult;
    }

    public Ontology getOntology(String ontologyId) throws RestClientException {
        URI uri = encodeURL("/api/ontologies/" + ontologyId, null);
        Ontology ontology = getForObject(uri, Ontology.class);
        if (ontology != null) {
            return ontology;
        }
        return null;
    }

    public Set<String> getSynonyms(Identifier identifier, String ontology) throws RestClientException {
        Set<String> synonyms = new HashSet<>();
        Term term = getTermById(identifier, ontology);
        if (term != null && term.getSynonyms() != null)
            Collections.addAll(synonyms, term.getSynonyms());
        return synonyms;
    }

    public Map getMetaData(Identifier identifier, String ontologyId) {
        HashMap<String, Object> metaData = new HashMap<>();
        Map synonym = this.getOBOSynonyms(identifier, ontologyId) == null ? Collections.emptyMap() : this.getOBOSynonyms(identifier, ontologyId);
        String definition = this.getFirstTermDescription(identifier, ontologyId);
        String comment = this.getComment(identifier, ontologyId);

        if (synonym != null && !synonym.isEmpty()) {
            metaData.put("synonym", synonym);
        }
        if (definition != null && !definition.isEmpty()) {
            metaData.put("definition", definition);
        }
        if (comment != null && !comment.isEmpty()) {
            metaData.put("comment", comment);
        }

        if (metaData.isEmpty()) {
            return new HashMap<>();
        }
        return metaData;
    }

    public String getComment(Identifier identifier, String ontologyId) {
        Map<String, List<String>> annotations = this.getAnnotations(identifier, ontologyId);
        if (!annotations.isEmpty() && annotations.keySet().contains("comment")) {
            return annotations.get("comment").get(0);
        }
        return null;
    }

    /**
     * This method gets the description of the first term identified by term ID and ontology ID.
     *
     * @param termId     the term ID.
     * @param ontologyId the ontology ID.
     * @return the first term's description.
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    public String getFirstTermDescription(Identifier termId, String ontologyId) throws RestClientException {
        Term term = getTermById(termId, ontologyId);
        String description = null;
        if (term != null) {
            if (term.getDescription() != null && term.getDescription().length > 0) {
                description = term.getDescription()[0];
            } else if (term.getAnnotation() != null && term.getAnnotation().containsAnnotation(("definition"))) {
                description = term.getAnnotation().getAnnotation("definition").get(0);
            }
        }
        return description;
    }

    private Map<String, String> getOboDefinitionCitationXRef(Term term) {
        Map<String, String> xrefs = new HashMap<>();
        for (OboDefinitionCitation citation : term.getOboDefinitionCitation()) {
            OBOXRef[] oboxRef = citation.getOboXrefs();
            for (OBOXRef xref : oboxRef) {
                if (xref.getId() != null && !xref.getId().isEmpty()) {
                    if (xref.getDatabase() != null) {
                        xrefs.put("xref_definition_" + xref.getId(), xref.getDatabase() + ":" + xref.getId());
                    } else {
                        xrefs.put("xref_definition_" + xref.getId(), xref.getId());
                    }
                }
            }
        }
        return xrefs;
    }

    private URI encodeURL(String path, String query) {
        URI uri;
        try {
            String hostname = config.getHostName().split("/")[0]; //e.g. www.ebi.ac.uk
            String hostnamePath = config.getHostName().split("/")[1]; //e.g. ols
            uri = new URI(config.getProtocol(), hostname, "/" + hostnamePath + path, query, null);
            return uri;
        } catch (URISyntaxException e) {
            throw new RestClientException("The query could not be encoded");
        }
    }

    private <T> T getForObject(String url, Class<T> clazz) throws RestClientException {
        try {
            return getForObject(new URI(url), clazz);
        } catch (URISyntaxException e) {
            throw new RestClientException(e.getMessage());
        }
    }

    private <T> T getForObject(URI uri, Class<T> clazz) throws RestClientException {
        try {
            return this.mapper.readValue(uri.toURL(), clazz);
        } catch (IOException e) {
            throw new RestClientException(e.getMessage());
        }
    }
}