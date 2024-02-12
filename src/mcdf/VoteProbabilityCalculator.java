package mcdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Tool to calculate the probability of a vote type appearing in the next vote
 * in the Minecraft snapshot 23w13a_or_b. The program outputs the exact value
 * of this probability as a fraction. The probability depends upon the 
 * new_vote_repeal_vote_chance, new_vote_extra_effect_chance, and
 * new_vote_extra_effect_max_count values in the world. The chance a vote appears
 * depends on the vote's weight (stored in 23w13a_or_b_vote_weights.csv). However,
 * the probability becomes complex because no duplicates are allowed in combined votes.
 * Therefore, the weighted list is different depending on each first choice of vote.
 * The main method can be called with no parameters to get user prompts on the command
 * terminal or with the inputs already provided as arguments.
 */
public class VoteProbabilityCalculator {
	/** CSV File containing the votes and their weights in the format:
	 * vote_id,100
	 */
	private static final String VOTE_WEIGHT_CSV_PATH = "23w13a_or_b_vote_weights.csv";

	/** Map of the vote ID as string keys and weights as values */
	public static HashMap<String, BigInteger> voteWeights = new HashMap<String, BigInteger>();
	
	/** Probability that an extra vote will be added at each step */
	public static Fraction newVoteExtraEffectChance; 
	
	/**
	 * A hash map of cached previous results. The keys are the multiset of removed weights 
	 * and the value is the resulting probability fraction that came from those removals. The
	 * multiset is a map where the key represents the weight removed and the value is the number
	 * of times that weight was removed from the list.
	 */
	public static HashMap<HashMap<BigInteger, Integer>, Fraction> cachedResults = 
			new HashMap<HashMap<BigInteger, Integer>, Fraction>();
	
	/**
	 * 
	 * @param args if empty, the user will be prompted to input the four required values. Otherwise,
	 * args must be <vote_id> <new_vote_repeal_vote_chance> <new_vote_extra_effect_chance> <new_vote_extra_effect_max_count>.
	 * Outputs the exact result fraction to standard out.
	 * @throws FileNotFoundException if 23w13a_or_b_vote_weights.csv file is missing
	 * @throws IllegalArgumentException if the args length is not 0 or 4
	 * @throws IllegalArgumentException if the inputted vote ID does not exist in the file
	 */
	public static void main(String[] args) throws FileNotFoundException {
		loadCSV();
		
		//Input parameters
		String chosenVote;
		BigInteger repealPercentage;
		BigInteger newVoteExtraEffectPercentage;
		int newVoteExtraEffectMaxCount;
		
		if(args.length == 0) {
			//No args provided, prompt user for inputs on terminal
			Scanner userIn = new Scanner(System.in);
			
			System.out.println("What is the ID of the vote you want the probability for?");
			chosenVote = userIn.nextLine();
			
			System.out.println("What is the current repeal percentage?");
			repealPercentage = new BigInteger(userIn.nextLine());
			System.out.println("What is the current new vote extra effect percentage?");
			newVoteExtraEffectPercentage = new BigInteger(userIn.nextLine());
			System.out.println("What is the current new vote extra effect max count?");
			newVoteExtraEffectMaxCount = Integer.parseInt(userIn.nextLine());
			
			userIn.close();
		} else {
			if(args.length != 4) {
				//Throw exception if there is an invalid number of arguments
				System.out.println("Invalid number of arguments. Argument length must be 0 or 4.");
				System.out.println("Leave args length as 0 for user prompts");
				System.out.println("The arguments should be: <vote_id> <new_vote_repeal_vote_chance> <new_vote_extra_effect_chance> <new_vote_extra_effect_max_count>");
				throw new IllegalArgumentException("Invalid number of arguments. Argument length must be 0 or 4.");
			}
			
			//Take inputs from arguments
			chosenVote = args[0];
			repealPercentage = new BigInteger(args[1]);
			newVoteExtraEffectPercentage = new BigInteger(args[2]);
			newVoteExtraEffectMaxCount = Integer.parseInt(args[3]);
		}
		
		String result = calculateProbability(chosenVote, repealPercentage, newVoteExtraEffectPercentage, newVoteExtraEffectMaxCount);
		
		//Output the final exact probability to the console
		System.out.println("Exact probability:");
		System.out.println(result.toString());
	}

