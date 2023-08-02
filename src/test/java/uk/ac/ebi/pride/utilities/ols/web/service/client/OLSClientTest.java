package uk.ac.ebi.pride.utilities.ols.web.service.client;


import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Ontology;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import java.net.URI;
import java.util.*;



/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 */
public class OLSClientTest {

    private static OLSClient olsClient = new OLSClient(new OLSWsConfig());
    private static final Logger logger = LoggerFactory.getLogger(OLSClientTest.class);

    @Test
    public void testGetTermById() throws Exception {
        Term term = olsClient.getTermById(new Identifier("MS:1001767", Identifier.IdentifierType.OBO), "MS");
        Assert.assertTrue(term.getTermOBOId().getIdentifier().equalsIgnoreCase("MS:1001767"));
    }

    @Test
    public void testGetOntologyNames() throws Exception {
        List<Ontology> ontologies = olsClient.getOntologies();
        logger.info(ontologies.toString());
        Assert.assertTrue(ontologies.size() > 0);
    }

    @Test
    public void testGetAllTermsFromOntology() throws Exception {
        List<Term> terms = olsClient.getAllTermsFromOntology("mi");
        logger.info(terms.toString());
        Assert.assertTrue(terms.size() > 0);
    }

    @Test
    public void testGetRootTerms() throws Exception {
        List<Term> rootTerms = olsClient.getRootTerms("ms");
        logger.info(rootTerms.toString());
        Assert.assertTrue(rootTerms.size() > 0);
    }

    @Test
    public void testGetTermsBySynonym() throws Exception {
        List<Term> terms = olsClient.getTermsByName("lncRNA", "mi", false);
        logger.info(terms.toString());
        Assert.assertTrue(terms.size() > 0);
        terms = olsClient.getTermsByName("lncRNA", "mi", true);
        Optional<Term> target = terms.stream().filter(term -> term.getTermOBOId().getIdentifier().equalsIgnoreCase("MI:2190")).findFirst();
        Assert.assertTrue(target.isPresent());
        Assert.assertTrue(target.get().getLabel().toLowerCase().contains("long"));
    }

    @Test
    public void testGetTermsByName() throws Exception {
        List<Term> terms = olsClient.getTermsByName("modification", "ms", false);
        logger.info(terms.toString());
        Assert.assertTrue(terms.size() > 0);
        terms = olsClient.getTermsByName("modification", "ms", true);
        Optional<Term> target = terms.stream().filter(term -> term.getTermOBOId().getIdentifier().equalsIgnoreCase("MS:1002672")).findFirst();
        Assert.assertTrue(target.isPresent());
        Assert.assertTrue(target.get().getLabel().toLowerCase().contains("modification"));
    }

    @Test
    public void testGetTermsBySynonym() throws Exception {
        List<Term> terms = olsClient.getTermsByName("lncRNA", "mi", false);
        logger.info(terms.toString());
        Assert.assertTrue(terms.size() > 0);
        terms = olsClient.getTermsByName("lncRNA", "mi", true);
        Optional<Term> target = terms.stream().filter(term -> term.getTermOBOId().getIdentifier().equalsIgnoreCase("MI:2190")).findFirst();
        Assert.assertTrue(target.isPresent());
        Assert.assertTrue(target.get().getLabel().toLowerCase().contains("long"));
    }

    @Test
    public void testGetTermChildrenByOBOId() throws Exception {
        List<Term> children = olsClient.getTermChildren(new Identifier("MS_1001143", Identifier.IdentifierType.OWL), "ms", 1);
        logger.debug(children.toString());
        Assert.assertTrue(contains(children, new Identifier("MS_1001568", Identifier.IdentifierType.OWL)));
    }

    @Test
    public void testGetTermChildrenByOBOIdMI() throws Exception {
        List<Term> children = olsClient.getTermChildren(new Identifier("MI:0954", Identifier.IdentifierType.OBO), "mi", 1);
        logger.debug(children.toString());
        Assert.assertFalse(children.isEmpty());
        Assert.assertTrue(contains(children, new Identifier("MI:0956", Identifier.IdentifierType.OBO)));
    }

