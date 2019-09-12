package org.bahmni.module.admin.observation;

import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.APIException;
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

    @Rule
    ExpectedException exception = ExpectedException.none();

    @Mock
    private ConceptService conceptService;

    private EncounterRow encounterRow;

    private Concept heightConcept;

    private Concept weightConcept;
    private ConceptDatatype conceptDatatype;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        encounterRow = new EncounterRow();
        encounterRow.encounterDateTime = "2019-09-11";

        Concept bmiDataConcept = mock(Concept.class);
        ConceptName bmiDataConceptName = mock(ConceptName.class);
        when(bmiDataConceptName.getName()).thenReturn("BMI Data");
        when(bmiDataConcept.getName()).thenReturn(bmiDataConceptName);
        when(conceptService.getConcept("BMI Data")).thenReturn(bmiDataConcept);
        when(conceptService.getConceptByName("BMI Data")).thenReturn(bmiDataConcept);

        ConceptName heightConceptName = mock(ConceptName.class);
        heightConcept = mock(Concept.class);
        when(heightConceptName.getName()).thenReturn("Height");
        when(heightConcept.getName()).thenReturn(heightConceptName);
        when(conceptService.getConcept("Height")).thenReturn(heightConcept);
        when(conceptService.getConceptByName("Height")).thenReturn(heightConcept);
        when(conceptService.getConceptsByName("Height")).thenReturn(singletonList(heightConcept));
        conceptDatatype = mock(ConceptDatatype.class);
        when(conceptDatatype.isCoded()).thenReturn(false);
        when(heightConcept.getDatatype()).thenReturn(conceptDatatype);

        weightConcept = mock(Concept.class);
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

        KeyValue csvHeightObs = new KeyValue("BMI Data.Height", "150");
        KeyValue csvWeightObs = new KeyValue("BMI Data.Weight", "100");
        encounterRow.obsRows = asList(csvHeightObs, csvWeightObs);

        List<EncounterTransaction.Observation> observations = observationMapper.getObservations(encounterRow);

        assertEquals(1, observations.size());
        EncounterTransaction.Observation bmiDataObs = observations.get(0);
        assertEquals("BMI Data", bmiDataObs.getConcept().getName());
        assertEquals(2, bmiDataObs.getGroupMembers().size());
        final List<String> actualConcepts = bmiDataObs.getGroupMembers().stream()
                .map(observation -> observation.getConcept().getName()).collect(Collectors.toList());
        assertThat(actualConcepts, containsInAnyOrder(asList("Height", "Weight").toArray()));

    }

    @Test
    public void shouldThrowExceptionIfDecimalValueisGivenForNumericConcept() throws ParseException {
        ConceptNumeric bmiConcept = mock(ConceptNumeric.class);
        when(bmiConcept.isNumeric()).thenReturn(true);
        String bmi = "BMI";
        when(conceptService.getConcept(bmi)).thenReturn(bmiConcept);
        when(conceptService.getConceptByName(bmi)).thenReturn(bmiConcept);
        ConceptName conceptName = new ConceptName();
        conceptName.setName(bmi);
        when(bmiConcept.getName()).thenReturn(conceptName);
        KeyValue csvHeightObs = new KeyValue(bmi, "1.34");
        encounterRow.obsRows = singletonList(csvHeightObs);

        ObservationMapper observationMapper = new ObservationMapper(conceptService);

        exception.expect(APIException.class);
        exception.expectMessage("Decimal is not allowed for BMI concept");
        observationMapper.getObservations(encounterRow);
    }

    @Test
    public void shouldCreateObservationForCodedConcept() throws ParseException {

        KeyValue csvHeightObs = new KeyValue("BMI Data.Height", "tall");
        encounterRow.obsRows = asList(csvHeightObs);
        Concept valueConcept = mock(Concept.class);

        when(conceptDatatype.isCoded()).thenReturn(true);
        when(conceptService.getConceptsByName("tall")).thenReturn(asList(valueConcept));

        ConceptName valueConceptName = mock(ConceptName.class);
        when(valueConcept.getFullySpecifiedName(Matchers.any())).thenReturn(null);
        when(valueConcept.getName()).thenReturn(valueConceptName);
        when(valueConceptName.getName()).thenReturn("tall");

        ObservationMapper observationMapper = new ObservationMapper(conceptService);
        List<EncounterTransaction.Observation> observations = observationMapper.getObservations(encounterRow);

        assertEquals(1, observations.size());

    }
}
