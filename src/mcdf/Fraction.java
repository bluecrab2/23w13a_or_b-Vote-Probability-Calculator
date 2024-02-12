package mcdf;

import java.math.BigInteger;

/**
 * Fraction object with numerator and denominator. No limit in precision, overflow will not occur.
 */
public class Fraction {
	/**
	 * The numerator of the number. It is an integer that can be positive or
	 * negative.
	 */
	private BigInteger numerator;

	/**
	 * The denominator of the number. It is a whole number that will always be
	 * positive.
	 */
	private BigInteger denominator;
	
	/**
	 * Static number equal to zero
	 */
	public final static Fraction ZERO = new Fraction(new BigInteger("0"), new BigInteger("1"));
	
	/**
	 * Static number equal to one
	 */
	public final static Fraction ONE = new Fraction(new BigInteger("1"), new BigInteger("1"));

	/**
	 * Constructor of Fraction using two BigIntegers for the numerator and
	 * denominator. Constructor will check if simplification is possible.
	 * 
	 * @param n Numerator of the number.
	 * @param d Denominator of the number.
	 * @throws IllegalArgumentException if denominator is equal to one.
	 */
	public Fraction(BigInteger n, BigInteger d) {
		if (d.compareTo(BigInteger.ZERO) == 0) {
			throw new IllegalArgumentException("Denominator of fraction equal to one.");
		}

		numerator = n;
		denominator = d;
		
		simplifyFraction();
	}

	/**
	 * Returns the numerator
	 * @return the numerator
	 */
	public BigInteger getNumerator() {
		return numerator;
	}

	/**
	 * Returns the denominator
	 * @return the denominator
	 */
	public BigInteger getDenominator() {
		return denominator;
	}
	
	/**
	 * Simplifies the fraction so that numerator and denominator have no
	 * common factor except 1
	 */
	private void simplifyFraction() {
		BigInteger gcf = euclideanAlgorithm(numerator, denominator);
		numerator = numerator.divide(gcf);
		denominator = denominator.divide(gcf);
	}
	
	/**
	 * Will find the GCF of two BigIntegers using the euclidean algorithm
	 * @param a the first number
	 * @param b the second number
	 * @return the GCF of the two provided numbers
	 */
	private static BigInteger euclideanAlgorithm(BigInteger a, BigInteger b) {
		if (a.equals(new BigInteger("0")))
			return b;
		if (b.equals(new BigInteger("0")))
			return a;

		BigInteger big;
		BigInteger small;
		BigInteger difference;
		if (greaterThanWholeNumber(a, b)) {
			big = a;
			small = b;
		} else {
			big = b;
			small = a;
		}
		while (!big.equals(small)) {
			difference = big.subtract(small.multiply(big.divide(small))).abs();
			if (difference.equals(new BigInteger("0"))) {
				return small;
			}
			if (greaterThanWholeNumber(difference, small)) {
				big = difference;
			} else {
				big = small;
				small = difference;
			}
		}
		return big;
	}
	
	/**
	 * Returns true if the first BigInteger is greater than or equal to
	 * the second BigInteger
	 * @param a left hand number to be compared
	 * @param b right hand number to be compared
	 * @return true if a >= b
	 */
	private static boolean greaterThanWholeNumber(BigInteger a, BigInteger b) // a>b?
	{
		return a.compareTo(b) > 0;
	}
	
	/**
	 * Adds this fraction with the other provided number and returns their
	 * sum
	 * @param b the other number that will be added
	 * @return the sum of both numbers
	 */
	public Fraction add(Fraction b) {
		Fraction a = this;
		BigInteger sumNumerator; // integer
		BigInteger sumDenominator; // whole number
		BigInteger aNumerator = a.getNumerator();
		BigInteger bNumerator = b.getNumerator();
		BigInteger aDenominator = a.getDenominator();
		BigInteger bDenominator = b.getDenominator();
		sumDenominator = aDenominator.multiply(bDenominator);
		sumNumerator = aNumerator.multiply(bDenominator).add(bNumerator.multiply(aDenominator));
		if(sumDenominator.compareTo(BigInteger.ZERO) < 0) {
			sumDenominator = sumDenominator.negate();
			sumNumerator = sumNumerator.negate();
		}
		Fraction result = new Fraction(sumNumerator, sumDenominator);
		return result;
	}
	
	/**
	 * Subtracts the provided fraction from this fraction and returns their
	 * difference
	 * @param b the fraction that will subtract
	 * @return the difference between this and the provided fraction
	 */
	public Fraction subtract(Fraction b) {
		return this.add(b.negate());
	}
	
	/**
	 * Multiplies this fraction by negative one to make positive fractions
	 * turn negative and negative fractions turn positive
	 * @return the number with an opposite sign
	 */
	public Fraction negate() {
		return new Fraction(numerator.multiply(new BigInteger("-1")), denominator);
	}
	
	/**
	 * Multiplies this fraction with the other fraction and returns
	 * their product
	 * @param b the other factor
	 * @return the product of both numbers
	 */
	public Fraction multiply(Fraction b) {
		return new Fraction(this.getNumerator().multiply(b.getNumerator()),
				this.getDenominator().multiply(b.getDenominator()));
	}
	
	/**
	 * Returns this number as a string. It will be in the form
	 * numerator/denominator unless the denominator is one in
	 * which case the divide symbol and denominator will not display
	 * @return the number in string format
	 */
	@Override
	public String toString() {
		String resultText = "";
		resultText += numerator;
		if (!denominator.equals(BigInteger.ONE)) // does not display a denominator of 1
			resultText += "/" + denominator;
		return resultText;
	}
}