    @Test
    public void testGetTermParentsByOBOIdGo() throws Exception {
        List<Term> children = olsClient.getTermChildren(new Identifier("GO:0140110", Identifier.IdentifierType.OBO), "GO", 1);
        logger.debug(children.toString());
        Assert.assertTrue(contains(children, new Identifier("GO:0034246", Identifier.IdentifierType.OBO)));
    }

    @Test
    public void testGetTermChildrenByShortFormId() throws Exception {
        List<Term> children = olsClient.getTermChildren(new Identifier("MS_1001143", Identifier.IdentifierType.OWL), "ms", 1);
        logger.debug(children.toString());
        Assert.assertTrue(contains(children, new Identifier("MS:1001568", Identifier.IdentifierType.OBO)));
    }

    @Test
    public void testGetTermChildrenByIrIId() throws Exception {
        List<Term> children = olsClient.getTermChildren(new Identifier("http://purl.obolibrary.org/obo/MS_1001143", Identifier.IdentifierType.IRI), "ms", 1);
        logger.debug(children.toString());
        Assert.assertTrue(contains(children, new Identifier("http://purl.obolibrary.org/obo/MS_1001568", Identifier.IdentifierType.IRI)));
    }

    private boolean contains(List<Term> terms, Identifier identifier) {
        for(Term term: terms)
            if(identifier.getType() == Identifier.IdentifierType.OBO &&
                identifier.getIdentifier().equalsIgnoreCase(term.getTermOBOId().getIdentifier()))
                return true;
            else if(identifier.getType() == Identifier.IdentifierType.IRI &&
                identifier.getIdentifier().equalsIgnoreCase(term.getIri().getIdentifier()))
                return true;
            else if(identifier.getType() == Identifier.IdentifierType.OWL &&
                identifier.getIdentifier().equalsIgnoreCase(term.getShortForm().getIdentifier()))
                return true;
        return false;
    }

    @Test
    public void testGetTermsByAnnotationData() throws Exception {
        List<Term> annotations = olsClient.getTermsByAnnotationData("mod","MassAvg", 30, 140);
        Assert.assertTrue(annotations.size() > 0);
    }

    @Test
    public void testGetTermParentsByOBOId() throws Exception {
        List<Term> parents = olsClient.getTermParents(new Identifier("GO:0034246", Identifier.IdentifierType.OBO), "GO", 1);
        logger.debug(parents.toString());
        Assert.assertTrue(contains(parents, new Identifier("GO:0140110", Identifier.IdentifierType.OBO)));
    }

    @Test
    public void testGetTermParentsByOBOIdMI() throws Exception {
        List<Term> parents = olsClient.getTermParents(new Identifier("MI:0013", Identifier.IdentifierType.OBO), "MI", 3);
        logger.debug(parents.toString());
        Assert.assertNotNull(parents);
        Assert.assertEquals(3, parents.size());
        Assert.assertTrue(contains(parents, new Identifier("MI:0000", Identifier.IdentifierType.OBO)));
        Assert.assertTrue(contains(parents, new Identifier("MI:0001", Identifier.IdentifierType.OBO)));
        Assert.assertTrue(contains(parents, new Identifier("MI:0045", Identifier.IdentifierType.OBO)));
    }

    @Test
    public void testGetTermParentsByShortForm() throws Exception {
        List<Term> parents = olsClient.getTermParents(new Identifier("GO_0034246", Identifier.IdentifierType.OWL), "GO", 1);
        logger.debug(parents.toString());
        Assert.assertTrue(contains(parents, new Identifier("GO_0140110", Identifier.IdentifierType.OWL)));
    }

