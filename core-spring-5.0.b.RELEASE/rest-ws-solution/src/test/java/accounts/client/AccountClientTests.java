package accounts.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import rewards.internal.account.Account;
import rewards.internal.account.Beneficiary;

import common.money.Percentage;

@RunWith(JUnitPlatform.class)
public class AccountClientTests {
	
	/**
	 * server URL ending with the servlet mapping on which the application can be reached.
	 */
	private static final String BASE_URL = "http://localhost:8080";
	
	private RestTemplate restTemplate = new RestTemplate();
	private Random random = new Random();
	
	@Test 
	public void listAccounts() {
		String url = BASE_URL + "/accounts";
		// we have to use Account[] instead of List<Account>, or Jackson won't know what type to unmarshal to
		Account[] accounts = restTemplate.getForObject(url, Account[].class);
		assertTrue(accounts.length >= 21);
		assertEquals("Keith and Keri Donald", accounts[0].getName());
		assertEquals(2, accounts[0].getBeneficiaries().size());
		assertEquals(Percentage.valueOf("50%"), accounts[0].getBeneficiary("Annabelle").getAllocationPercentage());
	}
	
	@Test
	public void getAccount() {
		String url = BASE_URL + "/accounts/{accountId}";
		Account account = restTemplate.getForObject(url, Account.class, 0); 
		assertEquals("Keith and Keri Donald", account.getName());
		assertEquals(2, account.getBeneficiaries().size());
		assertEquals(Percentage.valueOf("50%"), account.getBeneficiary("Annabelle").getAllocationPercentage());
	}
	
	@Test
	public void createAccount() {
		String url = BASE_URL + "/accounts";
		// use a unique number to avoid conflicts
		String number = String.format("12345%4d", random.nextInt(10000));
		Account account = new Account(number, "John Doe");
		account.addBeneficiary("Jane Doe");
		URI newAccountLocation = restTemplate.postForLocation(url, account);
		
		Account retrievedAccount = restTemplate.getForObject(newAccountLocation, Account.class);
		assertEquals(account.getNumber(), retrievedAccount.getNumber());
		
		Beneficiary accountBeneficiary = account.getBeneficiaries().iterator().next();
		Beneficiary retrievedAccountBeneficiary = retrievedAccount.getBeneficiaries().iterator().next();
		
		assertEquals(accountBeneficiary.getName(), retrievedAccountBeneficiary.getName());
		assertNotNull(retrievedAccount.getEntityId());
	}
	
	@Test
	public void addAndDeleteBeneficiary() {
		// perform both add and delete to avoid issues with side effects
		String addUrl = BASE_URL + "/accounts/{accountId}/beneficiaries";
		URI newBeneficiaryLocation = restTemplate.postForLocation(addUrl, "David", 1);
		Beneficiary newBeneficiary = restTemplate.getForObject(newBeneficiaryLocation, Beneficiary.class);
		assertEquals("David", newBeneficiary.getName());
		
		restTemplate.delete(newBeneficiaryLocation);

		try {
			System.out.println("You SHOULD get the exception \"No such beneficiary with name 'David'\" in the server.");
			restTemplate.getForObject(newBeneficiaryLocation, Beneficiary.class);
			fail("Should have received 404 Not Found after deleting beneficiary at " + newBeneficiaryLocation);
		} catch (HttpClientErrorException e) {
			assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
		}
	}
}
