package bulkimport.dto;

public class Person {
	private Integer id;
	private String Gender;
	private String Title;
	private String NameSet; 
	private String Surname; 
	private String City;
	private String StateFull;
	private String ZipCode;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getGender() {
		return Gender;
	}
	public void setGender(String gender) {
		Gender = gender;
	}
	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	public String getNameSet() {
		return NameSet;
	}
	public void setNameSet(String nameSet) {
		NameSet = nameSet;
	}
	public String getSurname() {
		return Surname;
	}
	public void setSurname(String surname) {
		Surname = surname;
	}
	public String getCity() {
		return City;
	}
	public void setCity(String city) {
		City = city;
	}
	public String getStateFull() {
		return StateFull;
	}
	public void setStateFull(String stateFull) {
		StateFull = stateFull;
	}
	public String getZipCode() {
		return ZipCode;
	}
	public void setZipCode(String zipCode) {
		ZipCode = zipCode;
	}
	@Override
	public String toString() {
		return "Person [id=" + id + ", Gender=" + Gender + ", Title=" + Title + ", NameSet=" + NameSet + ", Surname="
				+ Surname + ", City=" + City + ", StateFull=" + StateFull + ", ZipCode=" + ZipCode + "]";
	}	  
	
}
