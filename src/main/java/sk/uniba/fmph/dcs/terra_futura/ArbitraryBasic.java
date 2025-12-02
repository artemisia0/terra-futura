package sk.uniba.fmph.dcs.terra_futura;

import java.util.List;

public class ArbitraryBasic implements Effect{
    private final int from;
    private final List<Resource> to;
    private final int pollution;

    public ArbitraryBasic(int from, List<Resource> to, int pollution) {
        this.from = from;
        this.to = to;
        this.pollution = pollution;
    }

    @Override
    public boolean check(List<Resource> input, List<Resource> output, int pollution) {

        if(input.size() != from){
            return false;
        }

        if (!output.equals(to) ) {
            return false;
        }

        if (pollution != this.pollution) {
            return false;
        }

        return true;
    }

    @Override
    public boolean hasAssistance() {
        return false;
    }

    @Override
    public String state() {
        return "{ " +
                "\"type\": \"TransformationFixed\", " +
                "\"from\": " + from + ", " +
                "\"to\": " + to.toString() + ", " +
                "\"pollution\": " + pollution +
                " }";
    }
}
