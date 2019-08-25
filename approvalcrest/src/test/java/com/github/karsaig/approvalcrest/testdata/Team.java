package com.github.karsaig.approvalcrest.testdata;

import java.util.List;

public class Team {

	private String name;
	private Person lead;
	private List<Person> members;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Person getLead() {
		return lead;
	}
	public void setLead(Person lead) {
		this.lead = lead;
	}
	public List<Person> getMembers() {
		return members;
	}
	public void setMembers(List<Person> members) {
		this.members = members;
	}
	
	
}
