package org.opentdk.api.filter;

import lombok.Getter;

public enum EOperator {

    CONTAINS("CONTAINS", "like"),
    CONTAINS_DATE("CONTAINS_DATE", ""),
    CONTAINS_DATE_AFTER("CONTAINS_DATE_AFTER", ""),
    CONTAINS_DATE_BEFORE("CONTAINS_DATE_BEFORE", ""),
    CONTAINS_IGNORE_CASE("CONTAINS_IGNORE_CASE", ""),
    DATE_AFTER("DATE_AFTER", ""),
    DATE_BEFORE("DATE_BEFORE", ""),
    DATE_EQUALS("DATE_EQUALS", ""),
    ENDS_WITH("ENDS_WITH", ""),
    ENDS_WITH_IGNORE_CASE("ENDS_WITH_IGNORE_CASE", ""),
    EQUALS("EQUALS", "="),
    EQUALS_IGNORE_CASE("EQUALS_IGNORE_CASE", "="),
    GREATER_THAN("GRATER_THAN", ">"),
    GREATER_OR_EQUAL_THAN("GRATER_OR_EQUAL_THAN", ">="),
    LESS_THAN("LESS_THAN", "<"),
    LESS_OR_EQUAL_THAN("LESS_OR_EQUAL_THAN", "<="),
    NOT_EQUALS("NOT_EQUALS", "<>"),
    NOT_EQUALS_IGNORE_CASE("NOT_EQUALS_IGNORE_CASE", "<>"),
    STARTS_WITH("STARTS_WITH", ""),
    STARTS_WITH_IGNORE_CASE("STARTS_WITH_IGNORE_CASE", ""),
    AND("AND", "AND"),
    OR("OR", "OR"),
    IN("IN", "IN"),
    BETWEEN("BETWEEN", "BETWEEN");

    @Getter
    private final String paramName;
    @Getter
    private final String dVal;

    EOperator(String paramName, String dVal) {
        this.paramName = paramName;
        this.dVal = dVal;
    }
}
