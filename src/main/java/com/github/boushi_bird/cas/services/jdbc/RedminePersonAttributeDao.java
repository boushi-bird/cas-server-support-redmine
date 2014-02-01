package com.github.boushi_bird.cas.services.jdbc;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.jdbc.SingleRowJdbcPersonAttributeDao;

public class RedminePersonAttributeDao extends SingleRowJdbcPersonAttributeDao {
	private static final String SQL = "SELECT firstname, lastname, mail FROM users WHERE {0} AND status = 1 AND auth_source_id IS NULL";

	private static final String COL_MAIL = "mail";
	private static final String COL_FIRSTNAME = "firstname";
	private static final String COL_LASTNAME = "lastname";
	private static final String COL_FULLNAME = "fullname";

	public RedminePersonAttributeDao(final DataSource ds) {
		super(ds, SQL);
		final Map<String, Object> queryAttributeMapping = new LinkedHashMap<String, Object>();
		queryAttributeMapping.put("username", "login");
		this.setQueryAttributeMapping(queryAttributeMapping);
		final Map<String, Object> resultAttributeMapping = new LinkedHashMap<String, Object>();
		queryAttributeMapping.put(COL_MAIL, COL_MAIL);
		queryAttributeMapping.put(COL_FIRSTNAME, COL_FIRSTNAME);
		queryAttributeMapping.put(COL_LASTNAME, COL_LASTNAME);
		queryAttributeMapping.put(COL_FULLNAME, COL_FULLNAME);
		this.setResultAttributeMapping(resultAttributeMapping);
	}

	@Override
	protected List<IPersonAttributes> parseAttributeMapFromResults(
			final List<Map<String, Object>> queryResults,
			final String queryUserName) {
		final List<Map<String, Object>> queryResultsAdditional = new ArrayList<Map<String, Object>>();
		for (final Map<String, Object> queryResult : queryResults) {
			final Map<String, Object> resultAdditional = new LinkedHashMap<String, Object>(
					queryResult);
			queryResultsAdditional.add(resultAdditional);
			final Object firstname = queryResult.containsKey(COL_FIRSTNAME) ? queryResult
					.get(COL_FIRSTNAME) : null;
			final Object lastname = queryResult.containsKey(COL_LASTNAME) ? queryResult
					.get(COL_LASTNAME) : null;
			if (firstname != null && lastname != null) {
				resultAdditional.put(COL_FULLNAME,
						MessageFormat.format("{1} {0}", firstname, lastname));
			}
		}
		return super.parseAttributeMapFromResults(queryResultsAdditional,
				queryUserName);
	}
}
