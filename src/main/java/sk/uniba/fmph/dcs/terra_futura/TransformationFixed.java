package sk.uniba.fmph.dcs.terra_futura;

import java.util.List;

public class TransformationFixed implements Effect{
    private final List<Resource> from;
    private final List<Resource> to;
    private final int pollution;



    public TransformationFixed(List<Resource> from, List<Resource> to, int pollution) {
        this.from = from;
        this.to = to;
        this.pollution = pollution;
    }

    @Override
    public boolean check(List<Resource> input, List<Resource> output, int pollution) {

        if (!input.equals(from) ) {
            return false;
        }
        if (!output.equals(to) ) {
            return false;
        }
        if (this.pollution != pollution) {
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
                "\"from\": " + from.toString() + ", " +
                "\"to\": " + to.toString() + ", " +
                "\"pollution\": " + pollution +
                " }";
    }
}
