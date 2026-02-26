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

	public FMProperty(String name, String type) {
		this.name = name;
		this.type = type;
	}
}