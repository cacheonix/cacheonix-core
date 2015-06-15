package org.springframework.samples.imagedb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.LobRetrievalFailureException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

/**
 * Default implementation of the central image database business interface.
 *
 * <p>Uses JDBC with a LobHandler to retrieve and store image data.
 * Illustrates direct use of the <code>jdbc.core</code> package,
 * i.e. JdbcTemplate, rather than operation objects from the
 * <code>jdbc.object</code> package.
 *
 * @author Juergen Hoeller
 * @since 07.01.2004
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.support.lob.LobHandler
 */
public class DefaultImageDatabase extends SimpleJdbcDaoSupport implements ImageDatabase {

	private LobHandler lobHandler;

	/**
	 * Set the LobHandler to use for BLOB/CLOB access.
	 * Could use a DefaultLobHandler instance as default,
	 * but relies on a specified LobHandler here.
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler
	 */
	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	@Transactional(readOnly=true)
	public List<ImageDescriptor> getImages() throws DataAccessException {
		return getSimpleJdbcTemplate().query(
		    "SELECT image_name, description FROM imagedb",
		    new ParameterizedRowMapper<ImageDescriptor>() {
			    public ImageDescriptor mapRow(ResultSet rs, int rowNum) throws SQLException {
				    String name = rs.getString(1);
				    String description = lobHandler.getClobAsString(rs, 2);
				    return new ImageDescriptor(name, description);
			    }
		    });
	}

	@Transactional(readOnly=true)
	public void streamImage(final String name, final OutputStream contentStream) throws DataAccessException {
		getJdbcTemplate().query(
				"SELECT content FROM imagedb WHERE image_name=?", new Object[] {name},
				new AbstractLobStreamingResultSetExtractor() {
					protected void handleNoRowFound() throws LobRetrievalFailureException {
						throw new EmptyResultDataAccessException(
						    "Image with name '" + name + "' not found in database", 1);
					}
					public void streamData(ResultSet rs) throws SQLException, IOException {
						InputStream is = lobHandler.getBlobAsBinaryStream(rs, 1);
						if (is != null) {
							FileCopyUtils.copy(is, contentStream);
						}
					}
				}
		);
	}

	@Transactional
	public void storeImage(
	    final String name, final InputStream contentStream, final int contentLength, final String description)
	    throws DataAccessException {

		getJdbcTemplate().execute(
				"INSERT INTO imagedb (image_name, content, description) VALUES (?, ?, ?)",
				new AbstractLobCreatingPreparedStatementCallback(this.lobHandler) {
					protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
						ps.setString(1, name);
						lobCreator.setBlobAsBinaryStream(ps, 2, contentStream, contentLength);
						lobCreator.setClobAsString(ps, 3, description);
					}
				}
		);
	}

	public void checkImages() {
		// Could implement consistency check here...
		logger.info("Checking images: not implemented but invoked by scheduling");
	}

	@Transactional
	public void clearDatabase() throws DataAccessException {
		getJdbcTemplate().update("DELETE FROM imagedb");
	}

}
