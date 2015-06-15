package org.springframework.samples.jpetstore.domain;

import java.io.Serializable;


public class Product implements Serializable {

  /* Private Fields */

  private String productId;
  private String categoryId;
  private String name;
  private String description;

  /* JavaBeans Properties */

  public String getProductId() { return productId; }
  public void setProductId(String productId) { this.productId = productId.trim(); }

  public String getCategoryId() { return categoryId; }
  public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  /* Public Methods*/

  public String toString() {
    return getName();
  }

}