    @Test
    public void testGetTermParentsByIrIId() throws Exception {
        List<Term> parents = olsClient.getTermParents(new Identifier("http://purl.obolibrary.org/obo/GO_0034246", Identifier.IdentifierType.IRI), "GO", 1);
        logger.debug(parents.toString());
        Assert.assertTrue(contains(parents, new Identifier("http://purl.obolibrary.org/obo/GO_0140110", Identifier.IdentifierType.IRI)));
    }

    @Test
    public void SearchTermById(){
        List<Term> term = olsClient.searchTermById("GO:0034246", "go");
        Assert.assertTrue(term.get(0).getOntologyName().equals("go"));
    }

    @Test
    public void testGetOntology(){
        Ontology ontology = olsClient.getOntology("efo");
        Assert.assertEquals("efo", ontology.getNamespace());
    }

    @Test
    public void testGetExactTerm() throws Exception {
        String termLabel = "allosteric change in dynamics";
        String ontologyName = "mi";
        Term term = olsClient.getExactTermByName(termLabel, ontologyName);
        Assert.assertNotNull(term);
        Assert.assertEquals(term.getLabel(), termLabel);
        Assert.assertEquals(term.getOntologyName(), ontologyName);
        Assert.assertEquals(term.getTermOBOId().getIdentifier(), "MI:1166");
    }

    @Test
    public void testGetOBOSynonyms() throws Exception {
        Identifier identifier = new Identifier("MI:0018", Identifier.IdentifierType.OBO);
        Map<String,String> synonymsMap = olsClient.getOBOSynonyms(identifier, "mi");
        Collection<String> synonyms = synonymsMap.keySet();
        Assert.assertEquals(synonyms.size(), 8);
        Assert.assertTrue(synonyms.contains("classical two hybrid"));
        Assert.assertTrue(synonyms.contains("Gal4 transcription regeneration"));
        Assert.assertTrue(synonyms.contains("yeast two hybrid"));
        Assert.assertTrue(synonyms.contains("two-hybrid"));
        Assert.assertTrue(synonyms.contains("2 hybrid"));
        Assert.assertTrue(synonyms.contains("2-hybrid"));
        Assert.assertTrue(synonyms.contains("2h"));
        Assert.assertTrue(synonyms.contains("2H"));
    }

    @Test
    public void testGetExactTermsByName(){
        String termName = "liver";
        String ontologyName = "efo";
        List <Term > terms = olsClient.getExactTermsByName(termName, null);
        Assert.assertNotNull(terms);
        Assert.assertTrue(!terms.isEmpty());
        for (Term term : terms){
            Assert.assertTrue(term.getLabel().toLowerCase().contains(termName));
        }
        terms = olsClient.getExactTermsByName(termName, ontologyName);
        Assert.assertNotNull(terms);
        Assert.assertFalse(terms.isEmpty());
        Assert.assertEquals(1, terms.size());
        Assert.assertEquals(terms.get(0).getIri().getIdentifier(),"http://purl.obolibrary.org/obo/UBERON_0002107");
    }

    @Test
    public void testGetExactTermsByNameFromParent(){
        String termName = "liver";
        String ontologyName = "efo";
        String parentTerm = "http://www.ebi.ac.uk/efo/EFO_0000635"; //organism part
        List <Term > termsFromParent = olsClient.getExactTermsByNameFromParent(termName, null, parentTerm);
        List <Term > terms = olsClient.getExactTermsByName(termName, null);
        Assert.assertNotNull(termsFromParent);
        Assert.assertNotEquals(termsFromParent.size(), terms.size());
        terms = olsClient.getExactTermsByNameFromParent(termName, ontologyName, parentTerm);
        Assert.assertNotNull(terms);
        Assert.assertTrue(!terms.isEmpty());
        Assert.assertEquals(terms.get(0).getIri().getIdentifier(),"http://purl.obolibrary.org/obo/UBERON_0002107");
        Assert.assertEquals(terms.get(0).getOntologyName().toLowerCase(),"efo");
    }

