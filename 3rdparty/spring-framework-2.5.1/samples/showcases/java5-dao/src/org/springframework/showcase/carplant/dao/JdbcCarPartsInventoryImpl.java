package org.springframework.showcase.carplant.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Jdbc-based implementation of the {@link CarPartsInventory}.
 * Uses the Java5-specific {@link SimpleJdbcTemplate}.
 * More information on the SimpleJdbcTemplate can be found in the
 * reference manual. 
 * 
 * @see http://www.springframework.org/docs/reference/jdbc.html#jdbc-SimpleJdbcTemplate
 * @author Alef Arendsen
 * @since 2.0.4
 */
public class JdbcCarPartsInventoryImpl implements CarPartsInventory {

	private SimpleJdbcTemplate jdbcTemplate;
	
	// the use of the ParameterizedRowMapper prevents us from having to cast
	// all over the place. The use of the RowMapper (as shown in getPartsForModel())
	// is much more elegant now than it was before.
	// Note that the order of arguments for the query() method has changed. VARARGS
	// can only go last in the list of parameters, so the ParameterizedRowMapper moved
	// up in the ranks a bit.
	private static ParameterizedRowMapper<Part> partMapper = new ParameterizedRowMapper<Part>() {
		public Part mapRow(ResultSet rs, int rowNum) throws SQLException {
			Part part = new Part();
			part.setModel(rs.getString("model"));
			part.setName(rs.getString("name"));
			part.setPartNo(rs.getString("number"));
			return part;
		}
	};

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}
	
	public void addPart(String model, String number, String name) {
		// look ma, no Object[]!
		// this use of the SimpleJdbcTemplate significantly eases
		// issuing INSERT or UPDATE statements. Various methods 
		// features varargs in the new SimpleJdbcTemplate
		jdbcTemplate.update("INSERT INTO t_car_part VALUES (?,?,?,?)", name, number, model, 0);
		
	}
	
	public List<Part> getPartsForModel(CarModel carModel) {
		// as one can see, the use of parameterized types here cleans up the
		// query method significantly
		return jdbcTemplate.query("SELECT name, number, model FROM t_car_part WHERE model = ?", 
				partMapper, carModel.getName());
	}
	
	public void updatePartStock(String partNo, int i) {		
		int affected = jdbcTemplate.update("UPDATE t_car_part SET stock = stock + ? WHERE number = ?", i, partNo);
		if (affected == 0) {
			throw new PartNotFoundException();
		}
	}
}
