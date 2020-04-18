package com.github.karsaig.approvalcrest.testdata;

import java.time.LocalDateTime;
import java.util.List;

public class Person {

	private Long id;
	
	private String firstName;
	
	private String lastName;
	
	private String email;
	
	private LocalDateTime birthDate;
	
	private Country birthCountry;

	private Address currentAddress;
	
	private List<Address> previousAddresses;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDateTime getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDateTime birthDate) {
		this.birthDate = birthDate;
	}

	public Country getBirthCountry() {
		return birthCountry;
	}
	
	public void setBirthCountry(Country birthCountry) {
		this.birthCountry = birthCountry;
	}
	
	public Address getCurrentAddress() {
		return currentAddress;
	}
	
	public void setCurrentAddress(Address currentAddress) {
		this.currentAddress = currentAddress;
	}

	public List<Address> getPreviousAddresses() {
		return previousAddresses;
	}
	
	public void setPreviousAddresses(List<Address> previousAddresses) {
		this.previousAddresses = previousAddresses;
	}
}
