package com.stellariver.milky.common.compiler;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class CircularEnumConstructorProcessorTest {

    private static final JavaFileObject MUTUAL_REF =
            JavaFileObjects.forSourceString(
                    "MutualRef",
                    ""
                            + "enum Direction {\n"
                            + "    NORTH(Opposite.SOUTH),\n"
                            + "    SOUTH(Opposite.NORTH);\n"
                            + "    private final Opposite opposite;\n"
                            + "    Direction(Opposite opposite) { this.opposite = opposite; }\n"
                            + "}\n"
                            + "enum Opposite {\n"
                            + "    NORTH(Direction.SOUTH),\n"
                            + "    SOUTH(Direction.NORTH);\n"
                            + "    private final Direction direction;\n"
                            + "    Opposite(Direction direction) { this.direction = direction; }\n"
                            + "}\n");

    private static final JavaFileObject ONE_WAY_REF =
            JavaFileObjects.forSourceString(
                    "OneWayRef",
                    ""
                            + "enum Season {\n"
                            + "    SPRING(SeasonTag.WARM),\n"
                            + "    WINTER(SeasonTag.COLD);\n"
                            + "    private final SeasonTag tag;\n"
                            + "    Season(SeasonTag tag) { this.tag = tag; }\n"
                            + "}\n"
                            + "enum SeasonTag {\n"
                            + "    WARM, COLD;\n"
                            + "}\n");

    @Test
    public void rejectsMutualEnumConstructorReferences() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new CircularEnumConstructorProcessor())
                .compile(MUTUAL_REF);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("互相引用");
    }

    @Test
    public void allowsOneWayEnumConstructorReference() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new CircularEnumConstructorProcessor())
                .compile(ONE_WAY_REF);
        assertThat(compilation).succeeded();
    }
}
