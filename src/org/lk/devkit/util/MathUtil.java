package org.lk.devkit.util;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class with useful mathematical functions. They can be called in a static way, similar to the Java
 * Math library.
 * 
 * @author FME (LK Test Solutions)
 */
public class MathUtil {

	/**
	 * Get the arithmetic mean of a list of numeric values.
	 * 
	 * @param values a list of type Number (byte, short, Integer, double, double, long)
	 * @return the mean value of the committed values in double precision or NaN (Not a number) if the
	 *         list is empty
	 */
	public static double getArithmeticMean(final List<? extends Number> values) {
		return MathUtil.getArithmeticMean(values, false);
	}

	/**
	 * Get the arithmetic mean of a list of numeric values.
	 * 
	 * @param values        a list of type Number (byte, short, Integer, double, double, long)
	 * @param skipNegatives If true, the negative values get ignored.
	 * @return the mean value of the committed values in double precision or NaN (Not a number) if the
	 *         list is empty
	 */
	public static double getArithmeticMean(final List<? extends Number> values, boolean skipNegatives) {
		if (values.isEmpty()) {
			return Double.NaN;
		}
		double mean = 0;
		for (Number n : values) {
			double temp = n.doubleValue();
			if (Double.isFinite(temp) && !Double.isNaN(temp)) {
				if (skipNegatives && temp < 0) {
					continue;
				} else {
					mean += temp;
				}
			}
		}
		mean = mean / values.size();
		return mean;
	}

	/**
	 * Get the minimum of a list of numbers as double value.
	 * 
	 * @param values a list of type Number (byte, short, Integer, double, double, long)
	 * @param mode   1: Get the smallest list value, 2: Get the list value that is the closest to 0.
	 * @return the minimum of the committed values in double precision or NaN if the list is empty
	 */
	public static double getMinimum(final List<? extends Number> values, final int mode) {
		if (values.isEmpty()) {
			return Double.NaN;
		}

		double min = 0f;
		for (int i = 0; i < values.size(); i++) {
			double val = values.get(i).doubleValue();
			if (Double.isFinite(val) && !Double.isNaN(val)) {
				if (mode == 1) {
					if (i == 0 || (val < min)) {
						min = val;
					}
				} else if (mode == 2) {
					if (i == 0 || (Math.abs(val) < min)) {
						min = val;
					}
				} else {
					return Double.NaN;
				}
			}
		}
		return min;
	}

	/**
	 * Get the maximum of a list of numbers as double value.
	 * 
	 * @param values a list of type Number (byte, short, Integer, double, double, long)
	 * @return the maximum of the committed values in double precision or NaN the list is empty.
	 */
	public static double getMaximum(final List<? extends Number> values) {
		if (values.isEmpty()) {
			return Double.NaN;
		}

		double max = 0f;
		for (int i = 0; i < values.size(); i++) {
			double val = values.get(i).doubleValue();
			if (Double.isFinite(val) && !Double.isNaN(val)) {
				if (i == 0 || val > max) {
					max = val;
				}
			}
		}
		return max;
	}

	/**
	 * Returns an estimate of the percentile of the values in the rawValues List.
	 * 
	 * @param rawValues list of type Double with the values sequence
	 * @param p         the percentile value to compute, a value between 0 and 100
	 * @return Double value with the calculated percentile of the rawValues
	 */
	public static double getPercentile(List<Double> rawValues, double p) {
		double[] arr = rawValues.stream().mapToDouble(Double::doubleValue).toArray();
		return StatUtils.percentile(arr, p);
	}

	/**
	 * Get the value of the linear correlation between two variables. If the lists are null or empty,
	 * NaN will be returned. If the lists do not have the same length, the size of the iteration will be
	 * set to the smaller one.
	 * 
	 * @param xValues the first data set
	 * @param yValues the second data set
	 * @return a (negative) positive value if there is a (negative) positive linear correlation between
	 *         <code>xValues</code> and <code>yValues</code>
	 */
	public static double getCovariance(final List<? extends Number> xValues, final List<? extends Number> yValues) {
		if (xValues.isEmpty() || yValues.isEmpty()) {
			return Double.NaN;
		}

		double size = 0;
		if (xValues.size() == yValues.size()) {
			size = xValues.size();
		} else {
			int max = Integer.max(xValues.size(), yValues.size());
			int diff = Math.abs(xValues.size() - yValues.size());
			size = max - diff;
		}

		double retVal = 0, xMean = getArithmeticMean(xValues), yMean = getArithmeticMean(yValues);
		for (int i = 0; i < size; i++) {
			double tempX = xValues.get(i).doubleValue(), tempY = yValues.get(i).doubleValue();

			retVal += (tempX - xMean) * (tempY - yMean);
		}
		return retVal / size;
	}

