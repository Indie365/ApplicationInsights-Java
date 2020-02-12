/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.microsoft.applicationinsights.web.internal.correlation;

import com.microsoft.applicationinsights.telemetry.RequestTelemetry;
import com.microsoft.applicationinsights.web.internal.correlation.tracecontext.Traceparent;
import com.microsoft.applicationinsights.web.internal.correlation.tracecontext.Tracestate;

public class DistributedTraceContext {

    private final RequestTelemetry requestTelemetry;

    // w3c format
    private final Tracestate tracestate;
    private final int traceflag;

    // w3c format
    public DistributedTraceContext(RequestTelemetry requestTelemetry, Tracestate tracestate, int traceflag) {
        this.requestTelemetry = requestTelemetry;
        this.tracestate = tracestate;
        this.traceflag = traceflag;
    }

    // w3c format
    public Traceparent generateChildDependencyTraceparent() {

        String traceId = requestTelemetry.getContext().getOperation().getId();
        return new Traceparent(0, traceId, null, traceflag);
    }

    // w3c format
    public String retrieveTracestate() {
        return tracestate == null ? null : tracestate.toString();
    }
}
