/*
 * Tai-e: A Program Analysis Framework for Java
 *
 * Copyright (C) 2020 Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2020 Yue Li <yueli@nju.edu.cn>
 * All rights reserved.
 *
 * This software is designed for the "Static Program Analysis" course at
 * Nanjing University, and it supports a subset of Java features.
 * Tai-e is only for educational and academic purposes, and any form of
 * commercial use is disallowed.
 */

package pascal.taie.pta.core.cs;

import pascal.taie.pta.core.solver.PointerFlowEdge;
import pascal.taie.pta.set.PointsToSet;

import java.util.Set;

import static pascal.taie.util.CollectionUtils.newHybridSet;

abstract class AbstractPointer implements Pointer {

    private PointsToSet pointsToSet;
    private final Set<PointerFlowEdge> outEdges = newHybridSet();

    @Override
    public PointsToSet getPointsToSet() {
        return pointsToSet;
    }

    @Override
    public void setPointsToSet(PointsToSet pointsToSet) {
        this.pointsToSet = pointsToSet;
    }

    @Override
    public boolean addOutEdge(PointerFlowEdge edge) {
        return outEdges.add(edge);
    }

    @Override
    public Set<PointerFlowEdge> getOutEdges() {
        return outEdges;
    }
}
