package org.lk.devkit.filter;

import lombok.Getter;
import org.lk.devkit.util.DateUtil;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to define rules to filter data from tabular data formats.
 *
 * @author LK Test Solutions
 */
@Getter
public class FilterRule {

    /**
     * This object is used to specify the format of the rule value.
     */
    public enum ERuleFormat {
        STRING, QUOTED_STRING, REGEX, QUOTED_REGEX;
    }

    /**
     * Defines the name of the sequence, where the rules for specified value(s) will be checked. A sequence can be a column or row.
     */
    private String headerName;

    /**
     * Defines the value, used by check operation of the rule.
     */
    private String value;

    /**
     * An array with multiple values, used by check operation of the rule.
     */
    private String[] values;

    /**
     * The operator used by check operation for the defined value of the rule.
     */
    private EOperator filterOperator;

    /**
     * The operator used to concatenate the rule with other rules of this filter instance.
     */
    private EOperator ruleConcatenationOperator;

    /**
     * The complete rule including {@link #ruleConcatenationOperator}, {@link #headerName}, {@link #filterOperator} and {@link #value}.
     * <br> e.g. "and company = 'LK Test Solutions GmbH'"
     */
    private String ruleString;

    /**
     * See {@link ERuleFormat}
     */
    private ERuleFormat ruleFormat = ERuleFormat.STRING;

    /**
     * Constructor that is called when creating an instance of FilterRule with the full rule definition as String. This string will be parsed into the single rule elements like headerName, value,
     * filterOperator and ruleConcatenationOperator and the properties of these elements will automatically be set with the identified values.
     *
     * @param ruleStr complete rule as string
     */
    public FilterRule(String ruleStr) {
        this.ruleString = ruleStr;
        if (ruleStr.trim().toUpperCase().startsWith("AND")) {
            ruleConcatenationOperator = EOperator.AND;
        } else if (ruleStr.trim().toUpperCase().startsWith("OR")) {
            ruleConcatenationOperator = EOperator.OR;
        } else if (ruleStr.trim().toUpperCase().startsWith("IN")) {
            ruleConcatenationOperator = EOperator.IN;
        } else if (ruleStr.trim().toUpperCase().startsWith("BETWEEN")) {
            ruleConcatenationOperator = EOperator.BETWEEN;
        }
    }

    /**
     * Constructor that is called when creating an instance of FilterRule with the arguments for header name and value. In this case EOperator.EQUALS will be used as default operator for checking the
     * rule against a single value.
     *
     * @param hName String value with the Header name of the DataSet to which the rule applies.
     * @param value String value, used by the check operation of the FilterRule.
     */
    public FilterRule(String hName, String value) {
        this(hName, value, ERuleFormat.STRING);
    }

    /**
     * Constructor that is called when creating an instance of FilterRule with the arguments for header name, value and format. In this case EOperator.EQUALS will be used as default operator for
     * checking the rule against a single value. The format argument can be used to defines how the input value will be transformed when assigning the value to the instance of FilterRule and/or how to
     * interpret the value when checking the values validity.
     *
     * @param hName  String value with the Header name of the DataSet to which the rule applies.
     * @param value  String value, used by the check operation of the FilterRule.
     * @param format option to define how the string get compared
     */
    public FilterRule(String hName, String value, ERuleFormat format) {
        this(hName, new String[]{value}, format);
    }

    /**
     * Constructor that is called when creating an instance of FilterRule with the arguments for header name and a values Array. In this case EOperator.EQUALS will be used as default operator for
     * checking the rule against multiple values.
     *
     * @param hName  String value with the Header name of the DataSet to which the rule applies.
     * @param values Array of Strings, used by the check operation of the FilterRule.
     */
    public FilterRule(String hName, String[] values) {
        this(hName, values, ERuleFormat.STRING);
    }

    public FilterRule(String hName, String[] values, ERuleFormat format) {
        this.headerName = hName;
        if (values.length == 1) {
            this.value = values[0];
        }
        this.values = values;
        this.filterOperator = EOperator.EQUALS;
        this.ruleConcatenationOperator = EOperator.AND;
        this.ruleFormat = format;
        assignRuleString();
    }

