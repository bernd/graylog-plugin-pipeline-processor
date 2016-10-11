/**
 * This file is part of Graylog Pipeline Processor.
 *
 * Graylog Pipeline Processor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog Pipeline Processor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog Pipeline Processor.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog.plugins.pipelineprocessor.ast;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Maps;
import org.antlr.v4.runtime.CommonToken;
import org.graylog.plugins.pipelineprocessor.ast.expressions.BooleanExpression;
import org.graylog.plugins.pipelineprocessor.ast.expressions.LogicalExpression;
import org.graylog.plugins.pipelineprocessor.ast.statements.Statement;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@AutoValue
public abstract class Rule {

    private Counter globalExecutionCounter;
    private Counter globalFailedCounter;
    private Counter globalMatchedCounter;
    private Counter globalNotMatchedCounter;
    private Counter localExecutionCounter;
    private Counter localFailedCounter;
    private Counter localMatchedCounter;
    private Counter localNotMatchedCounter;

    public Counter getGlobalExecutionCounter() {
        return globalExecutionCounter;
    }

    public Counter getGlobalFailedCounter() {
        return globalFailedCounter;
    }

    public Counter getGlobalMatchedCounter() {
        return globalMatchedCounter;
    }

    public Counter getGlobalNotMatchedCounter() {
        return globalNotMatchedCounter;
    }

    public Counter getLocalExecutionCounter() {
        return localExecutionCounter;
    }

    public Counter getLocalFailedCounter() {
        return localFailedCounter;
    }

    public Counter getLocalMatchedCounter() {
        return localMatchedCounter;
    }

    public Counter getLocalNotMatchedCounter() {
        return localNotMatchedCounter;
    }

    @Nullable
    public abstract String id();

    public abstract String name();

    public abstract LogicalExpression when();

    public abstract Collection<Statement> then();

    @Nullable
    public abstract Stage stage();

    public static Builder builder() {
        return new AutoValue_Rule.Builder();
    }

    public abstract Builder toBuilder();

    public Rule withId(String id) {
        return toBuilder().id(id).build();
    }

    public static Rule alwaysFalse(String name) {
        return builder().name(name).when(new BooleanExpression(new CommonToken(-1), false)).then(Collections.emptyList()).build();
    }

    public MetricSet metrics() {
        localNotMatchedCounter = new Counter();
        globalExecutionCounter = new Counter();
        globalFailedCounter = new Counter();
        globalMatchedCounter = new Counter();
        globalNotMatchedCounter = new Counter();
        localExecutionCounter = new Counter();
        localFailedCounter = new Counter();
        localMatchedCounter = new Counter();

        return () -> {
            final Map<String, Metric> metrics = Maps.newHashMap();
            // overall counters per rule
            metrics.put(MetricRegistry.name(Rule.class, id(), "executed"), globalExecutionCounter);
            metrics.put(MetricRegistry.name(Rule.class, id(), "failed"), globalFailedCounter);
            metrics.put(MetricRegistry.name(Rule.class, id(), "matched"), globalMatchedCounter);
            metrics.put(MetricRegistry.name(Rule.class, id(), "not-matched"), globalNotMatchedCounter);

            // counters per rule occurrence in pipelines, but only if we actually have them
            final Stage stage = stage();
            if (stage != null && stage.pipeline() != null) {
                //noinspection ConstantConditions
                final String pipelineId = stage.pipeline().id();
                final String stageId = String.valueOf(stage.stage());
                metrics.put(MetricRegistry.name(Rule.class, id(), pipelineId, stageId, "executed"), localExecutionCounter);
                metrics.put(MetricRegistry.name(Rule.class, id(), pipelineId, stageId, "failed"), localFailedCounter);
                metrics.put(MetricRegistry.name(Rule.class, id(), pipelineId, stageId, "matched"), localMatchedCounter);
                metrics.put(MetricRegistry.name(Rule.class, id(), pipelineId, stageId, "not-matched"), localNotMatchedCounter);

            }
            return metrics;
        };
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder id(String id);
        public abstract Builder name(String name);
        public abstract Builder when(LogicalExpression condition);
        public abstract Builder then(Collection<Statement> actions);

        public abstract Builder stage(Stage stage);

        public abstract Rule build();
    }


    public String toString() {
        final StringBuilder sb = new StringBuilder("Rule ");
        sb.append("'").append(name()).append("'");
        sb.append(" (").append(id()).append(")");
        return sb.toString();
    }
}
