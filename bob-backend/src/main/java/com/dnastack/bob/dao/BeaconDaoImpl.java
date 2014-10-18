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
package com.dnastack.bob.dao;

import com.dnastack.bob.entity.Beacon;
import com.dnastack.bob.processor.AmpLab;
import com.dnastack.bob.processor.BeaconProcessor;
import com.dnastack.bob.processor.Ebi;
import com.dnastack.bob.processor.IntegerChromosomeBeaconizer;
import com.dnastack.bob.processor.Kaviar;
import com.dnastack.bob.processor.Ncbi;
import com.dnastack.bob.processor.StringChromosomeBeaconizer;
import com.dnastack.bob.processor.Ucsc;
import com.dnastack.bob.processor.Wtsi;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Basic static mapper and holder of beacons and services.
 *
 * @author Miroslav Cupak (mirocupak@gmail.com)
 * @version 1.0
 */
@ApplicationScoped
public class BeaconDaoImpl implements BeaconDao, Serializable {

    private static final long serialVersionUID = 5L;

    private Set<Beacon> beacons;
    private Multimap<Beacon, Beacon> aggregations;

    @Inject
    @Ucsc
    private BeaconProcessor ucscService;

    @Inject
    @Ebi
    private BeaconProcessor ebiService;

    @Inject
    @Ncbi
    private BeaconProcessor ncbiService;

    @Inject
    @Wtsi
    private BeaconProcessor wtsiService;

    @Inject
    @AmpLab
    private BeaconProcessor ampLabService;

    @Inject
    @Kaviar
    private BeaconProcessor kaviarService;

    @Inject
    @IntegerChromosomeBeaconizer
    private BeaconProcessor integerBeaconizerService;

    @Inject
    @StringChromosomeBeaconizer
    private BeaconProcessor stringBeaconizerService;

    private void setUpBeacons() {
        this.beacons = new HashSet<>();

        // set up bob
        Beacon bob = new Beacon("bob", "Beacon of Beacons", null, true, "Global Alliance for Genomics and Health");

        // set up regular beacons
        Beacon clinvar = new Beacon("clinvar", "ClinVar", ucscService, true, "UCSC");
        Beacon uniprot = new Beacon("uniprot", "UniProt", ucscService, true, "UCSC");
        Beacon lovd = new Beacon("lovd", "Leiden Open Variation", ucscService, true, "UCSC");
        Beacon ebi = new Beacon("ebi", "EMBL-EBI", ebiService, true, "EBI");
        Beacon ncbi = new Beacon("ncbi", "NCBI", ncbiService, true, "NCBI");
        Beacon wtsi = new Beacon("wtsi", "Wellcome Trust Sanger Institute", wtsiService, true, "WTSI");
        Beacon amplab = new Beacon("amplab", "AMPLab", ampLabService, true, "APMLab");
        Beacon kaviar = new Beacon("kaviar", "Known VARiants", kaviarService, true, "Institute for Systems Biology");

        Beacon platinum = new Beacon("platinum", "Illumina Platinum Genomes", stringBeaconizerService, true, "Google");
        Beacon thousandGenomes = new Beacon("thousandgenomes", "1000 Genomes Project", integerBeaconizerService, true, "Google");
        Beacon thousandGenomesPhase3 = new Beacon("thousandgenomes-phase3", "1000 Genomes Project - Phase 3", integerBeaconizerService, true, "Google");

        // set up aggregators
        Beacon google = new Beacon("google", "Google Genomics Public Data", null, true, "Google");
        platinum.addAggregator(google);
        thousandGenomes.addAggregator(google);
        thousandGenomesPhase3.addAggregator(google);

        // add beacons ot collection
        beacons.add(bob);

        beacons.add(clinvar);
        beacons.add(uniprot);
        beacons.add(lovd);
        beacons.add(ebi);
        beacons.add(ncbi);
        beacons.add(wtsi);
        beacons.add(amplab);
        beacons.add(kaviar);

        beacons.add(google);
        beacons.add(platinum);
        beacons.add(thousandGenomes);
        beacons.add(thousandGenomesPhase3);

        // point all regular beacons to bob
        for (Beacon b : beacons) {
            if (b.getProcessor() != null) {
                b.addAggregator(bob);
            }
        }
    }

    private void computeAggregations() {
        aggregations = HashMultimap.create();

        for (Beacon b : beacons) {
            if (b.getProcessor() != null) {
                for (Beacon parent : b.getAggregators()) {
                    if (parent.getProcessor() == null) {
                        aggregations.put(parent, b);
                    }
                }
            }
        }
    }

    @PostConstruct
    private void init() {
        setUpBeacons();
        computeAggregations();
    }

    private Beacon findBeacon(String beaconId) {
        for (Beacon b : beacons) {
            if (b.getId().equals(beaconId)) {
                return b;
            }
        }

        return null;
    }

    @Override
    public Collection<Beacon> getAllBeacons() {
        return Collections.unmodifiableCollection(beacons);
    }

    @Override
    public Collection<Beacon> getAggregatingBeacons() {
        Set<Beacon> res = new HashSet<>();
        for (Beacon b : getAllBeacons()) {
            if (b.getProcessor() == null) {
                res.add(b);
            }
        }

        return res;
    }

    @Override
    public Collection<Beacon> getRegularBeacons() {
        Set<Beacon> res = new HashSet<>();
        for (Beacon b : getAllBeacons()) {
            if (b.getProcessor() != null) {
                res.add(b);
            }
        }
        return res;
    }

    @Override
    public Collection<Beacon> getVisibleBeacons() {
        Set<Beacon> res = new HashSet<>();
        for (Beacon b : getAllBeacons()) {
            if (b.isVisible()) {
                res.add(b);
            }
        }
        return res;
    }

    @Override
    public Collection<Beacon> getHiddenBeacons() {
        Set<Beacon> res = new HashSet<>();
        for (Beacon b : getAllBeacons()) {
            if (!b.isVisible()) {
                res.add(b);
            }
        }
        return res;
    }

    @Override
    public Beacon getBeacon(String beaconId) {
        if (beaconId == null) {
            throw new NullPointerException("beaconId");
        }

        return findBeacon(beaconId);
    }

    @Override
    public Beacon getVisibleBeacon(String beaconId) {
        if (beaconId == null) {
            throw new NullPointerException("beaconId");
        }

        Beacon b = findBeacon(beaconId);
        if (b == null) {
            return null;
        }

        return b.isVisible() ? b : null;
    }

    @Override
    public Collection<Beacon> getAggregatees(Beacon b) {
        return Collections.unmodifiableCollection(aggregations.get(b));
    }

}