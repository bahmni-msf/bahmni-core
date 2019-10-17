package org.bahmni.module.admin.observation;

import org.apache.commons.lang.StringUtils;
import org.bahmni.csv.KeyValue;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction.Observation;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
public class CSVObservationHelper {
    private final ConceptCache conceptCache;
    private final ConceptService conceptService;
    private final String FORM2_TYPE = "form2";

    @Autowired
    CSVObservationHelper(ConceptService conceptService) {
        this.conceptCache = new ConceptCache(conceptService);
        this.conceptService = conceptService;
    }

    public static <T> T getLastItem(List<T> items) {
        if (isEmpty(items)) {
            throw new InvalidParameterException("Empty items");
        }
        return items.get(items.size() - 1);
    }

    protected Concept getConcept(String conceptName) {
        return conceptCache.getConcept(conceptName);
    }

    public void createObservations(List<Observation> observations, Date encounterDate,
                                   KeyValue obsRow, List<String> conceptNames) throws ParseException {
        Observation existingObservation = getRootObservationIfExists(observations, conceptNames, null);
        if (existingObservation == null) {
            observations.add(createObservation(conceptNames, encounterDate, obsRow));
        } else {
            updateObservation(conceptNames, existingObservation, encounterDate, obsRow);

        }
    }

    public void verifyNumericConceptValue(KeyValue obsRow, List<String> conceptNames) {
        String lastConceptName = getLastItem(conceptNames);
        Concept lastConcept = conceptService.getConceptByName(lastConceptName);
        if (lastConcept.isNumeric()) {
            ConceptNumeric cn = (ConceptNumeric) lastConcept;
            if (!cn.getAllowDecimal() && obsRow.getValue().contains(".")) {
                throw new APIException("Decimal is not allowed for " + cn.getName() + " concept");
            }
        }
    }

    private void updateObservation(List<String> conceptNames, Observation existingObservation,
                                   Date encounterDate, KeyValue obsRow) throws ParseException {
        existingObservation.addGroupMember(createObservation(conceptNames, encounterDate, obsRow));
    }

    private Observation getRootObservationIfExists(List<Observation> observations, List<String> conceptNames,
                                                   Observation existingObservation) {
        for (Observation observation : observations) {
            if (observation.getConcept().getName().equals(conceptNames.get(0))) {
                conceptNames.remove(0);
                if (conceptNames.size() == 0) {
                    conceptNames.add(observation.getConcept().getName());
                    return existingObservation;
                }
                existingObservation = observation;
                return getRootObservationIfExists(observation.getGroupMembers(), conceptNames, existingObservation);
            }
        }
        return existingObservation;
    }

    private Observation createObservation(List<String> conceptNames, Date encounterDate, KeyValue obsRow) {
        Concept obsConcept = conceptCache.getConcept(conceptNames.get(0));
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept(obsConcept.getUuid(),
                obsConcept.getName().getName());

        Observation observation = new Observation();
        observation.setConcept(concept);
        observation.setObservationDateTime(encounterDate);
        if (conceptNames.size() == 1) {
            observation.setValue(getValue(obsRow, obsConcept));
        } else {
            conceptNames.remove(0);
            observation.addGroupMember(createObservation(conceptNames, encounterDate, obsRow));
        }
        return observation;
    }

    private String getValue(KeyValue obsRow, Concept obsConcept) {
        if (obsConcept.getDatatype().isCoded()) {
            List<Concept> valueConcepts = conceptService.getConceptsByName(obsRow.getValue());
            Concept valueConcept = null;
            for (Concept concept : valueConcepts) {
                ConceptName name = concept.getFullySpecifiedName(Context.getLocale()) != null ?
                        concept.getFullySpecifiedName(Context.getLocale()) : concept.getName();
                if (name.getName().equalsIgnoreCase(obsRow.getValue())) {
                    valueConcept = concept;
                    break;
                }
            }
            if (valueConcept == null)
                throw new ConceptNotFoundException(obsRow.getValue() + " not found");
            return valueConcept.getUuid();
        }
        return obsRow.getValue();
    }

    public List<String> getCSVHeaderParts(KeyValue csvObservation) {
        String key = csvObservation.getKey();
        return isNotBlank(key) ? new ArrayList<>(asList(key.split("\\."))) : new ArrayList<>();
    }

    public boolean isForm2Type(KeyValue obsRow) {
        String key = obsRow.getKey();
        if (StringUtils.isNotBlank(key)) {
            String[] csvHeaderParts = key.split("\\.");
            return csvHeaderParts[0].equalsIgnoreCase(FORM2_TYPE);
        }
        return false;
    }

    public boolean isForm1Type(KeyValue keyValue) {
        return !isForm2Type(keyValue);
    }
}
