package org.springframework.samples.jpetstore.dao.ibatis;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
import org.springframework.samples.jpetstore.dao.ProductDao;
import org.springframework.samples.jpetstore.domain.Product;

public class SqlMapProductDao extends SqlMapClientDaoSupport implements ProductDao {

  public List getProductListByCategory(String categoryId) throws DataAccessException {
    return getSqlMapClientTemplate().queryForList("getProductListByCategory", categoryId);
  }

  public Product getProduct(String productId) throws DataAccessException {
    return (Product) getSqlMapClientTemplate().queryForObject("getProduct", productId);
  }

  public List searchProductList(String keywords) throws DataAccessException {
    Object parameterObject = new ProductSearch(keywords);
    return getSqlMapClientTemplate().queryForList("searchProductList", parameterObject);
  }


  /* Inner Classes */

  public static class ProductSearch {

    private List keywordList = new ArrayList();

    public ProductSearch(String keywords) {
      StringTokenizer splitter = new StringTokenizer(keywords, " ", false);
      while (splitter.hasMoreTokens()) {
        this.keywordList.add("%" + splitter.nextToken() + "%");
      }
    }

    public List getKeywordList() {
      return keywordList;
    }
  }

}
