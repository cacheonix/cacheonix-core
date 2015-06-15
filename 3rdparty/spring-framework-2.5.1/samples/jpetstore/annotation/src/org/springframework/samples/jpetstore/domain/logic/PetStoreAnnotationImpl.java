package org.springframework.samples.jpetstore.domain.logic;

import java.util.List;

import org.springframework.samples.jpetstore.dao.AccountDao;
import org.springframework.samples.jpetstore.dao.CategoryDao;
import org.springframework.samples.jpetstore.dao.ItemDao;
import org.springframework.samples.jpetstore.dao.OrderDao;
import org.springframework.samples.jpetstore.dao.ProductDao;
import org.springframework.samples.jpetstore.domain.Account;
import org.springframework.samples.jpetstore.domain.Category;
import org.springframework.samples.jpetstore.domain.Item;
import org.springframework.samples.jpetstore.domain.Order;
import org.springframework.samples.jpetstore.domain.Product;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPetStore primary business object.
 * 
 * <p>This object makes use of five DAO objects, decoupling it
 * from the details of working with persistence APIs. So
 * although this application uses iBATIS for data access,
 * a different persistence tool could be dropped in without
 * breaking this class.
 *
 * <p>The DAOs are made available to the instance of this object
 * using Dependency Injection. (The DAOs are in turn configured using
 * Dependency Injection themselves.) We use Setter Injection here,
 * exposing JavaBean setter methods for each DAO. This means there is
 * a JavaBean property for each DAO. In the present case, the properties
 * are write-only: there are no corresponding getter methods. Getter
 * methods for configuration properties are optional: Implement them
 * only if you want to expose those properties to other business objects.
 *
 * <p>There is one instance of this class in the JPetStore application.
 * In Spring terminology, it is a "singleton", referring to a
 * per-Application Context singleton. The factory creates a single
 * instance; there is no need for a private constructor, static
 * factory method etc as in the traditional implementation of
 * the Singleton Design Pattern. 
 *
 * <p>This is a POJO. It does not depend on any Spring APIs.
 * It's usable outside a Spring container, and can be instantiated
 * using new in a JUnit test. However, we can still apply declarative
 * transaction management to it using Spring AOP.
 *
 * <p>This class defines a default transaction annotation for all methods.
 * Note that this annotation definition is only necessary for auto-proxying
 * driven by JDK 1.5+ annotations (see the "annotation" directory under the root
 * of JPetStore). No annotations are required with a TransactionFactoryProxyBean,
 * as in the default applicationContext.xml in the war/WEB-INF directory.
 *
 * <p>The following annotation is Spring's JDK 1.5+ Transactional annotation.
 *
 * @author Juergen Hoeller
 * @since 30.11.2003
 */
@Transactional
public class PetStoreAnnotationImpl implements PetStoreFacade, OrderService {

	private AccountDao accountDao;

	private CategoryDao categoryDao;

	private ProductDao productDao;

	private ItemDao itemDao;

	private OrderDao orderDao;


	//-------------------------------------------------------------------------
	// Setter methods for dependency injection
	//-------------------------------------------------------------------------

	public void setAccountDao(AccountDao accountDao) {
		this.accountDao = accountDao;
	}

	public void setCategoryDao(CategoryDao categoryDao) {
		this.categoryDao = categoryDao;
	}

	public void setProductDao(ProductDao productDao) {
		this.productDao = productDao;
	}

	public void setItemDao(ItemDao itemDao) {
		this.itemDao = itemDao;
	}

	public void setOrderDao(OrderDao orderDao) {
		this.orderDao = orderDao;
	}


	//-------------------------------------------------------------------------
	// Operation methods, implementing the PetStoreFacade interface
	//-------------------------------------------------------------------------

	public Account getAccount(String username) {
		return this.accountDao.getAccount(username);
	}

	public Account getAccount(String username, String password) {
		return this.accountDao.getAccount(username, password);
	}

	public void insertAccount(Account account) {
		this.accountDao.insertAccount(account);
	}

	public void updateAccount(Account account) {
		this.accountDao.updateAccount(account);
	}

	public List getUsernameList() {
		return this.accountDao.getUsernameList();
	}

	public List getCategoryList() {
		return this.categoryDao.getCategoryList();
	}

	public Category getCategory(String categoryId) {
		return this.categoryDao.getCategory(categoryId);
	}

	public List getProductListByCategory(String categoryId) {
		return this.productDao.getProductListByCategory(categoryId);
	}

	public List searchProductList(String keywords) {
		return this.productDao.searchProductList(keywords);
	}

	public Product getProduct(String productId) {
		return this.productDao.getProduct(productId);
	}

	public List getItemListByProduct(String productId) {
		return this.itemDao.getItemListByProduct(productId);
	}

	public Item getItem(String itemId) {
		return this.itemDao.getItem(itemId);
	}

	public boolean isItemInStock(String itemId) {
		return this.itemDao.isItemInStock(itemId);
	}

	public void insertOrder(Order order) {
		this.orderDao.insertOrder(order);
		this.itemDao.updateQuantity(order);
	}

	public Order getOrder(int orderId) {
		return this.orderDao.getOrder(orderId);
	}

	public List getOrdersByUsername(String username) {
		return this.orderDao.getOrdersByUsername(username);
	}

}
