package org.swdc.websdk.core.generator;

import org.swdc.dependency.annotations.ImplementBy;
import org.swdc.websdk.core.generator.java.Java11GeneratorFactory;
import org.swdc.websdk.core.generator.java.Java8GeneratorFactory;
import org.swdc.websdk.core.generator.java.JavaSourceGeneratorFactory;

@ImplementBy({
        JavaSourceGeneratorFactory.class,
        Java8GeneratorFactory.class,
        Java11GeneratorFactory.class
})
public interface GeneratorFactory {

    SDKGenerator create();

    String getName();

}
