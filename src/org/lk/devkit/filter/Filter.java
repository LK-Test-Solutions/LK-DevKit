package org.lk.devkit.filter;

import org.lk.devkit.filter.FilterRule.ERuleFormat;

import java.util.*;

/**
 * This class gets used to define one or more conditions to select data from a data source. <br>
 * <br>
 * The following sample loads the content of <code>contacts.csv</code> into a
 * <code>DataContainer</code> and retrieves a filtered list with all surnames that belong to the
 * company LK Test Solutions:
 * 
 * <pre>
 * DataContainer container = DataContainer.newContainer("contacts.csv");
 * Filter filter = new Filter();
 * filter.addFilterRule("Company", "LK Test Solutions", EOperator.STARTS_WITH);
 * List{@literal <String>} result = container.getValues("Surname", filter);
 * </pre>
 *
 * @author LK Test Solutions
 */
public class Filter {
	/**
	 * Storage object for the filter rules that get used for comparison.
	 */
	private List<FilterRule> rules;
	/**
	 * This object can be used to only allow specific headers as filter criteria. They are not case-sensitive.
	 * So if this list has elements and any <code>addFilterRule</code> method gets called
	 * with an invalid header the rule will not be added to the {@link #rules} list.
	 */
	private List<String> plausibleHeaders;

	/**
	 * Create a new instance without arguments. The filter rules get added later.
	 */
	public Filter() {
		rules = new ArrayList<>();
		plausibleHeaders = new ArrayList<>();
	}

	/**
	 * Add a filter to the {@link #rules} by passing the whole condition as string.<br>
	 * e.g.: 
	 * <pre> 
	 * Filter fltr = new Filter();
	 * fltr.addFilterRule("AND Company = LK Test Solutions");
	 * </pre>
	 *
	 * @param rule The rule definition as String
	 */
	public void addFilterRule(String rule) {
		rules.add(new FilterRule(rule));
	}

	/**
	 * Adds an instance of type {@link FilterRule} to the {@link #rules}.
	 *
	 * @param rule instance of type {@link FilterRule}
	 */
	public void addFilterRule(FilterRule rule) {
		rules.add(rule);
	}

	/**
	 * Directly adds several {@link FilterRule} instances to the {@link #rules} list.
	 *
	 * @param filterRules A {@link List} object with items of type {@link FilterRule}
	 */
	public void addFilterRules(List<FilterRule> filterRules) {
		rules.addAll(filterRules);
	}

	/**
	 * Creates a new instance of type {@link FilterRule} and adds the instance to the {@link #rules} list.
	 *
	 * @param headerName	Name of the field or column header
	 * @param value			Filter criteria in the field or column
	 * @param mode			An instance of type {@link EOperator} e.g. <code>EOperator.CONTAINS</code>
	 */
	public void addFilterRule(String headerName, String value, EOperator mode) {
		addFilterRule(headerName, new String[] { value }, mode);
	}

	public void addFilterRule(String headerName, String[] values, EOperator mode) {
		addFilterRule(headerName, values, mode, ERuleFormat.STRING);
	}

	public void addFilterRule(String headerName, String value, EOperator mode, ERuleFormat ruleFormat) {
		addFilterRule(headerName, new String[] { value }, mode, ruleFormat);
	}

	/**
	 * Adds a rule definition into the rule property, that will check a condition against multiple
	 * values. The following sample filters all data set where the city equals to Munich, Vienna or
	 * Lisboa. If regular expression or quoted strings should be used the ruleFormat parameter can be
	 * set.
	 *
	 * <pre>
	 * Filter filter = new Filter();
	 * String[] cities = { "Munich", "Vienna", "Lisboa" };
	 * filter.addFilterRule("City", cities, EOperator.EQUALS);
	 * </pre>
	 *
	 * @param headerName Header or key to search for the defined values
	 * @param values     Array with multiple values that will be checked by the operation.
	 * @param mode       The operator that defines the filter operation for the value e.g.
	 *                   EOperator.EQUALS, EOperator.STARTS_WITH etc.
	 * @param ruleFormat See {@link ERuleFormat}
	 */
	public void addFilterRule(String headerName, String[] values, EOperator mode, ERuleFormat ruleFormat) {
		if (plausibleHeaders.isEmpty() || checkHeader(headerName)) {
			rules.add(new FilterRule(headerName, values, mode, ruleFormat));
		}
	}

	public void addFilterRule(String headerName, String value, EOperator mode, EOperator concat) {
		addFilterRule(headerName, new String[] { value }, mode, concat);
	}

	public void addFilterRule(String headerName, String[] values, EOperator mode, EOperator concat) {
		addFilterRule(headerName, values, mode, concat, ERuleFormat.STRING);
	}

	public void addFilterRule(String headerName, String value, EOperator mode, EOperator concat, ERuleFormat ruleFormat) {
		addFilterRule(headerName, new String[] { value }, mode, concat, ruleFormat);
	}

	public void addFilterRule(String headerName, String[] values, EOperator mode, EOperator concat, ERuleFormat ruleFormat) {
		if (plausibleHeaders.isEmpty() || checkHeader(headerName)) {
			rules.add(new FilterRule(headerName, values, mode, concat, ruleFormat));
		}
	}