    @Test
    public void testGetOntologyFromFilePath(){
        Ontology ontology = olsClient.getOntologyFromFilePath(URI.create("http://www.ebi.ac.uk/efo/efo.owl"));
        Assert.assertEquals(ontology.getNamespace(),"efo");
//        ontology = olsClient.getOntologyFromFilePath(URI.create("http://purl.obolibrary.org/obo/pride_cv.obo"));
//        Assert.assertEquals(ontology.getNamespace(),"pride");
        ontology = olsClient.getOntologyFromFilePath(URI.create("http://enanomapper.github.io/ontologies/enanomapper.owl"));
        Assert.assertEquals(ontology.getNamespace(),"enm");
//        ontology = olsClient.getOntologyFromFilePath(URI.create("http://opendata.inra.fr/EOL/eol_ontology"));
//        Assert.assertEquals(ontology.getNamespace(),"eol");
//        ontology = olsClient.getOntologyFromFilePath(URI.create("http://www.bio.ntnu.no/ontology/GeXO/gexo.owl"));
//        Assert.assertEquals(ontology.getNamespace(),"gexo");
        ontology = olsClient.getOntologyFromFilePath(URI.create("http://purl.obolibrary.org/obo/go.owl"));
        Assert.assertEquals(ontology.getNamespace(),"go");
    }

    @Test
    public void testGetOntologyFromId(){
        Ontology ontology = olsClient.getOntologyFromId("efo");
        Assert.assertEquals(ontology.getNamespace(),"efo");
//        ontology = olsClient.getOntologyFromId("pride");
//        Assert.assertEquals(ontology.getNamespace(),"pride");
        ontology = olsClient.getOntologyFromId("enm");
        Assert.assertEquals(ontology.getNamespace(),"enm");
//        ontology = olsClient.getOntologyFromId("eol");
//        Assert.assertEquals(ontology.getNamespace(),"eol");
//        ontology = olsClient.getOntologyFromId("gexo");
//        Assert.assertEquals(ontology.getNamespace(),"gexo");
        ontology = olsClient.getOntologyFromId("go");
        Assert.assertEquals(ontology.getNamespace(),"go");
    }

    @Test
    public void testGetMetaData() throws Exception {
        Identifier identifier0 = new Identifier("MI:0446", Identifier.IdentifierType.OBO);
        Map metadata0 = olsClient.getMetaData(identifier0, "mi");
        Assert.assertEquals(1, metadata0.size());
        Assert.assertNotNull(metadata0.get("definition"));
        Assert.assertEquals("PubMed is designed to provide access to citations from biomedical literature. The data can be found at both NCBI PubMed and Europe PubMed Central. \n" +
                "http://www.ncbi.nlm.nih.gov/pubmed\n" +
                "http://europepmc.org", metadata0.get("definition"));
        Identifier identifier1 = new Identifier("MI:0018", Identifier.IdentifierType.OBO);
        Map metadata1 = olsClient.getMetaData(identifier1, "mi");
        Assert.assertEquals(2, metadata1.size());
        Assert.assertNotNull(metadata1.get("synonym"));
        Assert.assertNotNull(metadata1.get("definition"));
        Identifier identifier2 = new Identifier("MOD:01161", Identifier.IdentifierType.OBO);
        Map metadata2 = olsClient.getMetaData(identifier2, "mod");
        Map synonyms = (Map) metadata2.get("synonym");
        Assert.assertEquals(2, metadata2.size());
        Assert.assertNotNull(metadata2.get("synonym"));
        Assert.assertNotNull(metadata2.get("definition"));
        Assert.assertNull(metadata2.get("comment"));
        Assert.assertEquals(3, synonyms.size());
        Assert.assertEquals("A protein modification that effectively removes oxygen atoms from a residue without the removal of hydrogen atoms.", metadata2.get("definition"));
    }

    @Test
    public void testGetTermXrefs() throws Exception {
        Identifier identifier1 = new Identifier("MI:0446", Identifier.IdentifierType.OBO);
        Map xrefs = olsClient.getTermXrefs(identifier1, "mi");
        Assert.assertEquals(3, xrefs.size());
        Assert.assertEquals("[0-9]+", xrefs.get("id-validation-regexp"));
        Assert.assertEquals("http://europepmc.org/abstract/MED/${ac}", xrefs.get("search-url"));
        Assert.assertEquals("PMID:14755292", xrefs.get("xref_definition_14755292"));
    }

