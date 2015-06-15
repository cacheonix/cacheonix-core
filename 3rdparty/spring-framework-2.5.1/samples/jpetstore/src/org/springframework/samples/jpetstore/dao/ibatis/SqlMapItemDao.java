package org.springframework.samples.jpetstore.dao.ibatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
import org.springframework.samples.jpetstore.dao.ItemDao;
import org.springframework.samples.jpetstore.domain.Item;
import org.springframework.samples.jpetstore.domain.LineItem;
import org.springframework.samples.jpetstore.domain.Order;

public class SqlMapItemDao extends SqlMapClientDaoSupport implements ItemDao {

  public void updateQuantity(Order order) throws DataAccessException {
    for (int i = 0; i < order.getLineItems().size(); i++) {
      LineItem lineItem = (LineItem) order.getLineItems().get(i);
      String itemId = lineItem.getItemId();
      Integer increment = new Integer(lineItem.getQuantity());
      Map param = new HashMap(2);
      param.put("itemId", itemId);
      param.put("increment", increment);
      getSqlMapClientTemplate().update("updateInventoryQuantity", param, 1);
    }
  }

  public boolean isItemInStock(String itemId) throws DataAccessException {
    Integer i = (Integer) getSqlMapClientTemplate().queryForObject("getInventoryQuantity", itemId);
    return (i != null && i.intValue() > 0);
  }

  public List getItemListByProduct(String productId) throws DataAccessException {
    return getSqlMapClientTemplate().queryForList("getItemListByProduct", productId);
  }

  public Item getItem(String itemId) throws DataAccessException {
    Item item = (Item) getSqlMapClientTemplate().queryForObject("getItem", itemId);
		if (item != null) {
			Integer qty = (Integer) getSqlMapClientTemplate().queryForObject("getInventoryQuantity", itemId);
			item.setQuantity(qty.intValue());
		}
    return item;
  }

}
