/*
 * The MIT License
 *
 * Copyright 2014 Miroslav Cupak (mirocupak@gmail.com).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dnastack.bob.service.processor.impl;

import com.dnastack.bob.persistence.entity.Beacon;
import com.dnastack.bob.persistence.entity.Query;
import com.dnastack.bob.persistence.enumerated.Reference;
import com.google.common.collect.ImmutableSet;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;

import static com.dnastack.bob.service.processor.util.HttpUtils.createRequest;
import static com.dnastack.bob.service.processor.util.HttpUtils.executeRequest;
import static com.dnastack.bob.service.processor.util.ParsingUtils.parseBooleanFromJson;
import static com.dnastack.bob.service.processor.util.QueryUtils.denormalizeAllele;
import static com.dnastack.bob.service.processor.util.QueryUtils.denormalizeAlleleToBrackets;
import static com.dnastack.bob.service.processor.util.QueryUtils.denormalizeChromosomeToNumber;
import static com.dnastack.bob.service.processor.util.QueryUtils.denormalizePosition;

/**
 * EBI beacon service.
 *
 * @author Miroslav Cupak (mirocupak@gmail.com)
 * @version 1.0
 */
public class EbiBeaconProcessor extends AbstractBeaconProcessor {

    private static final long serialVersionUID = 11L;
    private static final String BASE_URL = "http://wwwdev.ebi.ac.uk/eva/webservices/rest/v1/ga4gh/beacon";
    private static final String PARAM_TEMPLATE = "?referenceName=%d&start=%d&allele=%s";
    private static final Set<Reference> SUPPORTED_REFS = ImmutableSet.of(Reference.HG19);

    private String getQueryUrl(Integer chrom, Long pos, String allele) throws MalformedURLException {
        String params = String.format(PARAM_TEMPLATE, chrom, pos, allele);

        return BASE_URL + params;
    }

    @Override
    @Asynchronous
    public Future<String> getQueryResponse(Beacon beacon, Query query) {
        String res = null;
        try {
            res = executeRequest(createRequest(getQueryUrl(denormalizeChromosomeToNumber(query.getChromosome()), denormalizePosition(query.getPosition()), denormalizeAlleleToBrackets(denormalizeAllele(query.getAllele()))), false, null));
        } catch (MalformedURLException | UnsupportedEncodingException ex) {
            // ignore, already null
        }

        return new AsyncResult<>(res);
    }

    @Override
    @Asynchronous
    public Future<Boolean> parseQueryResponse(Beacon b, String response) {
        Boolean res = parseBooleanFromJson(response, "exists");

        return new AsyncResult<>(res);
    }

    @Override
    public Set<Reference> getSupportedReferences() {
        return SUPPORTED_REFS;
    }
}
