# 23w13a_or_b Vote Probability Calculator
In the [23w13a_or_b Minecraft April Fools' version](https://minecraft.wiki/w/Java_Edition_23w13a_or_b) (aka the "Vote Update"), votes generate randomly while playing. This program calculates the exact probability of a vote type appearing in the next vote during this version.

## Game Mechanics
The game uses a weighted list of all vote types to determine what vote appears randomly. Higher weights cause the vote to appear more frequently. The list of weights for every vote type can be seen at [this list originally compiled by Captain_S0L0](https://docs.google.com/spreadsheets/d/1JEZvSutJEPWBbAfigpnUyQlKq_4FmufBDp4jugfXY70/edit?usp=sharing). The total weight in the list is 113,008 so if we only consider simple votes, the probability that a 500 weighted vote will appear is 500/113008=125/28252. However, there are two special types of votes that change this simple probability.

### Repeal Votes
Repeal votes are a type of vote that will remove another vote if approved. The probability of a repeal vote appearing defaults to 50%. However, this percentage can be changed to any integer percentage from 20% to 80% using the `new_vote_repeal_vote_chance` vote. 

### Combined Votes
Combined votes are one vote that contains multiple vote types chained together. In game, these votes appear as "[Vote A], but [Vote B]". Most of the complexity in this program arises from these types of votes. The `new_vote_extra_effect_chance` determines the probability that a new vote part will be added at each step and the `new_vote_extra_effect_max_count` determines the maximum number of votes that can be added to a base vote. The extra effect chance defaults to 30% but can be set to any integer percentage from 0% to 80% using the `new_vote_extra_effect_chance` vote. The max count defaults to 1 but can be set to any integer from 0 to 5 using the `new_vote_extra_effect_max_count` vote. Repeal votes cannot be in combined votes, they are a separate type of vote that is chosen before the possibility of a combined vote.

Combined votes do not allow for any repeat vote types. So, after the first vote has been determined, that vote is effectively removed from the weighted list for determining the second vote. Then, to calculate the exact probability, we must consider the probability of our vote being chosen given every other possible first choice.

## Example
Here I show an example of how you can calculate the probability given the above mechanics. For simplicity, assume there are only five votes with the following weights:
| Vote ID | Weight |
|---------|--------|
| a       | 1      |
| b       | 2      |
| c       | 3      |
| d       | 4      |

For this example, we'll consider the default values of `new_vote_repeal_vote_chance` = 50%, `new_vote_extra_effect_chance` = 30%, and `new_vote_extra_effect_max_count` = 1.

Let's find the probability that vote `a` appears in the next vote. The initial total weight is 1 + 2 + 3 + 4 = 10. So in the first round, there's a 1/10 chance that a is chosen. Then, we consider all the other possible first choices:
* b is chosen with 2/10 probability. Without b, the total weight is 8 so the probability that a is chosen in the next round is 1/8. The next round only happens 30% of the time so this b + a scenario will happen with 30% * 2/10 * 1/8 probability.
* c is chosen with 3/10 probability. Without c, the total weight is 7 so the probability that a is chosen in the next round is 1/7. The next round only happens 30% of the time so this c + a scenario will happen with 30% * 3/10 * 1/7 probability.
* d is chosen with 4/10 probability. Without d, the total weight is 6 so the probability that a is chosen in the next round is 1/6. The next round only happens 30% of the time so this d + a scenario will happen with 30% * 4/10 * 1/6 probability.

Adding these together we get the expression: 1/10 + 30% * (2/10 * 1/8 + 3/10 * 1/7 + 4/10 * 1/6). However, half of the time the votes will be repeal votes so the final expression is:

1/2(1/10 + 30% * (2/10 * 1/8 + 3/10 * 1/7 + 4/10 * 1/6)) = 393/5600

The actual weighted list in Minecraft has 181 votes. When `new_vote_extra_effect_max_count` is greater than one, you have to consider multiple rounds of possible choices which grow very fast. You can see how this becomes impractical to calculate manually.

## Usage
1. In the releases section on the right, download the most recent version's vote-probability-calculator.zip under Assets.
2. Unzip the file on your machine.
3. Navigate to the folder using your terminal.
4. Run `java -jar 23w13a_or_b-vote-probability-calculator.jar`
5. You will receive four prompts.
    1. "What is the ID of the vote you want the probability for?": See the [Minecraft Wiki list](https://minecraft.wiki/w/Java_Edition_23w13a_or_b#Commands) to find the id. The program will throw an exception if the id does not exist.
    2. "What is the current repeal percentage?": Omit the percent sign, just put the integer of the current `new_vote_repeal_vote_chance` value.
    3. "What is the current new vote extra effect percentage?": Omit the percent sign, just put the integer of the current `new_vote_extra_effect_chance` value.
    4. "What is the current new vote extra effect max count?": The current `new_vote_extra_effect_max_count` value. This is intended to only go to the max survival value of 5. Beyond around 12, the runtime becomes very long.
6. After entering all these values, the fraction will be outputted to the terminal. The fraction may be very large! (I've gotten >600 digits long) If you want to get a decimal approximation of these large fractions, you can use [calculator.net](https://www.calculator.net/big-number-calculator.html) or [Qalculate!](https://qalculate.github.io/)

If you want to skip the user prompts, you can use `java -jar 23w13a_or_b-vote-probability-calculator.jar <vote_id> <new_vote_repeal_vote_chance> <new_vote_extra_effect_chance> <new_vote_extra_effect_max_count>` where the parameters are all the user prompt values in the same order as above.
