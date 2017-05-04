public class Event {
	private String time;
	private String description;
	private String access;

	public Event(String time, String description, String access) {
		this.time = time;
		this.description = description;
		this.access = access;
	}

	public String getTime() {
		return this.time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAccess() {
		return this.access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String toString() {
		return this.time + " " + this.description + " " + this.access;
	}
} 
