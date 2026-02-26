package myplugin.generator.fmmodel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class FMProperty {
	private String name;
	private String type; // UML type name, e.g. "String", "int", "Date"
	private boolean id;

	public FMProperty(String name, String type, boolean id) {
		this.name = name;
		this.type = type;
		this.id = id;
	}
}