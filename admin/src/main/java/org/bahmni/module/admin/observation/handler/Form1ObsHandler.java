package org.bahmni.module.admin.observation.handler;


import org.apache.commons.lang.StringUtils;
import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.bahmni.module.admin.observation.CSVObservationHelper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class Form1ObsHandler implements ObsHandler {

    private CSVObservationHelper csvObservationHelper;

    @Autowired
    public Form1ObsHandler(CSVObservationHelper csvObservationHelper) {
        this.csvObservationHelper = csvObservationHelper;
    }

    @Override
    public List<KeyValue> getRelatedCSVObs(EncounterRow encounterRow) {

        return null;
    }

    @Override
    public List<EncounterTransaction.Observation> handle(EncounterRow encounterRow) throws ParseException {
        List<EncounterTransaction.Observation> observations = new ArrayList<>();
        List<KeyValue> csvObservations = getRelatedCSVObs(encounterRow);
        for (KeyValue csvObservation : csvObservations) {
            if (StringUtils.isNotBlank(csvObservation.getValue())) {
                List<String> conceptNames = csvObservationHelper.getCSVHeaderParts(csvObservation);
                csvObservationHelper.verifyNumericConceptValue(csvObservation, conceptNames);
                csvObservationHelper.createObservations(observations, encounterRow.getEncounterDate(), csvObservation, conceptNames);
            }
        }

        return observations;
    }

}
