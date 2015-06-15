package org.springframework.samples.jpetstore.dao.ibatis;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
import org.springframework.samples.jpetstore.dao.CategoryDao;
import org.springframework.samples.jpetstore.domain.Category;

public class SqlMapCategoryDao extends SqlMapClientDaoSupport implements CategoryDao {

  public List getCategoryList() throws DataAccessException {
    return getSqlMapClientTemplate().queryForList("getCategoryList", null);
  }

  public Category getCategory(String categoryId) throws DataAccessException {
    return (Category) getSqlMapClientTemplate().queryForObject("getCategory", categoryId);
  }

}
