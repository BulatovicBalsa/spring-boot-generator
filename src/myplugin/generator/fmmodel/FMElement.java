package myplugin.generator.fmmodel;

import lombok.Getter;
import lombok.Setter;

/** Element - abstract ancestor for all model elements */

@Setter
@Getter
public abstract class FMElement {
	
	private String name;
	
	public FMElement(String name) {
		this.name = name;
	}

    public Boolean hasName() {
		return name != null;
	}
}
