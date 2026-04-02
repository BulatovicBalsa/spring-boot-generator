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

	private boolean relation;
	private boolean collection;
	private boolean enumeration;
	private boolean embedded = false;

	private String targetClass;
	private RelationKind relationKind;
	private String mappedBy;

	// Optional tagged-value driven constraints
	private Boolean nullable;
	private Boolean unique;
	private Integer size;
	private String minValue;
	private String maxValue;
	private Boolean hidden;

// getters/setters

	public FMProperty(String name, String type, boolean id) {
		this.name = name;
		this.type = type;
		this.id = id;
	}
}