	/**
	 * Computes the bias corrected sample standard deviation. The standard deviation is the positive
	 * square root of the variance. Bias corrected means that the underestimation or overestimation gets
	 * eliminated by using a correction formula after calculating the deviation.
	 * 
	 * @param rawValues list of type double with the values sequence
	 * @return double value with the calculated standard deviation of the rawValues
	 */
	public static double getStandardDeviation(List<Double> rawValues) {
		return MathUtil.getStandardDeviation(rawValues, true);
	}

	/**
	 * Computes the bias corrected sample standard deviation or the non bias corrected population
	 * standard deviation. The standard deviation is the positive square root of the variance. Bias
	 * corrected means that the underestimation or overestimation gets eliminated by using a correction
	 * formula after calculating the deviation.
	 * 
	 * @param rawValues     list of type Double with the values sequence
	 * @param biasCorrected true: Bias corrected. False: Non bias corrected.
	 * @return double value with the calculated standard deviation of the rawValues
	 */
	public static double getStandardDeviation(List<Double> rawValues, boolean biasCorrected) {
		StandardDeviation sd = new StandardDeviation();
		sd.setBiasCorrected(biasCorrected);
		return sd.evaluate(rawValues.stream().mapToDouble(d -> d).toArray());
	}

	/**
	 * Calculate a linear correlation coefficient (normed COVARIANCE) for two variables x and y by using
	 * <code>getCovariance</code> and <code>getDeviation</code>. If one of the used methods returns NaN,
	 * this method return NaN as well.
	 * 
	 * @param xValues the first set of data
	 * @param yValues the second set of data
	 * @return the measure of dispersion in the range of 0 to |1|, where 1 is a perfect positive
	 *         correlation, -1 a perfect negative correlation and 0 no linear correlation
	 */
	public static double getLinearCorrelationCoefficient(final List<Double> xValues, final List<Double> yValues) {
		return getCovariance(xValues, yValues) / (getStandardDeviation(xValues) * getStandardDeviation(yValues));
	}

	/**
	 * Get the significance (standard errors) of a calculated correlation between two variables. E.g. if
	 * the result would be 0.01, the significance has the value 0.99 or 99 %.
	 * 
	 * @param corr The linear correlation coefficient between -1 and 1
	 * @param size The size of the variable lists
	 * @return The significance in percent
	 */
	public static double getCorrelationSignificance(final double corr, final double size) {
		if (Math.abs(corr) > 1 || size < 2) {
			return Double.NaN;
		}
		return Math.sqrt((1 - corr * corr) / (size - 2));
	}

	/**
	 * Norm the values of a list of doubles to a range between 0 and 1. Useful to create a probability
	 * density function.
	 * 
	 * @param values a list of type double
	 */
	public static void norm(final List<Double> values) {
		double max = getMaximum(values);
		double min = getMinimum(values, 1);

		values.replaceAll(aDouble -> (aDouble - min) / (max - min));
	}

	/**
	 * The winsorization: Get the extreme values of a data set and set them to the next less extreme
	 * value depending on the wished percentile.
	 * 
	 * @param values        the data
	 * @param percentil     defines the range of the extreme values from 0 to 100 percent. For example:
	 *                      If percentile = 90, the first 5 percent and the last 5 percent of the data
	 *                      set will be marked as extreme.
	 * @param skipNegatives if true the negative values will be removed. Can be useful when there is
	 *                      data (like transaction response times) that cannot be negative.
	 * @param extremVal     the extreme values gets collected in the committed list to make them
	 *                      available for later purpose too.
	 * @param border        the highest value of the detected values that separates them from the
	 *                      extreme values.
	 * @return the list with the modified values
	 */
	public static final List<Double> getWinsorizedValue(final List<? extends Number> values, final byte percentil, final boolean skipNegatives, List<Double> extremVal, double border) {
		if (percentil < 0 || percentil > 100 || values.isEmpty()) {
			throw new IndexOutOfBoundsException();
		} else {
			double lowPercentile = 0.0F, highPercentile = 0.0F;
			List<Double> retVal = new ArrayList<>();
			for (Number n : values) {
				retVal.add(n.doubleValue());
			}

			Collections.sort(retVal, null);
			lowPercentile = ((100 - percentil) / 2) * 0.01F;
			highPercentile = 1 - lowPercentile;

			if (skipNegatives) {
				for (double temp : retVal) {
					if (temp < 0f)
						retVal.remove(temp);
				}
			}
			for (int i = 0; i < retVal.size(); i++) {
				if (i < lowPercentile * retVal.size()) {
					extremVal.add(retVal.get(i));
					retVal.set(i, retVal.get(i + 1));
				} else if (i > highPercentile * (retVal.size() - 1)) {
					extremVal.add(retVal.get(i));
					retVal.set(i, retVal.get(i - 1));
					border = retVal.get(i - 1);
				}
			}
			return retVal;
		}
	}

