package org.bahmni.module.admin.observation;

import org.apache.commons.lang.StringUtils;
import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.bahmni.module.admin.csv.utils.CSVUtils;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component(value = "adminObservationMapper")
public class ObservationMapper {

    @Autowired
    @Deprecated
    public ObservationMapper(ConceptService conceptService) {

    }

    @Autowired
    private CSVObservationHelper csvObservationHelper;

    public List<EncounterTransaction.Observation> getObservations(EncounterRow encounterRow) throws ParseException {
        //List<KeyValue> obsRows = CSVUtils.getNonForm2ObsRows(encounterRow);
        //List<EncounterTransaction.Observation> observations = getObservations(encounterRow, obsRows);
        List<EncounterTransaction.Observation> observations = getObservations(encounterRow, encounterRow.obsRows);
        return observations;
    }

    private List<EncounterTransaction.Observation> getObservations(EncounterRow encounterRow, List<KeyValue> obsRows) throws ParseException {
        List<EncounterTransaction.Observation> observations = new ArrayList<>();
        Date encounterDate = encounterRow.getEncounterDate();
        for (KeyValue obsRow : obsRows) {
            if (obsRow.getValue() != null && !StringUtils.isEmpty(obsRow.getValue().trim())) {
                List<String> conceptNames = new ArrayList<>(Arrays.asList(obsRow.getKey().split("\\.")));

                csvObservationHelper.verifyNumericConceptValue(obsRow, conceptNames);

                csvObservationHelper.createObservations(observations, encounterDate, obsRow, conceptNames);
            }
        }
        return observations;
    }




}
