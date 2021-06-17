/*
 * Tai-e: A Static Analysis Framework for Java
 *
 * Copyright (C) 2020-- Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2020-- Yue Li <yueli@nju.edu.cn>
 * All rights reserved.
 *
 * Tai-e is only for educational and academic purposes,
 * and any form of commercial use is disallowed.
 * Distribution of Tai-e is disallowed without the approval.
 */

package pascal.taie.analysis.pta.plugin.reflection;

import pascal.taie.analysis.pta.core.cs.context.Context;
import pascal.taie.analysis.pta.core.cs.element.CSCallSite;
import pascal.taie.analysis.pta.core.cs.element.CSObj;
import pascal.taie.analysis.pta.core.cs.element.CSVar;
import pascal.taie.analysis.pta.core.cs.selector.ContextSelector;
import pascal.taie.analysis.pta.core.heap.MockObj;
import pascal.taie.analysis.pta.core.solver.Solver;
import pascal.taie.analysis.pta.plugin.util.AbstractModel;
import pascal.taie.analysis.pta.plugin.util.CSObjUtils;
import pascal.taie.analysis.pta.pts.PointsToSet;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JMethod;
import pascal.taie.language.classes.StringReps;
import pascal.taie.language.classes.Subsignature;
import pascal.taie.language.type.ClassType;
import pascal.taie.util.collection.MapUtils;

import javax.annotation.Nullable;
import java.util.Map;

import static pascal.taie.util.collection.MapUtils.addToMapMap;
import static pascal.taie.util.collection.MapUtils.getMapMap;

/**
 * Models reflective-action methods, currently supports
 * - Class.newInstance()
 * - Constructor.newInstance()
 * - Method.invoke()
 * TODO:
 *  - pass reflective arguments and return values
 *  - trigger class initializer
 *  - check accessibility
 */
class ReflectiveActionModel extends AbstractModel {

    /**
     * Description for objects created by reflective newInstance() calls.
     */
    private final static String REF_OBJ_DESC = "ReflectiveObj";

    private final Subsignature initNoArg;

    private final ContextSelector selector;

    /**
     * Map from Invoke (of newInstance()) and type to reflectively-created objects.
     */
    private final Map<Invoke, Map<ClassType, MockObj>> newObjs = MapUtils.newMap();

    ReflectiveActionModel(Solver solver) {
        super(solver);
        initNoArg = Subsignature.get(StringReps.INIT_NO_ARG);
        selector = solver.getContextSelector();
    }

    @Override
    protected void registerVarAndHandler() {
        JMethod classNewInstance = hierarchy.getJREMethod("<java.lang.Class: java.lang.Object newInstance()>");
        registerRelevantVarIndexes(classNewInstance, BASE);
        registerAPIHandler(classNewInstance, this::handleClassNewInstance);

        // constructor.newInstance(args): BASE

        // method.invoke(o, args): BASE, 0
    }

    private void handleClassNewInstance(CSVar csVar, PointsToSet pts, Invoke invoke) {
        System.out.println(pts);
        Context context = csVar.getContext();
        pts.forEach(obj -> {
            JClass klass = CSObjUtils.toClass(obj);
            if (klass != null) {
                JMethod init = klass.getDeclaredMethod(initNoArg);
                if (init == null) {
                    return;
                }
                ClassType type = klass.getType();
                MockObj newObj = getMapMap(newObjs, invoke, type);
                if (newObj == null) {
                    newObj = new MockObj(REF_OBJ_DESC, invoke, type,
                            invoke.getContainer());
                    // TODO: process newObj by heapModel?
                    addToMapMap(newObjs, invoke, type, newObj);
                }
                CSObj csNewObj = csManager.getCSObj(context, newObj);
                Var result = invoke.getResult();
                if (result != null) {
                    solver.addVarPointsTo(context, result, csNewObj);
                }
                addReflectiveCallEdge(context, invoke, csNewObj, init, null);
            }
        });
    }

    private void addReflectiveCallEdge(
            Context callerCtx, Invoke callSite,
            @Nullable CSObj recvObj, JMethod callee, Var args) {
        if (!callee.isConstructor() && !callee.isStatic()) {
            // dispatch for instance method (except constructor)
            assert recvObj != null : "recvObj is required for instance method";
            callee = hierarchy.dispatch(recvObj.getObject().getType(),
                    callee.getRef());
            if (callee == null) {
                return;
            }
        }
        CSCallSite csCallSite = csManager.getCSCallSite(callerCtx, callSite);
        Context calleeCtx;
        if (callee.isStatic()) {
            calleeCtx = selector.selectContext(csCallSite, callee);
        } else {
            calleeCtx = selector.selectContext(csCallSite, recvObj, callee);
            // pass receiver object to 'this' variable of callee
            solver.addVarPointsTo(calleeCtx, callee.getIR().getThis(), recvObj);
        }
        ReflectiveCallEdge callEdge = new ReflectiveCallEdge(csCallSite,
                csManager.getCSMethod(calleeCtx, callee), args);
        solver.addCallEdge(callEdge);
    }
}
