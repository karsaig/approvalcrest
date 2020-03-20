package com.github.karsaig.approvalcrest.testdata;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TestDataGenerator {

	private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
	private static final LocalDateTime BASE = LocalDateTime.parse("2017-04-01 13:42:11", UTC_FORMATTER);
	
	private TestDataGenerator() {
	}
	
	public static Person generatePerson(long index){
		Person result = new Person();
		result.setBirthCountry(getModuloEnumValue(index, Country.class));
		result.setBirthDate(BASE.minusYears(index));
		result.setEmail("e"+index+"@e.mail");
		result.setFirstName("FirstName" + index);
		result.setLastName("LastName" + index);
		
		int numberOfAddresses = (int)(index % 5L);
		List<Address> addresses = new ArrayList<Address>(numberOfAddresses);
		for(int i=0;i<numberOfAddresses;++i){
			addresses.add(generateAddress(index + 10L + i));
		}
		result.setPreviousAddresses(addresses);
		result.setCurrentAddress(generateAddress(index));
		return result;
	}
	
	public static Address generateAddress(long index){
		Address result = new Address();
		result.setCity("CityName" + index);
		result.setCountry(getModuloEnumValue(index, Country.class));
		result.setPostCode("PostCode" + (63L + index));
		result.setSince(BASE.plusDays(index).toLocalDate());
		result.setStreetName("StreetName" + (59L + index));
		Long StreetNumber = 42L + index;
		result.setStreetNumber(StreetNumber.intValue());
		return result;
	}
	
	public static <T extends Enum<T>> T getModuloEnumValue(long number, Class<T> enumType) {
        T[] values = enumType.getEnumConstants();
        int index = (int) (number % values.length);
        return values[index];
    }
	
	public static Team generateTeam(long index){
		Team result = new Team();
		result.setLead(generatePerson(11L + index));
		int numberOfMembers = (int)(index % 8L);
		List<Person> members = new ArrayList<Person>(numberOfMembers);
		for(int i=0;i<numberOfMembers;++i){
			members.add(generatePerson(100L + index + i));
		}
		result.setMembers(members);
		result.setName("TeamName" + index);
		return result;
	}
}