	/**
	 * Loads the vote weights from the CSV file and puts them in the voteWeights map
	 * @throws FileNotFoundException if the CSV file is missing
	 */
	public static void loadCSV() throws FileNotFoundException {
		//Read in votes and their weights from CSV file
		File f = new File(VOTE_WEIGHT_CSV_PATH);
		Scanner fileScan = new Scanner(f);
		while(fileScan.hasNext()) {
			String line = fileScan.next();
			String[] params = line.split(",");
			voteWeights.put(params[0], new BigInteger(params[1]));
		}
		fileScan.close();
	}

	/**
	 * Calculates the probability of a vote type appearing in the next vote considering combined and repeal votes.
	 * @param chosenVote ID String of the vote to check the probability of
	 * @param repealPercentage Current new_vote_repeal_vote_chance value percentage integer
	 * @param newVoteExtraEffectPercentage Current new_vote_extra_effect_chance value percentage integer
	 * @param newVoteExtraEffectMaxCount  Current new_vote_extra_effect_max_count value
	 * @return String representing the exact fraction of the probability that chosenVote will appear in the next
	 * vote.
	 */
	public static String calculateProbability(String chosenVote, BigInteger repealPercentage,
			BigInteger newVoteExtraEffectPercentage, int newVoteExtraEffectMaxCount) {
		newVoteExtraEffectChance = new Fraction(newVoteExtraEffectPercentage, new BigInteger("100"));

		BigInteger chosenWeight = voteWeights.get(chosenVote);
		if(chosenWeight == null)
			throw new IllegalArgumentException("Vote ID " + chosenVote + " does not exist.");
		
		//Create list of other votes weights
		ArrayList<BigInteger> weights = new ArrayList<BigInteger>();
		for(BigInteger weight : voteWeights.values()) {
			weights.add(weight);
		}
		weights.remove(chosenWeight);
		
		//Initialize removedWeights to be empty for first recursive call
		HashMap<BigInteger, Integer> removedWeights = new HashMap<BigInteger, Integer>();
		
		//Recursive call to find the probability from combined votes given input values
		Fraction result = VoteProbabilityCalculator.probabilityGivenMultipleRounds(chosenWeight, weights, removedWeights, newVoteExtraEffectMaxCount + 1);
		
		//Multiply result by the probability it is not a repeal vote
		Fraction notRepealProbability = Fraction.ONE.subtract(new Fraction(repealPercentage, new BigInteger("100")));
		result = result.multiply(notRepealProbability);
		
		return result.toString();
	}
	
