package org.bahmni.module.admin.observation;

import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class ObservationMapperTest {

    @Mock
    private ConceptService conceptService;

    private EncounterRow encounterRow;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        encounterRow = new EncounterRow();
        encounterRow.encounterDateTime = "2019-09-11";
        KeyValue csvHeightObs = new KeyValue("BMI Data.Height", "150");
        KeyValue csvWeightObs = new KeyValue("BMI Data.Weight", "100");
        encounterRow.obsRows = asList(csvHeightObs, csvWeightObs);

        Concept bmiDataConcept = mock(Concept.class);
        ConceptName bmiDataConceptName = mock(ConceptName.class);
        when(bmiDataConceptName.getName()).thenReturn("BMI Data");
        when(bmiDataConcept.getName()).thenReturn(bmiDataConceptName);
        when(conceptService.getConcept("BMI Data")).thenReturn(bmiDataConcept);
        when(conceptService.getConceptByName("BMI Data")).thenReturn(bmiDataConcept);

        Concept heightConcept = mock(Concept.class);
        ConceptName heightConceptName = mock(ConceptName.class);
        when(heightConceptName.getName()).thenReturn("Height");
        when(heightConcept.getName()).thenReturn(heightConceptName);
        when(conceptService.getConcept("Height")).thenReturn(heightConcept);
        when(conceptService.getConceptByName("Height")).thenReturn(heightConcept);
        when(conceptService.getConceptsByName("Height")).thenReturn(singletonList(heightConcept));
        ConceptDatatype conceptDatatype = mock(ConceptDatatype.class);
        when(conceptDatatype.isCoded()).thenReturn(false);
        when(heightConcept.getDatatype()).thenReturn(conceptDatatype);

        Concept weightConcept = mock(Concept.class);
        ConceptName weightConceptName = mock(ConceptName.class);
        when(weightConceptName.getName()).thenReturn("Weight");
        when(weightConcept.getName()).thenReturn(weightConceptName);
        when(conceptService.getConcept("Weight")).thenReturn(weightConcept);
        when(conceptService.getConceptByName("Weight")).thenReturn(weightConcept);
        when(conceptService.getConceptsByName("Weight")).thenReturn(singletonList(weightConcept));
        when(weightConcept.getDatatype()).thenReturn(conceptDatatype);

        PowerMockito.mockStatic(Context.class);
        when(Context.getConceptService()).thenReturn(conceptService);
    }

    @Test
    public void shouldCreateHeightAndWeightObsUnderBMIDataFromEncounter() throws ParseException {

        ObservationMapper observationMapper = new ObservationMapper(conceptService);

        List<EncounterTransaction.Observation> observations = observationMapper.getObservations(encounterRow);

        assertEquals(1, observations.size());
        EncounterTransaction.Observation bmiDataObs = observations.get(0);
        assertEquals("BMI Data", bmiDataObs.getConcept().getName());
        assertEquals(2, bmiDataObs.getGroupMembers().size());
        final List<String> actualConcepts = bmiDataObs.getGroupMembers().stream()
                .map(observation -> observation.getConcept().getName()).collect(Collectors.toList());
        assertThat(actualConcepts, containsInAnyOrder(asList("Height", "Weight").toArray()));

    }
}