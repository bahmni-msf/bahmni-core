package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotRow;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotTable;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.bahmni.module.bahmnicore.contract.form.helper.FormUtil.getFormNameAlongWithVersion;

public class BahmniFormBuilderObsToTabularViewMapper extends BahmniObservationsToTabularViewMapper {
    public PivotTable constructTable(Set<EncounterTransaction.Concept> concepts,
                                     Collection<BahmniObservation> bahmniObservations, String groupByConcept) {
        PivotTable pivotTable = new PivotTable();
        if (bahmniObservations == null) {
            return pivotTable;
        }
        List<PivotRow> rows = constructRows(concepts, bahmniObservations, groupByConcept);

        pivotTable.setHeaders(concepts);
        pivotTable.setRows(rows);
        return pivotTable;
    }

    private List<PivotRow> constructRows(Set<EncounterTransaction.Concept> concepts,
                                         Collection<BahmniObservation> bahmniObservations, String groupByConceptName) {
        Map<String, List<BahmniObservation>> rowsMapper = getRowsMapper(bahmniObservations);
        List<PivotRow> rows = new ArrayList<>();
        rowsMapper.forEach((rowIdentifier, rowObservations) -> {
            PivotRow row = new PivotRow();
            rowObservations.forEach(observation -> addColumn(concepts, row, observation));
            if (isValidRow(groupByConceptName, row)) {
                rows.add(row);
            }
        });
        return rows;
    }

    private boolean isValidRow(String groupByConceptName, PivotRow row) {
        return row.getColumns().containsKey(groupByConceptName) && isNonNullRow(groupByConceptName, row);
    }


    private Map<String, List<BahmniObservation>> getRowsMapper(Collection<BahmniObservation> bahmniObservations) {
        Map<String, List<BahmniObservation>> rowsMapper = new HashMap<>();
        bahmniObservations.forEach(bahmniObservation -> {
            String rowIdentifier = getRowIdentifier(bahmniObservation);
            List<BahmniObservation> bahmniObs;
            bahmniObs = rowsMapper.containsKey(rowIdentifier) ? rowsMapper.remove(rowIdentifier) : new ArrayList<>();
            bahmniObs.add(bahmniObservation);
            rowsMapper.put(rowIdentifier, bahmniObs);
        });
        return rowsMapper;
    }

    private String getRowIdentifier(BahmniObservation bahmniObservation) {
        return bahmniObservation.getEncounterUuid() + getFormNameAlongWithVersion(bahmniObservation.getFormFieldPath());
    }
}