    @Test
    public void testGetLabelByIriString(){
        String iri = "http://www.orpha.net/ORDO/Orphanet_101150";
        List<Term> terms = olsClient.getExactTermsByIriString(iri);
        Assert.assertTrue(terms.size() > 0);

    }

    @Test
    public void testObsolete(){
        String id = "http://edamontology.org/data_0007";
        Assert.assertTrue(olsClient.isObsolete(id));
        String oboid = "EFO:0005099";
        Assert.assertTrue(olsClient.isObsolete(oboid, "EFO"));
        Assert.assertTrue(olsClient.isObsolete(oboid, "efo"));
        String shortForm = "EFO_0005099";
        Assert.assertTrue(olsClient.isObsolete(shortForm, "EFO"));
        Assert.assertTrue(olsClient.isObsolete(shortForm, "efo"));
        String iri = "http://www.ebi.ac.uk/efo/EFO_0005099";
        Assert.assertTrue(olsClient.isObsolete(iri, "EFO"));
        Assert.assertTrue(olsClient.isObsolete(iri, "efo"));
        Assert.assertTrue(olsClient.isObsolete("MS:1001057", "ms"));
        Assert.assertTrue(olsClient.isObsolete("EFO_0000891"));
    }

    @Test
    public void testReplacedBy(){
        String id = "EFO_0005099";
        String ontology = "efo";
        Term term = olsClient.getReplacedBy(id);
        String termiri = term.getIri().getIdentifier();
        Assert.assertEquals("http://purl.obolibrary.org/obo/PO_0025197", termiri);
        term = olsClient.getReplacedBy(id, ontology);
        termiri = term.getIri().getIdentifier();
        Assert.assertEquals("http://purl.obolibrary.org/obo/PO_0025197", termiri);
        id = "EFO:0005099";
        term = olsClient.getReplacedBy(id, ontology);
        termiri = term.getIri().getIdentifier();
        Assert.assertEquals("http://purl.obolibrary.org/obo/PO_0025197", termiri);
        id = "http://www.ebi.ac.uk/efo/EFO_0005099";
        term = olsClient.getReplacedBy(id, ontology);
        termiri = term.getIri().getIdentifier();
        Assert.assertEquals("http://purl.obolibrary.org/obo/PO_0025197", termiri);
        id = "EFO_0000400";
        term = olsClient.getReplacedBy(id, ontology);
        Assert.assertNull(term);
        id = "EFO_0000891";
        term = olsClient.getReplacedBy(id);
        Assert.assertEquals("http://purl.obolibrary.org/obo/UBERON_0000010", term.getIri().getIdentifier());
    }

    @Test
    public void testGetTermByIriId() throws Exception {
        Term term = olsClient.getTermById(new Identifier("GO:0031145", Identifier.IdentifierType.OBO), "GO");
        term = olsClient.getTermByIRIId(term.getIri().getIdentifier(), term.getOntologyPrefix());
        Assert.assertTrue(term.getTermOBOId().getIdentifier().equalsIgnoreCase("GO:0031145"));
    }

    @Test
    public void testGetTermByNameSetPageSizeAndNum(){
        olsClient.setSearchPageNum(0);
        olsClient.setSearchPageSize(20);
        List<Term> terms = olsClient.getTermsByName("liver", null, false);
        Assert.assertTrue(terms.size() <= 20);
    }

    @Test
    public void testGetExactTermByIriString(){
        List<Term> terms = olsClient.getExactTermsByIriString("http://purl.obolibrary.org/obo/UBERON_0000014");
        for (Term term : terms){
            Assert.assertTrue(term.getIri().getIdentifier().equals("http://purl.obolibrary.org/obo/UBERON_0000014"));
        }
    }
}