package uk.ac.ebi.pride.utilities.ols.web.service.cache;

import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfigProd;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OntologyCacheService {

    private static OLSClient olsClient = new OLSClient(new OLSWsConfigProd());

    private Set<String> ontologyTermSet;
    private AtomicInteger size=new AtomicInteger(0);
    private final int maxCapacity=100;

    OntologyCacheService(){
        System.out.println("######## New Ontology Cache Object Initialized ##########");
        //creating a concurrent hash set implementation for java 8
        ontologyTermSet = ConcurrentHashMap.newKeySet(maxCapacity);
    }

    public boolean isTermExisting(String attributeAccession, String ontologyAccession){
        StringBuilder ontologyKey = new StringBuilder(attributeAccession+ontologyAccession);
        if(ontologyTermSet.contains(ontologyKey.toString())){
            System.out.println("Ontology Cache Current Size:"+size.get());
            return true;
        }else{
            Term term =  olsClient.getTermById(new Identifier(attributeAccession, Identifier.IdentifierType.OBO), ontologyAccession);
            if(term!=null){
                //check if cache size is exceeding
                if(size.get()<maxCapacity) {
                    ontologyTermSet.add(ontologyKey.toString());
                    size.incrementAndGet();
                    System.out.println("Ontology Cache Size Incremented:"+size.get());
                }
                return true;
            }else{
                return false;
            }
        }
    }



}
