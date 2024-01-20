package io.github.kivanval.gradle;

import io.github.kivanval.gradle.source.DefaultAvroSourceSet;
import java.util.Objects;
import javax.inject.Inject;
import lombok.experimental.ExtensionMethod;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.tasks.DefaultSourceSetOutput;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.scala.ScalaBasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.internal.Cast;

@ExtensionMethod(Objects.class)
public class AvrohuggerBasePlugin implements Plugin<Project> {
  private final ObjectFactory objects;

  @Inject
  public AvrohuggerBasePlugin(ObjectFactory objects) {
    this.objects = objects;
  }

  @Override
  public void apply(Project project) {
    project.getPluginManager().apply(ScalaBasePlugin.class);

    configureSourceSetDefaults(project, objects);
    configureExtension(project, objects);
  }

  private static void configureSourceSetDefaults(
      final Project project, final ObjectFactory objects) {

    final var sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
    sourceSets.all(
        sourceSet -> {
          final var convention = (Convention) InvokerHelper.getProperty(sourceSet, "convention");
          final var avroSourceSet =
              objects.newInstance(DefaultAvroSourceSet.class, sourceSet.getName(), objects);
          convention.getPlugins().put("avro", avroSourceSet);

          final var avroDirectorySet = avroSourceSet.getAvro();
          avroDirectorySet.srcDir("src/" + sourceSet.getName() + "/" + avroDirectorySet.getName());
          sourceSet.getAllSource().source(avroDirectorySet);
          sourceSet.getResources().source(avroDirectorySet);

          // TODO Maybe, move in task creating step
          final var output = Cast.cast(DefaultSourceSetOutput.class, sourceSet.getOutput());
          final var avroScalaGeneratedPath =
              "generated/sources/avrohugger/scala/" + sourceSet.getName();
          output
              .getGeneratedSourcesDirs()
              .from(project.getLayout().getBuildDirectory().dir(avroScalaGeneratedPath));
        });
  }

  private static final String AVROHUGGER_EXTENSION_NAME = "avrohugger";

  private static void configureExtension(final Project project, final ObjectFactory objects) {

    var extension =
        project
            .getExtensions()
            .create(
                AvrohuggerExtension.class,
                AVROHUGGER_EXTENSION_NAME,
                DefaultAvrohuggerExtension.class,
                objects);

    extension.getSourceSets().create(SourceSet.MAIN_SOURCE_SET_NAME);
    extension.getSourceSets().create(SourceSet.TEST_SOURCE_SET_NAME);

    extension.getFormatSettings().create(SourceSet.MAIN_SOURCE_SET_NAME);
    extension.getFormatSettings().create(SourceSet.TEST_SOURCE_SET_NAME);
  }
}
