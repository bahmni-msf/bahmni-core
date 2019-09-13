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
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Component
public class CSVObservationHelper {
    private final ConceptCache conceptCache;
    private final ConceptService conceptService;

    @Autowired
    CSVObservationHelper(ConceptService conceptService) {
        this.conceptCache = new ConceptCache(conceptService);
        this.conceptService = conceptService;
    }

    protected Concept getConcept(String conceptName) {
        return conceptCache.getConcept(conceptName);
    }

    public void createObservations(List<EncounterTransaction.Observation> observations, Date encounterDate, KeyValue obsRow, List<String> conceptNames) throws ParseException {
        EncounterTransaction.Observation existingObservation = getRootObservationIfExists(observations, conceptNames, null);
        if (existingObservation == null) {
            observations.add(createObservation(conceptNames, encounterDate, obsRow));
        } else {
            updateObservation(conceptNames, existingObservation, encounterDate, obsRow);

        }
    }

    public void verifyNumericConceptValue(KeyValue obsRow, List<String> conceptNames) {
        String lastConceptName = conceptNames.get(conceptNames.size() - 1);
        Concept lastConcept = conceptService.getConceptByName(lastConceptName);
        if (lastConcept.isNumeric()) {
            ConceptNumeric cn = (ConceptNumeric) lastConcept;
            if (!cn.getAllowDecimal() && obsRow.getValue().contains(".")) {
                throw new APIException("Decimal is not allowed for " + cn.getName() + " concept");
            }
        }
    }

    private void updateObservation(List<String> conceptNames, EncounterTransaction.Observation existingObservation, Date encounterDate, KeyValue obsRow) throws ParseException {
        existingObservation.addGroupMember(createObservation(conceptNames, encounterDate, obsRow));
    }

    private EncounterTransaction.Observation getRootObservationIfExists(List<EncounterTransaction.Observation> observations, List<String> conceptNames, EncounterTransaction.Observation existingObservation) {
        for (EncounterTransaction.Observation observation : observations) {
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

    private EncounterTransaction.Observation createObservation(List<String> conceptNames, Date encounterDate, KeyValue obsRow) throws ParseException {
        Concept obsConcept = conceptCache.getConcept(conceptNames.get(0));
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept(obsConcept.getUuid(), obsConcept.getName().getName());

        EncounterTransaction.Observation observation = new EncounterTransaction.Observation();
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

    private String getValue(KeyValue obsRow, Concept obsConcept) throws ParseException {
        if (obsConcept.getDatatype().isCoded()) {
            List<Concept> valueConcepts = conceptService.getConceptsByName(obsRow.getValue());
            Concept valueConcept = null;
            for (Concept concept : valueConcepts) {
                ConceptName name = concept.getFullySpecifiedName(Context.getLocale()) != null ? concept.getFullySpecifiedName(Context.getLocale()) : concept.getName();
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
        return isNotBlank(key) ? new ArrayList<>(asList(key.split("\\."))) : new ArrayList<String>();
    }

    public static boolean isForm2Type(KeyValue obsRow) {
        String key = obsRow.getKey();
        if (StringUtils.isNotBlank(key)) {
            String[] csvHeaderParts = key.split("\\.");
            return csvHeaderParts[0].equalsIgnoreCase("form2");
        }
        return false;
    }

    public boolean isForm1Type(KeyValue keyValue) {
        return !isForm2Type(keyValue);
    }
}
