package com.itc.service.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.itc.service.model.Customer;

public class CustomerDao {

	private String TABLE_NAME = "customer_details";

	private JdbcTemplate jdbcTemplate;
	private PlatformTransactionManager transactionManager;

	public void setTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public boolean createAndStoreCustomer(final Customer customer) {
		TransactionDefinition def = new DefaultTransactionDefinition();
		TransactionStatus status = transactionManager.getTransaction(def);
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar c = Calendar.getInstance();
			c.setTime(new Date()); // Now use today date.
			c.add(Calendar.DATE, 30); // Adding 30 days
			String expiryDateString = sdf.format(c.getTime());
			final Date expiryDateObj;
			try {
				expiryDateObj = sdf.parse(expiryDateString);
			} catch (ParseException e) {
				return false;
			}
			final String insertSql1 = "insert into " + TABLE_NAME
					+ " (first_name, last_name, email_id, organization_name, phone_number, activation_key, ip_address, expiry_date, num_of_days) values(?,?,?,?,?,?,?,?,?)";
			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(insertSql1, Statement.RETURN_GENERATED_KEYS);
					ps.setString(1, customer.getFirstName());
					ps.setString(2, customer.getLastName());
					ps.setString(3, customer.getEmailId());
					ps.setString(4, customer.getOrganizationName());
					ps.setString(5, customer.getPhoneNumber());
					ps.setString(6, customer.getActivationKey());
					ps.setString(7, customer.getIpAddress());
					ps.setTimestamp(8, new Timestamp(expiryDateObj.getTime()));
					ps.setInt(9, customer.getNumberOfDays());
					return ps;
				}
			}, keyHolder);

			// The newly generated key will be saved in this object
			final long newCustomerId = keyHolder.getKey().longValue();
			if (newCustomerId > 0) {
				transactionManager.commit(status);
				return true;
			}
		} catch (DataAccessException e) {
			transactionManager.rollback(status);
		}
		return false;
	}

	@Transactional
	public boolean checkIfCustomerIsAlreadyPresentForTheEmail(String emailId) {
		String query = "select id from " + TABLE_NAME + " where email_id = '" + emailId + "'";
		return jdbcTemplate.query(query, new ResultSetExtractor<Boolean>() {
			public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
				while (rs.next()) {
					return true;
				}
				return false;
			}
		});
	}

	@Transactional
	public boolean checkIfCustomerIsAlreadyValidated(String emailId, String activationKey) {
		String query = "select is_validated from " + TABLE_NAME + " where email_id = '" + emailId
				+ "' and activation_key = '" + activationKey + "'";
		return jdbcTemplate.query(query, new ResultSetExtractor<Boolean>() {
			public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
				while (rs.next()) {
					boolean isValidated = rs.getBoolean("is_validated");
					if (isValidated) {
						return true;
					}
				}
				return false;
			}
		});
	}

	@Transactional
	public boolean validateCustomer(String emailId, String activationKey) {
		String query = "update " + TABLE_NAME + " set is_validated = 1 where email_id = '" + emailId
				+ "' and activation_key = '" + activationKey + "'";
		try {
			int updateStatus = jdbcTemplate.update(query);
			if (updateStatus == 1) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}
}