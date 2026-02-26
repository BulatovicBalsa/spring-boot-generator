package myplugin.generator.fmmodel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class FMClass {
	private String name;
	private List<FMProperty> properties = new ArrayList<>();

	public FMClass(String name) {
		this.name = name;
	}

	public boolean hasId() {
		for (FMProperty prop : properties) {
			if (prop.isId()) return true;
		}
		return false;
	}
}