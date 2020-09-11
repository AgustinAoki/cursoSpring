package rewards.internal.account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;

import common.money.MonetaryAmount;
import common.money.Percentage;

/**
 * Loads accounts from a data source using the JDBC API.
 */


// TODO-05: OPTIONAL STEP. Refactor this repository to use Spring's JdbcTemplate.
// 1. Run the JdbcAccountRepositoryTests class. It should pass.
// 2. Modify the constructor to initialize a new JdbcTemplate data member
public class JdbcAccountRepository implements AccountRepository {

	private DataSource dataSource;

	public JdbcAccountRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	// TODO-07: OPTIONAL STEP. Refactor this method using a ResultSetExtractor.
    //          (Note: If you prefer, use a lambda insead of the AccountExtractor)
	// 1. Implement a ResultSetExtractor called AccountExtractor
	// 2. Make this a private inner class and let extractData() call mapAccount() to do all the work.
	// 3. Use the JdbcTemplate to redo the SELECT below, using your new AccountExtractor
	// 4. When complete, save all changes and rerun the JdbcAccountRepositoryTests class. It should pass.
    // 5. Congratulations - you have finished the lab
	public Account findByCreditCard(String creditCardNumber) {
		String sql = "select a.ID as ID, a.NUMBER as ACCOUNT_NUMBER, a.NAME as ACCOUNT_NAME, c.NUMBER as CREDIT_CARD_NUMBER, " +
			"	b.NAME as BENEFICIARY_NAME, b.ALLOCATION_PERCENTAGE as BENEFICIARY_ALLOCATION_PERCENTAGE, b.SAVINGS as BENEFICIARY_SAVINGS " +
			"from T_ACCOUNT a, T_ACCOUNT_CREDIT_CARD c " +
			"left outer join T_ACCOUNT_BENEFICIARY b " +
			"on a.ID = b.ACCOUNT_ID " +
			"where c.ACCOUNT_ID = a.ID and c.NUMBER = ?";
		
		Account account = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, creditCardNumber);
			rs = ps.executeQuery();
			account = mapAccount(rs);
		} catch (SQLException e) {
			throw new RuntimeException("SQL exception occurred finding by credit card number", e);
		} finally {
			if (rs != null) {
				try {
					// Close to prevent database cursor exhaustion
					rs.close();
				} catch (SQLException ex) {
				}
			}
			if (ps != null) {
				try {
					// Close to prevent database cursor exhaustion
					ps.close();
				} catch (SQLException ex) {
				}
			}
			if (conn != null) {
				try {
					// Close to prevent database connection exhaustion
					conn.close();
				} catch (SQLException ex) {
				}
			}
		}
		return account;
	}

	// TODO-06: OPTIONAL STEP. Refactor this method to use Spring's JdbcTemplate.
	// 1. Use your JdbcTemplate to replace the UPDATE below
	// 2. Rerun the JdbcAccountRepositoryTests. When they pass, you are done.
	public void updateBeneficiaries(Account account) {
		String sql = "update T_ACCOUNT_BENEFICIARY SET SAVINGS = ? where ACCOUNT_ID = ? and NAME = ?";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(sql);
			for (Beneficiary beneficiary : account.getBeneficiaries()) {
				ps.setBigDecimal(1, beneficiary.getSavings().asBigDecimal());
				ps.setLong(2, account.getEntityId());
				ps.setString(3, beneficiary.getName());
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			throw new RuntimeException("SQL exception occurred updating beneficiary savings", e);
		} finally {
			if (ps != null) {
				try {
					// Close to prevent database cursor exhaustion
					ps.close();
				} catch (SQLException ex) {
				}
			}
			if (conn != null) {
				try {
					// Close to prevent database connection exhaustion
					conn.close();
				} catch (SQLException ex) {
				}
			}
		}
	}

	/**
	 * Map the rows returned from the join of T_ACCOUNT and T_ACCOUNT_BENEFICIARY to an fully-reconstituted Account
	 * aggregate.
	 * 
	 * @param rs the set of rows returned from the query
	 * @return the mapped Account aggregate
	 * @throws SQLException an exception occurred extracting data from the result set
	 */
	private Account mapAccount(ResultSet rs) throws SQLException {
		Account account = null;
		while (rs.next()) {
			if (account == null) {
				String number = rs.getString("ACCOUNT_NUMBER");
				String name = rs.getString("ACCOUNT_NAME");
				account = new Account(number, name);
				// set internal entity identifier (primary key)
				account.setEntityId(rs.getLong("ID"));
			}
			account.restoreBeneficiary(mapBeneficiary(rs));
		}
		if (account == null) {
			// no rows returned - throw an empty result exception
			throw new EmptyResultDataAccessException(1);
		}
		return account;
	}

	/**
	 * Maps the beneficiary columns in a single row to an AllocatedBeneficiary object.
	 * 
	 * @param rs the result set with its cursor positioned at the current row
	 * @return an allocated beneficiary
	 * @throws SQLException an exception occurred extracting data from the result set
	 */
	private Beneficiary mapBeneficiary(ResultSet rs) throws SQLException {
		String name = rs.getString("BENEFICIARY_NAME");
		MonetaryAmount savings = MonetaryAmount.valueOf(rs.getString("BENEFICIARY_SAVINGS"));
		Percentage allocationPercentage = Percentage.valueOf(rs.getString("BENEFICIARY_ALLOCATION_PERCENTAGE"));
		return new Beneficiary(name, allocationPercentage, savings);
	}
}
