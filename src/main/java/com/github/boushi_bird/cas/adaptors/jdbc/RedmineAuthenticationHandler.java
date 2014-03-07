package com.github.boushi_bird.cas.adaptors.jdbc;

import java.util.Map;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.DefaultPasswordEncoder;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class RedmineAuthenticationHandler extends
		AbstractUsernamePasswordAuthenticationHandler {
	private static final String WHERE = " WHERE login = ? AND status = 1 AND auth_source_id IS NULL";
	private static final String SQL = "SELECT hashed_password, salt FROM users"
			+ WHERE;
	private static final String SQL_WITHOUT_SALT = "SELECT hashed_password FROM users"
			+ WHERE;

	private static final String COL_SALT = "salt";
	private static final String COL_HASHED_PASSWORD = "hashed_password";

	private static final String SQL_UPDATE_LAST_LOGIN_ON = "update users set last_login_on = now()"
			+ WHERE;

	private boolean useSalt;

	private boolean updateLastLoginOn;

	@NotNull
	private JdbcTemplate jdbcTemplate;

	@NotNull
	private DataSource dataSource;

	public RedmineAuthenticationHandler() {
		this.useSalt = true;
		this.updateLastLoginOn = false;
		this.setPasswordEncoder(new DefaultPasswordEncoder("SHA"));
	}

	@Override
	protected final boolean authenticateUsernamePasswordInternal(
			final UsernamePasswordCredentials credentials)
			throws AuthenticationException {
		final String username = getPrincipalNameTransformer().transform(
				credentials.getUsername());
		final String password = credentials.getPassword();
		final String encryptedPassword = this.getPasswordEncoder().encode(
				password);
		final boolean authenticated;
		if (this.useSalt) {
			authenticated = this.authenticateWithSalt(username,
					encryptedPassword);
		} else {
			authenticated = this.authenticateWithoutSalt(username,
					encryptedPassword);
		}
		if (this.updateLastLoginOn) {
			this.doUpdateLastLoginOn(username);
		}
		return authenticated;
	}

	private boolean authenticateWithSalt(final String username,
			final String encryptedPassword) {
		final Map<String, Object> user;
		try {
			user = this.query(SQL, username);
		} catch (final IncorrectResultSizeDataAccessException e) {
			return false;
		}
		final String salt = String.valueOf(user.get(COL_SALT));
		final String hashedPassword = String.valueOf(user
				.get(COL_HASHED_PASSWORD));
		if (StringUtils.isNotEmpty(salt)) {
			return hashedPassword.equals(this.getPasswordEncoder().encode(
					salt + encryptedPassword));
		} else {
			return hashedPassword.equals(encryptedPassword);
		}
	}

	private boolean authenticateWithoutSalt(final String username,
			final String encryptedPassword) {
		final Map<String, Object> user;
		try {
			user = this.query(SQL_WITHOUT_SALT, username);
		} catch (final IncorrectResultSizeDataAccessException e) {
			return false;
		}
		final String hashedPassword = String.valueOf(user
				.get(COL_HASHED_PASSWORD));
		return hashedPassword.equals(encryptedPassword);
	}

	private void doUpdateLastLoginOn(final String username) {
		this.update(SQL_UPDATE_LAST_LOGIN_ON, username);
	}

	private Map<String, Object> query(final String sql, final Object... args) {
		return getJdbcTemplate().queryForMap(sql, args);
	}

	private int update(final String sql, final Object... args) {
		return getJdbcTemplate().update(sql, args);
	}

	public final void setUseSalt(boolean useSalt) {
		this.useSalt = useSalt;
	}

	public void setUpdateLastLoginOn(boolean updateLastLoginOn) {
		this.updateLastLoginOn = updateLastLoginOn;
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSource = dataSource;
	}

	protected JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	protected DataSource getDataSource() {
		return dataSource;
	}
}
