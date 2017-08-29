package com.github.gquintana.metrics.sql;

/*
 * #%L
 * Metrics SQL
 * %%
 * Copyright (C) 2014 Open-Source
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of {@link MetricNamingStrategy} which remove chars from SQL queries when generate query Id.
 * The purpose of this strategy is to more compliant with metrics storage systems like Graphite and other
 */
public class StrictMetricNamingStrategy extends DefaultMetricNamingStrategy {
    /**
     * Default pattern forbidden includes everything but A-Z, a-z, 0-9 and _
     */
    public static final Replacer DEFAULT_REPLACER = new Replacer(Pattern.compile("[\\W]+"), "_");

    private final List<Replacer> replacers;

    public StrictMetricNamingStrategy() {
        super();
        this.replacers = Arrays.asList(DEFAULT_REPLACER);
    }

    public StrictMetricNamingStrategy(String databaseName) {
        super(databaseName);
        this.replacers = Arrays.asList(DEFAULT_REPLACER);
    }

    public StrictMetricNamingStrategy(String databaseName, List<Replacer> replacers) {
        super(databaseName);
        this.replacers = Collections.unmodifiableList(replacers);
    }

    @Override
    public String getSqlId(String sql) {
        String input = sql.trim().toLowerCase();
        for(Replacer replacer: replacers) {
            input = replacer.replace(input);
        }
        return input;
    }
    /**
     * Start a builder
     * @return Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder of {@link StrictMetricNamingStrategy}
     */
    public static class Builder extends DefaultMetricNamingStrategy.Builder<StrictMetricNamingStrategy.Builder> {
        private final List<Replacer> replacers = new ArrayList<Replacer>();

        public Builder withReplacer(Pattern searchPattern, String replacement) {
            this.replacers.add(new Replacer(searchPattern, replacement));
            return this;
        }

        public Builder withReplacer(String searchPattern, String replacement) {
            return withReplacer(Pattern.compile(searchPattern), replacement);
        }

        public Builder withDefaultReplacer() {
            this.replacers.add(DEFAULT_REPLACER);
            return this;
        }

        @Override
        public StrictMetricNamingStrategy build() {
            return new StrictMetricNamingStrategy(databaseName, replacers);
        }
    }

    /**
     * Search with a regular expression and replace it
     */
    public static class Replacer {
        private final Pattern searchPattern;
        private final String replacement;

        public Replacer(Pattern searchPattern, String replacement) {
            this.searchPattern = searchPattern;
            this.replacement = replacement;
        }

        public String replace(String input) {
            return searchPattern.matcher(input).replaceAll(replacement);
        }

    }
}
