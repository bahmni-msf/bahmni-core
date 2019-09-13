package org.bahmni.module.admin.observation.handler;


import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.text.ParseException;
import java.util.List;

public interface ObsHandler {

    List<KeyValue> getRelatedCSVObs(EncounterRow encounterRow);

    List<EncounterTransaction.Observation> handle(EncounterRow encounterRow) throws ParseException;
}