    /**
     * Constructor that is called when creating an instance of FilterRule with the arguments for header name, value and operator. In this case any operator from the EOperator ENUM can be chosen for
     * checking the rule against a single value.
     *
     * @param hName String value with the Header name of the DataSet to which the rule applies.
     * @param value String value, used by the check operation of the FilterRule.
     * @param m     Value of type EOperator, used for the check operation.
     */
    public FilterRule(String hName, String value, EOperator m) {
        this(hName, value, m, ERuleFormat.STRING);
    }

    public FilterRule(String hName, String value, EOperator m, ERuleFormat ruleFormat) {
        this(hName, new String[]{value}, m, ruleFormat);
    }

    /**
     * Constructor that is called when creating an instance of FilterRule with the arguments for header name, values Array and operator. In this case any operator from the EOperator ENUM can be chosen
     * for checking the rule against multiple values.
     *
     * @param hName  String value with the Header name of the DataSet to which the rule applies.
     * @param inValues Array of Strings, used by the check operation of the FilterRule.
     * @param m      Value of type EOperator, used for the check operation.
     */
    public FilterRule(String hName, String[] inValues, EOperator m) {
        this(hName, inValues, m, ERuleFormat.STRING);
    }

    public FilterRule(String hName, String[] inValues, EOperator operator, ERuleFormat ruleFormat) {
        if (isValidOperator(inValues, operator)) {
            headerName = hName;
            if (inValues.length == 1) {
                this.value = inValues[0];
            }
            values = inValues;
            filterOperator = operator;
            ruleConcatenationOperator = EOperator.AND;
            assignRuleString(ruleFormat);
        } else {
            throw new IllegalArgumentException("Operator doesn't comply to values!");
        }
    }

    /**
     * Constructor that is called when creating an instance of FilterRule with the arguments for header name, value and operator. In this case any operator from the EOperator ENUM can be chosen for
     * checking the rule against a single value.
     *
     * @param hName  String value with the Header name of the DataSet to which the rule applies.
     * @param value  String value, used by the check operation of the FilterRule.
     * @param m      Value of type EOperator, used for the check operation.
     * @param concat Value of type EOperator, used for concatenating multiple filter conditions (e.g. EOperator.CONCATENATE_AND, EOperator.CONCATENATE_OR)
     */
    public FilterRule(String hName, String value, EOperator m, EOperator concat) {
        this(hName, value, m, concat, ERuleFormat.STRING);
    }

    public FilterRule(String hName, String value, EOperator m, EOperator concat, ERuleFormat ruleFormat) {
        this(hName, new String[]{value}, m, concat, ruleFormat);
    }

    /**
     * Constructor that is called when creating an instance of FilterRule with the arguments for header name, values Array and operator. In this case any operator from the EOperator ENUM can be chosen
     * for checking the rule against multiple values.
     *
     * @param hName  String value with the Header name of the DataSet to which the rule applies.
     * @param values Array of Strings, used by the check operation of the FilterRule.
     * @param m      Value of type EOperator, used for the check operation.
     * @param concat The operator that gets used to add the rule string to the one before.
     */
    public FilterRule(String hName, String[] values, EOperator m, EOperator concat) {
        this(hName, values, m, concat, ERuleFormat.STRING);
    }

    public FilterRule(String hName, String[] values, EOperator m, EOperator concat, ERuleFormat ruleFormat) {
        if (isValidOperator(values, m)) {
            this.headerName = hName;
            if (values.length == 1) {
                this.value = values[0];
            }
            this.values = values;
            this.filterOperator = m;
            this.ruleConcatenationOperator = concat;
            assignRuleString(ruleFormat);
        } else {
            throw new IllegalArgumentException("Operator doesn't comply to values!");
        }
    }

    public void assignRuleString(ERuleFormat format) {
        this.ruleFormat = format;
        assignRuleString();
    }

