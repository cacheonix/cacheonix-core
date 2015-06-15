package org.springframework.samples.jpetstore.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.jpetstore.domain.Category;

public interface CategoryDao {

	List getCategoryList() throws DataAccessException;

  Category getCategory(String categoryId) throws DataAccessException;

}
