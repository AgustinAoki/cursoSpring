package rewards;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import common.money.MonetaryAmount;

/**
 * A system test that verifies the components of the RewardNetwork application
 * work together to reward for dining successfully. Uses Spring to bootstrap the
 * application for use in a test environment.
 */

/* TODO 01: Remove setUp() and tearDown() methods and use annotations instead
 *            such as @SpringJUnitConfig(classes=TestInfrastructureConfig.class),
 *            which is a composite annotation of @ExtendWith(SpringExtension.class)
 *            and @ContextConfiguration
 *          - Use @RunWith(JUnitPlatform.class) to run JUnit 5 test using JUnit 4 runner
 * 			- Remove the attribute 'context' which is not needed anymore.
 * 			- Use @Autowire to populate the rewardNetwork instance.
 *			- Re-run the current test, it should pass.
 */

/* TODO 02: In the package rewards/internal, annotate all 'Stub*Repository' classes
 *            with the @Repository annotation (WITHOUT specifying any profile yet).
 * 			- Rerun the current test, it should fail.  Why?
 */

/* TODO 03: Using the @Profile annotation, assign the 'jdbc' profile to all Jdbc*Repository classes 
 * 			  (such as JdbcAccountRepository).  (Be sure to annotate the actual repository classes in
 * 			  src/main/java, not the test classes in src/main/test!)
 * 			- In the same way, assign the 'stub' profile to all Stub*Repository classes 
 * 			  (such as StubAccountRepository)
 * 			- Add @ActiveProfiles to this test class (below) and specify the "stub" profile.
 * 			- Run the current test, it should pass. 
 * 			  Examine the logs, they should indicate "stub" repositories were used.
 */

/* TODO 04: Change active-profile to "jdbc". Rerun the test, it should pass.  
 * 			Which repository implementations are being used now?
 */

/* TODO 05: Go to corresponding step in TestInfrastructureDevConfig.
 */

/* TODO 06: Now that the bean 'dataSource' is specific to the jdbc-dev profile, should we expect 
 * 			this test to be successful?
 * 			Make the appropriate changes so the current test uses 2 profiles ('jdbc' and 'jdbc-dev').
 * 			Rerun the test, it should pass.
 */

/* TODO 07: Open TestInfrastructureProductionConfig and note the different datasource that will be
 * 			used if the profile = 'jdbc-production'.
 * 			Now update the current test so it uses profiles 'jdbc' and 'jdbc-production'. 
 * 			Rerun the test, it should pass.
 */

/* TODO 08: Bonus question: see the 'Optional Step' inside the Detailed Instructions.
 */

public class RewardNetworkTests {

	
	/**
	 * The object being tested.
	 */
	private RewardNetwork rewardNetwork;

	/**
	 * Need this to enable clean shutdown at the end of the application
	 */
	private ConfigurableApplicationContext context;

	@BeforeEach
	public void setUp() {
		// Create the test configuration for the application from one file
		context = SpringApplication.run(TestInfrastructureConfig.class);
		// Get the bean to use to invoke the application
		rewardNetwork = context.getBean(RewardNetwork.class);
	}

	@AfterEach
	public void tearDown() throws Exception {
		// simulate the Spring bean destruction lifecycle:
		if (context != null)
			context.close();
	}

	@Test
	@DisplayName("test if reward computation and distribution works")
	public void testRewardForDining() {
		// create a new dining of 100.00 charged to credit card
		// '1234123412341234' by merchant '123457890' as test input
		Dining dining = Dining.createDining("100.00", "1234123412341234",
				"1234567890");

		// call the 'rewardNetwork' to test its rewardAccountFor(Dining) method
		RewardConfirmation confirmation = rewardNetwork
				.rewardAccountFor(dining);

		// assert the expected reward confirmation results
		assertNotNull(confirmation);
		assertNotNull(confirmation.getConfirmationNumber());

		// assert an account contribution was made
		AccountContribution contribution = confirmation
				.getAccountContribution();
		assertNotNull(contribution);

		// the contribution account number should be '123456789'
		assertEquals("123456789", contribution.getAccountNumber());

		// the total contribution amount should be 8.00 (8% of 100.00)
		assertEquals(MonetaryAmount.valueOf("8.00"), contribution.getAmount());

		// the total contribution amount should have been split into 2
		// distributions
		assertEquals(2, contribution.getDistributions().size());

		// the total contribution amount should have been split into 2 distributions
		// each distribution should be 4.00 (as both have a 50% allocation)
		assertAll("distribution of reward",
				() -> assertEquals(2, contribution.getDistributions().size()),
				() -> assertEquals(MonetaryAmount.valueOf("4.00"), contribution.getDistribution("Annabelle").getAmount()),
				() -> assertEquals(MonetaryAmount.valueOf("4.00"), contribution.getDistribution("Corgan").getAmount()));
	}
}