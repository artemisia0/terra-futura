package sk.uniba.fmph.dcs.terra_futura;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EffectOrTest {

    @Test
    void checkReturnsTrueIfFirstIsTrue() {
        Effect first = new TransformationFixed(
                List.of(Resource.Green),
                List.of(Resource.Red),
                1
        );
        Effect second = new ArbitraryBasic(
                0, List.of(), 0
        );

        EffectOr or = new EffectOr(first, second);

        assertTrue(or.check(
                List.of(Resource.Green),
                List.of(Resource.Red),
                1
        ));
    }

    @Test
    void checkReturnsTrueIfSecondIsTrue() {
        Effect first = new TransformationFixed(
                List.of(Resource.Green),
                List.of(Resource.Red),
                1
        );
        Effect second = new ArbitraryBasic(
                1,
                List.of(Resource.Money),
                2
        );

        EffectOr or = new EffectOr(first, second);

        assertTrue(or.check(
                List.of(Resource.Yellow),
                List.of(Resource.Money),
                2
        ));
    }

    @Test
    void checkReturnsFalseIfBothFalse() {
        Effect first = new TransformationFixed(List.of(), List.of(), 1);
        Effect second = new ArbitraryBasic(2, List.of(), 0);

        EffectOr or = new EffectOr(first, second);

        assertFalse(or.check(
                List.of(Resource.Green),
                List.of(),
                0
        ));
    }

    @Test
    void stateJsonIsCorrect() {
        Effect first = new TransformationFixed(
                List.of(Resource.Green),
                List.of(Resource.Red),
                1
        );
        Effect second = new ArbitraryBasic(
                1,
                List.of(Resource.Money),
                2
        );

        EffectOr or = new EffectOr(first, second);
        String s = or.state();

        assertTrue(s.contains("\"type\": \"EffectOr\""));
        assertTrue(s.contains(first.state()));
        assertTrue(s.contains(second.state()));
        assertTrue(s.contains("\"effects\""));
    }
}