    public void assignRuleString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" ").append(headerName).append(" ");

        String quote = "";
        if(ruleFormat == ERuleFormat.QUOTED_STRING || ruleFormat == ERuleFormat.QUOTED_REGEX) {
            quote = "'";
        }
        if (filterOperator.equals(EOperator.IN)) {
            sb.append("(");
        }
        sb.append(quote);

        if (values.length > 1) {
            for (int i = 0; i < values.length; i++) {
                String val = values[i];
                if (i > 0) {
                    sb.append(",");
                }
//                if (val instanceof String) {
//                    sb.append("'" + val + "'");
//                } else {
                    sb.append(val);
//                }
            }
        } else {
            sb.append(values[0]);
        }
        sb.append(quote);
        if (filterOperator.equals(EOperator.IN)) {
            sb.append(")");
        }
        ruleString = sb.toString();
    }

    /**
     * Checks, if the defined rule matches to a given value.
     *
     * @param val Value to check
     * @return boolean value; true = rule matches to the defined value; false = rule doesn't match to the defined value.
     */
    public boolean checkValue(String val) {
        boolean returnCode = false;
        if (val != null) {
            for (String filterValue : values) {
                returnCode = isValidValue(val, filterValue);
                if (returnCode) {
                    break;
                }
            }
        }
        return returnCode;
    }

    @Override
    public boolean equals(Object o) {
        boolean ret = false;
        if (o != null) {
            if (o.getClass().equals(getClass())) {
                FilterRule rule = (FilterRule) o;
                if (getValue() != null) {
                    if (getHeaderName().equals(rule.getHeaderName()) && getValue().equals(rule.getValue()) && getFilterOperator() == rule.getFilterOperator()) {
                        ret = true;
                    }
                } else if (getValues() != null) {
                    if (getHeaderName().equals(rule.getHeaderName()) && getValues().equals(rule.getValues()) && getFilterOperator() == rule.getFilterOperator()) {
                        ret = true;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Checks if the operator is applicable to the value.
     *
     * @param value Value which the operator should be applied to.
     * @param m     Operator for validation
     * @return boolean value True if operator is applicable to value.
     */
    private boolean isValidOperator(String value, EOperator m) {
        boolean ret;
        if ((m == EOperator.GREATER_THAN) || m == EOperator.LESS_THAN) {
            try {
                Double.parseDouble(value);
                ret = true;
            } catch (NumberFormatException e) {
                ret = false;
            }
        } else {
            ret = true;
        }
        return ret;
    }

    /**
     * Checks if the operator is applicable to the values.
     *
     * @param values Value array which the operator should be applied to.
     * @param m      Operator for validation
     * @return boolean value True if operator is applicable to all values.
     */
    private boolean isValidOperator(String[] values, EOperator m) {
        boolean ret = true;
        for (String v : values) {
            if (!isValidOperator(v, m)) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    /**
     * Calls the comparison operation for the rule and returns true (rule matches) or false (rule doesn't match).
     *
     * @param val         Value of DataSet, where the rule will apply to
     * @param filterValue Value to compare with: defined in the filter rule
     * @return boolean value; true = rule matches to the defined value; false = rule doesn't match to the defined value.
     */
    public boolean isValidValue(String val, String filterValue) {
        if(val.contentEquals("null") || filterValue.contentEquals("null") ) {
            return false;
        }
        return switch (filterOperator) {
            case CONTAINS -> {
                if ((ruleFormat == ERuleFormat.QUOTED_REGEX) || (ruleFormat == ERuleFormat.REGEX)) {
                    yield isValidExpression(".*" + filterValue + ".*", val, false);
                } else {
                    yield val.trim().contains(filterValue);
                }
            }
            case CONTAINS_DATE -> {
                Optional<String> result = DateUtil.findDate(val);
                if(result.isPresent()) {
                    yield DateUtil.compare(result.get(), filterValue) == 0;
                }
                yield false;
            }
            case CONTAINS_DATE_AFTER -> {
                Optional<String> result = DateUtil.findDate(val);
                if(result.isPresent()) {
                    yield DateUtil.compare(result.get(), filterValue) > 0;
                }
                yield false;
            }
            case CONTAINS_DATE_BEFORE -> {
                Optional<String> result = DateUtil.findDate(val);
                if(result.isPresent()) {
                    yield DateUtil.compare(result.get(), filterValue) < 0;
                }
                yield false;
            }
            case CONTAINS_IGNORE_CASE -> {
                if ((ruleFormat.equals(ERuleFormat.QUOTED_REGEX)) || (ruleFormat.equals(ERuleFormat.REGEX))) {
                    yield isValidExpression(".*" + filterValue + ".*", val, true);
                } else {
                    yield val.trim().toUpperCase().contains(filterValue.toUpperCase());
                }
            }
            case DATE_AFTER -> DateUtil.compare(val, filterValue) > 0;
            case DATE_BEFORE -> DateUtil.compare(val, filterValue) < 0;
            case DATE_EQUALS -> DateUtil.compare(val, filterValue) == 0;
            case ENDS_WITH -> {
                if ((ruleFormat.equals(ERuleFormat.QUOTED_REGEX)) || (ruleFormat.equals(ERuleFormat.REGEX))) {
                    yield isValidExpression(".*" + filterValue, val, false);
                } else {
                    yield val.trim().endsWith(filterValue);
                }
            }
            case ENDS_WITH_IGNORE_CASE -> {
                if ((ruleFormat.equals(ERuleFormat.QUOTED_REGEX)) || (ruleFormat.equals(ERuleFormat.REGEX))) {
                    yield isValidExpression(".*" + filterValue, val, true);
                } else {
                    yield val.trim().toUpperCase().endsWith(filterValue.toUpperCase());
                }
            }
            case EQUALS -> {
                if ((ruleFormat.equals(ERuleFormat.QUOTED_REGEX)) || (ruleFormat.equals(ERuleFormat.REGEX))) {
                    yield isValidExpression(filterValue, val, false);
                } else {
                    yield val.trim().equals(filterValue);
                }
            }
            case EQUALS_IGNORE_CASE -> {
                if ((ruleFormat.equals(ERuleFormat.QUOTED_REGEX)) || (ruleFormat.equals(ERuleFormat.REGEX))) {
                    yield isValidExpression(filterValue, val, true);
                } else {
                    yield val.trim().equalsIgnoreCase(filterValue);
                }
            }
            case GREATER_THAN -> Integer.parseInt(val) > Integer.parseInt(filterValue);
            case GREATER_OR_EQUAL_THAN -> Integer.parseInt(val) >= Integer.parseInt(filterValue);
            case LESS_THAN -> Integer.parseInt(val) < Integer.parseInt(filterValue);
            case LESS_OR_EQUAL_THAN -> Integer.parseInt(val) <= Integer.parseInt(filterValue);
            case NOT_EQUALS -> {
                if ((ruleFormat.equals(ERuleFormat.QUOTED_REGEX)) || (ruleFormat.equals(ERuleFormat.REGEX))) {
                    yield !isValidExpression(filterValue, val, false);
                } else {
                    yield !val.trim().equals(filterValue);
                }
            }
            case NOT_EQUALS_IGNORE_CASE -> {
                if ((ruleFormat.equals(ERuleFormat.QUOTED_REGEX)) || (ruleFormat.equals(ERuleFormat.REGEX))) {
                    yield !isValidExpression(filterValue, val, true);
                } else {
                    yield !val.trim().equalsIgnoreCase(filterValue);
                }
            }
            case STARTS_WITH -> {
                if ((ruleFormat.equals(ERuleFormat.QUOTED_REGEX)) || (ruleFormat.equals(ERuleFormat.REGEX))) {
                    yield isValidExpression(filterValue + ".*", val, false);
                } else {
                    yield val.trim().startsWith(filterValue);
                }
            }
            case STARTS_WITH_IGNORE_CASE -> {
                if ((ruleFormat.equals(ERuleFormat.QUOTED_REGEX)) || (ruleFormat.equals(ERuleFormat.REGEX))) {
                    yield isValidExpression(filterValue + ".*", val, true);
                } else {
                    yield val.trim().toUpperCase().startsWith(filterValue.toUpperCase());
                }
            }
            case AND -> throw new IllegalArgumentException("AND not supported as comparator");
            case OR -> throw new IllegalArgumentException("OR not supported as comparator");
            case IN -> throw new IllegalArgumentException("IN not supported as comparator");
            case BETWEEN -> throw new IllegalArgumentException("BETWEEN not supported as comparator");
        };
    }

    private boolean isValidExpression(String filterValue, String val, boolean ignoreCase) {
        Pattern pat = null;
        if (ignoreCase) {
            pat = Pattern.compile(filterValue, Pattern.CASE_INSENSITIVE);
        } else {
            pat = Pattern.compile(filterValue);
        }
        Matcher match = pat.matcher(val);
        return match.matches();
    }

}