	/**
	 * Recursive method to find the probability of a vote appearing anywhere in a vote
	 * including combined votes. Combined votes use the normal weight list except there
	 * are no repeat vote types allowed. The recursive aspect involves choosing each of
	 * the votes and removing it from the list then checking the new probability of the
	 * vote appearing in the list with the chosen vote removed. The method completes this
	 * to a depth of rounds - 1. The results are cached so if the method finds that it
	 * is searching for a probability on a list with the same removed values from an earlier
	 * calculated value then it will use that precalculated value.
	 * @param eventWeight the weight of the event of interest. The weight from the vote
	 * that the user wants to find the probability of.
	 * @param otherWeights list of all the other current vote weights possible in the vote
	 * weighted list. Does not include the event of interest.
	 * @param removedWeights multiset of weights that have been removed from the weighted
	 * list in this possibility branch. Used to check the cached results if this possibility
	 * has previously been calculated. Actually a map where the big integer key represents 
	 * the weight and the integer value represents the number of times that the weight has
	 * been removed from the list. 
	 * @param rounds depth to continue checking. Starts as the total possible length of a
	 * combined vote.
	 * @return a fraction representing the exact probability that the eventWeight will appear
	 * in a vote including in any combined vote.
	 */
	public static Fraction probabilityGivenMultipleRounds(BigInteger eventWeight, 
			ArrayList<BigInteger> otherWeights, HashMap<BigInteger, Integer> removedWeights, int rounds) {
		//Check for cached result
		Fraction cacheResult = cachedResults.get(removedWeights);
		if(cacheResult != null)
			return cacheResult;
		
		//Calculate the total weight
		BigInteger totalWeight = sum(otherWeights).add(eventWeight);
		
		//Start the probability as the chance that the vote is chosen this round
		Fraction probability = new Fraction(eventWeight, totalWeight);
		
		//If there is still a chance for a combined vote, add the probabilities that
		//the desired vote is chosen given the choice of every other vote.
		if(rounds > 1) {
			for(int i = 0; i < otherWeights.size(); i++) {
				//Remove the ith element of otherWeights to run the probabilities if it was chosen
				ArrayList<BigInteger> updatedWeightList = removeIdxAndCreateNewArrayList(i, otherWeights);
				
				//Recursive call to check the probability the event will be chosen given a vote was removed
				Fraction branchProbability = probabilityGivenMultipleRounds(eventWeight, updatedWeightList, 
						addToMultiset(removedWeights, otherWeights.get(i)), rounds - 1);
				
				//Add the probability of the event being included given the ith vote is removed times the probability that
				//the ith probability was chosen and removed
				Fraction addendProbability = branchProbability
						.multiply(new Fraction(otherWeights.get(i), totalWeight))
						.multiply(newVoteExtraEffectChance);
				
				//Sum probabilities on all branches
				probability = probability.add(addendProbability);
			}
		}
		
		//Save result in cache
		cachedResults.put(removedWeights, probability);
		return probability;
	}

	/**
	 * Sums all the BigIntegers in an ArrayList
	 * @param nums list of BigInteger addends
	 * @return the sum of all elements of the nums ArrayList
	 */
	private static BigInteger sum(ArrayList<BigInteger> nums) {
		BigInteger sum = BigInteger.ZERO;
		for(int i = 0; i < nums.size(); i++)
			sum = sum.add(nums.get(i));
		return sum;
	}
	
	/**
	 * Creates a shallow clone of the ArrayList without the element at index idx
	 * @param idx element index to be removed
	 * @param nums original ArrayList
	 * @return ArrayList without index idx
	 */
	private static ArrayList<BigInteger> removeIdxAndCreateNewArrayList(int idx, ArrayList<BigInteger> nums) {
		ArrayList<BigInteger> retNums = new ArrayList<BigInteger>();
		for(int i = 0; i < nums.size(); i++) {
			if(i != idx)
				retNums.add(nums.get(i));
		}
		return retNums;
	}

	/**
	 * Adds the desired bigInteger element to a HashMap that behaves as a multiset
	 * (a set with duplicates allowed). The integer value represents the number of
	 * the element contained in the multiset. The method will create a new entry
	 * in the multiset with 1 as a count if the bigInteger does not already exist
	 * in the multiset. If the element already exists in the multiset, the count
	 * will be increased by one.
	 * @param multiset HashMap representing a multiset. The BigInteger keys
	 * are the members of the set while the Integer values are the count of
	 * the number of those members in the multiset.
	 * @param bigInteger member that should be added to the multiset
	 * @return multiset with the bigInteger member added
	 */
	private static HashMap<BigInteger, Integer> addToMultiset(HashMap<BigInteger, Integer> multiset,
			BigInteger bigInteger) {
		HashMap<BigInteger, Integer> retMultiset = new HashMap<BigInteger, Integer>();
		for(BigInteger k : multiset.keySet()) {
			retMultiset.put(k, multiset.get(k));
		}
		
		Integer count = retMultiset.get(bigInteger);
		if(count == null) {
			retMultiset.put(bigInteger, 1);
		} else {
			count++;
			retMultiset.put(bigInteger, count);
		}
		
		return retMultiset;
	}
}
