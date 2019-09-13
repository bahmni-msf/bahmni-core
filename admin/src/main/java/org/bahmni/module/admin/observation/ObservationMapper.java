package org.bahmni.module.admin.observation;

import org.apache.commons.lang.StringUtils;
import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.bahmni.module.admin.observation.handler.ObsHandler;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component(value = "adminObservationMapper")
public class ObservationMapper {

    private ConceptService conceptService;

    @Autowired
    @Deprecated
    public ObservationMapper(ConceptService conceptService) {

    }

    @Autowired
    private List<ObsHandler> obsHandlers;



    public List<EncounterTransaction.Observation> getObservations(EncounterRow encounterRow) throws ParseException {
        return null;

    }

}
