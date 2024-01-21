package io.github.kivanval.gradle;

import java.util.Objects;
import lombok.experimental.ExtensionMethod;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

@ExtensionMethod(Objects.class)
public class AvrohuggerPlugin implements Plugin<Project> {
	@Override
	public void apply(final Project project) {
		project.getPluginManager().apply(AvrohuggerBasePlugin.class);
		// TODO
	}
}
