package config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO-01: This class will be used to define our application beans. Annotate to
 * mark it as a special class for providing Spring with bean configuration
 * instructions.
 * <p>
 * TODO-02: Define four empty @Bean methods, one for the reward-network and
 * three for the repositories. For consistency, the RewardNetworkImpl should
 * have the bean name 'rewardNetwork'. We have already provided three JDDC
 * repository implementations - for Accounts, Restaurants and Rewards
 * <p>
 * TODO-03: Each repository has a DataSource property to set, but the DataSource
 * is defined elsewhere (TestInfrastructureConfig.java), so you will need to
 * define a constructor to allow Spring to set the dataSource. As it is the only
 * constructor, Spring will automatically call it, so @Autowired is not
 * required.
 * <p>
 * TODO-04: Implement each @Bean method to contain the code needed to
 * instantiate its object and set its dependencies. Be careful - do you need to
 * use a constructor or call a setter to set dependencies? If you aren't sure,
 * refer to the diagram in the lab-instructions for more details.
 * <p>
 * Each bean method should return an interface not an implementation type.
 */
public class RewardsConfig {

	// TODO-03: Set this by adding a constructor.
	private DataSource dataSource;

}
