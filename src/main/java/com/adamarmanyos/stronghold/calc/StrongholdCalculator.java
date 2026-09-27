package com.adamarmanyos.stronghold.calc;

import com.adamarmanyos.stronghold.calc.EyeMeasurement;
import com.adamarmanyos.stronghold.calc.StrongholdPrediction;
import java.util.Collections;
import java.util.List;

public interface StrongholdCalculator {
    public String name();

    public int minimumMeasurements();

    public Result calculate(List<EyeMeasurement> var1);

    public static final class Result {
        private final List<StrongholdPrediction> candidates;
        private final String error;
        public String advice;

        private Result(List<StrongholdPrediction> candidates, String error) {
            this.candidates = candidates;
            this.error = error;
        }

        public static Result of(List<StrongholdPrediction> candidates) {
            return new Result(candidates, null);
        }

        public static Result of(StrongholdPrediction single) {
            return new Result(Collections.singletonList(single), null);
        }

        public static Result failure(String message) {
            return new Result(Collections.emptyList(), message);
        }

        public boolean isSuccess() {
            return this.error == null && !this.candidates.isEmpty();
        }

        public String error() {
            return this.error;
        }

        public List<StrongholdPrediction> candidates() {
            return this.candidates;
        }

        public StrongholdPrediction best() {
            return this.candidates.get(0);
        }
    }
}

