package org.springframework.samples.jpetstore.dao.ibatis;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
import org.springframework.samples.jpetstore.dao.OrderDao;
import org.springframework.samples.jpetstore.domain.LineItem;
import org.springframework.samples.jpetstore.domain.Order;

public class SqlMapOrderDao extends SqlMapClientDaoSupport implements OrderDao {

  private SqlMapSequenceDao sequenceDao;

	public void setSequenceDao(SqlMapSequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}

	public List getOrdersByUsername(String username) throws DataAccessException {
    return getSqlMapClientTemplate().queryForList("getOrdersByUsername", username);
  }

  public Order getOrder(int orderId) throws DataAccessException {
    Object parameterObject = new Integer(orderId);
    Order order = (Order) getSqlMapClientTemplate().queryForObject("getOrder", parameterObject);
		if (order != null) {
    	order.setLineItems(getSqlMapClientTemplate().queryForList("getLineItemsByOrderId", new Integer(order.getOrderId())));
		}
    return order;
  }

  public void insertOrder(Order order) throws DataAccessException {
		order.setOrderId(this.sequenceDao.getNextId("ordernum"));
		getSqlMapClientTemplate().insert("insertOrder", order);
		getSqlMapClientTemplate().insert("insertOrderStatus", order);
    for (int i = 0; i < order.getLineItems().size(); i++) {
      LineItem lineItem = (LineItem) order.getLineItems().get(i);
      lineItem.setOrderId(order.getOrderId());
      getSqlMapClientTemplate().insert("insertLineItem", lineItem);
    }
  }

}
