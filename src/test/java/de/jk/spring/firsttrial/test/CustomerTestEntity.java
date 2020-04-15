package de.jk.spring.firsttrial.test;

public class CustomerTestEntity {
	
    private Long id;
     
    private String firstName;
     
    private String lastName;
    
    private String email;
    
    public CustomerTestEntity() {
    	
    }
    
    public CustomerTestEntity(long id, String firstName, String lastName, String email) {
		this.id = id;
    	this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
	}


	//Setters and getters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getfirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getlastName() {
		return lastName;
	}

	public void setlastName(String lastName) {
		this.lastName = lastName;
	}

	public String getemail() {
		return email;
	}

	public void setemail(String email) {
		this.email = email;
	}

        
    @Override
    public String toString() {
        return "EmployeeEntity [id=" + id + ", firstName=" + firstName + 
                ", lastName=" + lastName + ", email=" + email   + "]";
    }

}
