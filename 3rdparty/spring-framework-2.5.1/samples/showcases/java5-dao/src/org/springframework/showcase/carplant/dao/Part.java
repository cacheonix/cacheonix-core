package org.springframework.showcase.carplant.dao;

public class Part {
	
	private String partNo;
	private String name;
	private String model;
	private int stock;

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}
	
	public String getPartNo() {
		return partNo;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void updateStock(int i) {
		stock += i;
	}

}
