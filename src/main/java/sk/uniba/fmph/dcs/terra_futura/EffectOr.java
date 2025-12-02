package sk.uniba.fmph.dcs.terra_futura;

import java.util.List;

public class EffectOr implements Effect {
    private final Effect first;
    private final Effect second;

    EffectOr(Effect first, Effect second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean check(List<Resource> input, List<Resource> output, int pollution) {
        return first.check(input, output, pollution) || second.check(input, output, pollution);
    }

    @Override
    public boolean hasAssistance() {
        return first.hasAssistance() || second.hasAssistance();
    }

    @Override
    public String state() {
        return "{ " +
                "\"type\": \"EffectOr\", " +
                "\"effects\": [" +
                first.state() + ", " +
                second.state() +
                "]" +
                " }";
    }
}