	/**
	 * Sets a Set of plausible headers to the Filter. As soon as this method has been called, only
	 * headers equal to the ones in the plausibleHeaders List can be applied to the filter.
	 *
	 * @param headers Set of applicable headers (e.g. column names of a table) - NOT case-sensitive
	 */
	public void setPlausibleHeaders(Set<String> headers) {
		plausibleHeaders = new ArrayList<>(headers);
	}

	/**
	 * Adds a Set of plausible headers to the filter. As soon as this method has been called, only
	 * headers equal to the ones in the plausibleHeaders List can be applied to the filter.
	 *
	 * @param headers Set of applicable headers (e.g. column names of a table) - NOT case-sensitive
	 */
	public void addPlausibleHeaders(Set<String> headers) {
		plausibleHeaders.addAll(headers);
	}

	/**
	 * Clears all plausible headers from the filter. After the call of this method, any header can be
	 * applied again to the filter.
	 */
	public void clearPlausibleHeaders() {
		plausibleHeaders = new ArrayList<>();
	}

	/**
	 * Checks if a defined headerName matches to a value defined in the plausibleHeaders list of current
	 * the Filter instance.
	 *
	 * @param headerName The header name to search for in the plausibleHeaders list
	 * @return true = <code>headerName</code> exists in <code>plausibleHeaders</code>; false =
	 *         <code>headerName</code> doesn't exist in <code>plausibleHeaders</code>
	 */
	private boolean checkHeader(String headerName) {
		boolean ret = false;
		headerName = headerName.trim().toLowerCase();
		if (!plausibleHeaders.isEmpty()) {
			for (String h : plausibleHeaders) {
				if (headerName.equals(h.trim().toLowerCase())) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}

	/**
	 * Returns the List property <code>rules</code> with elements of type {@link FilterRule}
	 *
	 * @return The property <code>rules</code> of type List.
	 */
	public List<FilterRule> getFilterRules() {
		return rules;
	}

	public List<String> getFilterRulesHeaders() {
		List<String> headersList = new ArrayList<String>();
		for (FilterRule rule : rules) {
			headersList.add(rule.getHeaderName());
		}
		List<String> outHeaders = new ArrayList<String>();
		outHeaders.addAll(new HashSet<>(headersList));
		return outHeaders;
	}

	public List<String> getFilterRulesValues(String headerName) {
		List<String> valuesList = new ArrayList<>();
		for (FilterRule rule : rules) {
			if (rule.getFilterOperator() == EOperator.EQUALS || rule.getFilterOperator() == EOperator.EQUALS_IGNORE_CASE) {
				if (rule.getHeaderName().equalsIgnoreCase(headerName)) {
					valuesList.addAll(Arrays.asList(rule.getValues()));
				}
			}
		}
		return valuesList;
	}

	/**
	 * Removes all entries from the {@link #rules}.
	 */
	public void clear() {
		rules = new ArrayList<>();
	}

	/**
	 * Deletes a rule from the filter instance. The rule has to have the same headerName, value and mode
	 * as the rule that has to be deleted in the filter.
	 *
	 * @param headerName Header of the column or row within a tabular format.
	 * @param value      The value.
	 * @param mode       The operator, used to the values e.g. EOperator.EQUALS,
	 *                   EOperator.STARTS_WITH etc.
	 * @return True, if the rule has been deleted, else false.
	 */
	public boolean deleteRule(String headerName, String value, EOperator mode) {
		return deleteRule(new FilterRule(headerName, value, mode));
	}

	/**
	 * Deletes a rule from the filter instance. The rule has to have the same headerName, values and
	 * mode as the rule that has to be deleted in the filter.
	 *
	 * @param headerName Header of the column or row within a tabular format.
	 * @param values     The values array.
	 * @param mode       The operator, used to the values e.g. EOperator.EQUALS,
	 *                   EOperator.STARTS_WITH etc.
	 * @return True, if the rule has been deleted, else false.
	 */
	public boolean deleteRule(String headerName, String[] values, EOperator mode) {
		return deleteRule(new FilterRule(headerName, values, mode));
	}

	/**
	 * Deletes a rule from the filter instance. For example the rule can be created with new
	 * FilterRule(), but has to have the same headerName, value and mode as the rule that has to be
	 * deleted in the filter.
	 *
	 * @param rule An equal rule to the one that has to be deleted.
	 * @return True, if the rule has been deleted, else false.
	 */
	public boolean deleteRule(FilterRule rule) {
		boolean ret = false;
		int ruleIndex = 0;
		for (FilterRule r : rules) {
			if (r.getHeaderName().equals(rule.getHeaderName())) {
				rules.remove(ruleIndex);
				ret = true;
				break;
			}
			ruleIndex++;
		}
		return ret;
	}

	/**
	 * Deletes a rule from the filter instance, specified through an index.
	 *
	 * @param index Index of the rule in the rules property of the filter instance.
	 * @return True, if the rule has been deleted, false otherwise.
	 */
	public boolean deleteRule(int index) {
		return rules.remove(index) != null;
	}
}