	/**
	 * Calculate a list of ranks out of a given collection of numbers. E.g. [2.5, 2.3, 2.7, 2.4] would
	 * return [3, 1, 4, 2]. This can be used to find the existence of a nonlinear correlation between
	 * variables.
	 * 
	 * @param values the input that will not be changed in this method
	 * @return the list of ranks or an empty list if the list is empty
	 */
	public static List<Double> getRankList(final List<? extends Number> values) {
		List<Double> sorted = new ArrayList<>(), ranks = new ArrayList<>();
		for (Number temp : values) {
			sorted.add(temp.doubleValue());
		}
		Collections.sort(sorted, null);

		for (Number temp : values) {
			for (int j = 0; j < sorted.size(); j++) {
				if (Double.compare(temp.doubleValue(), sorted.get(j)) == 0) {
					ranks.add((double) j);
					break;
				}
			}
		}
		return ranks;
	}

	/**
	 * Get the axis between the abscissa and the correlation line. The calculation does only make sense,
	 * if there is a linear correlation between two variables. If not, the result will be very small and
	 * has no significance.
	 * 
	 * @param xDev the deviation of the first variable
	 * @param yDev the deviation of the second variable
	 * @param corr the linear correlation coefficient of the variables
	 * @param mode true: degree, false: radiant
	 * @return the axis depending on the mode
	 */
	public static double getAngleFromXAxisToCorrelationLine(final double xDev, final double yDev, final double corr, final boolean mode) {
		double factor = 1;
		if (mode) {
			factor = 180 / Math.PI;
		}

		return factor * Math.atan((yDev / xDev) * corr);
	}

	/**
	 * Get the curvature of a mathematical function at a specified point.
	 * 
	 * @param firstDev the first derivation at this point
	 * @param secDev   the second derivation at this point
	 * @return the curvature
	 */
	public static double getCurvePoint(final double firstDev, final double secDev) {
		if (firstDev == 0) {
			return Double.NaN;
		}
		return secDev / Math.pow(1 + (firstDev * firstDev), 3 / 2);
	}

	/**
	 * Turn a list of numbers to an array of doubles.
	 * 
	 * @param values the input
	 * @return the output as double array
	 */
	public static double[] listToArray(final List<? extends Number> values) {
		double[] retVal = new double[values.size()];

		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = values.get(i).doubleValue();
		}
		return retVal;
	}

	/**
	 * Calculate the logarithm of every value in a list of numbers.
	 * 
	 * @param values the input list
	 * @return the list with logarithm values
	 */
	public static List<Double> logListValues(final List<? extends Number> values) {
		List<Double> retVal = new ArrayList<>();
		for (Number n : values) {
			if (n.doubleValue() >= 0) {
				retVal.add(Math.log(n.doubleValue()));
			}
		}
		return retVal;
	}

	/**
	 * Calculate the square root of every value in a list of numbers.
	 * 
	 * @param values the input list
	 * @return the list with logarithm values
	 */
	public static List<Double> rootListValues(final List<? extends Number> values) {
		List<Double> retVal = new ArrayList<>();
		for (Number n : values) {
			if (n.doubleValue() >= 0) {
				retVal.add(Math.sqrt(n.doubleValue()));
			}
		}
		return retVal;
	}

	/**
	 * Calculate the inverse of every value in a list of numbers.
	 * 
	 * @param values the input list
	 * @return the list with inverse values
	 */
	public static List<Double> inversListValues(final List<? extends Number> values) {
		List<Double> retVal = new ArrayList<>();
		for (Number n : values) {
			retVal.add(1 / n.doubleValue());
		}
		return retVal;
	}

	public static List<Double> logisticListValues(final List<? extends Number> values, final double sat) {
		List<Double> retVal = new ArrayList<>();
		for (Number n : values) {
			double temp = Math.log(sat / n.doubleValue() - 1);
			if (!Double.isNaN(temp)) {
				retVal.add(temp);
			}
		}
		return retVal;
	}

	/**
	 * Get the greater double value.
	 * 
	 * @param f1 first double value
	 * @param f2 second double value
	 * @return the greater double value
	 */
	public static double greaterDoubleValue(final double f1, final double f2) {
		if (f1 > f2) {
			return f1;
		}
		return f2;
	}
}