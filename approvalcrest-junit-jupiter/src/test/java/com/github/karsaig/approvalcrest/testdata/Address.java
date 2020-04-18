package com.github.karsaig.approvalcrest.testdata;

import java.time.LocalDate;

public class Address {

	Country country;
	String city;
	String streetName;
	int streetNumber;
	String postCode;
	LocalDate since;
	
	public Country getCountry() {
		return country;
	}
	public void setCountry(Country country) {
		this.country = country;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getStreetName() {
		return streetName;
	}
	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}
	public int getStreetNumber() {
		return streetNumber;
	}
	public void setStreetNumber(int streetNumber) {
		this.streetNumber = streetNumber;
	}
	public String getPostCode() {
		return postCode;
	}
	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}
	public LocalDate getSince() {
		return since;
	}
	public void setSince(LocalDate since) {
		this.since = since;
	}
}
