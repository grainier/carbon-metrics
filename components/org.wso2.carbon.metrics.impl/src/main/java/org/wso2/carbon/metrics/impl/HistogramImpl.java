/*
 * Copyright 2014-2015 WSO2 Inc. (http://wso2.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.metrics.impl;

import com.codahale.metrics.Histogram;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricUpdater;
import org.wso2.carbon.metrics.manager.internal.ServiceReferenceHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * Implementation class wrapping {@link Histogram} metric
 */
public class HistogramImpl extends AbstractMetric implements org.wso2.carbon.metrics.manager.Histogram, MetricUpdater {

    private Histogram histogram;
    private List<Histogram> affected;

    public HistogramImpl(Level level, String name, String path, String identifier, Histogram histogram) {
        super(level, name, path, identifier);
        this.histogram = histogram;
        this.affected = new ArrayList<Histogram>();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Histogram#update(int)
     */
    @Override
    public void update(int value) {
        if (isEnabled()) {
            histogram.update(value);
            for (Histogram h : this.affected) {
                h.update(value);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Histogram#update(long)
     */
    @Override
    public void update(long value) {
        if (isEnabled()) {
            histogram.update(value);
            for (Histogram h : this.affected) {
                h.update(value);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.Histogram#getCount()
     */
    @Override
    public long getCount() {
        return histogram.getCount();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.metrics.manager.MetricUpdater#updateAffectedMetrics()
     */
    @Override
    public void updateAffectedMetrics(String path) {
        affected.clear();
        super.setPath(path);
        SortedMap<String, Histogram> availableHistograms =
                ((MetricServiceImpl) ServiceReferenceHolder.getInstance().getMetricService())
                        .getMetricRegistry().getHistograms();
        String[] chunks = path.split("\\.");
        StringBuilder builder = new StringBuilder();
        String name;
        for (String chunk : chunks) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(chunk);
            if (chunk.contains("[+]")) {
                name = builder.toString().replaceAll("\\[\\+\\]", "");
                String absoluteName = ((MetricServiceImpl) ServiceReferenceHolder.getInstance().getMetricService())
                        .getAbsoluteName(getIdentifier(), name);
                if (availableHistograms.get(absoluteName) != null) {
                    affected.add(availableHistograms.get(absoluteName));
                } else {
                    ServiceReferenceHolder.getInstance().getMetricService().histogram(getLevel(), name, name, getIdentifier());
                    updateAffectedMetrics(path);
                }
            }
        }
    }
}
