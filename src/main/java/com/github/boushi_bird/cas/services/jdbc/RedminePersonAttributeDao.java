package com.github.boushi_bird.cas.services.jdbc;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.jdbc.SingleRowJdbcPersonAttributeDao;
import org.springframework.jdbc.core.JdbcTemplate;

public class RedminePersonAttributeDao extends SingleRowJdbcPersonAttributeDao {
	private static final String SQL = "SELECT firstname, lastname, mail FROM users WHERE {0} AND status = 1 AND auth_source_id IS NULL";
	private static final String SQL_GET_USERFORMAT = "SELECT value FROM settings WHERE name = 'user_format';";

	private static final String ATTRIBUTE_USERNAME = "username";

	private static final String COL_LOGIN = "login";
	private static final String COL_MAIL = "mail";
	private static final String COL_FIRSTNAME = "firstname";
	private static final String COL_LASTNAME = "lastname";
	private static final String COL_USERNAME = "username";

	private final JdbcTemplate jdbcTemplate;

	public RedminePersonAttributeDao(final DataSource ds) {
		super(ds, SQL);
		this.jdbcTemplate = new JdbcTemplate(ds);
		final Map<String, Object> queryAttributeMapping = new LinkedHashMap<String, Object>();
		queryAttributeMapping.put(ATTRIBUTE_USERNAME, COL_LOGIN);
		this.setQueryAttributeMapping(queryAttributeMapping);
		final Map<String, Object> resultAttributeMapping = new LinkedHashMap<String, Object>();
		resultAttributeMapping.put(COL_MAIL, COL_MAIL);
		resultAttributeMapping.put(COL_FIRSTNAME, COL_FIRSTNAME);
		resultAttributeMapping.put(COL_LASTNAME, COL_LASTNAME);
		resultAttributeMapping.put(COL_USERNAME, COL_USERNAME);
		this.setResultAttributeMapping(resultAttributeMapping);
	}

	@Override
	protected List<IPersonAttributes> parseAttributeMapFromResults(
			final List<Map<String, Object>> queryResults,
			final String queryUserName) {
		final RedmineUserFormat userFormat = getUserFormat();
		final List<Map<String, Object>> queryResultsAdditional = new ArrayList<Map<String, Object>>();
		for (final Map<String, Object> queryResult : queryResults) {
			final Map<String, Object> resultAdditional = new LinkedHashMap<String, Object>(
					queryResult);
			queryResultsAdditional.add(resultAdditional);
			final String username = userFormat
					.createUserFormatName(queryResult);
			if (username != null) {
				resultAdditional.put(COL_USERNAME, username);
			}
		}
		return super.parseAttributeMapFromResults(queryResultsAdditional,
				queryUserName);
	}

	private RedmineUserFormat getUserFormat() {
		final Map<String, Object> result = this.jdbcTemplate
				.queryForMap(SQL_GET_USERFORMAT);

		if (result != null && result.containsKey("value")) {
			final String value = result.get("value").toString();
			for (final RedmineUserFormat userFormat : RedmineUserFormat
					.values()) {
				if (userFormat.name().equalsIgnoreCase(value)) {
					return userFormat;
				}
			}
		}
		return RedmineUserFormat.USERNAME;
	}

	private enum RedmineUserFormat {
		/** firstname_lastname */
		FIRSTNAME_LASTNAME {
			@Override
			public String createUserFormatName(Map<String, Object> queryResult) {
				final String firstname = getFirstName(queryResult);
				final String lastname = getLastName(queryResult);
				if (firstname == null || lastname == null) {
					return null;
				}
				return MessageFormat.format("{0} {1}", firstname, lastname);
			}
		},
		/** firstname_lastinitial */
		FIRSTNAME_LASTINITIAL {
			@Override
			public String createUserFormatName(Map<String, Object> queryResult) {
				final String firstname = getFirstName(queryResult);
				final String lastname = getLastName(queryResult);
				if (firstname == null || lastname == null
						|| lastname.length() < 1) {
					return null;
				}
				return MessageFormat.format("{0} {1}.", firstname,
						lastname.substring(0, 1));
			}
		},
		/** firstname */
		FIRSTNAME {
			@Override
			public String createUserFormatName(Map<String, Object> queryResult) {
				return getFirstName(queryResult);
			}
		},
		/** lastname_firstname */
		LASTNAME_FIRSTNAME {
			@Override
			public String createUserFormatName(Map<String, Object> queryResult) {
				final String firstname = getFirstName(queryResult);
				final String lastname = getLastName(queryResult);
				if (firstname == null || lastname == null) {
					return null;
				}
				return MessageFormat.format("{1} {0}", firstname, lastname);
			}
		},
		/** lastname_coma_firstname */
		LASTNAME_COMA_FIRSTNAME {
			@Override
			public String createUserFormatName(Map<String, Object> queryResult) {
				final String firstname = getFirstName(queryResult);
				final String lastname = getLastName(queryResult);
				if (firstname == null || lastname == null) {
					return null;
				}
				return MessageFormat.format("{1}, {0}", firstname, lastname);
			}
		},
		/** lastname */
		LASTNAME {
			@Override
			public String createUserFormatName(Map<String, Object> queryResult) {
				return getLastName(queryResult);
			}
		},
		/** username */
		USERNAME, ;

		public String createUserFormatName(final Map<String, Object> queryResult) {
			return getLogin(queryResult);
		}

		protected String getFirstName(final Map<String, Object> queryResult) {
			return getStringValue(queryResult, COL_FIRSTNAME);
		}

		protected String getLastName(final Map<String, Object> queryResult) {
			return getStringValue(queryResult, COL_LASTNAME);
		}

		protected String getLogin(final Map<String, Object> queryResult) {
			return getStringValue(queryResult, COL_LOGIN);
		}

		private String getStringValue(final Map<String, Object> queryResult,
				final String key) {
			if (queryResult.containsKey(key)) {
				final Object value = queryResult.get(key);
				if (value != null) {
					return value.toString();
				}
			}
			return null;
		}
	}
}
